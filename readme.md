✅ 1️⃣ Create the Outbox Table

CREATE TABLE academic_schema.outbox_events (
event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    aggregate_type VARCHAR(50) NOT NULL,   -- e.g. ENROLLMENT, ATTENDANCE
    aggregate_id BIGINT NOT NULL,           -- enrollment_id, section_id, etc

    event_type VARCHAR(100) NOT NULL,       -- e.g. STUDENT_ENROLLED
    payload JSONB NOT NULL,                 -- full event data

    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- delivery tracking
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMPTZ,

    -- safety / debugging
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT
);
✅ 2️⃣ Indexes (Critical)

-- Fast polling
CREATE INDEX idx_outbox_unprocessed
ON academic_schema.outbox_events (occurred_at)
WHERE processed = FALSE;

-- Aggregate replay
CREATE INDEX idx_outbox_aggregate
ON academic_schema.outbox_events (aggregate_type, aggregate_id);

🧠 How Events Map to Your Schema
| Action                  | aggregate_type     | event_type                  |
| ----------------------- | ------------------ | --------------------------- |
| Student enrolled        | ENROLLMENT         | STUDENT_ENROLLED            |
| Enrollment soft-deleted | ENROLLMENT         | STUDENT_UNENROLLED          |
| Section assignment      | SECTION_ENROLLMENT | STUDENT_ASSIGNED_TO_SECTION |
| Attendance recorded     | ATTENDANCE         | ATTENDANCE_RECORDED         |
| Score entered           | MARKBOOK           | SCORE_RECORDED              |
| Term finalized          | ACADEMIC_TERM      | TERM_FINALIZED              |


✅ 3️⃣ Example Payloads (What Goes in JSONB)
🎓 Student enrolled

{
"studentId": 12345,
"studentNumber": "STU-2024-0001",
"classId": 77,
"schoolId": 10,
"academicYear": "2024/2025",
"enrolledAt": "2025-01-12T09:14:00Z"
}

📝 Attendance recorded

{
"studentId": 12345,
"sectionId": 88,
"attendanceDate": "2025-01-15",
"status": "PRESENT"
}

🔁 How You Publish Events (Correct Way)
✅ Inside your same DB transaction

Your R2DBC service logic:

BEGIN
insert enrollment
insert outbox_event
COMMIT


If anything fails → nothing is saved.

✅ Example SQL inside service
INSERT INTO academic_schema.outbox_events (
aggregate_type,
aggregate_id,
event_type,
payload
)
VALUES (
'ENROLLMENT',
:enrollmentId,
'STUDENT_ENROLLED',
:payload::jsonb
);
🚚 How Events Leave the Database

You now add a separate publisher component:

Publisher loop (every few seconds)

Select unprocessed events

Publish to RabbitMQ / Kafka

Mark processed = true

UPDATE academic_schema.outbox_events
SET processed = TRUE, processed_at = NOW()
WHERE event_id = :eventId;


⚠️ If publish fails → retry safely
⚠️ Events are idempotent

3️⃣ Java R2DBC Code (Concrete Example)
📦 Outbox Entity
@Table("outbox_events")
public class OutboxEvent {

    @Id
    private UUID id;

    private String aggregateType;   // STUDENT, ENROLLMENT
    private UUID aggregateId;

    private String eventType;       // STUDENT_ENROLLED
    private String payload;         // JSON
    private String status;          // NEW, SENT, FAILED

    private Instant createdAt;
    private Instant processedAt;
}
🧾 Repository (Reactive)
public interface OutboxRepository extends ReactiveCrudRepository<OutboxEvent, UUID> {

    @Query("""
        SELECT * FROM outbox_events
        WHERE status = 'NEW'
        ORDER BY created_at
        LIMIT :limit
    """)
    Flux<OutboxEvent> findBatch(int limit);

    @Modifying
    @Query("""
        UPDATE outbox_events
        SET status = :status,
            processed_at = NOW()
        WHERE id = :id
    """)
    Mono<Integer> updateStatus(UUID id, String status);
}

🚀 Kafka Publisher (Spring Cloud Stream)
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final StreamBridge streamBridge;

    @Scheduled(fixedDelay = 1000)
    public void publish() {
        outboxRepository.findBatch(50)
            .flatMap(this::publishSingle)
            .onErrorContinue((ex, obj) ->
                log.error("Outbox publish failed", ex)
            )
            .subscribe();
    }

    private Mono<Void> publishSingle(OutboxEvent event) {
        return Mono.fromCallable(() ->
                streamBridge.send(
                    "studentEnrollment-out-0",
                    event.getPayload()
                )
            )
            .flatMap(sent -> {
                if (sent) {
                    return outboxRepository.updateStatus(event.getId(), "SENT").then();
                }
                return Mono.error(new IllegalStateException("Kafka send failed"));
            });
    }
}

4️⃣ Consumer Side (Very Important)
Idempotency is mandatory

Every consumer must:

Use eventId or aggregateId

Ignore duplicates

Example:

CREATE TABLE processed_events (
event_id UUID PRIMARY KEY,
processed_at TIMESTAMPTZ
);


✅ Step 3 — Idempotent Kafka Consumers (Reactive, R2DBC)

Kafka guarantees at-least-once delivery, so duplicates WILL happen.
Your consumers must be idempotent.

3.1 Consumer-side idempotency table

Each service keeps its own table (never shared).

CREATE TABLE processed_events (
event_id UUID PRIMARY KEY,
processed_at TIMESTAMPTZ DEFAULT NOW()
);

3.2 Repository
public interface ProcessedEventRepository
extends ReactiveCrudRepository<ProcessedEvent, UUID> {
}

3.3 Idempotent Consumer Pattern (Correct Way)
@Component
@RequiredArgsConstructor
public class StudentEnrollmentConsumer {

    private final ProcessedEventRepository processedEventRepo;
    private final EnrollmentService enrollmentService;

    @Bean
    public Consumer<StudentEnrolledEvent> studentEnrollment() {
        return event ->
            processedEventRepo.existsById(event.eventId())
                .flatMap(alreadyProcessed -> {
                    if (alreadyProcessed) {
                        log.info("Duplicate event ignored {}", event.eventId());
                        return Mono.empty();
                    }

                    return enrollmentService.handle(event)
                        .then(processedEventRepo.save(
                            new ProcessedEvent(event.eventId())
                        ))
                        .then();
                })
                .subscribe();
    }
}

🔑 Why this is correct

Duplicate events → ignored

Crash-safe

No distributed locks

Fully reactive

✅ Step 4 — Retry + Dead Letter Queue (DLQ)

Kafka never drops messages — you must decide when to give up.

4.1 Spring Cloud Stream Retry (Binder Level)
spring:
cloud:
stream:
bindings:
studentEnrollment-in-0:
destination: student.enrollment.v1
group: academic-service
consumer:
max-attempts: 5
back-off-initial-interval: 1000
back-off-max-interval: 10000
back-off-multiplier: 2.0

4.2 Dead Letter Topic
spring:
cloud:
stream:
kafka:
bindings:
studentEnrollment-in-0:
consumer:
enable-dlq: true
dlq-name: student.enrollment.dlq

Result
student.enrollment.v1
↓
retries (x5)
↓
student.enrollment.dlq

4.3 DLQ Consumer (Optional but Recommended)
@Bean
public Consumer<StudentEnrolledEvent> studentEnrollmentDlq() {
return event ->
log.error("DLQ EVENT: {}", event);
}


You can:

Alert (Slack, email)

Store for manual replay

Investigate data issues

✅ Step 5 — Event Contract Design (Very Important)
❌ What NOT to do

Don’t reuse JPA/R2DBC entities

Don’t leak DB fields

Don’t publish internal IDs blindly

5.1 Canonical Event Envelope (Best Practice)
public record DomainEvent<T>(
UUID eventId,
String eventType,
String version,
Instant occurredAt,
T payload
) {}

5.2 Example: Student Enrolled Event
public record StudentEnrolledEvent(
UUID eventId,
UUID studentId,
String studentNumber,
Long schoolId,
String gradeLevel,
Instant enrolledAt
) {}


Wrap it:

DomainEvent<StudentEnrolledEvent> event =
new DomainEvent<>(
UUID.randomUUID(),
"STUDENT_ENROLLED",
"v1",
Instant.now(),
payload
);

5.3 Topic Naming Convention (Strongly Recommended)
<domain>.<event>.<version>


Examples:

student.enrolled.v1
student.updated.v1
academic.term.created.v1


This avoids breaking consumers.

🔐 Step 6 — Transaction Question (Revisited)

❓ Can we wrap DB save + Kafka publish in TransactionalOperator?

❌ No — and here’s why

Kafka ≠ DB

Different transaction managers

XA/2PC is not reactive-safe

Leads to partial commits



Now here is the deeper architectural question:

Should promotion be:

A) Manual (admin triggers it)
B) Automatic at end of academic year
C) Scheduled batch job
D) Event-driven after term closure

This decision affects your long-term system evolution.

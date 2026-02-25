package com.emis.academicservice.dto.request;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonMaterials {

    private List<ResourceItem> resources;
    private ClassActivity classActivity;
}
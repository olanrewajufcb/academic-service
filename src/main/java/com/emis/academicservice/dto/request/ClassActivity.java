package com.emis.academicservice.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassActivity {

    private String description;
    private Boolean groupWork;
}
package com.emis.academicservice.dto.request;

import com.emis.academicservice.enums.ResourceType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceItem {

    private ResourceType type;
    private String title;
    private String url;
}
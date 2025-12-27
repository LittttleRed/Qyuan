package com.example.qyuanes.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 专利实体类 - Patent文档模型
 */
@Data  // Lombok注解：自动生成getter/setter等方法
@NoArgsConstructor // Lombok注解：自动生成无参构造函数
@AllArgsConstructor // Lombok注解：自动生成全参构造函数
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略JSON中未知的字段
public class Patent {
    @JsonProperty("patent_number")
    private String patent_number;
    @JsonProperty("patent_name")
    private String patent_name;
    @JsonProperty("abstract")
    private String abstractContent;
    @JsonProperty("inventor")
    private String inventor;
    @JsonProperty("assignee")
    private String assignee;
    @JsonProperty("application_date")
    private OffsetDateTime application_date;
    @JsonProperty("authorization_date")
    private OffsetDateTime authorization_date;
    @JsonProperty("citation_count")
    private Integer citation_count;
    @JsonProperty("country")
    private String country;
}

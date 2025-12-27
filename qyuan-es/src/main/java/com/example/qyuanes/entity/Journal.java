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

public class Journal {
    @JsonProperty("journal_id")
    private Integer journal_id;
    @JsonProperty("journal_name")
    private String journal_name;
    @JsonProperty("journal_url")
    private String journal_url;
    @JsonProperty("doaj_url")
    private String doaj_url;
    @JsonProperty("issn")
    private String issn;
    @JsonProperty("publish_time")
    private Integer publish_time;
    @JsonProperty("keywords")
    private String keywords;
    @JsonProperty("article_number")
    private Integer article_number;
    @JsonProperty("languages")
    private String languages;
    @JsonProperty("publisher")
    private String publisher;
}


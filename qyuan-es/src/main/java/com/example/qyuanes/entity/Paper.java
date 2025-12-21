package com.example.qyuanes.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 文章实体类 - ES文档模型
 * 对应ES索引中的文档结构
 */
@Data  // Lombok注解：自动生成getter/setter等方法
@NoArgsConstructor // Lombok注解：自动生成无参构造函数
@AllArgsConstructor // Lombok注解：自动生成全参构造函数
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略JSON中未知的字段（如tags），避免反序列化失败
public class Paper {
    
    /**
     * 文档ID（ES中的_id）
     */
    @JsonProperty("paper_id")
    private Integer paper_id;
    
    @JsonProperty("title")
    private String title;
    
 
    @JsonProperty("submitter")
    private String submitter;
    

    @JsonProperty("abstract")
    private String abstractContent;

    @JsonProperty("doi")
    private String doi;
    

    @JsonProperty("journal_source")
    private String journal_source;

    @JsonProperty("pdf_file_url")
    private String pdf_file_url;

    @JsonProperty("url")
    private String url;

    @JsonProperty("category_id")
    private Integer category_id;

    @JsonProperty("updated")
    private OffsetDateTime updated;

    @JsonProperty("read_count")
    private Integer read_count;

    @JsonProperty("favoriate_count")
    private Integer favoriate_count;
}   


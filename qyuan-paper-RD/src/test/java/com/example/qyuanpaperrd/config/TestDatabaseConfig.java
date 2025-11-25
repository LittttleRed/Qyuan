package com.example.qyuanpaperrd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 测试数据库配置类
 * 使用SQLite内存数据库进行测试
 */
@Configuration
public class TestDatabaseConfig {

    /**
     * 配置SQLite内存数据源
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        try {
            dataSource.setDriverClass(org.sqlite.JDBC.class);
            dataSource.setUrl("jdbc:sqlite::memory:");
            
            // 初始化数据库表结构
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                // 创建paper表
                stmt.execute("CREATE TABLE IF NOT EXISTS paper (" +
                    "paper_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "submitter TEXT NOT NULL, " +
                    "abstract TEXT, " +
                    "doi TEXT, " +
                    "journal_source TEXT, " +
                    "pdf_file_url TEXT, " +
                    "url TEXT, " +
                    "category_id INTEGER, " +
                    "updated DATETIME NOT NULL)");
                
                // 创建author_paper表
                stmt.execute("CREATE TABLE IF NOT EXISTS author_paper (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "paper_id INTEGER NOT NULL, " +
                    "author_last_name TEXT NOT NULL, " +
                    "author_first_name TEXT NOT NULL, " +
                    "author_rank INTEGER NOT NULL, " +
                    "author_orcid TEXT)");
                    
                // 创建claim表
                stmt.execute("CREATE TABLE IF NOT EXISTS claim (" +
                    "claim_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "paper_id INTEGER NOT NULL, " +
                    "claim_picture TEXT, " +
                    "status INTEGER NOT NULL DEFAULT 0)");
                    
                // 创建user_paper表
                stmt.execute("CREATE TABLE IF NOT EXISTS user_paper (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "paper_id INTEGER NOT NULL, " +
                    "user_rank INTEGER)");
                    
                // 创建journal表
                stmt.execute("CREATE TABLE IF NOT EXISTS journal (" +
                    "journal_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "journal_name TEXT NOT NULL, " +
                    "journal_url TEXT NOT NULL, " +
                    "doaj_url TEXT NOT NULL, " +
                    "issn TEXT, " +
                    "publish_time INTEGER NOT NULL, " +
                    "keywords TEXT, " +
                    "article_number INTEGER NOT NULL, " +
                    "languages TEXT NOT NULL, " +
                    "publisher TEXT)");
                    
                // 创建patent表
                stmt.execute("CREATE TABLE IF NOT EXISTS patent (" +
                    "patent_number TEXT, " +
                    "patent_name TEXT, " +
                    "abstract TEXT, " +
                    "inventor TEXT, " +
                    "assignee TEXT, " +
                    "application_date DATETIME, " +
                    "authorization_date DATETIME, " +
                    "citation_count INTEGER, " +
                    "country TEXT)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure SQLite test database", e);
        }
        
        return dataSource;
    }
}

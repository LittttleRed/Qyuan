-- 创建paper表（SQLite兼容）
CREATE TABLE IF NOT EXISTS paper (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    abstract_text TEXT,
    doi TEXT,
    journal_source TEXT,
    pdf_file_url TEXT,
    url TEXT,
    category_id INTEGER,
    submitter TEXT,
    download_count INTEGER DEFAULT 0,
    favorite_count INTEGER DEFAULT 0,
    created DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0
);

-- 创建author_paper表（SQLite兼容）
CREATE TABLE IF NOT EXISTS author_paper (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paper_id INTEGER NOT NULL,
    author_last_name TEXT,
    author_first_name TEXT,
    author_rank INTEGER,
    author_orcid TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0
);
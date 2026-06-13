CREATE DATABASE IF NOT EXISTS nostalgia_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE nostalgia_db;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(100),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS eras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    year_start INT,
    year_end INT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    story TEXT,
    memory TEXT,
    images JSON,
    category_id BIGINT NOT NULL,
    era_id BIGINT NOT NULL,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    author_name VARCHAR(50) DEFAULT '匿名用户',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_era (era_id),
    INDEX idx_created_at (created_at),
    INDEX idx_view_count (view_count),
    INDEX idx_category_era (category_id, era_id),
    FULLTEXT INDEX ft_content (title, item_name, content, story, memory)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    author_name VARCHAR(50) DEFAULT '匿名用户',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_session VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_session),
    INDEX idx_post_id (post_id),
    INDEX idx_user_session (user_session)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categories (name, icon, sort_order) VALUES
('家用电器', '🏠', 1),
('影音设备', '📺', 2),
('通讯工具', '📞', 3),
('玩具玩偶', '🧸', 4),
('文具书籍', '📚', 5),
('服饰配饰', '👕', 6),
('食品饮料', '🍬', 7),
('日常用品', '🪑', 8);

INSERT INTO eras (name, year_start, year_end, sort_order) VALUES
('60年代', 1960, 1969, 1),
('70年代', 1970, 1979, 2),
('80年代', 1980, 1989, 3),
('90年代', 1990, 1999, 4),
('00年代', 2000, 2009, 5);

INSERT INTO posts (title, item_name, content, story, memory, images, category_id, era_id, view_count, author_name) VALUES
('老黑白电视机的回忆', '黑白电视机', '这是一台80年代的黑白电视机，还记得小时候全村人围在一起看西游记的场景吗？', '这台电视是爸爸当年攒了三个月工资买的，当时是村里第一台电视。', '每到傍晚，院子里就坐满了人，大家自带小板凳，那种热闹的感觉再也找不到了。', '["https://picsum.photos/seed/tv1/600/400","https://picsum.photos/seed/tv2/600/400"]', 2, 3, 128, '老张'),
('童年的铁皮青蛙', '铁皮青蛙', '上弦的铁皮青蛙，一拧就能跳老远，是那个年代最经典的玩具之一。', '记得是过生日时爷爷给买的，当时爱不释手，睡觉都要放在枕头边。', '现在看到它，仿佛又回到了那个无忧无虑的童年时光。', '["https://picsum.photos/seed/frog1/600/400"]', 4, 3, 256, '李阿姨'),
('老款双卡录音机', '双卡录音机', '当年的奢侈品，能拥有一台双卡录音机是多么让人羡慕的事情。', '这台录音机是哥哥从南方带回来的，当时可是稀罕物。', '记得用它录了好多磁带，邓丽君、谭咏麟的歌都是从这里听到的。', '["https://picsum.photos/seed/radio1/600/400","https://picsum.photos/seed/radio2/600/400"]', 2, 4, 189, '王叔');

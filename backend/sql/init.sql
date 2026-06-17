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
    description TEXT,
    icon VARCHAR(20),
    color_scheme VARCHAR(100),
    representative_categories VARCHAR(255),
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
    era_background TEXT,
    current_status TEXT,
    preservation_status VARCHAR(50),
    usage_scene VARCHAR(100),
    story_summary VARCHAR(500),
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
    FULLTEXT INDEX ft_content (title, item_name, content, story, memory, era_background, current_status)
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

CREATE TABLE IF NOT EXISTS timeline_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    event_type ENUM('ACQUISITION', 'USAGE', 'IDLE', 'DISPOSAL') NOT NULL,
    event_date DATE NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    location VARCHAR(100),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_event_date (event_date)
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

INSERT INTO eras (name, year_start, year_end, description, icon, color_scheme, representative_categories, sort_order) VALUES
('60年代', 1960, 1969, '艰苦奋斗的岁月，物资虽匮乏但精神富足，充满理想主义色彩。缝纫机、搪瓷杯、军绿挎包是这个年代的生活符号。', '★', '#5d4037:#d7ccc8:#ffe082', '日常用品,服饰配饰,文具书籍', 1),
('70年代', 1970, 1979, '改革开放的前夜，喇叭裤、的确良衬衫开始流行，双卡录音机和黑白电视进入少数家庭。', '✿', '#8d6e63:#ffcc80:#fff59d', '影音设备,服饰配饰,食品饮料', 2),
('80年代', 1980, 1989, '改革开放浪潮涌动，流行文化蓬勃发展。邓丽君的歌声、琼瑶的小说、铁皮玩具、任天堂红白机承载了无数童年记忆。', '♪', '#1976d2:#90caf9:#e3f2fd', '家用电器,影音设备,玩具玩偶', 3),
('90年代', 1990, 1999, '市场化浪潮席卷全国，港台文化成为主流。BP机、大哥大、CD随身听、小霸王学习机是那个时代的科技象征。', '⚡', '#ff6f00:#ffab40:#fff8e1', '通讯工具,影音设备,玩具玩偶', 4),
('00年代', 2000, 2009, '互联网时代的开启，MP3、数码相机、智能手机逐渐普及。超级女声、周杰伦、QQ聊天记录着青春的印记。', '◈', '#00acc1:#80deea:#e0f7fa', '通讯工具,影音设备,日常用品', 5);

INSERT INTO posts (title, item_name, content, story, memory, era_background, current_status, preservation_status, usage_scene, story_summary, images, category_id, era_id, view_count, author_name) VALUES
('老黑白电视机的回忆', '黑白电视机', '这是一台80年代的黑白电视机，还记得小时候全村人围在一起看西游记的场景吗？', '这台电视是爸爸当年攒了三个月工资买的，当时是村里第一台电视。', '每到傍晚，院子里就坐满了人，大家自带小板凳，那种热闹的感觉再也找不到了。', '80年代中国电视机开始进入普通家庭，但对于大多数农村家庭来说仍是奢侈品。一台14寸黑白电视售价约400元，相当于普通工人三四个月的工资。当时电视台节目有限，每晚黄金时段的电视剧是全村人最重要的娱乐活动。', '2005年搬家时送给了老家亲戚，据说至今仍能正常开机收看，偶尔还会被拿出来播放老录像带，成为家族怀旧聚会的焦点。', '完好保存', '家庭客厅 / 全村集体观看', '1985年父亲花三个月工资购入的全村第一台黑白电视，承载着全村人围看西游记的集体记忆，至今仍能正常使用。', '[{"url":"https://picsum.photos/seed/tv1/600/400","width":600,"height":400,"isMain":true,"sortOrder":0},{"url":"https://picsum.photos/seed/tv2/600/400","width":600,"height":400,"isMain":false,"sortOrder":1}]', 2, 3, 128, '老张'),
('童年的铁皮青蛙', '铁皮青蛙', '上弦的铁皮青蛙，一拧就能跳老远，是那个年代最经典的玩具之一。', '记得是过生日时爷爷给买的，当时爱不释手，睡觉都要放在枕头边。', '现在看到它，仿佛又回到了那个无忧无虑的童年时光。', '80年代中国制造的铁皮玩具风靡全国，采用镀锡铁皮冲压工艺，经久耐用。铁皮青蛙、铁皮坦克、铁皮小汽车是那个年代孩子最渴望的礼物，价格约一两元钱，相当于家长大半天的工资。', '如今作为珍贵收藏品摆放在家中书架的玻璃柜中，每次整理旧物看到它，都会拿出来给孩子演示上弦跳跃，讲述那个简单快乐的童年。', '略有锈迹', '儿童玩耍 / 书桌摆设', '1988年六一儿童节爷爷赠送的生日礼物，伴随整个童年，如今作为珍贵收藏摆放在书架上。', '[{"url":"https://picsum.photos/seed/frog1/600/400","width":600,"height":400,"isMain":true,"sortOrder":0}]', 4, 3, 256, '李阿姨'),
('老款双卡录音机', '双卡录音机', '当年的奢侈品，能拥有一台双卡录音机是多么让人羡慕的事情。', '这台录音机是哥哥从南方带回来的，当时可是稀罕物。', '记得用它录了好多磁带，邓丽君、谭咏麟的歌都是从这里听到的。', '90年代初双卡录音机是年轻人追求时尚的标志，支持磁带对录功能，可以翻录朋友的音乐磁带。一盒正版磁带约6-10元，对于学生来说价格不菲，因此互相磁带转录成为当时流行的音乐分享方式。', '2018年送给了对老物件感兴趣的侄儿，侄儿将其修复后放在自己的复古风格咖啡馆里，偶尔还会播放老磁带，引来很多顾客的好奇与赞叹。', '功能正常', '卧室听歌 / 校园晚会伴奏', '1992年哥哥从深圳带回的双卡录音机，录满了一抽屉磁带，陪伴度过无数个放学后的音乐时光。', '[{"url":"https://picsum.photos/seed/radio1/600/400","width":600,"height":400,"isMain":true,"sortOrder":0},{"url":"https://picsum.photos/seed/radio2/800/600","width":800,"height":600,"isMain":false,"sortOrder":1}]', 2, 4, 189, '王叔');

INSERT INTO timeline_events (post_id, event_type, event_date, title, description, location, sort_order) VALUES
(1, 'ACQUISITION', '1985-03-15', '获得电视机', '爸爸攒了三个月工资，在县城供销社花了420元买下了这台14寸黑白电视机。', '县城供销社', 0),
(1, 'USAGE', '1985-03-20', '第一次看电视', '全村人都来看西游记，院子里坐了30多个人，大家自带小板凳。', '老家院子', 1),
(1, 'USAGE', '1987-10-01', '看国庆阅兵', '第一次看彩色转播的阅兵式，虽然电视是黑白的，但依然激动人心。', '老家客厅', 2),
(1, 'IDLE', '1995-06-01', '闲置角落', '家里买了彩色电视，这台黑白电视被放到了储物间角落。', '储物间', 3),
(1, 'DISPOSAL', '2005-09-10', '捐赠老家', '搬家时把电视送给了老家的亲戚，据说现在还能正常使用。', '老家亲戚家', 4),
(2, 'ACQUISITION', '1988-06-01', '六一礼物', '过生日时爷爷给买的，在当时是非常珍贵的玩具。', '县城百货大楼', 0),
(2, 'USAGE', '1988-06-15', '每天把玩', '爱不释手，睡觉都要放在枕头边，上弦时特别小心。', '家里', 1),
(2, 'IDLE', '1992-09-01', '上学闲置', '上小学后开始忙学习，玩具被收到了抽屉里。', '书桌抽屉', 2),
(2, 'DISPOSAL', '2020-05-20', '收藏至今', '整理旧物时发现，依然完好，现在作为收藏品摆放在书架上。', '家中书架', 3),
(3, 'ACQUISITION', '1992-08-20', '南方带回', '哥哥从深圳打工回来，花了800元买了这台双卡录音机。', '深圳华强北', 0),
(3, 'USAGE', '1992-09-01', '录磁带', '每天放学后都用它录收音机里的歌曲，攒了满满一抽屉磁带。', '卧室', 1),
(3, 'USAGE', '1995-05-04', '五四晚会', '班级五四晚会用它伴奏，成为晚会的明星设备。', '学校教室', 2),
(3, 'IDLE', '2000-03-15', 'MP3时代', 'MP3开始流行，录音机被闲置在衣柜顶上。', '衣柜顶', 3),
(3, 'DISPOSAL', '2018-10-01', '送给侄儿', '侄儿对老物件感兴趣，送给了他继续收藏。', '侄儿家', 4);

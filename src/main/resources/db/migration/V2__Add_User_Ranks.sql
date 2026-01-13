-- 1. ランクマスタテーブルの作成
CREATE TABLE user_ranks (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,     -- 'BRONZE', 'SILVER', 'GOLD' など
                            display_name VARCHAR(50) NOT NULL,    -- 'ブロンズ会員', 'シルバー会員' など
                            discount_rate DECIMAL(5, 4) NOT NULL DEFAULT 0.0000, -- 0.1000 = 10%割引
                            description TEXT,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. 初期データの投入（これをやっておかないとUser登録時に困る）
INSERT INTO user_ranks (name, display_name, discount_rate, description) VALUES
                                                                            ('BRONZE', 'ブロンズ会員', 0.00, '通常会員です。割引はありません。'),
                                                                            ('SILVER', 'シルバー会員', 0.05, '5%の割引が適用されます。'),
                                                                            ('GOLD',   'ゴールド会員', 0.10, '10%の割引が適用されます。');

-- 3. UsersテーブルにランクIDを追加
-- 最初は全員 'BRONZE' (ID=1) に設定する
ALTER TABLE users
    ADD COLUMN rank_id BIGINT NOT NULL DEFAULT 1;

-- 4. 外部キー制約を追加
ALTER TABLE users
    ADD CONSTRAINT fk_users_rank
        FOREIGN KEY (rank_id) REFERENCES user_ranks(id);

-- 5. インデックス作成
CREATE INDEX idx_users_rank_id ON users(rank_id);
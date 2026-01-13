-- ============================================================
-- ECサイト データベース設計（PostgreSQL）
-- バージョン: 2.0（改善版）
-- 作成日: 2025-01-14
-- ============================================================

-- ============================================================
-- 拡張機能
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";  -- UUID生成用
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- 暗号化用

-- ============================================================
-- ENUM型定義
-- ============================================================

-- 住所タイプ
CREATE TYPE address_type AS ENUM ('default', 'shipping', 'billing');

-- 支払い方法
CREATE TYPE payment_method AS ENUM ('credit_card', 'bank_transfer', 'cash_on_delivery', 'paypal', 'stripe');

-- 支払いステータス
CREATE TYPE payment_status AS ENUM ('pending', 'paid', 'failed', 'refunded', 'cancelled');

-- 注文ステータス
CREATE TYPE order_status AS ENUM ('pending', 'confirmed', 'preparing', 'shipped', 'delivered', 'cancelled', 'returned');

-- 割引タイプ
CREATE TYPE discount_type AS ENUM ('percentage', 'fixed_amount');

-- ============================================================
-- 1. 認証情報テーブル（最高機密）
-- ============================================================
CREATE TABLE user_credentials (
                                  id BIGSERIAL PRIMARY KEY,
                                  email VARCHAR(320) UNIQUE NOT NULL,
                                  password_hash VARCHAR(255) NOT NULL,
                                  is_active BOOLEAN DEFAULT TRUE NOT NULL,
                                  is_locked BOOLEAN DEFAULT FALSE NOT NULL,
                                  failed_login_attempts INTEGER DEFAULT 0 NOT NULL CHECK (failed_login_attempts >= 0),
                                  last_login_at TIMESTAMP WITH TIME ZONE,
                                  password_changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- インデックス
CREATE INDEX idx_user_credentials_email ON user_credentials(email);
CREATE INDEX idx_user_credentials_is_active ON user_credentials(is_active);
CREATE INDEX idx_user_credentials_is_locked ON user_credentials(is_locked);

-- コメント
COMMENT ON TABLE user_credentials IS '認証情報テーブル（最高機密）- パスワードは必ずBCryptでハッシュ化';
COMMENT ON COLUMN user_credentials.email IS 'ログイン用メールアドレス（小文字で統一）';
COMMENT ON COLUMN user_credentials.password_hash IS 'BCryptハッシュ（60文字必要）';
COMMENT ON COLUMN user_credentials.is_active IS '論理削除フラグ（false=削除済み）';
COMMENT ON COLUMN user_credentials.is_locked IS 'アカウントロックフラグ（3回失敗でtrue）';
COMMENT ON COLUMN user_credentials.failed_login_attempts IS 'ログイン失敗回数（成功時に0にリセット）';
COMMENT ON COLUMN user_credentials.password_changed_at IS 'パスワード変更日時（90日で強制変更）';

-- ============================================================
-- 2. ユーザー基本情報テーブル
-- ============================================================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       credential_id BIGINT UNIQUE NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       phone_number VARCHAR(20) CHECK (phone_number ~ '^0[0-9]{1,4}-[0-9]{1,4}-[0-9]{4}$'),
    date_of_birth DATE CHECK (date_of_birth < CURRENT_DATE - INTERVAL '13 years'),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_users_credential FOREIGN KEY (credential_id)
        REFERENCES user_credentials(id) ON DELETE RESTRICT
);

-- インデックス
CREATE INDEX idx_users_credential_id ON users(credential_id);

-- コメント
COMMENT ON TABLE users IS 'ユーザー基本情報テーブル（PII: Personally Identifiable Information）';
COMMENT ON COLUMN users.date_of_birth IS '生年月日（13歳未満は登録不可）';

-- ============================================================
-- 3. 住所情報テーブル
-- ============================================================
CREATE TABLE addresses (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           address_type address_type NOT NULL DEFAULT 'default',
                           recipient_name VARCHAR(100) NOT NULL,
                           postal_code VARCHAR(10) NOT NULL CHECK (postal_code ~ '^[0-9]{3}-[0-9]{4}$'),
    prefecture VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    phone_number VARCHAR(20) CHECK (phone_number ~ '^0[0-9]{1,4}-[0-9]{1,4}-[0-9]{4}$'),
    is_default BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- インデックス
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_user_type ON addresses(user_id, address_type);
CREATE INDEX idx_addresses_user_default ON addresses(user_id, is_default);
CREATE INDEX idx_addresses_postal_code ON addresses(postal_code);

-- コメント
COMMENT ON TABLE addresses IS '住所マスタテーブル（1ユーザーが複数の住所を登録可能）';
COMMENT ON COLUMN addresses.recipient_name IS '受取人名（本人と異なる場合あり）';
COMMENT ON COLUMN addresses.is_default IS 'デフォルト配送先フラグ（1ユーザーに1つのみtrue）';

-- ============================================================
-- 4. 商品情報テーブル
-- ============================================================
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
                          stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
                          image_url TEXT,
                          is_active BOOLEAN DEFAULT TRUE NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- インデックス
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_active_price ON products(is_active, price);
CREATE INDEX idx_products_name ON products USING btree(name);

-- コメント
COMMENT ON TABLE products IS '商品マスタテーブル';
COMMENT ON COLUMN products.price IS '現在価格（注文時にorder_itemsにコピー）';
COMMENT ON COLUMN products.stock IS '在庫数（トリガーで自動減算）';
COMMENT ON COLUMN products.is_active IS '販売中フラグ（false=販売停止）';

-- ============================================================
-- 5. 注文情報テーブル（改善版）
-- ============================================================
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        order_number VARCHAR(50) UNIQUE NOT NULL,

    -- 金額情報（追加）
                        subtotal_amount DECIMAL(12,2) NOT NULL CHECK (subtotal_amount >= 0),
                        tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
                        discount_amount DECIMAL(12,2) DEFAULT 0 CHECK (discount_amount >= 0),
                        total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),

                        order_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        payment_method payment_method NOT NULL,
                        payment_status payment_status NOT NULL DEFAULT 'pending',
                        order_status order_status NOT NULL DEFAULT 'pending',

    -- メタデータ
                        notes TEXT,

                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

                        CONSTRAINT fk_orders_user FOREIGN KEY (user_id)
                            REFERENCES users(id) ON DELETE RESTRICT
);

-- インデックス
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_orders_user_date ON orders(user_id, order_date);
CREATE INDEX idx_orders_status ON orders(order_status, payment_status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);

-- コメント
COMMENT ON TABLE orders IS '注文ヘッダーテーブル';
COMMENT ON COLUMN orders.order_number IS '注文番号（例: ORDER-20250114-0001）';
COMMENT ON COLUMN orders.subtotal_amount IS '商品合計金額（税抜き）';
COMMENT ON COLUMN orders.tax_amount IS '消費税額（10%）';
COMMENT ON COLUMN orders.discount_amount IS '割引額';
COMMENT ON COLUMN orders.total_amount IS '合計金額 = subtotal + tax - discount（トリガーで自動計算）';

-- ============================================================
-- 6. 注文配送先情報テーブル（スナップショット）
-- ============================================================
CREATE TABLE order_shipping_info (
                                     id BIGSERIAL PRIMARY KEY,
                                     order_id BIGINT UNIQUE NOT NULL,
                                     recipient_name VARCHAR(100) NOT NULL,
                                     postal_code VARCHAR(10) NOT NULL,
                                     prefecture VARCHAR(50) NOT NULL,
                                     city VARCHAR(100) NOT NULL,
                                     address_line1 VARCHAR(255) NOT NULL,
                                     address_line2 VARCHAR(255),
                                     phone_number VARCHAR(20) NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

                                     CONSTRAINT fk_order_shipping_order FOREIGN KEY (order_id)
                                         REFERENCES orders(id) ON DELETE CASCADE
);

-- インデックス
CREATE INDEX idx_order_shipping_order_id ON order_shipping_info(order_id);

-- コメント
COMMENT ON TABLE order_shipping_info IS '注文時の配送先情報スナップショット（addressesからコピー）';
COMMENT ON COLUMN order_shipping_info.order_id IS 'ordersテーブルとの1対1リレーション';

-- ============================================================
-- 7. 注文明細テーブル
-- ============================================================
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price DECIMAL(12,2) NOT NULL CHECK (unit_price >= 0),
                             subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
                                 REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_product FOREIGN KEY (product_id)
                                 REFERENCES products(id) ON DELETE RESTRICT
);

-- インデックス
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_order_product ON order_items(order_id, product_id);

-- コメント
COMMENT ON TABLE order_items IS '注文明細テーブル';
COMMENT ON COLUMN order_items.product_name IS '注文時の商品名（商品マスタが変更されても履歴として保持）';
COMMENT ON COLUMN order_items.unit_price IS '注文時の単価（商品価格が変更されても履歴として保持）';
COMMENT ON COLUMN order_items.subtotal IS '小計 = unit_price * quantity（トリガーで自動計算）';

-- ============================================================
-- 8. クーポンマスタテーブル（追加）
-- ============================================================
CREATE TABLE coupons (
                         id BIGSERIAL PRIMARY KEY,
                         code VARCHAR(50) UNIQUE NOT NULL,
                         discount_type discount_type NOT NULL,
                         discount_value DECIMAL(12,2) NOT NULL CHECK (discount_value > 0),
                         min_purchase_amount DECIMAL(12,2) DEFAULT 0 CHECK (min_purchase_amount >= 0),
                         max_discount_amount DECIMAL(12,2) CHECK (max_discount_amount > 0),
                         valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
                         valid_until TIMESTAMP WITH TIME ZONE NOT NULL CHECK (valid_until > valid_from),
                         usage_limit INTEGER CHECK (usage_limit > 0),
                         used_count INTEGER DEFAULT 0 NOT NULL CHECK (used_count >= 0),
                         is_active BOOLEAN DEFAULT TRUE NOT NULL,
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- インデックス
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_validity ON coupons(is_active, valid_from, valid_until);

-- コメント
COMMENT ON TABLE coupons IS 'クーポンマスタテーブル';
COMMENT ON COLUMN coupons.discount_type IS '割引タイプ（percentage=割引率、fixed_amount=固定額）';
COMMENT ON COLUMN coupons.discount_value IS 'パーセントの場合は1-100、固定額の場合は金額';
COMMENT ON COLUMN coupons.usage_limit IS '使用回数制限（NULL=無制限）';

-- ============================================================
-- 9. 注文割引テーブル（追加）
-- ============================================================
CREATE TABLE order_discounts (
                                 id BIGSERIAL PRIMARY KEY,
                                 order_id BIGINT NOT NULL,
                                 coupon_id BIGINT,
                                 discount_type discount_type NOT NULL,
                                 discount_amount DECIMAL(12,2) NOT NULL CHECK (discount_amount >= 0),
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

                                 CONSTRAINT fk_order_discounts_order FOREIGN KEY (order_id)
                                     REFERENCES orders(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_order_discounts_coupon FOREIGN KEY (coupon_id)
                                     REFERENCES coupons(id) ON DELETE RESTRICT
);

-- インデックス
CREATE INDEX idx_order_discounts_order_id ON order_discounts(order_id);
CREATE INDEX idx_order_discounts_coupon_id ON order_discounts(coupon_id);

-- コメント
COMMENT ON TABLE order_discounts IS '注文に適用された割引の記録';

-- ============================================================
-- 10. 監査ログテーブル（追加）
-- ============================================================
CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            table_name VARCHAR(100) NOT NULL,
                            record_id BIGINT NOT NULL,
                            action VARCHAR(50) NOT NULL CHECK (action IN ('SELECT', 'INSERT', 'UPDATE', 'DELETE')),
    user_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    accessed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- インデックス
CREATE INDEX idx_audit_logs_table_record ON audit_logs(table_name, record_id);
CREATE INDEX idx_audit_logs_accessed_at ON audit_logs(accessed_at);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- パーティショニング（オプション：大量のログを扱う場合）
-- CREATE TABLE audit_logs_y2025m01 PARTITION OF audit_logs
--     FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

-- コメント
COMMENT ON TABLE audit_logs IS '監査ログテーブル（重要なテーブルへのアクセスを記録）';
COMMENT ON COLUMN audit_logs.old_values IS '変更前の値（JSON形式）';
COMMENT ON COLUMN audit_logs.new_values IS '変更後の値（JSON形式）';

-- ============================================================
-- トリガー関数
-- ============================================================

-- 1. updated_at自動更新関数
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. 在庫減算関数（排他ロック使用）
CREATE OR REPLACE FUNCTION decrease_product_stock()
RETURNS TRIGGER AS $$
DECLARE
current_stock INTEGER;
BEGIN
    -- 排他ロックで在庫を取得
SELECT stock INTO current_stock
FROM products
WHERE id = NEW.product_id
    FOR UPDATE;

-- 在庫チェック
IF current_stock < NEW.quantity THEN
        RAISE EXCEPTION '在庫不足です（商品ID: %, 在庫: %, 注文: %）',
            NEW.product_id, current_stock, NEW.quantity;
END IF;

    -- 在庫減算
UPDATE products
SET stock = stock - NEW.quantity,
    updated_at = CURRENT_TIMESTAMP
WHERE id = NEW.product_id;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. order_itemsのsubtotal自動計算関数
CREATE OR REPLACE FUNCTION calculate_order_item_subtotal()
RETURNS TRIGGER AS $$
BEGIN
    NEW.subtotal = NEW.unit_price * NEW.quantity;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. ordersのtotal_amount自動計算関数
CREATE OR REPLACE FUNCTION calculate_order_total()
RETURNS TRIGGER AS $$
DECLARE
items_total DECIMAL(12,2);
    discount_total DECIMAL(12,2);
BEGIN
    -- 注文明細の合計を計算
SELECT COALESCE(SUM(subtotal), 0) INTO items_total
FROM order_items
WHERE order_id = NEW.id;

-- 割引の合計を計算
SELECT COALESCE(SUM(discount_amount), 0) INTO discount_total
FROM order_discounts
WHERE order_id = NEW.id;

-- 小計を設定
NEW.subtotal_amount = items_total;
    NEW.discount_amount = discount_total;

    -- 消費税を計算（10%）
    NEW.tax_amount = ROUND((items_total - discount_total) * 0.10, 2);

    -- 合計金額を計算
    NEW.total_amount = items_total + NEW.tax_amount - discount_total;

    -- 合計が負になる場合はエラー
    IF NEW.total_amount < 0 THEN
        RAISE EXCEPTION '注文金額がマイナスになります';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5. order_items/order_discounts変更時にorders更新
CREATE OR REPLACE FUNCTION update_order_total_on_items_change()
RETURNS TRIGGER AS $$
BEGIN
    -- ordersテーブルを更新（calculate_order_totalトリガーが発火）
UPDATE orders
SET updated_at = CURRENT_TIMESTAMP
WHERE id = COALESCE(NEW.order_id, OLD.order_id);

RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- 6. 監査ログ記録関数
CREATE OR REPLACE FUNCTION log_table_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit_logs (
            table_name,
            record_id,
            action,
            old_values,
            accessed_at
        ) VALUES (
            TG_TABLE_NAME,
            OLD.id,
            'DELETE',
            row_to_json(OLD)::JSONB,
            CURRENT_TIMESTAMP
        );
RETURN OLD;
ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit_logs (
            table_name,
            record_id,
            action,
            old_values,
            new_values,
            accessed_at
        ) VALUES (
            TG_TABLE_NAME,
            NEW.id,
            'UPDATE',
            row_to_json(OLD)::JSONB,
            row_to_json(NEW)::JSONB,
            CURRENT_TIMESTAMP
        );
RETURN NEW;
ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO audit_logs (
            table_name,
            record_id,
            action,
            new_values,
            accessed_at
        ) VALUES (
            TG_TABLE_NAME,
            NEW.id,
            'INSERT',
            row_to_json(NEW)::JSONB,
            CURRENT_TIMESTAMP
        );
RETURN NEW;
END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- トリガー設定
-- ============================================================

-- updated_at自動更新トリガー
CREATE TRIGGER update_user_credentials_timestamp
    BEFORE UPDATE ON user_credentials
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_users_timestamp
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_addresses_timestamp
    BEFORE UPDATE ON addresses
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_products_timestamp
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_orders_timestamp
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_coupons_timestamp
    BEFORE UPDATE ON coupons
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- 在庫減算トリガー
CREATE TRIGGER decrease_stock_on_order_item_insert
    AFTER INSERT ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION decrease_product_stock();

-- order_itemsのsubtotal自動計算トリガー
CREATE TRIGGER calculate_subtotal_before_insert
    BEFORE INSERT OR UPDATE ON order_items
                         FOR EACH ROW
                         EXECUTE FUNCTION calculate_order_item_subtotal();

-- ordersのtotal_amount自動計算トリガー
CREATE TRIGGER calculate_order_total_before_update
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION calculate_order_total();

-- order_items変更時にorders更新
CREATE TRIGGER update_order_total_on_item_change
    AFTER INSERT OR UPDATE OR DELETE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_order_total_on_items_change();

-- order_discounts変更時にorders更新
CREATE TRIGGER update_order_total_on_discount_change
    AFTER INSERT OR UPDATE OR DELETE ON order_discounts
    FOR EACH ROW
    EXECUTE FUNCTION update_order_total_on_items_change();

-- 監査ログトリガー（重要テーブルのみ）
CREATE TRIGGER audit_user_credentials
    AFTER INSERT OR UPDATE OR DELETE ON user_credentials
    FOR EACH ROW
    EXECUTE FUNCTION log_table_changes();

CREATE TRIGGER audit_orders
    AFTER INSERT OR UPDATE OR DELETE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION log_table_changes();

CREATE TRIGGER audit_order_items
    AFTER INSERT OR UPDATE OR DELETE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION log_table_changes();

-- ============================================================
-- Row Level Security (RLS)
-- ============================================================

-- RLSを有効化
/*ALTER TABLE addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_shipping_info ENABLE ROW LEVEL SECURITY;*/

-- ポリシー作成（ユーザーは自分のデータのみアクセス可能）
/*CREATE POLICY user_addresses_policy ON addresses
    FOR ALL
    USING (user_id = current_setting('app.current_user_id', true)::BIGINT);

CREATE POLICY user_orders_policy ON orders
    FOR ALL
    USING (user_id = current_setting('app.current_user_id', true)::BIGINT);

CREATE POLICY user_order_items_policy ON order_items
    FOR ALL
    USING (
        order_id IN (
            SELECT id FROM orders
            WHERE user_id = current_setting('app.current_user_id', true)::BIGINT
        )
    );

CREATE POLICY user_order_shipping_policy ON order_shipping_info
    FOR ALL
    USING (
        order_id IN (
            SELECT id FROM orders
            WHERE user_id = current_setting('app.current_user_id', true)::BIGINT
        )
    );
*/
-- ============================================================
-- サンプルデータ（開発環境用）
-- ============================================================

-- 認証情報（パスワード: "Password123!"）
INSERT INTO user_credentials (email, password_hash, is_active) VALUES
                                                                   ('test1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true),
                                                                   ('test2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true);

-- ユーザー基本情報
INSERT INTO users (credential_id, name, phone_number, date_of_birth) VALUES
                                                                         (1, '山田太郎', '03-1234-5678', '1990-01-01'),
                                                                         (2, '佐藤花子', '03-9876-5432', '1995-05-15');

-- 住所
INSERT INTO addresses (user_id, address_type, recipient_name, postal_code, prefecture, city, address_line1, phone_number, is_default) VALUES
                                                                                                                                          (1, 'default', '山田太郎', '100-0001', '東京都', '千代田区', '千代田1-1-1', '03-1234-5678', true),
                                                                                                                                          (2, 'default', '佐藤花子', '150-0001', '東京都', '渋谷区', '渋谷1-1-1', '03-9876-5432', true);

-- 商品
INSERT INTO products (name, description, price, stock, is_active) VALUES
                                                                      ('ノートパソコン', '高性能ノートパソコン', 120000.00, 50, true),
                                                                      ('マウス', 'ワイヤレスマウス', 2000.00, 200, true),
                                                                      ('キーボード', 'メカニカルキーボード', 8000.00, 100, true);

-- クーポン
INSERT INTO coupons (code, discount_type, discount_value, min_purchase_amount, valid_from, valid_until, usage_limit) VALUES
                                                                                                                         ('WELCOME10', 'percentage', 10, 5000, '2025-01-01', '2025-12-31', 100),
                                                                                                                         ('SAVE1000', 'fixed_amount', 1000, 10000, '2025-01-01', '2025-12-31', 50);
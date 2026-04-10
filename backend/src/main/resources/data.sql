-- PostGIS 확장 활성화
CREATE EXTENSION IF NOT EXISTS postgis;

-- 더미 데이터 (Spring Boot가 자동 실행)
-- password: password123 (BCrypt 해시, 동작 검증됨)

-- 레거시 시드 마이그레이션: 구 알파/베타 계정이 DB에 남아있으면 유니클로로 통합
UPDATE companies
SET name = '유니클로', email = 'uniqlo@bizmap.com', password = '$2a$10$zwo3XFSkh3fP7b1KHJYdhOCO3Yfuc7g1jVYE.s3Fg9agg9YmsoE76'
WHERE email = 'alpha@bizmap.com';

UPDATE stores
SET company_id = (SELECT id FROM companies WHERE email = 'uniqlo@bizmap.com')
WHERE company_id IN (SELECT id FROM companies WHERE email = 'beta@bizmap.com');

DELETE FROM companies WHERE email = 'beta@bizmap.com';

-- 유니클로 계정 (fresh DB 또는 누락 시)
INSERT INTO companies (name, email, password, created_at)
SELECT '유니클로', 'uniqlo@bizmap.com', '$2a$10$zwo3XFSkh3fP7b1KHJYdhOCO3Yfuc7g1jVYE.s3Fg9agg9YmsoE76', NOW()
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE email = 'uniqlo@bizmap.com');

-- 유니클로 매장 (구 회사 A) 10개
INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '강남점', 'RETAIL', '서울시 강남구 테헤란로 1', 37.4979, 127.0276, '02-1111-0001', '09:00', '21:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '강남점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '서초점', 'FOOD', '서울시 서초구 서초대로 2', 37.4837, 127.0074, '02-1111-0002', '10:00', '22:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '서초점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '역삼점', 'SERVICE', '서울시 강남구 역삼로 3', 37.5007, 127.0365, '02-1111-0003', '08:00', '20:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '역삼점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '삼성점', 'RETAIL', '서울시 강남구 삼성로 4', 37.5088, 127.0630, '02-1111-0004', '09:00', '21:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '삼성점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '압구정점', 'FOOD', '서울시 강남구 압구정로 5', 37.5270, 127.0286, '02-1111-0005', '11:00', '23:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '압구정점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '청담점', 'SERVICE', '서울시 강남구 청담동 6', 37.5247, 127.0474, '02-1111-0006', '10:00', '20:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '청담점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '논현점', 'OTHER', '서울시 강남구 논현로 7', 37.5101, 127.0248, '02-1111-0007', '09:00', '18:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '논현점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '신사점', 'RETAIL', '서울시 강남구 신사동 8', 37.5233, 127.0220, '02-1111-0008', '10:00', '21:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '신사점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '방배점', 'FOOD', '서울시 서초구 방배로 9', 37.4813, 126.9975, '02-1111-0009', '11:00', '22:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '방배점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '잠실점', 'RETAIL', '서울시 송파구 잠실로 10', 37.5133, 127.1001, '02-1111-0010', '09:00', '21:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '잠실점' AND company_id = c.id);

-- 유니클로 매장 (구 회사 B) 10개
INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '홍대점', 'FOOD', '서울시 마포구 홍익로 1', 37.5563, 126.9236, '02-2222-0001', '10:00', '24:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '홍대점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '합정점', 'RETAIL', '서울시 마포구 합정동 2', 37.5495, 126.9134, '02-2222-0002', '09:00', '21:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '합정점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '망원점', 'SERVICE', '서울시 마포구 망원로 3', 37.5565, 126.9100, '02-2222-0003', '08:00', '20:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '망원점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '연남점', 'FOOD', '서울시 마포구 연남동 4', 37.5662, 126.9236, '02-2222-0004', '11:00', '23:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '연남점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '상수점', 'OTHER', '서울시 마포구 상수동 5', 37.5477, 126.9226, '02-2222-0005', '10:00', '22:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '상수점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '이태원점', 'FOOD', '서울시 용산구 이태원로 6', 37.5344, 126.9946, '02-2222-0006', '12:00', '24:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '이태원점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '한남점', 'RETAIL', '서울시 용산구 한남대로 7', 37.5340, 127.0040, '02-2222-0007', '09:00', '21:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '한남점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '성수점', 'SERVICE', '서울시 성동구 성수이로 8', 37.5447, 127.0558, '02-2222-0008', '08:00', '19:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '성수점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '을지로점', 'RETAIL', '서울시 중구 을지로 9', 37.5660, 126.9910, '02-2222-0009', '09:00', '20:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '을지로점' AND company_id = c.id);

INSERT INTO stores (company_id, name, category, address, latitude, longitude, phone, open_time, close_time, is_active, created_at, updated_at)
SELECT c.id, '종로점', 'OTHER', '서울시 종로구 종로 10', 37.5704, 126.9831, '02-2222-0010', '10:00', '21:00', true, NOW(), NOW()
FROM companies c WHERE c.email = 'uniqlo@bizmap.com'
AND NOT EXISTS (SELECT 1 FROM stores WHERE name = '종로점' AND company_id = c.id);

-- ========== 상품 데이터 ==========
INSERT INTO products (name, category, description, created_at)
SELECT '플리스 자켓', '아우터', '부드럽고 따뜻한 플리스 소재 자켓', NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '플리스 자켓');

INSERT INTO products (name, category, description, created_at)
SELECT '히트텍 이너', '이너웨어', '발열 기능성 이너웨어', NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '히트텍 이너');

INSERT INTO products (name, category, description, created_at)
SELECT '와이드 청바지', '하의', '편안한 핏의 와이드 데님 팬츠', NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '와이드 청바지');

INSERT INTO products (name, category, description, created_at)
SELECT '다운 패딩', '아우터', '경량 구스다운 패딩 점퍼', NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '다운 패딩');

INSERT INTO products (name, category, description, created_at)
SELECT '맨투맨 스웨트셔츠', '상의', '데일리 캐주얼 맨투맨', NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '맨투맨 스웨트셔츠');

-- ========== 상품별 사이즈 데이터 ==========
INSERT INTO product_sizes (product_id, size)
SELECT p.id, s.size FROM products p
CROSS JOIN (VALUES ('S'), ('M'), ('L'), ('XL')) AS s(size)
WHERE NOT EXISTS (
    SELECT 1 FROM product_sizes ps WHERE ps.product_id = p.id AND ps.size = s.size
);

-- ========== 재고 데이터 ==========
-- 회사 A 매장들에 재고 배치
-- 강남점: 플리스 자켓 M 5, L 3 / 히트텍 이너 S 10, M 8 / 와이드 청바지 L 0 / 다운 패딩 XL 2 / 맨투맨 M 7
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 5, NOW() FROM stores s, products p
WHERE s.name = '강남점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 3, NOW() FROM stores s, products p
WHERE s.name = '강남점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 10, NOW() FROM stores s, products p
WHERE s.name = '강남점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 8, NOW() FROM stores s, products p
WHERE s.name = '강남점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 0, NOW() FROM stores s, products p
WHERE s.name = '강남점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 2, NOW() FROM stores s, products p
WHERE s.name = '강남점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 7, NOW() FROM stores s, products p
WHERE s.name = '강남점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

-- 서초점: 플리스 자켓 S 4 / 히트텍 이너 L 6 / 와이드 청바지 M 3 / 다운 패딩 M 0 / 맨투맨 L 5
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 4, NOW() FROM stores s, products p
WHERE s.name = '서초점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 6, NOW() FROM stores s, products p
WHERE s.name = '서초점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 3, NOW() FROM stores s, products p
WHERE s.name = '서초점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 0, NOW() FROM stores s, products p
WHERE s.name = '서초점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 5, NOW() FROM stores s, products p
WHERE s.name = '서초점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

-- 역삼점: 플리스 자켓 XL 2 / 히트텍 이너 M 0 / 와이드 청바지 S 7 / 다운 패딩 L 4 / 맨투맨 S 9
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 2, NOW() FROM stores s, products p
WHERE s.name = '역삼점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 0, NOW() FROM stores s, products p
WHERE s.name = '역삼점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 7, NOW() FROM stores s, products p
WHERE s.name = '역삼점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 4, NOW() FROM stores s, products p
WHERE s.name = '역삼점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 9, NOW() FROM stores s, products p
WHERE s.name = '역삼점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

-- 삼성점: 플리스 자켓 M 6 / 히트텍 이너 XL 3 / 와이드 청바지 L 5 / 다운 패딩 S 0 / 맨투맨 XL 4
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 6, NOW() FROM stores s, products p
WHERE s.name = '삼성점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 3, NOW() FROM stores s, products p
WHERE s.name = '삼성점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 5, NOW() FROM stores s, products p
WHERE s.name = '삼성점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 0, NOW() FROM stores s, products p
WHERE s.name = '삼성점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 4, NOW() FROM stores s, products p
WHERE s.name = '삼성점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

-- 잠실점: 플리스 자켓 L 8 / 히트텍 이너 S 12 / 와이드 청바지 XL 2 / 다운 패딩 M 6 / 맨투맨 M 3
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 8, NOW() FROM stores s, products p
WHERE s.name = '잠실점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 12, NOW() FROM stores s, products p
WHERE s.name = '잠실점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 2, NOW() FROM stores s, products p
WHERE s.name = '잠실점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 6, NOW() FROM stores s, products p
WHERE s.name = '잠실점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 3, NOW() FROM stores s, products p
WHERE s.name = '잠실점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

-- 회사 B 매장들에 재고 배치
-- 홍대점: 플리스 자켓 M 4 / 히트텍 이너 L 7 / 와이드 청바지 S 0 / 다운 패딩 M 5 / 맨투맨 L 6
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 4, NOW() FROM stores s, products p
WHERE s.name = '홍대점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 7, NOW() FROM stores s, products p
WHERE s.name = '홍대점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 0, NOW() FROM stores s, products p
WHERE s.name = '홍대점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 5, NOW() FROM stores s, products p
WHERE s.name = '홍대점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 6, NOW() FROM stores s, products p
WHERE s.name = '홍대점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

-- 합정점: 플리스 자켓 S 3 / 히트텍 이너 M 9 / 와이드 청바지 L 4 / 다운 패딩 XL 0 / 맨투맨 S 8
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 3, NOW() FROM stores s, products p
WHERE s.name = '합정점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 9, NOW() FROM stores s, products p
WHERE s.name = '합정점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 4, NOW() FROM stores s, products p
WHERE s.name = '합정점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 0, NOW() FROM stores s, products p
WHERE s.name = '합정점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 8, NOW() FROM stores s, products p
WHERE s.name = '합정점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

-- 이태원점: 플리스 자켓 L 5 / 히트텍 이너 S 0 / 와이드 청바지 M 6 / 다운 패딩 L 3 / 맨투맨 XL 7
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 5, NOW() FROM stores s, products p
WHERE s.name = '이태원점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 0, NOW() FROM stores s, products p
WHERE s.name = '이태원점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 6, NOW() FROM stores s, products p
WHERE s.name = '이태원점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 3, NOW() FROM stores s, products p
WHERE s.name = '이태원점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 7, NOW() FROM stores s, products p
WHERE s.name = '이태원점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

-- 성수점: 플리스 자켓 XL 1 / 히트텍 이너 L 4 / 와이드 청바지 XL 0 / 다운 패딩 S 8 / 맨투맨 M 5
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 1, NOW() FROM stores s, products p
WHERE s.name = '성수점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 4, NOW() FROM stores s, products p
WHERE s.name = '성수점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 0, NOW() FROM stores s, products p
WHERE s.name = '성수점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 8, NOW() FROM stores s, products p
WHERE s.name = '성수점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 5, NOW() FROM stores s, products p
WHERE s.name = '성수점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

-- 종로점: 플리스 자켓 S 6 / 히트텍 이너 XL 2 / 와이드 청바지 M 8 / 다운 패딩 XL 0 / 맨투맨 L 4
INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'S', 6, NOW() FROM stores s, products p
WHERE s.name = '종로점' AND p.name = '플리스 자켓'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'S');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 2, NOW() FROM stores s, products p
WHERE s.name = '종로점' AND p.name = '히트텍 이너'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'M', 8, NOW() FROM stores s, products p
WHERE s.name = '종로점' AND p.name = '와이드 청바지'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'M');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'XL', 0, NOW() FROM stores s, products p
WHERE s.name = '종로점' AND p.name = '다운 패딩'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'XL');

INSERT INTO store_inventory (store_id, product_id, size, quantity, updated_at)
SELECT s.id, p.id, 'L', 4, NOW() FROM stores s, products p
WHERE s.name = '종로점' AND p.name = '맨투맨 스웨트셔츠'
AND NOT EXISTS (SELECT 1 FROM store_inventory si WHERE si.store_id = s.id AND si.product_id = p.id AND si.size = 'L');

-- ====================================
-- 게시물 데이터 확인 SQL
-- ====================================

-- 1. 전체 게시물 수 확인
SELECT COUNT(*) AS "전체 게시물 수" FROM posts;

-- 2. 최근 10개 게시물 확인
SELECT postid, title, category, location, price, status, createdat
FROM posts
ORDER BY createdat DESC
FETCH FIRST 10 ROWS ONLY;

-- 3. 카테고리별 게시물 수
SELECT category, COUNT(*) AS cnt
FROM posts
GROUP BY category
ORDER BY cnt DESC;

-- 4. 위치별 게시물 수 (상위 10개)
SELECT location, COUNT(*) AS cnt
FROM posts
GROUP BY location
ORDER BY cnt DESC
FETCH FIRST 10 ROWS ONLY;

-- 5. 판매 상태별 게시물 수
SELECT status, COUNT(*) AS cnt
FROM posts
GROUP BY status;

-- 6. 시퀀스 현재 값 확인
SELECT sequence_name, last_number 
FROM user_sequences 
WHERE sequence_name = 'POST_SEQ';


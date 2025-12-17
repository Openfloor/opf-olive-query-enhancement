# Part 2. 운영 및 트러블슈팅 시나리오

---

## 문항 1: DB Lock 및 트랜잭션 지연

### 1.1 상황

올영세일 기간 중, 특정 매장에서 '반품 처리' 버튼 클릭 시 시스템 무응답 장애 발생.
본부의 대량 일괄 업데이트 작업으로 인한 **DB Lock(블로킹)** 의심.

---

### 1.2 Lock 확인 쿼리 (Oracle)

#### 1) Lock 대기 세션 확인
```sql
SELECT
    s.sid,
    s.serial#,
    s.username,
    s.program,
    s.status,
    s.sql_id,
    l.type,
    l.lmode,
    l.request,
    o.object_name
FROM v$session s
JOIN v$lock l ON s.sid = l.sid
LEFT JOIN dba_objects o ON l.id1 = o.object_id
WHERE l.block = 1 OR l.request > 0
ORDER BY l.block DESC;
```

#### 2) Blocking 세션 관계 확인
```sql
SELECT
    blocking.sid AS blocking_sid,
    blocking.serial# AS blocking_serial,
    blocking.username AS blocking_user,
    blocking.sql_id AS blocking_sql,
    waiting.sid AS waiting_sid,
    waiting.serial# AS waiting_serial,
    waiting.username AS waiting_user,
    waiting.seconds_in_wait
FROM v$session waiting
JOIN v$session blocking ON waiting.blocking_session = blocking.sid
WHERE waiting.blocking_session IS NOT NULL;
```

#### 3) Lock 유발 SQL 확인
```sql
SELECT sql_text
FROM v$sql
WHERE sql_id = '확인된_sql_id';
```

#### 4) 장시간 실행 트랜잭션 확인
```sql
SELECT
    s.sid,
    s.serial#,
    s.username,
    t.start_time,
    t.used_ublk,
    t.used_urec
FROM v$transaction t
JOIN v$session s ON t.addr = s.taddr
ORDER BY t.start_time;
```

---

### 1.3 긴급 조치 방법

#### Step 1: Blocking 세션 식별
```sql
-- Blocking Session의 SID, SERIAL# 확인
SELECT sid, serial#, username, sql_id
FROM v$session
WHERE sid = (SELECT blocking_session FROM v$session WHERE sid = 대기중인_SID);
```

#### Step 2: 업무 담당자 확인
- 해당 세션이 본부 배치 작업인지 확인
- 작업 중단 가능 여부 협의

#### Step 3: 세션 강제 종료 (최후 수단)
```sql
-- 일반 종료
ALTER SYSTEM KILL SESSION 'sid,serial#';

-- 즉시 강제 종료 (IMMEDIATE)
ALTER SYSTEM KILL SESSION 'sid,serial#' IMMEDIATE;
```

#### Step 4: 조치 후 확인
```sql
-- Lock 해소 확인
SELECT COUNT(*) FROM v$lock WHERE block = 1;

-- 대기 세션 해소 확인
SELECT * FROM v$session WHERE blocking_session IS NOT NULL;
```

---

### 1.4 재발 방지 대책

| 구분 | 대책 |
|-----|-----|
| **배치 스케줄 조정** | 대량 업데이트는 트래픽 적은 새벽 시간대 수행 |
| **트랜잭션 분할** | 1만건 단위로 COMMIT하여 Lock 범위 최소화 |
| **Lock Timeout 설정** | `ALTER SESSION SET ddl_lock_timeout = 60;` |
| **모니터링 알람** | Lock 대기 5분 초과 시 자동 알람 발송 |

---

## 문항 2: 배포 후 캐시(Cache) 및 버전 정합성 문제

### 2.1 상황

Vue.js 신규 플랫폼 정기 배포 후, 일부 매장에서 '화면 미갱신, 버튼 미동작' SR 폭주.
클라이언트가 **구버전 JS/CSS 캐시**를 사용하는 것으로 의심.

---

### 2.2 구간별 확인 및 조치

#### 1) 브라우저 (Client)

**확인 방법:**
```
- 개발자 도구(F12) → Network 탭
- JS/CSS 파일의 Response Header 확인
  - `Cache-Control`, `ETag`, `Last-Modified` 값 확인
- 304 (Not Modified) vs 200 응답 확인
```

**조치 방법:**
```
- 강력 새로고침: Ctrl + Shift + R (Windows) / Cmd + Shift + R (Mac)
- 캐시 삭제: 개발자 도구 → Application → Clear Storage
- 사용자 안내: "캐시 삭제 후 재접속" 공지
```

#### 2) CDN (CloudFront)

**확인 방법:**
```bash
# 캐시 상태 확인 (Response Header)
curl -I https://cdn.example.com/js/app.js

# 확인 항목:
# - X-Cache: Hit from cloudfront (캐시됨)
# - X-Cache: Miss from cloudfront (원본 조회)
# - Age: 캐시된 시간(초)
```

**조치 방법:**
```bash
# AWS CLI로 캐시 무효화 (Invalidation)
aws cloudfront create-invalidation \
    --distribution-id E1234567890 \
    --paths "/js/*" "/css/*" "/*.html"
```

**AWS Console:**
```
CloudFront → Distribution → Invalidations → Create Invalidation
- /js/*
- /css/*
- /index.html
```

#### 3) 웹 서버 (Nginx/Apache)

**확인 방법:**
```bash
# Nginx 캐시 설정 확인
cat /etc/nginx/nginx.conf | grep -A5 "location.*\.(js|css)"

# 현재 서빙 중인 파일 버전 확인
ls -la /var/www/html/js/
md5sum /var/www/html/js/app.js
```

**조치 방법 (Nginx):**
```nginx
# 정적 파일 캐시 헤더 설정
location ~* \.(js|css)$ {
    expires -1;  # 즉시 만료 (긴급 조치)
    add_header Cache-Control "no-cache, must-revalidate";
}
```

```bash
# Nginx 재시작
sudo nginx -s reload
```

---

### 2.3 근본적 해결책: 빌드/배포 파이프라인 개선

#### 1) 파일명 해시 버저닝 (권장)

**Vue CLI / Vite 설정:**
```javascript
// vite.config.js
export default {
  build: {
    rollupOptions: {
      output: {
        // 빌드 시 해시 포함된 파일명 생성
        entryFileNames: 'js/[name].[hash].js',
        chunkFileNames: 'js/[name].[hash].js',
        assetFileNames: 'assets/[name].[hash].[ext]'
      }
    }
  }
}
```

**결과:**
```
# 배포 전
/js/app.js
/css/style.css

# 배포 후 (해시 자동 변경)
/js/app.a1b2c3d4.js
/css/style.e5f6g7h8.css
```

#### 2) HTML 캐시 방지

```nginx
# index.html은 항상 최신 버전 조회
location = /index.html {
    add_header Cache-Control "no-cache, no-store, must-revalidate";
    add_header Pragma "no-cache";
    expires 0;
}

# 해시된 정적 파일은 장기 캐시
location ~* \.[a-f0-9]{8}\.(js|css)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

#### 3) CI/CD 파이프라인 적용

```yaml
# .gitlab-ci.yml 또는 GitHub Actions
deploy:
  script:
    # 1. 빌드 (해시 파일명 생성)
    - npm run build

    # 2. S3 배포
    - aws s3 sync dist/ s3://bucket-name --delete

    # 3. CloudFront 캐시 무효화 (index.html만)
    - aws cloudfront create-invalidation \
        --distribution-id $CF_DIST_ID \
        --paths "/index.html"
```

---

### 2.4 캐시 전략 요약

| 파일 유형 | 캐시 정책 | 이유 |
|----------|----------|-----|
| `index.html` | no-cache | 항상 최신 버전 필요 |
| `app.[hash].js` | 1년 (immutable) | 해시 변경 시 새 파일로 인식 |
| `style.[hash].css` | 1년 (immutable) | 해시 변경 시 새 파일로 인식 |
| `images/*` | 1주일 | 자주 변경되지 않음 |
| `fonts/*` | 1년 | 거의 변경 없음 |

---

### 2.5 배포 체크리스트

```
[ ] 빌드 파일명에 해시 포함 확인
[ ] index.html Cache-Control: no-cache 설정
[ ] CDN Invalidation 실행 (index.html)
[ ] 배포 후 브라우저에서 새 버전 로드 확인
[ ] 구버전 파일 정리 (선택)
```

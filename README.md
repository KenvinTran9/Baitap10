# Bai tap JWT - Spring Boot 3 + Security 6 + Nimbus JOSE + JWT

Day la loi giai cho vi du trong bai giang `04_JWT.pdf`. Ung dung giu nguyen luong cua bai giang nhung thay ba thu vien `jjwt-*` bang mot dependency:

```xml
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>10.10</version>
</dependency>
```

## Chuc nang da lam

- Dang ky tai khoan: `POST /auth/signup`
- Dang nhap va nhan JWT: `POST /auth/login`
- Xem tai khoan hien tai: `GET /users/me`
- Xem danh sach nguoi dung: `GET /users`
- JWT duoc ky HS256 bang `SignedJWT` + `MACSigner`
- Kiem tra `alg`, `typ`, chu ky, `iss`, `sub`, `iat`, `exp`, `jti`
- Spring Security stateless va custom `OncePerRequestFilter`
- Response loi 400 / 401 / 403 / 409 theo `application/problem+json`
- Giao dien dang ky, dang nhap va profile bang Fetch API
- H2 de chay ngay; co profile MySQL rieng
- Unit test va integration test cho toan bo luong

## Yeu cau

- JDK 17 tro len
- Maven 3.6 tro len

## Chay nhanh

Tai thu muc du an:

```powershell
mvn spring-boot:run
```

Mo `http://localhost:8005/login`, tao tai khoan, dang nhap va xem profile.

H2 console: `http://localhost:8005/h2-console`

- JDBC URL: `jdbc:h2:mem:jwt_demo`
- User: `sa`
- Password: de trong

## Test API

File `requests.http` co san 4 request theo thu tu dang ky, dang nhap, xem profile va xem danh sach user. Neu dung Postman, dat header:

```text
Authorization: Bearer <token>
```

## Chay test

```powershell
mvn test
```

## Dung MySQL nhu bai giang

Tao database:

```sql
CREATE DATABASE jwt_nimbus_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Sau do dat bien moi truong va bat profile `mysql`:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "mat-khau-mysql"
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

Co the doi host, port va ten database bang `DB_HOST`, `DB_PORT`, `DB_NAME`.

## Secret JWT

Gia tri trong `application.properties` chi la secret demo. Khi trien khai that, tao secret ngau nhien it nhat 32 byte, ma hoa Base64 va dat qua bien moi truong:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
```

Khong commit secret production vao Git. Ung dung dung `sessionStorage` cho giao dien demo; he thong production nen uu tien cookie `HttpOnly`, `Secure`, `SameSite` hoac mo hinh BFF de giam rui ro XSS.

## Diem thay doi so voi JJWT trong bai giang

| Tac vu | JJWT trong slide | Nimbus trong bai nay |
|---|---|---|
| Tao claims | `Jwts.builder()` | `JWTClaimsSet.Builder()` |
| Tao token | `compact()` | `SignedJWT.serialize()` |
| Ky HS256 | `signWith(key)` | `signedJwt.sign(new MACSigner(secret))` |
| Parse | `Jwts.parser()` | `SignedJWT.parse(token)` |
| Xac thuc chu ky | parser tu verify | `signedJwt.verify(new MACVerifier(secret))` |

Luu y: `verify()` cua Nimbus chi xac thuc chu ky. Vi vay `JwtService` con tu kiem tra issuer, thoi han va cac claim bat buoc sau khi verify.

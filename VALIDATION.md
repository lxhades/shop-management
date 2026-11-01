# Kết quả kiểm tra bộ source

Ngày: 01/11/2026.

## Đã thực hiện

- Phân tích cú pháp toàn bộ Java bằng google-java-format 1.24.0; chuẩn hóa định dạng thành công.
- Parse pom.xml bằng XML parser.
- Parse Docker Compose, GitLab CI, manifests Kubernetes và monitoring bằng YAML parser.
- Kiểm tra mọi phần tử script của GitLab CI là chuỗi lệnh hợp lệ về kiểu YAML.
- Parse Postman collection và dashboard Grafana bằng JSON parser.
- Kiểm tra thứ tự 15 request Postman, biến ID và status mong đợi.
- Kiểm tra ZIP không lỗi và có đủ các tệp cấu hình chính.

## Chưa xác nhận bằng chạy thực tế

Đã thử `mvn test`, nhưng môi trường tạo source chặn kết nối mạng của tiến trình Maven
(không tải được parent POM từ Maven Central). Vì vậy chưa xác nhận biên dịch thành công,
chưa chạy được integration tests. Không đưa JAR đã build vào bộ ZIP.

Môi trường này không có Docker, MySQL, Minikube, Helm hoặc GitLab Runner.
Chưa chạy Compose/Kubernetes/pipeline, chưa thực thi Postman và chưa kiểm tra Grafana thật.
Các cấu hình được cung cấp cùng hướng dẫn để chạy trên máy của bạn.

## Kiểm tra trên máy cá nhân

```bash
mvn clean test
mvn clean package
docker compose up --build -d
```

Tests tích hợp dùng H2 test scope; không cần MySQL cho `mvn test`.
Hai test methods kiểm tra CRUD API, tìm kiếm/lọc, validate, 404, chặn xóa danh mục có sản phẩm,
render các trang Thymeleaf và lỗi validation của form.
H2 MySQL compatibility mode không thay thế việc kiểm tra tích hợp với MySQL thật.
Sau khi chạy Compose, import Postman collection và Run collection đủ 15 requests.

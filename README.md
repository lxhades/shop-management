# TX1 — ECommerce Mini

Ứng dụng Spring Boot 3.5.7 / Java 17 / Maven / Thymeleaf / JPA Hibernate / MySQL 8.
Phạm vi theo yêu cầu chi tiết: CRUD Category và Product; không có Order, đăng nhập hay thanh toán.
Giữ Double cho price theo đề bài. Dự án thực tế nên dùng BigDecimal.

## 1. Chạy nhanh bằng Docker Desktop (khuyến nghị)

Cài Docker Desktop và bật Linux containers. Không cần cài Java/Maven trên máy nếu dùng cách này.
Giải nén, mở PowerShell tại thư mục chứa `pom.xml`:

```powershell
docker compose up --build -d
docker compose ps
docker compose logs -f shop-app
```

Đợi log `Started ShopApplication`, mở http://localhost:8080.
Thêm danh mục trước, rồi thêm sản phẩm. Chạy thử tìm kiếm, sửa, xóa.
Dockerfile multi-stage tự build JAR bằng Maven, sau đó chạy bằng JRE 17.
MySQL được chờ tới khi truy vấn được CSDL trước khi app khởi động.

- Giao diện: http://localhost:8080/products và /categories
- API: http://localhost:8080/api/products và /api/categories
- Metrics: http://localhost:8080/actuator/prometheus
- MySQL từ máy cá nhân: localhost:3307, database shop_management, user shop, password shoppassword.
- App trong Compose dùng mysql-db:3306; không dùng localhost:3307.
- Mật khẩu trong bộ bài là thông tin demo; thay nếu triển khai ngoài máy học tập.

Dừng nhưng giữ dữ liệu:

```powershell
docker compose down
```

Chỉ khi muốn xóa toàn bộ dữ liệu MySQL demo:

```powershell
docker compose down -v
```

## 2. Chạy bằng IntelliJ / Maven + MySQL

Cài JDK 17, Maven 3.6.3+, MySQL 8. Tạo database bằng `sql/schema.sql`
hoặc để Hibernate tạo bảng; tài khoản phải có quyền tạo database/bảng.
`src/main/resources/application.properties` mặc định: localhost:3306, root/rootpassword.
Chỉnh lại URL/user/password cho MySQL của bạn, hoặc dùng PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/shop_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
mvn clean test
mvn spring-boot:run
```

Trong IntelliJ: mở pom.xml → chọn JDK 17 → Maven Reload → chạy ShopApplication.
Nếu muốn chỉ chạy MySQL bằng Compose rồi app trong IntelliJ:

```powershell
docker compose up -d mysql-db
$env:DB_URL="jdbc:mysql://localhost:3307/shop_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="shop"
$env:DB_PASSWORD="shoppassword"
mvn spring-boot:run
```

## 3. Cấu trúc source

```text
src/main/java/com/example/shop/
  ShopApplication.java
  entity/       Category, Product (@ManyToOne)
  dto/          ProductForm (dùng chung JSON và form)
  repository/   Spring Data JPA + truy vấn tìm kiếm/lọc
  service/      Nghiệp vụ, transaction, kiểm tra trước khi xóa
  controller/   WebController, ApiController, ApiErrors
src/main/resources/
  templates/    Giao diện Thymeleaf cho Category / Product
  static/       CSS không phụ thuộc CDN
  application.properties
src/test/       Integration tests dùng H2 ở MySQL compatibility mode
k8s/            MySQL PVC/Secret/Deployment/Service; App Deployment/Service
monitoring/     Helm values, ServiceMonitor, dashboard JSON
postman/        Collection chạy theo thứ tự
sql/            SQL tạo database/tables (tùy chọn)
.gitlab-ci.yml  Build → test → package → build/load image → deploy
```

## 4. Kiểm thử Postman

Import `postman/Shop-Management.postman_collection.json`.
Mở Collection → Variables → đặt `baseUrl=http://localhost:8080`.
Chọn Run collection, chạy **đủ 15 request theo thứ tự 01 → 15**.
Request tạo tự lưu categoryId/productId; các request sau dùng lại và cuối lượt xóa dữ liệu vừa tạo.
Không điền ID cố định, không cần tự tạo dữ liệu trước.
Collection kiểm tra status code 201/200/204/400/404/409 và tìm kiếm đúng sản phẩm.
Nếu dừng giữa chừng, dữ liệu của lượt đó có thể còn; có thể xóa qua giao diện.

| Method | Đường dẫn | Chức năng |
|---|---|---|
| GET | /api/categories | Danh sách danh mục |
| GET | /api/categories/{id} | Chi tiết danh mục |
| POST | /api/categories | Thêm danh mục |
| PUT | /api/categories/{id} | Sửa danh mục |
| DELETE | /api/categories/{id} | Xóa danh mục; 409 nếu đang có sản phẩm |
| GET | /api/products?q=Java&categoryId=1 | Danh sách/tìm kiếm/lọc |
| GET | /api/products/{id} | Chi tiết sản phẩm |
| POST | /api/products | Thêm sản phẩm |
| PUT | /api/products/{id} | Sửa sản phẩm |
| DELETE | /api/products/{id} | Xóa sản phẩm |

JSON danh mục: `{"name":"Sách","description":"Sách lập trình"}`.
JSON sản phẩm: `{"name":"Java Core","price":150000,"stock":10,"categoryId":1}`.
Thay categoryId bằng ID có thật. Tên bắt buộc; giá >0; tồn kho >=0; danh mục bắt buộc.
ID không tồn tại → 404, sai kiểu hoặc validate → 400.
Web dùng POST cho lưu/xóa; API dùng POST/PUT/DELETE theo bảng.

## 5. Triển khai Minikube trên Windows PowerShell

Cài Docker Desktop, Minikube và kubectl; Docker Desktop đang chạy.
Với máy RAM 12 GB, nên dừng Compose, đóng ứng dụng nặng. Chạy app trước;
monitoring là bước riêng, cần thêm RAM. Lệnh start dưới đây cấp 4 GB cho cluster.

```powershell
docker compose down
minikube start --driver=docker --cpus=2 --memory=4096
docker build -t shop-management-app:latest .
minikube image load shop-management-app:latest
kubectl config use-context minikube
kubectl apply -f k8s/mysql-deployment.yml
kubectl rollout status deployment/mysql --timeout=300s
```

Lần đầu tạo schema: chạy app 1 replica trước để Hibernate không tạo bảng đồng thời:

```powershell
(Get-Content k8s/app-deployment.yml) -replace 'replicas: 2','replicas: 1' | Set-Content -Encoding utf8 app-first.yml
kubectl apply -f app-first.yml
kubectl rollout status deployment/shop-app --timeout=300s
kubectl scale deployment/shop-app --replicas=2
kubectl rollout status deployment/shop-app --timeout=300s
Remove-Item app-first.yml
kubectl get pods
kubectl get svc
minikube service shop-app-service --url
```

Giữ cửa sổ `minikube service` mở nếu công cụ tạo tunnel. Dùng URL được in ra.
Cách truy cập cố định thuận tiện cho Postman (terminal riêng):

```powershell
kubectl port-forward svc/shop-app-service 8080:80
```

Sau đó dùng baseUrl http://localhost:8080. Cổng 8080 phải trống.
Sửa code và triển khai lại thủ công:

```powershell
docker build -t shop-management-app:latest .
minikube image load shop-management-app:latest --overwrite=true
kubectl rollout restart deployment/shop-app
kubectl rollout status deployment/shop-app --timeout=300s
```

PVC giữ dữ liệu MySQL qua lần restart. `minikube stop` dừng cluster.
Ứng dụng dùng ddl-auto=update cho bài học; production nên dùng migration và validate.

## 6. GitLab CI/CD — cấu hình Runner trước khi push

Pipeline này chọn **GitLab Runner shell executor trên Linux/WSL2**, cùng máy,
cùng tài khoản hệ điều hành và cùng Docker/Minikube context với cluster.
Shared runner trên GitLab.com không truy cập được Minikube ở laptop.
Không chạy shell Linux pipeline này bằng Windows PowerShell executor.

1. Trong WSL2 Ubuntu, cài JDK 17, Maven, GitLab Runner, Docker CLI, kubectl, Minikube.
   Bật Docker Desktop → Settings → Resources → WSL Integration cho Ubuntu.
2. Khởi tạo Minikube từ chính tài khoản WSL dùng chạy Runner:

```bash
minikube start --driver=docker --cpus=2 --memory=4096
java -version
mvn -version
docker info
minikube status
kubectl get nodes
```

3. GitLab project → Settings → CI/CD → Runners → New project runner.
   Đặt tag **minikube-local**, không chọn chạy untagged; lấy runner authentication token.
4. Đăng ký và chạy Runner dưới **chính tài khoản trên** (không dùng sudo cho các lệnh này):

```bash
gitlab-runner register --url https://gitlab.com --token YOUR_RUNNER_TOKEN --executor shell --description tx1-local
# Nhập token tại máy mình, không commit token vào source.
gitlab-runner run
```

Nếu dùng GitLab tự host, thay URL. Tag cấu hình tại GitLab phải là minikube-local.
Giữ terminal Runner mở. Runner kiểu shell chỉ nên gắn với project tin cậy của bạn.
Kiểm tra Runner Online trên GitLab. Nếu là service user khác, phải cấu hình đầy đủ Docker,
Minikube và kubeconfig cho user đó; cách terminal foreground phía trên dễ cho bài demo.
5. Tại thư mục dự án (terminal khác):

```bash
git init
git add .
git commit -m "feat: implement ecommerce mini CRUD and devops"
git branch -M main
git remote add origin https://gitlab.com/YOUR_USERNAME/shop-management.git
git push -u origin main
```

Nếu repository đã có origin, dùng `git remote -v` để kiểm tra trước.
6. GitLab → Build → Pipelines: kiểm tra 4 stage. Deploy chỉ chạy nhánh main.

Pipeline build/test/package bằng Maven; deploy build image gắn commit SHA, load vào Minikube,
render manifest với SHA đó rồi rollout. Nhờ tag khác nhau, Pod lấy đúng phiên bản mới.
Deploy được tuần tự hóa bằng resource_group. JAR và JUnit reports được lưu làm artifacts.
Dockerfile tự build JAR thêm một lần để cũng hỗ trợ Compose độc lập.
Pipeline không dùng Docker Hub và không yêu cầu registry password.

## 7. Prometheus + Grafana

Cài Helm. Thực hiện sau khi app Minikube đã chạy ổn định; monitoring cần thêm tài nguyên.
Nếu thiếu RAM, giảm app xuống 1 replica hoặc dừng stack sau khi chụp minh chứng.

```powershell
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack --namespace monitoring --create-namespace -f monitoring/values.yml --wait --timeout 10m
kubectl apply -f monitoring/servicemonitor.yml
kubectl get pods -n monitoring
kubectl get servicemonitor -n monitoring
```

Tên Helm release phải là `monitoring` để khớp selector `release: monitoring`.
ServiceMonitor ở namespace monitoring chọn Service có label app: shop-app trong default,
scrape cổng có tên http và đường dẫn /actuator/prometheus. Không phụ thuộc annotation.

Terminal 1:

```powershell
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80
```

Lấy mật khẩu trên Windows PowerShell:

```powershell
$encoded = kubectl get secret monitoring-grafana -n monitoring -o jsonpath="{.data.admin-password}"
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($encoded))
```

Linux/WSL:

```bash
kubectl get secret monitoring-grafana -n monitoring -o jsonpath='{.data.admin-password}' | base64 --decode
```

Mở http://localhost:3000, user admin, password lấy từ Secret.
Dashboards → New → Import → upload `monitoring/shop-dashboard.json` → chọn Prometheus.
Dashboard gồm process CPU, JVM heap, HTTP request rate/mean latency, Hikari pool và pod memory.
CPU process và JVM heap không đồng nghĩa với CPU/memory toàn container.
Biểu đồ pod memory dùng metrics cAdvisor từ stack Kubernetes.

Để kiểm tra Targets, terminal 2:

```powershell
kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090
```

Mở http://localhost:9090 → Status → Target health, tìm serviceMonitor/monitoring/shop-app.
Target phải UP. Chạy Postman/tải trang vài lần và đợi 1–2 phút để rate có dữ liệu.
PromQL thử: `up{service="shop-app-service"}` và `http_server_requests_seconds_count`.
Gỡ monitoring: `helm uninstall monitoring -n monitoring`.

## 8. Lỗi thường gặp

| Hiện tượng | Kiểm tra/cách xử lý |
|---|---|
| Port 8080 đang dùng | Dừng app cũ/Compose/port-forward, hoặc đổi port publish |
| Không kết nối MySQL | Kiểm tra host, port, user/password; xem docker compose logs mysql-db |
| Access denied sau đổi mật khẩu | Volume cũ giữ tài khoản cũ; dùng mật khẩu cũ hoặc chỉ reset volume khi được phép mất dữ liệu |
| ErrImageNeverPull | minikube image load đúng image/tag trong đúng profile |
| Pod Pending | kubectl describe pod; kiểm tra tài nguyên và PVC |
| CrashLoopBackOff | kubectl logs deployment/shop-app; kiểm tra MySQL Service, Secret |
| Pipeline pending | Runner Online, đúng tag minikube-local và quyền chạy main |
| kubectl không kết nối | Runner cùng user/profile; kubectl config current-context, minikube status |
| Grafana No data | Target UP, đúng datasource, chọn khoảng thời gian; tạo HTTP traffic và đợi |
| ServiceMonitor không tồn tại | Cài Helm stack trước để có CRD rồi mới apply ServiceMonitor |

## 9. Checklist nộp bài / demo trong 120 phút

- 0–10 phút: chạy MySQL + ứng dụng, kiểm tra kết nối.
- 10–50 phút: demo CRUD danh mục/sản phẩm, validate và quan hệ ManyToOne.
- 50–65 phút: tìm kiếm/lọc; Postman chạy 15 request.
- 65–85 phút: Docker Compose; lưu ảnh kết quả.
- 85–105 phút: Minikube, pods/service và truy cập web.
- 105–120 phút: GitLab pipeline và monitoring nếu công cụ đã cài sẵn.

Cài mới toàn bộ Docker/WSL/Runner/Helm trong 120 phút có thể không kịp; chuẩn bị trước.
Minh chứng: bảng DB/FK; giao diện CRUD; lỗi validate; Postman pass; Compose ps;
GitLab pipeline xanh; kubectl get pods/svc; Grafana có dữ liệu.

## 10. Tham khảo chính thức

- Spring Boot: https://docs.spring.io/spring-boot/3.5/system-requirements.html
- Prometheus Operator: https://prometheus-operator.dev/docs/developer/getting-started/
- Docker Compose: https://docs.docker.com/compose/
- GitLab shell executor: https://docs.gitlab.com/runner/executors/shell/

Xem `VALIDATION.md` để biết kiểm tra thực sự đã chạy trong môi trường tạo source.

# Chạy nhanh bằng Docker Compose
git clone https://gitlab.com/lxhades2004/shop-management.git
cd shop-management
docker compose up --build -d

Repository Private sẽ yêu cầu xác thực GitLab. Đợi ứng dụng khởi động rồi mở http://localhost:8080.
Dockerfile tự build Java bên trong container, nên không cần cài Java/Maven trên máy mới nếu chỉ dùng cách này
# Chạy Kubernetes và Grafana 

1. Cài Docker Desktop, bật Kubernetes và chờ node Ready.
2. Clone source như trên.
3. Build image:
   docker build -t shop-management-app:latest .

4. Triển khai MySQL:
   kubectl config use-context docker-desktop
   kubectl apply -f k8s/mysql-deployment.yml
   kubectl rollout status deployment/mysql --timeout=300s

5. Triển khai ứng dụng một bản sao trước:
   (Get-Content k8s/app-deployment.yml) -replace 'replicas: 2','replicas: 1' | Set-Content -Encoding utf8 app-first.yml
   kubectl apply -f app-first.yml
   kubectl rollout status deployment/shop-app --timeout=300s
   kubectl scale deployment/shop-app --replicas=2
   Remove-Item app-first.yml

6. Mở ứng dụng:
   kubectl port-forward svc/shop-app-service 8081:80

Truy cập http://localhost:8081.
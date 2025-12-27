# Kubernetes 部署指南 - qyuan-paper-RD

## 📁 文件说明

- `k8s-deployment.yaml` - Podman 生成的原始配置
- `k8s-deployment-optimized.yaml` - 优化的生产级配置（推荐使用）
- `k8s-ingress.yaml` - Ingress 配置（用于外部访问）

## 🚀 快速开始

### 前提条件

1. 已有 Kubernetes 集群（minikube, kind, k3s, 或云服务商 K8s）
2. 已安装 kubectl
3. 已将镜像推送到镜像仓库

### 步骤 1: 推送镜像到镜像仓库

```bash
# 如果使用 Docker Hub
docker tag localhost/qyuan-paper-rd_qyuan-paper-rd:latest your-username/qyuan-paper-rd:latest
docker push your-username/qyuan-paper-rd:latest

# 如果使用私有仓库
podman tag localhost/qyuan-paper-rd_qyuan-paper-rd:latest your-registry/qyuan-paper-rd:latest
podman push your-registry/qyuan-paper-rd:latest
```

### 步骤 2: 修改配置文件

编辑 `k8s-deployment-optimized.yaml`，修改镜像名称：

```yaml
# 找到这一行（大约第 145 行）
image: localhost/qyuan-paper-rd_qyuan-paper-rd:latest

# 改为你的镜像
image: your-registry/qyuan-paper-rd:latest
```

### 步骤 3: 部署到 Kubernetes

```bash
# 部署所有资源
kubectl apply -f k8s-deployment-optimized.yaml

# 或者如果使用 Ingress
kubectl apply -f k8s-deployment-optimized.yaml
kubectl apply -f k8s-ingress.yaml
```

### 步骤 4: 验证部署

```bash
# 查看 pod 状态
kubectl get pods -n qyuan-paper-rd

# 查看 service
kubectl get svc -n qyuan-paper-rd

# 查看日志
kubectl logs -n qyuan-paper-rd deployment/qyuan-paper-rd

# 查看 health check
kubectl port-forward -n qyuan-paper-rd svc/qyuan-paper-rd-service 9003:9003
curl http://localhost:9003/actuator/health
```

## 📊 架构说明

### 组件说明

1. **Namespace**: `qyuan-paper-rd` - 独立命名空间
2. **ConfigMap**: 存储非敏感配置（Redis、Nacos 地址等）
3. **Secret**: 存储敏感信息（华为云 AK/SK）
4. **PVC**: Redis 数据持久化
5. **Deployment**: Redis 和 Spring Boot 应用
6. **Service**: 集群内服务发现
7. **Ingress**: 外部访问入口

### 资源配置

| 组件 | CPU Request | CPU Limit | Memory Request | Memory Limit |
|------|-------------|-----------|----------------|--------------|
| Redis | 100m | 200m | 128Mi | 256Mi |
| Application | 250m | 500m | 512Mi | 1Gi |

## 🔧 常用命令

### 查看 Pod 状态
```bash
kubectl get pods -n qyuan-paper-rd -w
```

### 查看日志
```bash
# 应用日志
kubectl logs -n qyuan-paper-rd deployment/qyuan-paper-rd -f

# Redis 日志
kubectl logs -n qyuan-paper-rd deployment/redis -f
```

### 重启部署
```bash
kubectl rollout restart deployment/qyuan-paper-rd -n qyuan-paper-rd
```

### 扩缩容
```bash
# 扩展到 3 个副本
kubectl scale deployment/qyuan-paper-rd -n qyuan-paper-rd --replicas=3
```

### 进入容器
```bash
kubectl exec -it -n qyuan-paper-rd deployment/qyuan-paper-rd -- /bin/bash
```

### 删除部署
```bash
kubectl delete -f k8s-deployment-optimized.yaml
```

## 🌍 外部访问配置

### 方式 1: NodePort（简单）

修改 `k8s-deployment-optimized.yaml` 中的 Service：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: qyuan-paper-rd-service
  namespace: qyuan-paper-rd
spec:
  type: NodePort  # 改为 NodePort
  selector:
    app: qyuan-paper-rd
  ports:
  - port: 9003
    targetPort: 9003
    nodePort: 30003  # 30000-32767 范围内
```

访问: `http://<node-ip>:30003`

### 方式 2: LoadBalancer（云服务商）

```yaml
spec:
  type: LoadBalancer  # 保持不变
```

云服务商会自动分配外部 IP。

### 方式 3: Ingress（推荐生产环境）

```bash
kubectl apply -f k8s-ingress.yaml
```

需要先安装 Ingress Controller：

```bash
# NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml

# 或使用 Helm
helm install ingress-nginx ingress-nginx/ingress-nginx
```

## 🔐 安全建议

### 1. 使用 Secret 管理敏感信息

```bash
# 从环境变量创建 Secret
kubectl create secret generic qyuan-secret \
  --from-literal=HUAWEICLOUD_SDK_AK='your-ak' \
  --from-literal=HUAWEICLOUD_SDK_SK='your-sk' \
  -n qyuan-paper-rd
```

### 2. 使用私有镜像仓库

```bash
# 创建镜像拉取 Secret
kubectl create secret docker-registry regcred \
  --docker-server=your-registry \
  --docker-username=username \
  --docker-password=password \
  -n qyuan-paper-rd
```

### 3. Network Policy（网络策略）

限制 Pod 间通信：

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: qyuan-paper-rd-netpol
  namespace: qyuan-paper-rd
spec:
  podSelector:
    matchLabels:
      app: qyuan-paper-rd
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: ingress-nginx  # 只允许 Ingress 访问
    ports:
    - protocol: TCP
      port: 9003
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: redis  # 只能访问 Redis
    ports:
    - protocol: TCP
      port: 6379
  - to:  # 允许访问外部服务（Nacos、华为云 OBS）
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 8848  # Nacos
    - protocol: TCP
      port: 443   # HTTPS
```

## 📈 监控和日志

### Prometheus + Grafana

```yaml
# 添加 ServiceMonitor for Prometheus
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: qyuan-paper-rd
  namespace: qyuan-paper-rd
spec:
  selector:
    matchLabels:
      app: qyuan-paper-rd
  endpoints:
  - port: http
    path: /actuator/prometheus
```

### 日志收集

推荐使用 EFK (Elasticsearch + Fluentd + Kibana) 或 Loki。

## 🐛 故障排查

### Pod 无法启动

```bash
# 查看 Pod 事件
kubectl describe pod -n qyuan-paper-rd <pod-name>

# 查看 Pod 日志
kubectl logs -n qyuan-paper-rd <pod-name>
```

### 镜像拉取失败

```bash
# 检查镜像是否存在
kubectl get pods -n qyuan-paper-rd

# 检查 imagePullSecret
kubectl get secret -n qyuan-paper-rd
```

### 健康检查失败

```bash
# 检查健康检查配置
kubectl get deployment qyuan-paper-rd -n qyuan-paper-rd -o yaml

# 进入容器检查
kubectl exec -it -n qyuan-paper-rd <pod-name> -- /bin/bash
curl http://localhost:9003/actuator/health
```

## 📚 相关资源

- [Kubernetes 官方文档](https://kubernetes.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/guides/topicals/spring-boot-kubernetes/)
- [Podman Kubernetes](https://docs.podman.io/en/latest/markdown/podman-generate-kube.1.html)

## ⚠️ 注意事项

1. **生产环境**：
   - 修改 `replicas` 至少为 2
   - 配置 HPA（Horizontal Pod Autoscaler）
   - 启用 Pod 反亲和性
   - 配置 Pod 中断预算

2. **镜像仓库**：
   - 使用私有仓库替代 localhost
   - 定期更新镜像版本

3. **监控告警**：
   - 配置 Prometheus 监控
   - 设置资源使用告警

4. **备份**：
   - 定期备份 Redis 数据
   - 备份 Kubernetes 资源配置

## 🔄 更新部署

```bash
# 更新镜像
kubectl set image deployment/qyuan-paper-rd \
  qyuan-paper-rd=your-registry/qyuan-paper-rd:v2.0 \
  -n qyuan-paper-rd

# 查看滚动更新状态
kubectl rollout status deployment/qyuan-paper-rd -n qyuan-paper-rd

# 回滚
kubectl rollout undo deployment/qyuan-paper-rd -n qyuan-paper-rd
```

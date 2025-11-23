# 构建阶段
FROM node:18-alpine AS build
WORKDIR /app

# 复制package.json并安装依赖
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY . .
RUN npm run build:prod

# 运行阶段
FROM nginx:alpine
WORKDIR /app

# 复制构建产物
COPY --from=build /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]

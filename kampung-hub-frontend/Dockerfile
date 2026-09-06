# Multi-stage Dockerfile for Angular 17 Standalone Frontend
# Stage 1: Build Angular application
FROM node:20-alpine AS build
WORKDIR /app

# Cache npm dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build production bundle
COPY . .
RUN npm run build

# Stage 2: Minimal Nginx Web Server
FROM nginx:alpine
COPY --from=build /app/dist/kampung-hub-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

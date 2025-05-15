FROM node:22-alpine as build
WORKDIR /app
COPY ./frontend/package*.json ./
RUN npm install
COPY ./frontend/ .
RUN npm run build

FROM nginx:alpine
COPY ./nginx.prod.conf /etc/nginx/nginx.conf
COPY ./certificates/selfsigned.crt /etc/ssl/certificate.crt
COPY ./certificates/selfsigned.key /etc/ssl/private.key
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

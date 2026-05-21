FROM nginx:alpine

RUN apk add --no-cache apache2-utils \
    && htpasswd -cb /etc/nginx/.htpasswd eos changeme123

COPY build/ /usr/share/nginx/html/

RUN printf 'server {\n\
    listen 80;\n\
    server_name _;\n\
    root /usr/share/nginx/html;\n\
    index index.html;\n\
    auth_basic "EOS Architecture Docs";\n\
    auth_basic_user_file /etc/nginx/.htpasswd;\n\
    location / {\n\
        try_files $uri $uri/ /index.html;\n\
    }\n\
}\n' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

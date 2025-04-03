#!/bin/bash

services=(
    "eureka-server"
    "config-server"
    "apigateway-service"
    "user-service"
    "product-service"
    "order-service"
    "cart-service"
    "payment-service"
    "image-service"
)

for service in "${services[@]}"; do
    echo "--------------------------------------------"
    echo "[$service] 빌드 시작..."
    echo "--------------------------------------------"

    cd "$service"
    chmod +x ./gradlew
    ./gradlew clean build -x test

    if [ $? -ne 0 ]; then
        echo "[$service] 빌드 실패!"
        exit 1
    fi

    cd ..
    echo "[$service] 빌드 완료!"
done

echo "============================================"
echo "모든 마이크로서비스 빌드가 성공적으로 완료되었습니다!"
echo "============================================"
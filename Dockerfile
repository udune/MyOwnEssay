#빌드 이미지
FROM gradle:8.11.1-jdk17 AS build
#작업 디렉토리 설정
WORKDIR /app
#호스트의 모든 파일을 컨테이너의 작업 디렉토리로 복사
COPY . .
#테스트를 제외한 빌드 실행
RUN gradle build -x test --no-daemon

#실행 이미지
FROM openjdk:17-jdk-slim
#작업 디렉토리 설정
WORKDIR /app
#빌드 이미지에서 생성된 jar 파일을 실행 이미지로 복사
COPY --from=build /app/build/libs/*.jar app.jar
#8080 포트 노출
EXPOSE 8080
#애플리케이션 실행
ENTRYPOINT ["java","-jar","app.jar"]
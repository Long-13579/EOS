.PHONY: backend frontend dev docker

backend:
	cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

docker-up:
	cd backend && docker compose up -d

docker-clean:
	cd backend && docker compose down -v

docker-down:
	cd backend && docker compose down

frontend:
	cd frontend && npm run dev

dev:
	make -j2 backend frontend
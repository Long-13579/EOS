.PHONY: backend frontend dev

backend:
	cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

frontend:
	cd frontend && npm run dev

dev:
	make -j2 backend frontend
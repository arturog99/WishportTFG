# WishPort Backend

API REST simplificada para la gestión de reservas de pistas deportivas.

## Tecnologías

- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA (Hibernate)
- MySQL 8
- Maven

## Configuración

1. Crear base de datos MySQL:
```sql
CREATE DATABASE wishport_db;
```

2. Configurar `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wishport_db?createDatabaseIfNotExist=true&serverTimezone=Europe/Madrid
spring.datasource.username=root
spring.datasource.password=tu_password
wishport.jwt.secret=tu_clave_secreta_muy_larga
```

3. Ejecutar:
```bash
mvn spring-boot:run
```

## Estructura del proyecto

```
com.wishport.backend
├── entities/        # Usuario, Pista, Reserva
├── repositories/    # Repositorios JPA
├── controllers/     # Controladores REST
├── security/        # JwtUtil, JwtAuthenticationFilter
├── config/          # SecurityConfig
└── WishportBackendApplication.java
```

## Endpoints API (pendiente de implementar)

### Usuarios
- POST `/api/usuarios` - Registrar usuario
- POST `/api/usuarios/login` - Login
- GET `/api/usuarios/me` - Usuario actual

### Pistas
- GET `/api/pistas` - Listar todas

### Reservas
- POST `/api/reservas` - Crear reserva
- GET `/api/reservas/usuario/{id}` - Reservas por usuario
- DELETE `/api/reservas/{id}` - Cancelar reserva

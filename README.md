# 🎾 WishPort — Gestión de Reservas Deportivas

WishPort es una aplicación completa para la gestión de reservas de instalaciones deportivas, compuesta por una app nativa Android y un backend REST API en Spring Boot. Diseñada para agilizar el proceso de reserva y validación de accesos mediante códigos QR.

---

## 📋 Funcionalidades

### 👤 Usuario
- Registro e inicio de sesión seguro con JWT  
- Explorar pistas deportivas disponibles  
- Ver el detalle de cada pista con horarios y estado  
- Reservar una pista en una franja horaria específica  
- Proceso de checkout con simulación de pago  
- Gestionar y cancelar reservas propias  
- Editar el perfil personal  
- Auto-login: la sesión se mantiene entre cierres de la app  

### 🔧 Administrador
- Todas las funcionalidades del usuario estándar  
- Escanear códigos QR para validar el acceso de los usuarios a las instalaciones  
- Panel visual para comprobar las reservas del día en curso  

---

## 🛠️ Stack Tecnológico

| Capa            | Tecnología |
|-----------------|-----------|
| **Frontend**    | Java 8, Retrofit 2, Glide, ZXing |
| **Backend**     | Java 17, Spring Boot 3.2.0, Spring Security |
| **Seguridad**   | JWT, AuthInterceptor |
| **Escáner QR**  | ZXing (zxing-android-embedded) |
| **Base de Datos**| MySQL 8 (Aiven Cloud) |
| **ORM**         | Spring Data JPA / Hibernate |
| **Despliegue**  | Render (contenedores Docker) |
| **Build**       | Maven (backend), Gradle/Kotlin DSL (frontend) |

---

## 🏗️ Arquitectura

El proyecto sigue una arquitectura monorepo con separación clara entre frontend y backend:

### Frontend (Android)
**Patrón MVVM con separación estricta de responsabilidades:**

**Flujo de Datos:**  
UI (Activities) ➔ ViewModels ➔ Repositories ➔ ApiService (Retrofit) ➔ Backend REST

**Estructura del Proyecto:**
- `ui/`: Pantallas (Activities) - LoginActivity, PistasActivity, ReservasActivity, etc.
- `api/`: Configuración de Retrofit, Interceptores y gestión de Tokens
- `adapters/`: Controladores para las listas de RecyclerView (PistaAdapter, ReservaAdapter)
- `utils/`: Utilidades como TokenManager para gestión de JWT
- `res/`: Recursos Android (layouts, drawables, values)

**Dependencias Principales:**
- Retrofit 2 + Gson: Cliente HTTP y serialización JSON
- Glide: Carga y caché de imágenes
- ZXing: Escáner de códigos QR
- AndroidX: Componentes modernos de Android (AppCompat, CardView, ConstraintLayout)
- Core Library Desugaring: Soporte para APIs modernas en versiones antiguas de Android

**Configuración:**
- compileSdk: 34
- minSdk: 24 (Android 7.0)
- targetSdk: 34
- Java 8 con desugaring para compatibilidad  

### Backend (Spring Boot)
**Arquitectura en capas REST:**

**Estructura del Proyecto:**
- `entities/`: Entidades JPA (Usuario, Pista, Reserva)  
- `repositories/`: Repositorios Spring Data JPA  
- `controllers/`: Controladores REST API  
- `security/`: JwtUtil, JwtAuthenticationFilter  
- `config/`: SecurityConfig, WebConfig (CORS)  

---

## 🚀 Configuración y Ejecución

### Backend

#### Requisitos Previos
- JDK 17 o superior
- Maven 3.6+
- MySQL 8 (o usar la base de datos en la nube)

#### Configuración Local
1. Crear base de datos MySQL:
```sql
CREATE DATABASE wishport_db;
```

2. Configurar `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wishport_db?createDatabaseIfNotExist=true&serverTimezone=Europe/Madrid
spring.datasource.username=root
spring.datasource.password=tu_password
wishport.jwt.secret=tu_clave_secreta_muy_larga
```

3. Ejecutar:
```bash
cd backend
mvn spring-boot:run
```

#### Despliegue con Docker
```bash
cd backend
docker build -t wishport-backend .
docker run -p 8080:8080 wishport-backend
```

### Frontend (Android)

#### Requisitos Previos
- Android Studio Ladybug o superior  
- JDK 11 o superior  
- Dispositivo físico o emulador con Android 7.0 (API 24) o superior  
- Conexión a internet activa  

#### Pasos para ejecutar
1. Clona este repositorio en tu equipo  
2. Abre la carpeta `frontend/` desde Android Studio  
3. Espera a que termine de sincronizar Gradle  
4. Pulsa **Run 'app'** para compilar e instalar en tu emulador o móvil  

---

⚠️ **Aviso sobre el servidor Cloud:**  
El backend está alojado en la capa gratuita de Render, por lo que entra en suspensión tras un periodo de inactividad. La primera vez que abras la app puede tardar entre 40 y 50 segundos en responder mientras el servidor despierta. Después funcionará con total fluidez.

---

## 🔌 API Endpoints

### Autenticación
- `POST /api/usuarios/login` - Iniciar sesión y obtener JWT
- `POST /api/usuarios` - Registrar nuevo usuario
- `GET /api/usuarios/me` - Obtener perfil del usuario actual (requiere JWT)

### Pistas
- `GET /api/pistas` - Listar todas las pistas
- `GET /api/pistas/{id}` - Obtener detalle de una pista

### Reservas
- `POST /api/reservas` - Crear nueva reserva (requiere JWT)
- `GET /api/reservas/usuario/{id}` - Obtener reservas de un usuario (requiere JWT)
- `DELETE /api/reservas/{id}` - Cancelar reserva (requiere JWT)

### Imágenes
- `GET /images/{filename}` - Servir imágenes estáticas de pistas

---

## 🛡️ Cuentas de Prueba

| Rol | Email | Password |
|-----|------|----------|
| Usuario normal (Reservas) | usuario@prueba.com | prueba1234 |

*(Alternativamente, puedes registrar un nuevo usuario desde la pantalla inicial).*

---

## 🔮 Mejoras a Futuro

- 🔔 Notificaciones Push: Integración con Firebase (FCM) para enviar recordatorios previos al partido  
- 📍 Geolocalización: Uso de Google Maps API para mostrar la ubicación del club y trazar la ruta óptima  
- 💳 Pagos Reales: Sustituir la pasarela simulada por el SDK oficial de Stripe  
- 📅 Sincronización de Calendario: Exportación automática de la reserva a Google Calendar mediante Intents nativos  

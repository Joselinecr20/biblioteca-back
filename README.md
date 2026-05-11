# Biblioteca - Sistema de Gestión de Biblioteca Universitaria

Backend REST API desarrollado con Spring Boot 3.2.5 + JWT + MySQL.

---

## Requisitos Previos

| Herramienta | Versión mínima |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| MySQL | 8.0+ |
| XAMPP (opcional) | Cualquiera con MySQL 8 |

---

## Configuración de la Base de Datos

1. Iniciar MySQL (XAMPP → Start MySQL)
2. Abrir phpMyAdmin o MySQL Workbench
3. Ejecutar el script completo:

```
biblioteca_v3_final (1).sql
```

Esto crea la base de datos `biblioteca` con todas las tablas y datos de prueba.

### Actualizar contraseñas a BCrypt

Las contraseñas del script SQL son texto plano. El sistema usa BCrypt. Ejecuta este UPDATE para que el login funcione:

```sql
-- Contraseña: admin123
UPDATE cuenta SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y' WHERE usuario = 'admin';

-- Contraseña: bib123
UPDATE cuenta SET password = '$2a$10$YcmCFhE6LZDhNNaUTmSNRuWyKLZ0lSVOJn0V.pz5jBHRR3e1FBVKK' WHERE usuario = 'bibliotecario1';

-- Contraseña: est123
UPDATE cuenta SET password = '$2a$10$Ul/JhSX3sW1oXGl2Zm5N7e9SyaJvEEgK4nXQRJJf3kH5g7Vu3bW5.' WHERE usuario = 'estudiante1';
```

> **Nota:** Si prefieres generar tus propios hashes, usa https://bcrypt-generator.com/ con cost 10.

---

## Cómo Correr el Proyecto

```bash
# Clonar / posicionarse en la carpeta del proyecto
cd C:\xampp\htdocs\Biblioteca

# Compilar y correr
mvn spring-boot:run
```

El servidor inicia en: `http://localhost:8080`

### application.properties relevante

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/biblioteca
spring.datasource.username=root
spring.datasource.password=         # vacío para XAMPP por defecto
jwt.secret=claveSecretaBiblioteca2024SuperSegura2048bits
jwt.expiration=86400000             # 24 horas en ms
```

---

## Autenticación

Todos los endpoints protegidos requieren el header:

```
Authorization: Bearer <token>
```

El token se obtiene con `POST /auth/login`.

---

## Endpoints y Ejemplos JSON

### POST /auth/login

**Request:**
```json
{
  "usuario": "estudiante1",
  "password": "est123"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "idCuenta": 3,
  "usuario": "estudiante1",
  "rol": "estudiante",
  "nombre": "maria",
  "apellido": "gonzalez"
}
```

---

### GET /libros

**Response 200:** (sin autenticación requerida)
```json
[
  {
    "idLibro": 1,
    "titulo": "cien anos de soledad",
    "editorial": "editorial sudamericana",
    "anioPublicacion": 1967,
    "descripcion": "obra maestra del realismo magico latinoamericano",
    "portadaUrl": null,
    "isbn": "978-0-06-088328-7",
    "categoria": "ficcion",
    "autores": ["gabriel garcia marquez"],
    "imagenes": [...],
    "cantidadDisponible": 3
  }
]
```

### GET /libros/{id}

```
GET /libros/1
Authorization: (no requerida)
```

### POST /libros *(admin, bibliotecario)*

```json
{
  "titulo": "El Principito",
  "editorial": "Reynal & Hitchcock",
  "anioPublicacion": 1943,
  "descripcion": "Cuento filosófico y poético",
  "portadaUrl": "https://ejemplo.com/portada.jpg",
  "isbn": "978-84-376-1000-0",
  "idCategoria": 1,
  "idAutores": [1]
}
```

### PUT /libros/{id} *(admin, bibliotecario)*

Mismo body que POST.

### DELETE /libros/{id} *(solo admin)*

```
DELETE /libros/1
Authorization: Bearer <token-admin>
```

---

### GET /usuarios *(admin, bibliotecario)*

**Response 200:**
```json
[
  {
    "idUsuario": 3,
    "nombre": "maria",
    "apellido": "gonzalez",
    "correo": "maria@uees.edu.sv",
    "telefono": "7000-0002",
    "idCuenta": 3,
    "usuario": "estudiante1",
    "rol": "estudiante",
    "activo": true,
    "fechaRegistro": "2024-01-01T00:00:00"
  }
]
```

### POST /usuarios *(solo admin)*

```json
{
  "nombre": "Ana",
  "apellido": "Lopez",
  "correo": "ana@uees.edu.sv",
  "telefono": "7001-0001",
  "usuario": "estudiante2",
  "password": "pass123",
  "idRol": 3
}
```

### PUT /usuarios/{id} *(solo admin)*

Mismo body que POST. Si `password` está en blanco, no se actualiza.

---

### GET /reservas *(admin, bibliotecario)*

**Response 200:**
```json
[
  {
    "idReserva": 1,
    "idUsuario": 3,
    "nombreUsuario": "maria gonzalez",
    "idLibro": 1,
    "tituloLibro": "cien anos de soledad",
    "portadaLibro": null,
    "idBiblioteca": 1,
    "nombreBiblioteca": "biblioteca central",
    "fechaReserva": "2024-06-01T10:30:00",
    "fechaExpiracion": "2024-06-04",
    "estado": "pendiente",
    "observaciones": null
  }
]
```

### GET /reservas/mis *(estudiante)*

Retorna solo las reservas del estudiante autenticado.

### POST /reservas *(estudiante)*

```json
{
  "idLibro": 1,
  "idBiblioteca": 1
}
```

Validaciones automáticas:
- Sin multas pendientes
- No superar `max_prestamos` del rol (3 para estudiantes)
- `cantidad_disponible > 0`

### PUT /reservas/{id}/aprobar *(admin, bibliotecario)*

```
PUT /reservas/1/aprobar
Authorization: Bearer <token-bibliotecario>
```

Crea el préstamo automáticamente y descuenta disponibilidad.

### PUT /reservas/{id}/rechazar *(admin, bibliotecario)*

```json
{
  "observaciones": "El libro está reservado para un evento académico"
}
```

### DELETE /reservas/{id} *(estudiante)*

Solo puede cancelar sus propias reservas en estado `pendiente`.

---

### GET /prestamos *(admin, bibliotecario)*

**Response 200:**
```json
[
  {
    "idPrestamo": 1,
    "idUsuario": 3,
    "nombreUsuario": "maria gonzalez",
    "idLibro": 1,
    "tituloLibro": "cien anos de soledad",
    "idBiblioteca": 1,
    "nombreBiblioteca": "biblioteca central",
    "idReserva": 1,
    "fechaPrestamo": "2024-06-01T11:00:00",
    "fechaDevolucion": "2024-06-16",
    "fechaDevolucionReal": null,
    "estado": "activo"
  }
]
```

### GET /prestamos/mis *(estudiante)*

### POST /prestamos/{id}/devolver *(admin, bibliotecario)*

```json
{
  "fechaDevolucionReal": "2024-06-20"
}
```

Si `fechaDevolucionReal > fechaDevolucion`, se genera multa automática:
- `diasRetraso = diferencia en días`
- `monto = diasRetraso × 0.25`

---

### GET /multas *(admin, bibliotecario)*

**Response 200:**
```json
[
  {
    "idMulta": 1,
    "idPrestamo": 1,
    "idUsuario": 3,
    "nombreUsuario": "maria gonzalez",
    "tituloLibro": "cien anos de soledad",
    "monto": 1.00,
    "diasRetraso": 4,
    "estado": "pendiente",
    "fechaGeneracion": "2024-06-20T09:00:00",
    "fechaPago": null
  }
]
```

### GET /multas/mis *(estudiante)*

### PUT /multas/{id}/pagar *(admin, bibliotecario)*

```
PUT /multas/1/pagar
Authorization: Bearer <token-bibliotecario>
```

---

## Roles y Permisos

| Endpoint | admin | bibliotecario | estudiante |
|---|:---:|:---:|:---:|
| GET /libros, /libros/{id} | ✓ | ✓ | ✓ |
| POST /libros | ✓ | ✓ | |
| PUT /libros/{id} | ✓ | ✓ | |
| DELETE /libros/{id} | ✓ | | |
| GET /usuarios | ✓ | ✓ | |
| POST /usuarios | ✓ | | |
| PUT /usuarios/{id} | ✓ | | |
| GET /reservas | ✓ | ✓ | |
| GET /reservas/mis | | | ✓ |
| POST /reservas | | | ✓ |
| PUT /reservas/{id}/aprobar | ✓ | ✓ | |
| PUT /reservas/{id}/rechazar | ✓ | ✓ | |
| DELETE /reservas/{id} | | | ✓ |
| GET /prestamos | ✓ | ✓ | |
| GET /prestamos/mis | | | ✓ |
| POST /prestamos/{id}/devolver | ✓ | ✓ | |
| GET /multas | ✓ | ✓ | |
| GET /multas/mis | | | ✓ |
| PUT /multas/{id}/pagar | ✓ | ✓ | |

---

## Estructura del Proyecto

```
src/main/java/com/biblioteca/
├── BibliotecaApplication.java
├── config/
│   ├── JwtConfig.java
│   └── SecurityConfig.java
├── controller/         (AuthController, LibroController, UsuarioController,
│                        ReservaController, PrestamoController, MultaController)
├── service/            (AuthService, LibroService, UsuarioService,
│                        ReservaService, PrestamoService, MultaService)
├── repository/         (12 repositorios JPA)
├── model/              (13 entidades JPA)
├── dto/
│   ├── request/        (LoginRequest, LibroRequest, UsuarioRequest,
│   │                    ReservaRequest, DevolucionRequest)
│   └── response/       (LoginResponse, LibroResponse, UsuarioResponse,
│                        ReservaResponse, PrestamoResponse, MultaResponse)
├── security/           (JwtUtil, JwtFilter, UserDetailsServiceImpl,
│                        CuentaUserDetails)
└── exception/          (GlobalExceptionHandler, ResourceNotFoundException,
                         BusinessException)
```

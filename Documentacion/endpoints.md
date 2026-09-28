# Catálogo de Endpoints de la API REST

Este documento detalla todos los endpoints expuestos por la API REST desarrollada en **FastAPI**, especificando el método HTTP, la estructura de la petición, las cabeceras de autorización y los ejemplos de respuesta.

**Base URL Local:** `http://localhost:8000` (o `http://10.0.2.2:8000` desde el emulador de Android).

---

## 1. Módulo de Autenticación

### 1.1 Iniciar Sesión
- **Ruta:** `POST /login`
- **Requiere Autenticación:** No
- **Cuerpo de la Petición (`application/json`):**
  ```json
  {
    "email": "admin@paralelo.com",
    "password": "admin123"
  }
  ```
- **Código de Respuesta:** `200 OK`
- **Ejemplo de Respuesta:**
  ```json
  {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "bearer",
    "user": {
      "id": 1,
      "nombre": "Usuario",
      "apellido": "Demo",
      "email": "admin@paralelo.com",
      "foto": null,
      "createdAt": "2026-09-28T12:00:00"
    }
  }
  ```

---

### 1.2 Registrar Usuario
- **Ruta:** `POST /register`
- **Requiere Autenticación:** No
- **Cuerpo de la Petición (`application/json`):**
  ```json
  {
    "nombre": "Carlos",
    "apellido": "Gomez",
    "email": "carlos@paralelo.com",
    "password": "passwordSeguro123",
    "foto": null
  }
  ```
- **Código de Respuesta:** `201 Created`
- **Ejemplo de Respuesta:**
  ```json
  {
    "id": 2,
    "nombre": "Carlos",
    "apellido": "Gomez",
    "email": "carlos@paralelo.com",
    "foto": null,
    "createdAt": "2026-09-28T12:05:00"
  }
  ```

---

## 2. Módulo de Usuarios (CRUD)

Todos los endpoints de este módulo requieren la cabecera:
`Authorization: Bearer <TOKEN_JWT>`

### 2.1 Listar Usuarios
- **Ruta:** `GET /users`
- **Parámetros Query:** `skip=0`, `limit=100` (opcionales)
- **Código de Respuesta:** `200 OK`
- **Ejemplo de Respuesta:**
  ```json
  [
    {
      "id": 1,
      "nombre": "Usuario",
      "apellido": "Demo",
      "email": "admin@paralelo.com",
      "foto": null,
      "createdAt": "2026-09-28T12:00:00"
    },
    {
      "id": 2,
      "nombre": "Carlos",
      "apellido": "Gomez",
      "email": "carlos@paralelo.com",
      "foto": null,
      "createdAt": "2026-09-28T12:05:00"
    }
  ]
  ```

---

### 2.2 Consultar Usuario por ID
- **Ruta:** `GET /users/{id}`
- **Código de Respuesta:** `200 OK` / `404 Not Found`
- **Ejemplo de Respuesta:**
  ```json
  {
    "id": 1,
    "nombre": "Usuario",
    "apellido": "Demo",
    "email": "admin@paralelo.com",
    "foto": null,
    "createdAt": "2026-09-28T12:00:00"
  }
  ```

---

### 2.3 Crear Usuario desde Panel
- **Ruta:** `POST /users`
- **Cuerpo de la Petición (`application/json`):**
  ```json
  {
    "nombre": "Maria",
    "apellido": "Rodriguez",
    "email": "maria@paralelo.com",
    "password": "Password123",
    "foto": "https://url-foto.com/perfil.jpg"
  }
  ```
- **Código de Respuesta:** `201 Created`

---

### 2.4 Actualizar Usuario
- **Ruta:** `PUT /users/{id}`
- **Cuerpo de la Petición (`application/json`):**
  ```json
  {
    "nombre": "Maria Elena",
    "apellido": "Rodriguez",
    "email": "maria_updated@paralelo.com",
    "foto": "https://url-foto.com/nueva.jpg"
  }
  ```
- **Código de Respuesta:** `200 OK`

---

### 2.5 Eliminar Usuario
- **Ruta:** `DELETE /users/{id}`
- **Código de Respuesta:** `200 OK`
- **Ejemplo de Respuesta:**
  ```json
  {
    "message": "Usuario 2 eliminado exitosamente",
    "id": 2
  }
  ```

---

## 3. Módulo de Archivos

### 3.1 Subir Archivo
- **Ruta:** `POST /upload`
- **Cabecera:** `Authorization: Bearer <TOKEN_JWT>`
- **Tipo de Contenido:** `multipart/form-data`
- **Parámetro Form:** `file` (archivo binario: `.jpg`, `.png`, `.pdf`, `.txt`, `.csv`, etc.)
- **Código de Respuesta:** `201 Created`
- **Ejemplo de Respuesta:**
  ```json
  {
    "id": 15,
    "filename": "4a71bf0d9c_foto_usuario_15.jpg",
    "url": "/uploads/4a71bf0d9c_foto_usuario_15.jpg",
    "original_name": "foto_usuario_15.jpg",
    "size": 84520,
    "createdAt": "2026-09-28T12:10:00"
  }
  ```

---

### 3.2 Eliminar Archivo
- **Ruta:** `DELETE /upload/{id}`
- **Cabecera:** `Authorization: Bearer <TOKEN_JWT>`
- **Código de Respuesta:** `200 OK`
- **Ejemplo de Respuesta:**
  ```json
  {
    "message": "Archivo 15 eliminado exitosamente",
    "id": 15
  }
  ```

---

## 4. Módulo de Dashboard (Consumo Concurrente)

Estos endpoints están diseñados para ser consumidos en simultáneo por la aplicación móvil al cargar la pantalla principal:

### 4.1 Perfil del Usuario Autenticado
- **Ruta:** `GET /dashboard/profile`
- **Cabecera:** `Authorization: Bearer <TOKEN_JWT>`
- **Código de Respuesta:** `200 OK`

---

### 4.2 Métricas del Sistema y Base de Datos
- **Ruta:** `GET /dashboard/stats`
- **Cabecera:** `Authorization: Bearer <TOKEN_JWT>`
- **Código de Respuesta:** `200 OK`
- **Ejemplo de Respuesta:**
  ```json
  {
    "total_users": 5,
    "total_files": 12,
    "system_status": "OPERATIONAL",
    "active_threads": 8,
    "memory_usage_mb": 45.2
  }
  ```

---

### 4.3 Notificaciones Recientes
- **Ruta:** `GET /dashboard/notifications`
- **Cabecera:** `Authorization: Bearer <TOKEN_JWT>`
- **Código de Respuesta:** `200 OK`
- **Ejemplo de Respuesta:**
  ```json
  [
    {
      "id": 1,
      "title": "Bienvenido a Paralelo Móvil",
      "message": "Has iniciado sesión correctamente.",
      "timestamp": "2026-09-28 12:00:00",
      "type": "info"
    }
  ]
  ```

---

## 5. Módulo de Procesamiento Pesado (Divisores)

Endpoints mantenidos para pruebas comparativas computacionales en CPU:

### 5.1 Cálculo Secuencial
- **Ruta:** `GET /api/proceso/secuencial`
- **Ejemplo de Respuesta:**
  ```json
  {
    "mode": "sequential",
    "start_num": 10000000,
    "total_elements": 100000,
    "total_divisors": 1725690,
    "execution_time_seconds": 18.254112
  }
  ```

### 5.2 Cálculo Concurrente
- **Ruta:** `GET /api/proceso/concurrente`
- **Ejemplo de Respuesta:**
  ```json
  {
    "mode": "concurrent",
    "start_num": 10000000,
    "total_elements": 100000,
    "threads_used": 4,
    "total_divisors": 1725690,
    "execution_time_seconds": 5.124890
  }
  ```

# API Endpoints y Modelo Simplificado

Documento generado a partir de:
- `routes/api.php`
- Controladores en `app/Http/Controllers/Api`
- Resources en `app/Http/Resources`
- Models en `app/Models`

## Convenciones generales

- Base path: `/api`
- Auth: Sanctum Bearer token en endpoints protegidos
- Validacion Laravel: errores de validacion retornan `422` con estructura estandar (`message`, `errors`)
- Endpoints con `Resource::collection(...)` paginados retornan estructura Laravel:
  - `data`: array de items
  - `links`: links de paginacion
  - `meta`: metadatos de paginacion

## Endpoints existentes

## 1) Salud

### GET `/api/health`
- Auth: no
- Resultado `200`:

```json
{
  "message": "Esto es freetable"
}
```

## 2) Auth

### POST `/api/auth/register`
- Auth: no
- Body:

```json
{
  "name": "string (required)",
  "email": "email (required, unique)",
  "password": "string (required, min:6)"
}
```

- Resultado `201`:

```json
{
  "user": {
    "id": 1,
    "name": "Juan",
    "email": "juan@mail.com",
    "role": "client",
    "created_at": "2026-03-25T10:00:00.000000Z"
  },
  "token": "plain_text_token"
}
```

### POST `/api/auth/login`
- Auth: no
- Body:

```json
{
  "email": "email (required)",
  "password": "string (required)"
}
```

- Resultado `200`:

```json
{
  "user": {
    "id": 1,
    "name": "Juan",
    "email": "juan@mail.com",
    "role": "client",
    "created_at": "2026-03-25T10:00:00.000000Z"
  },
  "token": "plain_text_token"
}
```

- Error credenciales `401`:

```json
{
  "message": "Credenciales invalidas"
}
```

### POST `/api/auth/logout`
- Auth: si
- Resultado `200`:

```json
{
  "message": "Logout correcto"
}
```

### GET `/api/auth/me`
- Auth: si
- Resultado `200`:

```json
{
  "user": {
    "id": 1,
    "name": "Juan",
    "email": "juan@mail.com",
    "role": "client",
    "created_at": "2026-03-25T10:00:00.000000Z"
  }
}
```

## 3) Categorias

### GET `/api/categories`
- Auth: no
- Resultado `200`:

```json
{
  "data": [
    {
      "id": 1,
      "name": "Italiana",
      "slug": "italiana"
    }
  ]
}
```

## 4) Restaurantes

### GET `/api/restaurants`
- Auth: no
- Query params:
  - `search` (opcional)
  - `category_id` (opcional)
  - `per_page` (opcional, min 1, max 50, default 10)
- Resultado `200` (paginado):

```json
{
  "data": [
    {
      "id": 1,
      "name": "La Mesa",
      "description": "...",
      "address": "...",
      "phone": "...",
      "manager_id": 2,
      "created_at": "2026-03-25T10:00:00.000000Z",
      "manager": {
        "id": 2,
        "name": "Manager",
        "email": "manager@mail.com",
        "role": "manager",
        "created_at": "2026-03-25T10:00:00.000000Z"
      },
      "categories": [
        {
          "id": 1,
          "name": "Italiana",
          "slug": "italiana"
        }
      ],
      "cover_image": {
        "id": 5,
        "path": "restaurants/images/cover.jpg",
        "url": "/storage/restaurants/images/cover.jpg",
        "alt": "Portada",
        "is_cover": true
      },
      "images": [],
      "menus": []
    }
  ],
  "links": {},
  "meta": {}
}
```

### GET `/api/restaurants/{restaurant}`
- Auth: no
- Resultado `200`:

```json
{
  "data": {
    "id": 1,
    "name": "La Mesa",
    "description": "...",
    "address": "...",
    "phone": "...",
    "manager_id": 2,
    "created_at": "2026-03-25T10:00:00.000000Z",
    "manager": { "id": 2, "name": "Manager", "email": "manager@mail.com", "role": "manager", "created_at": "..." },
    "categories": [ { "id": 1, "name": "Italiana", "slug": "italiana" } ],
    "cover_image": { "id": 5, "path": "...", "url": "...", "alt": "...", "is_cover": true },
    "images": [ { "id": 5, "path": "...", "url": "...", "alt": "...", "is_cover": true } ],
    "menus": [ { "id": 9, "name": "Menu Lunch", "file_path": "restaurants/menus/menu.pdf", "url": "/storage/restaurants/menus/menu.pdf" } ]
  }
}
```

### POST `/api/restaurants`
- Auth: si
- Permisos: admin o manager
- Body:

```json
{
  "name": "string (required)",
  "description": "string|null",
  "address": "string (required)",
  "phone": "string|null",
  "manager_id": "int|null (solo admin)",
  "category_ids": [1, 2]
}
```

- Resultado `201`:

```json
{
  "message": "Restaurante creado correctamente",
  "data": { "...": "RestaurantResource" }
}
```

- Error permisos `403`:

```json
{ "message": "No autorizado" }
```

### PUT/PATCH `/api/restaurants/{restaurant}`
- Auth: si
- Permisos: admin o manager dueno del restaurante
- Body: mismos campos que create, con reglas `sometimes` en algunos campos
- Resultado `200`:

```json
{
  "message": "Restaurante actualizado correctamente",
  "data": { "...": "RestaurantResource" }
}
```

- Error permisos `403`: `{ "message": "No autorizado" }`

### DELETE `/api/restaurants/{restaurant}`
- Auth: si
- Permisos: admin o manager dueno
- Resultado `200`:

```json
{
  "message": "Restaurante eliminado correctamente"
}
```

### PUT `/api/restaurants/{restaurant}/categories`
- Auth: si
- Permisos: admin o manager dueno
- Body:

```json
{
  "category_ids": [1, 3, 5]
}
```

- Resultado `200`:

```json
{
  "message": "Categorias actualizadas correctamente",
  "data": { "...": "RestaurantResource" }
}
```

## 5) Archivos (imagenes y menus)

### POST `/api/restaurants/{restaurant}/images`
- Auth: si
- Permisos: admin o manager dueno
- Content-Type: `multipart/form-data`
- Campos:
  - `image`: archivo imagen requerido, max 2048 KB
  - `is_cover`: boolean opcional
  - `alt`: string opcional
- Resultado `201`:

```json
{
  "message": "Imagen subida correctamente",
  "data": {
    "id": 10,
    "path": "restaurants/images/x.jpg",
    "url": "/storage/restaurants/images/x.jpg",
    "alt": "Fachada",
    "is_cover": true
  }
}
```

### POST `/api/restaurants/{restaurant}/menus`
- Auth: si
- Permisos: admin o manager dueno
- Content-Type: `multipart/form-data`
- Campos:
  - `file`: PDF requerido, max 5120 KB
  - `name`: string requerido
- Resultado `201`:

```json
{
  "message": "Menu subido correctamente",
  "data": {
    "id": 3,
    "name": "Menu Cena",
    "file_path": "restaurants/menus/menu-cena.pdf",
    "url": "/storage/restaurants/menus/menu-cena.pdf"
  }
}
```

## 6) Reservas

### POST `/api/restaurants/{restaurant}/reservations`
- Auth: si
- Body:

```json
{
  "reservation_time": "date (required, after:now)",
  "people": "int (required, min 1, max 30)"
}
```

- Resultado `201`:

```json
{
  "message": "Reserva creada correctamente",
  "data": {
    "id": 4,
    "reservation_time": "2026-03-28 21:00:00",
    "people": 2,
    "status": "pending",
    "created_at": "2026-03-25T10:00:00.000000Z",
    "user": { "id": 1, "name": "Juan", "email": "juan@mail.com", "role": "client", "created_at": "..." },
    "restaurant": { "id": 1, "name": "La Mesa", "description": "...", "address": "...", "phone": "...", "manager_id": 2, "created_at": "...", "manager": null, "categories": [], "cover_image": null, "images": [], "menus": [] }
  }
}
```

### GET `/api/reservations/me`
- Auth: si
- Query params:
  - `per_page` (opcional, min 1, max 50, default 10)
- Resultado `200` (paginado):

```json
{
  "data": [
    {
      "id": 4,
      "reservation_time": "2026-03-28 21:00:00",
      "people": 2,
      "status": "pending",
      "created_at": "2026-03-25T10:00:00.000000Z",
      "user": null,
      "restaurant": {
        "id": 1,
        "name": "La Mesa",
        "description": "...",
        "address": "...",
        "phone": "...",
        "manager_id": 2,
        "created_at": "...",
        "manager": null,
        "categories": [],
        "cover_image": null,
        "images": [],
        "menus": []
      }
    }
  ],
  "links": {},
  "meta": {}
}
```

### GET `/api/restaurants/{restaurant}/reservations`
- Auth: si
- Permisos: admin o manager dueno
- Query params:
  - `per_page` (opcional, min 1, max 50, default 10)
- Resultado `200` (paginado):

```json
{
  "data": [
    {
      "id": 4,
      "reservation_time": "2026-03-28 21:00:00",
      "people": 2,
      "status": "pending",
      "created_at": "2026-03-25T10:00:00.000000Z",
      "user": { "id": 1, "name": "Juan", "email": "juan@mail.com", "role": "client", "created_at": "..." },
      "restaurant": { "id": 1, "name": "La Mesa", "description": "...", "address": "...", "phone": "...", "manager_id": 2, "created_at": "...", "manager": null, "categories": [], "cover_image": null, "images": [], "menus": [] }
    }
  ],
  "links": {},
  "meta": {}
}
```

- Error permisos `403`: `{ "message": "No autorizado" }`

### PATCH `/api/reservations/{reservation}/status`
- Auth: si
- Permisos: admin o manager del restaurante
- Body:

```json
{
  "status": "pending|confirmed|cancelled"
}
```

- Resultado `200`:

```json
{
  "message": "Estado de reserva actualizado correctamente",
  "data": { "...": "ReservationResource" }
}
```

### DELETE `/api/reservations/{reservation}`
- Auth: si
- Permisos: usuario dueno de reserva, admin, o manager del restaurante
- Resultado `200`:

```json
{
  "message": "Reserva cancelada correctamente",
  "data": { "...": "ReservationResource con status=cancelled" }
}
```

## 7) Comentarios

### GET `/api/restaurants/{restaurant}/comments`
- Auth: no
- Query params:
  - `per_page` (opcional, min 1, max 50, default 10)
- Resultado `200` (paginado + resumen rating):

```json
{
  "data": [
    {
      "id": 8,
      "content": "Muy buena comida",
      "rating": "4.5",
      "created_at": "2026-03-25T10:00:00.000000Z",
      "user": {
        "id": 1,
        "name": "Juan",
        "email": "juan@mail.com",
        "role": "client",
        "created_at": "..."
      }
    }
  ],
  "links": {},
  "meta": {
    "current_page": 1,
    "from": 1,
    "last_page": 1,
    "path": "...",
    "per_page": 10,
    "to": 1,
    "total": 1,
    "average_rating": 4.5,
    "ratings_count": 1
  }
}
```

### POST `/api/restaurants/{restaurant}/comments`
- Auth: si
- Body:

```json
{
  "content": "string (required, 2..2000)",
  "rating": "string regex: 1,1.5,2,2.5,...,5"
}
```

- Resultado `201`:

```json
{
  "message": "Comentario creado correctamente",
  "data": {
    "id": 8,
    "content": "Muy buena comida",
    "rating": "4.5",
    "created_at": "2026-03-25T10:00:00.000000Z",
    "user": {
      "id": 1,
      "name": "Juan",
      "email": "juan@mail.com",
      "role": "client",
      "created_at": "..."
    }
  }
}
```

### DELETE `/api/comments/{comment}`
- Auth: si
- Permisos: dueno del comentario, admin, o manager del restaurante
- Resultado `200`:

```json
{
  "message": "Comentario eliminado correctamente"
}
```

- Error permisos `403`: `{ "message": "No autorizado" }`

## Modelo simplificado (JSON) de clases y relaciones

```json
{
  "User": {
    "fields": ["id", "name", "email", "password", "role", "created_at", "updated_at"],
    "relations": {
      "restaurants": "hasMany(Restaurant, manager_id)",
      "reservations": "hasMany(Reservation)",
      "comments": "hasMany(Comment)"
    }
  },
  "Restaurant": {
    "fields": ["id", "name", "description", "address", "phone", "manager_id", "created_at", "updated_at"],
    "relations": {
      "manager": "belongsTo(User, manager_id)",
      "reservations": "hasMany(Reservation)",
      "comments": "hasMany(Comment)",
      "images": "hasMany(RestaurantImage)",
      "menus": "hasMany(RestaurantMenu)",
      "coverImage": "hasOne(RestaurantImage where is_cover=true)",
      "categories": "belongsToMany(Category, category_restaurant)"
    }
  },
  "Reservation": {
    "fields": ["id", "user_id", "restaurant_id", "reservation_time", "people", "status", "created_at", "updated_at"],
    "relations": {
      "user": "belongsTo(User)",
      "restaurant": "belongsTo(Restaurant)"
    }
  },
  "Comment": {
    "fields": ["id", "user_id", "restaurant_id", "content", "rating", "created_at", "updated_at"],
    "relations": {
      "user": "belongsTo(User)",
      "restaurant": "belongsTo(Restaurant)"
    }
  },
  "Category": {
    "fields": ["id", "name", "slug", "created_at", "updated_at"],
    "relations": {
      "restaurants": "belongsToMany(Restaurant, category_restaurant)"
    }
  },
  "RestaurantImage": {
    "fields": ["id", "restaurant_id", "path", "alt", "is_cover", "created_at", "updated_at"],
    "computed": ["url = Storage::url(path)"],
    "relations": {
      "restaurant": "belongsTo(Restaurant)"
    }
  },
  "RestaurantMenu": {
    "fields": ["id", "restaurant_id", "name", "file_path", "created_at", "updated_at"],
    "computed": ["url = Storage::url(file_path)"],
    "relations": {
      "restaurant": "belongsTo(Restaurant)"
    }
  }
}
```

## Cardinalidad rapida

```json
{
  "User -> Restaurant": "1:N (manager)",
  "User -> Reservation": "1:N",
  "User -> Comment": "1:N",
  "Restaurant -> Reservation": "1:N",
  "Restaurant -> Comment": "1:N",
  "Restaurant -> RestaurantImage": "1:N",
  "Restaurant -> RestaurantMenu": "1:N",
  "Restaurant -> Category": "N:M",
  "Restaurant -> coverImage": "1:1 (filtrado por is_cover=true)"
}
```

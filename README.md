# Freetable Android App (Cliente)

Aplicacion Android nativa (Java + XML) para consumir la API de reservas de restaurantes.

## Funcionalidades implementadas (cliente)

- Autenticacion: registro, login, sesion persistente y logout.
- Navegacion inferior con 3 tabs:
  - Inicio: categorias, restaurantes populares y recomendaciones.
  - Buscar: busqueda de restaurantes por texto.
  - Perfil: datos de usuario y listado de mis reservas.
- Detalle de restaurante:
  - carga de datos del restaurante
  - creacion de reserva con fecha/hora y numero de personas
- Reservas:
  - listar mis reservas
  - cancelar reserva desde perfil

## Configuracion de API

La URL base esta en:

- `app/src/main/java/com/example/freetableapp/data/remote/ApiClient.java`

Valor actual:

- `http://10.0.2.2:8000/api/`

> `10.0.2.2` funciona para emulador Android apuntando a localhost de tu PC.

## Estructura principal

- `app/src/main/java/com/example/freetableapp/auth`
- `app/src/main/java/com/example/freetableapp/data`
- `app/src/main/java/com/example/freetableapp/ui`
- `app/src/main/java/com/example/freetableapp/restaurant`

## Build rapido

```powershell
Set-Location "C:\Users\ycamacho\AndroidStudioProjects\freetableApp"
.\gradlew.bat assembleDebug --no-daemon
```

APK debug generado en:

- `app/build/outputs/apk/debug/app-debug.apk`


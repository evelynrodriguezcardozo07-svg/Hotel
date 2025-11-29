# 🏨 Sistema de Gestión Hotelera - Guía de Despliegue

## 📋 Resumen del Proyecto

- **Frontend**: Angular 19 desplegado en GitHub Pages
- **Backend**: Spring Boot 3.5.7 con PostgreSQL
- **Base de Datos**: PostgreSQL en Render

## 🌐 URLs del Proyecto

- **Frontend (GitHub Pages)**: https://evelynrodriguezcardozo07-svg.github.io/Hotel/
- **Backend (Render)**: Se configurará después del despliegue
- **Repositorio**: https://github.com/evelynrodriguezcardozo07-svg/Hotel

---

## 🚀 Despliegue del Backend en Render

### Paso 1: Crear Cuenta en Render

1. Ve a [https://render.com](https://render.com)
2. Haz clic en **"Get Started for Free"**
3. Registrate con tu cuenta de GitHub
4. Autoriza a Render para acceder a tus repositorios

### Paso 2: Crear Base de Datos PostgreSQL

1. En el dashboard de Render, haz clic en **"New +"** → **"PostgreSQL"**
2. Configura la base de datos:
   - **Name**: `hotel-database` (o el nombre que prefieras)
   - **Database**: `hoteldemo`
   - **User**: Se genera automáticamente
   - **Region**: Ohio (US East) - es la más cercana y gratuita
   - **PostgreSQL Version**: 16
   - **Plan**: **Free** (gratis con 90 días de retención)
3. Haz clic en **"Create Database"**
4. ⏳ Espera 2-3 minutos mientras se crea la base de datos
5. **IMPORTANTE**: Guarda la siguiente información que aparecerá:
   - **Internal Database URL** (la usaremos)
   - **External Database URL**
   - Hostname, Port, Database, Username, Password

### Paso 3: Cargar el Schema de la Base de Datos

1. En la página de tu base de datos en Render, ve a la pestaña **"Connect"**
2. Copia el comando **PSQL Command** (algo como):
   ```bash
   PGPASSWORD=xxx psql -h dpg-xxx.ohio-postgres.render.com -U hotel_database_user hotel_database
   ```
3. Abre tu terminal local y ejecuta ese comando (necesitas tener PostgreSQL instalado localmente)
   
   **Alternativa sin PostgreSQL local**: Usa la Shell Web de Render:
   - En tu base de datos, ve a la pestaña **"Shell"** (arriba)
   - Se abrirá una consola web conectada a tu base de datos

4. Una vez conectado, copia y pega el contenido del archivo `database/schema-postgresql.sql` completo
5. Presiona Enter y verifica que se ejecute sin errores

### Paso 4: Crear el Web Service (Backend)

1. En el dashboard de Render, haz clic en **"New +"** → **"Web Service"**
2. Selecciona **"Build and deploy from a Git repository"**
3. Haz clic en **"Connect"** en tu repositorio `Hotel`
4. Configura el servicio:

   **Configuración Básica:**
   - **Name**: `hotel-backend` (o el que prefieras, este será tu subdominio)
   - **Region**: Ohio (US East) - misma región que la BD
   - **Branch**: `master`
   - **Root Directory**: Déjalo vacío (o pon `.` si pide algo)
   - **Runtime**: `Docker`
   - **Plan**: **Free**

   **NO necesitas configurar Build/Start Commands** porque usamos Dockerfile

5. Haz clic en **"Advanced"** para configurar variables de entorno

### Paso 5: Configurar Variables de Entorno

En la sección **"Environment Variables"**, agrega las siguientes variables:

```
DATABASE_URL=<pega aquí el Internal Database URL de tu base de datos>
JWT_SECRET=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
CULQI_PUBLIC_KEY=pk_test_e91ae6aa184b726d
CULQI_SECRET_KEY=sk_test_1573b0e8079863ff
GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
CORS_ALLOWED_ORIGINS=https://evelynrodriguezcardozo07-svg.github.io
```

**IMPORTANTE**: 
- Reemplaza `DATABASE_URL` con el **Internal Database URL** que copiaste
- El formato debe ser: `postgresql://usuario:password@host:puerto/database`
- El JWT_SECRET actual es de prueba, considera cambiarlo en producción

6. Haz clic en **"Create Web Service"**

### Paso 6: Esperar el Despliegue

1. Render comenzará a construir tu aplicación (tomará 5-10 minutos la primera vez)
2. Verás los logs en tiempo real
3. Busca mensajes como:
   ```
   Started ProyectoFinalWebApplication in X seconds
   ```
4. Cuando veas **"Your service is live 🎉"**, el backend está desplegado

### Paso 7: Obtener la URL del Backend

1. En la página de tu servicio, encontrarás la URL en la parte superior
2. Será algo como: `https://hotel-backend-xxxx.onrender.com`
3. **¡COPIA ESTA URL!** La necesitaremos para el frontend

### Paso 8: Probar el Backend

Abre en tu navegador o Postman:
```
https://tu-backend-url.onrender.com/api/hotels/publicos
```

Deberías ver una respuesta JSON con hoteles disponibles.

---

## 🔄 Actualizar el Frontend con la URL del Backend

### Paso 1: Actualizar environment.prod.ts

1. Abre el archivo: `hotel-frontend/src/environments/environment.prod.ts`
2. Reemplaza `YOUR_APP_NAME` con tu URL real de Render:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://hotel-backend-xxxx.onrender.com/api'
   };
   ```

### Paso 2: Rebuild y Redeploy del Frontend

En tu terminal:

```powershell
cd "hotel-frontend"

# Construir con producción
ng build --configuration production --base-href "/Hotel/"

# Desplegar en GitHub Pages
npx angular-cli-ghpages --dir=dist/hotel-frontend/browser
```

### Paso 3: Verificar que Funcione

1. Abre tu sitio: https://evelynrodriguezcardozo07-svg.github.io/Hotel/
2. Prueba buscar hoteles
3. Intenta hacer login con:
   - Email: `cliente@hotel.com`
   - Password: `password123`

---

## 📊 Datos de Prueba

El sistema incluye usuarios de prueba (password para todos: `password123`):

| Email | Rol | Descripción |
|-------|-----|-------------|
| `admin@hotel.com` | admin | Administrador del sistema |
| `propietario@hotel.com` | host | Propietario de hoteles |
| `cliente@hotel.com` | guest | Usuario cliente |

---

## 🐛 Solución de Problemas

### El backend no inicia

1. Revisa los logs en Render (pestaña "Logs")
2. Verifica que `DATABASE_URL` esté correctamente configurado
3. Asegúrate de que el schema se cargó correctamente en PostgreSQL

### Error de CORS en el frontend

1. Verifica que `CORS_ALLOWED_ORIGINS` incluya tu URL de GitHub Pages
2. Asegúrate de NO tener `http://` en lugar de `https://`

### La base de datos no tiene datos

1. Ejecuta nuevamente el script `database/schema-postgresql.sql`
2. El script incluye datos de prueba al final

### El frontend no conecta con el backend

1. Verifica que actualizaste `environment.prod.ts` con la URL correcta
2. Asegúrate de haber reconstruido el proyecto (`ng build`)
3. Verifica que redeployeaste en GitHub Pages

### Render dice "Build failed"

1. Verifica que el `Dockerfile` esté en la raíz del repositorio
2. Revisa los logs para ver el error específico
3. Asegúrate de que Java 25 esté disponible (el Dockerfile usa `eclipse-temurin:25-jdk`)

---

## 🔧 Comandos Útiles

### Redesplegar Frontend
```powershell
cd hotel-frontend
ng build --configuration production --base-href "/Hotel/"
npx angular-cli-ghpages --dir=dist/hotel-frontend/browser
```

### Ver logs del Backend (Render)
- Ve a tu servicio en Render → pestaña "Logs"

### Conectar a la Base de Datos
- Ve a tu database en Render → pestaña "Shell"
- O usa el PSQL command localmente

### Forzar Redespliegue del Backend
1. Ve a tu servicio en Render
2. Click en "Manual Deploy" → "Deploy latest commit"

---

## 📝 Notas Importantes

### Plan Free de Render

- ✅ Gratis para siempre
- ⚠️ El servicio se "duerme" después de 15 minutos de inactividad
- ⚠️ La primera solicitud después de dormir tarda ~30-50 segundos en despertar
- ✅ 750 horas de ejecución al mes (suficiente para desarrollo/demo)
- ✅ Base de datos PostgreSQL con 90 días de retención

### Seguridad

- 🔒 Cambia el `JWT_SECRET` en producción
- 🔒 Las claves de Culqi son de TEST - usa claves reales en producción
- 🔒 Considera habilitar HTTPS en todas las peticiones (Render lo hace por defecto)

### Mantenimiento

- La base de datos Free se borra después de 90 días de inactividad
- Haz backups periódicos si es importante
- Render puede tardar en despertar, avisa a tus usuarios

---

## 🎉 ¡Listo!

Tu aplicación completa está desplegada:
- ✅ Frontend en GitHub Pages
- ✅ Backend en Render
- ✅ Base de Datos PostgreSQL en Render

**URL Final**: https://evelynrodriguezcardozo07-svg.github.io/Hotel/

---

## 📞 Soporte

Si tienes problemas, verifica:
1. Los logs de Render (pestaña Logs del servicio)
2. La consola del navegador (F12) en el frontend
3. Que todas las URLs estén correctamente configuradas

**Repositorio**: https://github.com/evelynrodriguezcardozo07-svg/Hotel

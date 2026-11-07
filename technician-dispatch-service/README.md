# technician-dispatch-service

Microservicio **liviano** que simula la disponibilidad de los técnicos de HelpFlow.
Recibe un ticket (id, categoría y prioridad) y responde **qué técnico se sugiere**, **qué tan disponible está** y **cuántas horas tardaría** en atenderlo.

- Puerto: **8081**
- Sin base de datos, sin JPA y sin seguridad (así lo define el enunciado).
- No guarda estado: cada respuesta se calcula en el momento.
- Lo consume únicamente `support-ticket-service`; los usuarios finales no lo llaman directamente.

---

## Endpoint

### `POST /technician-dispatch`

**Petición**

```json
{
  "ticketId": 125,
  "categoria": "RED",
  "prioridad": "ALTA"
}
```

| Campo | Tipo | Reglas |
|---|---|---|
| `ticketId` | número | obligatorio, mayor que 0 |
| `categoria` | texto | `HARDWARE`, `SOFTWARE`, `RED`, `ACCESOS`, `OTRO` |
| `prioridad` | texto | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` |

**Respuesta `200 OK`**

```json
{
  "ticketId": 125,
  "tecnicoId": "TEC-006",
  "disponibilidad": "MEDIA",
  "tiempoEstimadoHoras": 6
}
```

Si falta un campo o un valor no es válido, responde **400** en formato ProblemDetail.

### `GET /actuator/health`

Indica si el servicio está vivo (`{"status":"UP"}`).

---

## Cómo calcula la respuesta (determinista)

"Determinista" significa que **la misma entrada siempre produce la misma salida**.

1. **Seed:** `Objects.hash(ticketId, categoria, prioridad)` convierte los tres datos en un número. Para los mismos datos, el número siempre es el mismo.
2. **Generador:** `new Random(seed)`. Un `Random` con semilla fija siempre genera la misma secuencia de "aleatorios".
3. **Técnico:** se elige uno de los técnicos de la especialidad pedida.

   | Categoría | Técnicos posibles |
   |---|---|
   | HARDWARE | TEC-001, TEC-002 |
   | SOFTWARE | TEC-003, TEC-004 |
   | RED | TEC-005, TEC-006 |
   | ACCESOS | TEC-007, TEC-008 |
   | OTRO | TEC-009, TEC-010 |

   Los códigos coinciden con los técnicos que `support-ticket-service` crea en su base de datos (`V3__datos_iniciales.sql`).

4. **Tiempo estimado** según la prioridad (más urgente = menos espera):

   | Prioridad | Rango de horas |
   |---|---|
   | CRITICA | 0 – 3 |
   | ALTA | 1 – 6 |
   | MEDIA | 2 – 12 |
   | BAJA | 4 – 24 |

5. **Disponibilidad** según las horas:

   | Horas | Disponibilidad |
   |---|---|
   | 0 – 2 | ALTA |
   | 3 – 8 | MEDIA |
   | más de 8 | BAJA |

---

## Estructura de carpetas

Como es un servicio pequeño y sin base de datos, usa la estructura clásica por capas (no necesita arquitectura hexagonal).

```
technician-dispatch-service/
├── pom.xml                      Dependencias: web, validation, actuator, test
├── Dockerfile                   Imagen multi-stage (Maven + JDK → JRE)
├── .dockerignore                Archivos que no se copian a la imagen
└── src/
    ├── main/
    │   ├── java/com/helpflow/dispatch/
    │   │   ├── TechnicianDispatchApplication.java   Clase main: arranca Spring Boot
    │   │   ├── controller/
    │   │   │   └── DispatchController.java          Recibe POST /technician-dispatch y registra un log
    │   │   ├── service/
    │   │   │   └── DispatchService.java             Lógica: seed → técnico → horas → disponibilidad
    │   │   └── dto/
    │   │       ├── DispatchRequest.java             Forma del JSON de entrada (+ validaciones)
    │   │       └── DispatchResponse.java            Forma del JSON de salida
    │   └── resources/
    │       └── application.yml                      Puerto 8081, ProblemDetail, actuator
    └── test/java/com/helpflow/dispatch/
        ├── service/DispatchServiceTest.java         Determinismo, especialidad y clasificación
        └── controller/DispatchControllerTest.java   Endpoint con MockMvc (200 y 400)
```

### Para qué sirve cada carpeta

| Carpeta | Responsabilidad |
|---|---|
| `controller/` | La "puerta" HTTP. Valida la entrada con `@Valid` y delega en el servicio. No tiene lógica. |
| `service/` | La lógica de negocio del servicio (el cálculo determinista). |
| `dto/` | Objetos que representan el JSON que entra y sale (records de Java). |
| `resources/` | Configuración (`application.yml`). |
| `test/` | Pruebas automatizadas. |

Flujo de una petición:

```
JSON ──► DispatchController ──(@Valid)──► DispatchService ──► DispatchResponse ──► JSON
```

---

## Cómo se conecta con support-ticket-service

```
support-ticket-service                         technician-dispatch-service
┌───────────────────────────────┐   HTTP POST   ┌──────────────────────────┐
│ TechnicianDispatchRestAdapter │ ────────────► │ DispatchController       │
│ (RestClient)                  │ ◄──────────── │ DispatchService          │
└───────────────────────────────┘   JSON 200    └──────────────────────────┘
```

- `support-ticket-service` conoce la dirección por la variable `DISPATCH_URL`.
  - En Docker: `http://technician-dispatch-service:8081` (nombre del contenedor dentro de la red de Docker).
  - En local: `http://localhost:8081` (valor por defecto).
- Si este servicio está caído o no responde, `support-ticket-service` **no falla**: elige un técnico con sus reglas internas.

---

## Ejecutar

**Solo este servicio (local):**

```bash
cd technician-dispatch-service
mvn spring-boot:run
```

**Con todo el sistema (desde la raíz del repositorio):**

```bash
docker compose up --build
```

**Probarlo con curl:**

```bash
curl -X POST http://localhost:8081/technician-dispatch \
  -H "Content-Type: application/json" \
  -d '{"ticketId":125,"categoria":"RED","prioridad":"ALTA"}'
```

## Pruebas

```bash
mvn test
```

| Clase | Qué verifica |
|---|---|
| `DispatchServiceTest` | Misma combinación → mismo técnico; el técnico pertenece a la especialidad; clasificación de disponibilidad por horas |
| `DispatchControllerTest` | El endpoint responde 200 con los campos esperados y 400 si falta `ticketId` |

## Tecnologías

Java 21 · Spring Boot 3.4 (Web, Validation, Actuator) · JUnit 5 · MockMvc · Docker.

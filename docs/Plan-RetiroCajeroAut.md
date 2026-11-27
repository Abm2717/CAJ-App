# Retiro de efectivo en cajero automático (CAJ): esquema relacional y estructura de paquetes

Alcance: caso de uso "Retirar efectivo". Backend en Java 25, Spring Boot y Maven. Base de datos PostgreSQL en Supabase y pruebas con H2. No incluye código; los nombres de clases y tablas son una propuesta.

## 1. Decisiones generales

- **Un esquema de PostgreSQL por módulo:** `cuentas`, `retiros` y `cajero`. H2 también soporta `CREATE SCHEMA`.
- **Llaves foráneas solo dentro de un módulo.** Entre módulos, las referencias son lógicas: un UUID que la API del módulo dueño valida. Así cada módulo es dueño de sus datos y separarlo después no obliga a romper el esquema.
- **Solo tipos que funcionan igual en PostgreSQL y H2:**
  - `UUID` generado por la aplicación, sin `gen_random_uuid()` ni `RANDOM_UUID()`.
  - `NUMERIC(14,2)` para dinero.
  - `VARCHAR` con `CHECK ... IN (...)` en lugar de `ENUM`.
  - `TIMESTAMP WITH TIME ZONE`, `GENERATED ALWAYS AS IDENTITY` y `SEQUENCE`.
  - `VARCHAR(4000)` para JSON serializado, sin `JSONB`.
  - Sin índices parciales.
- **Migraciones con Flyway** en dos ubicaciones: `db/migration/comun` y `db/migration/{vendor}`. Lo exclusivo de PostgreSQL (`GRANT`/`REVOKE`, trigger de inmutabilidad, RLS) va en la carpeta `postgresql`.
- **H2 en pruebas** con `MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;LOCK_TIMEOUT=3000`.
- **Paquete raíz `com.caj_app`.** Spring Modulith toma como módulos los subpaquetes directos del paquete de la clase principal. Por eso el primer paso de la implementación es mover `CajAppApplication` de `com.caj_app.cuenta` a `com.caj_app`.
- **Supabase, accesible solo desde el backend:**
  - Ningún esquema del módulo se expone en la Data API.
  - RLS activado sin políticas.
  - Se revocan los privilegios de `anon` y `authenticated`.
  - El backend usa un rol propio (`caj_app`) y Flyway usa otro rol, el dueño de las tablas.
  - Conexión directa o en modo sesión del pooler, para no tener problemas con sentencias preparadas.

## 2. Esquema relacional

### Módulo `cuentas`

| Tabla | Columnas | Llaves y restricciones |
|---|---|---|
| `cliente` | id UUID, nombre_completo VARCHAR(150), creado_en | **PK** id |
| `cuenta` | id UUID, cliente_id UUID, numero VARCHAR(20), saldo NUMERIC(14,2), moneda CHAR(3), estado VARCHAR(15), version BIGINT, actualizado_en | **PK** id · **FK** cliente_id → cliente · UNIQUE numero · CHECK saldo >= 0 · CHECK estado IN (ACTIVA, BLOQUEADA) |
| `movimiento_cuenta` | id UUID, cuenta_id UUID, operacion_id UUID, tipo VARCHAR(30), monto NUMERIC(14,2), saldo_resultante NUMERIC(14,2), fecha_contable DATE, creado_en | **PK** id · **FK** cuenta_id → cuenta · **UNIQUE (operacion_id, tipo)** · CHECK monto > 0 · CHECK tipo IN (CARGO_RETIRO, CARGO_COMISION, ABONO_REVERSO_RETIRO, ABONO_REVERSO_COMISION) · INDEX (cuenta_id, fecha_contable) |

- `saldo` es un acumulado del libro de movimientos y se actualiza en la misma transacción que el movimiento.
- **Retirado hoy** = suma de CARGO_RETIRO − suma de ABONO_REVERSO_RETIRO del día. El día lo da `fecha_contable`, calculada con un `Clock` en zona America/Hermosillo.
- La comisión va en movimientos aparte (CARGO_COMISION), así que no cuenta para el límite diario.
- **Un abono de reverso hereda la `fecha_contable` del cargo original,** no la del día en que se aplica. Si el barrido revierte después de medianoche, el límite diario se libera en el día correcto.

### Módulo `retiros`

| Tabla | Columnas | Llaves y restricciones |
|---|---|---|
| `clave_idempotencia` | clave VARCHAR(64), cuenta_id UUID, hash_solicitud CHAR(64), estado VARCHAR(15), codigo_http INTEGER, respuesta VARCHAR(4000), creada_en, expira_en | **PK** clave · CHECK estado IN (EN_PROCESO, COMPLETADA, RECHAZADA) |
| `retiro` | id UUID, clave_idempotencia VARCHAR(64), cuenta_id UUID *(lógica)*, cajero_id UUID *(lógica)*, monto, comision_base, tasa_iva NUMERIC(5,4), comision_total, monto_entregado, estado, folio_conciliacion VARCHAR(20), motivo VARCHAR(60), version BIGINT, creado_en, actualizado_en | **PK** id · UNIQUE clave_idempotencia *(sin FK: las claves vencidas se borran y el retiro se queda)* · UNIQUE folio_conciliacion · CHECK monto > 0 · CHECK estado IN (CARGADO, COMPLETADO, REVERTIDO, PENDIENTE_CONCILIACION) · INDEX (estado, creado_en) |
| `intento_dispensacion` | id UUID, retiro_id UUID, resultado VARCHAR(15), monto_solicitado, monto_entregado, iniciado_en, terminado_en | **PK** id · **FK** retiro_id → retiro · CHECK resultado IN (EXITO, FALLA, PARCIAL, SIN_RESPUESTA) |
| `recibo` | retiro_id UUID, folio VARCHAR(20), cuenta_enmascarada VARCHAR(8), nombre_titular VARCHAR(150), monto_retirado, comision_cobrada, emitido_en | **PK y FK** retiro_id → retiro · UNIQUE folio |
| `folio_conciliacion_seq` | secuencia | — |

- `retiro` guarda una copia de la comisión con la que se cobró (base, IVA y total). Si la configuración cambia después, la operación sigue siendo auditable.
- `recibo` guarda una copia del nombre y la cuenta ya enmascarada. El número completo nunca sale del módulo `cuentas`.

### Módulo `cajero` (efectivo y auditoría)

| Tabla | Columnas | Llaves y restricciones |
|---|---|---|
| `cajero` | id UUID, codigo VARCHAR(20), ubicacion VARCHAR(150), estado | **PK** id · UNIQUE codigo |
| `inventario_billete` | cajero_id UUID, denominacion INTEGER, disponibles INTEGER, reservados INTEGER, version BIGINT | **PK (cajero_id, denominacion)** · **FK** → cajero · CHECK denominacion IN (200, 500) · CHECK disponibles >= 0 AND reservados >= 0 |
| `reserva_efectivo` | operacion_id UUID *(lógica = retiro.id)*, cajero_id UUID, billetes_200 INT, billetes_500 INT, estado, creada_en, actualizada_en | **PK** operacion_id · **FK** cajero_id → cajero · CHECK estado IN (RESERVADA, ENTREGADA, LIBERADA, RETENIDA) |
| `bitacora` | id BIGINT GENERATED ALWAYS AS IDENTITY, ocurrido_en TIMESTAMP WITH TIME ZONE, tipo_evento VARCHAR(30), resultado VARCHAR(25), operacion_id UUID, clave_idempotencia VARCHAR(64), cuenta_id UUID, cajero_id UUID, monto NUMERIC(14,2), comision NUMERIC(14,2), motivo VARCHAR(60), detalle VARCHAR(4000), correlacion_id VARCHAR(64) | **PK** id · CHECK tipo_evento IN (INTENTO_RECHAZADO, CARGO_APLICADO, RETIRO_COMPLETADO, RETIRO_REVERTIDO, PENDIENTE_CONCILIACION) · CHECK resultado IN (EXITOSO, RECHAZADO, REVERTIDO, PENDIENTE_CONCILIACION, EN_CURSO) · INDEX (operacion_id), (cuenta_id, ocurrido_en) · **sin FK** |

- **Cómo se mueven los billetes:**
  - Reservar: `disponibles` baja y `reservados` sube.
  - Entregar: `reservados` baja.
  - Liberar: los billetes regresan a `disponibles`.
- **Supuesto sobre entrega parcial o sin respuesta:** no se sabe qué salió físicamente, así que la reserva queda **RETENIDA** y no vuelve a `disponibles`. Se aclara en el arqueo, que está fuera de alcance. Con esto nunca se ofrece un billete que quizá ya no está.
- **Regla de desglose:** como el monto es múltiplo de 200, los billetes de 500 solo pueden usarse en pares. Por ejemplo, con solo billetes de 500 únicamente se pueden formar múltiplos de $1,000.
- **La bitácora no tiene FK** porque registra intentos rechazados que no tienen fila en `retiro`, y para no acoplarse a otros esquemas.

## 3. Garantías transaccionales

### 3.1 Atomicidad del retiro

El dispensador es un efecto físico, así que no puede estar dentro de una transacción de base de datos. El retiro se divide en transacciones cortas y, si algo falla después del cargo, se compensa con un reverso explícito.

1. **Transacción A, el cargo (todo o nada):**
   1. Inserta la clave de idempotencia con estado EN_PROCESO.
   2. Bloquea la cuenta.
   3. Calcula lo retirado hoy y aplica las reglas.
   4. Bloquea el inventario del cajero, calcula el desglose y reserva los billetes.
   5. Inserta el retiro con estado CARGADO, inserta los dos cargos (monto y comisión) y actualiza el saldo.
   6. Escribe el evento CARGO_APLICADO en la bitácora.

   Si cualquier paso falla, se revierte todo y no hay cargo.
2. **Dispensador:** se llama fuera de toda transacción y sin bloqueos. Un decorador corta a los 15 s y convierte la falta de respuesta en SIN_RESPUESTA.
3. **Transacción B, confirmación (éxito):**
   - El retiro pasa a COMPLETADO, con la condición `estado = CARGADO` y la misma `version`.
   - La reserva pasa a ENTREGADA.
   - Se guardan el intento y el recibo, y se escribe la bitácora.
   - La clave pasa a COMPLETADA con la respuesta guardada.
4. **Transacción B', reverso (falla, entrega parcial o sin respuesta):**
   - Bloquea la cuenta y aplica dos abonos de reverso.
   - El retiro pasa a REVERTIDO.
   - La reserva queda LIBERADA o RETENIDA.
   - Se escriben la bitácora y la clave.
5. **Transacción C, si el reverso falla:**
   - El retiro pasa a PENDIENTE_CONCILIACION, con un folio tomado de la secuencia.
   - Se registra en la bitácora.
   - La clave guarda una respuesta con ese folio.
6. **Barrido programado:** busca retiros que siguen en CARGADO más allá de 15 s más un margen y les aplica el reverso. Cubre una caída del proceso a mitad de la dispensación. Usa la misma condición de estado y versión, así nunca revierte dos veces junto con el flujo normal.
7. **Rechazos:** la transacción A se revierte. El orquestador abre una transacción aparte que guarda la clave como RECHAZADA, con su respuesta, y registra INTENTO_RECHAZADO en la bitácora.

Se descartó mantener una sola transacción abierta durante la dispensación por dos razones. Retendría el bloqueo y la conexión 15 s. Además, si el efectivo sale pero el commit falla, el cliente se lleva el dinero sin cargo, que es el peor caso posible.

El ADR-001 habla de "`@Transactional` y bloqueo de fila". Este diseño lo respeta: cada paso es una `@Transactional` con bloqueo de fila. El reverso compensatorio cumple su regla central, "si el cajero no entrega, el saldo no se descuenta", y también el requerimiento 6.

### 3.2 Bloqueo de fila sobre la cuenta

- **Bloqueo pesimista:** la cuenta se lee con `PESSIMISTIC_WRITE`, que genera `SELECT ... FOR UPDATE` tanto en PostgreSQL como en H2.
- **Tiempo de espera de 3 s:** PostgreSQL no tiene `FOR UPDATE WAIT n`, así que el hint de JPA no basta y se configura en cada motor.
  - En PostgreSQL: `SET LOCAL lock_timeout = '3s'` al inicio de las transacciones de cargo y reverso. Como respaldo, `ALTER ROLE caj_app SET lock_timeout = '3s'`.
  - En H2: `LOCK_TIMEOUT=3000` en la URL.
  - Si se agota, se rechaza con "intenta de nuevo".
- **Orden fijo de bloqueo: cuenta, luego inventario.** El reverso usa el mismo orden, así no hay interbloqueos.
- **Llamadas solo dentro de una transacción:** los métodos de `OperacionesCuenta` usan `Propagation.MANDATORY`. Fallan si alguien los llama fuera de una transacción, donde el bloqueo se soltaría de inmediato.
- **Última línea de defensa:** `CHECK saldo >= 0`.
- **Ejemplo:** una cuenta con saldo de $6,000 recibe dos retiros simultáneos de $5,000.
  - El primero bloquea, cobra $5,034.80 y confirma.
  - El segundo espera y luego lee $965.20, así que la regla de saldo lo rechaza y queda en la bitácora.

### 3.3 Idempotencia (tres capas)

1. **La PK de `clave_idempotencia`.** El frontend genera un UUID por confirmación y lo envía en el encabezado `Idempotency-Key`. Si llega un duplicado al mismo tiempo, su inserción choca con la del primero; se lee la fila existente y:
   - si está EN_PROCESO, se responde **409**;
   - si está COMPLETADA o RECHAZADA, se devuelve **la misma respuesta guardada**;
   - si `hash_solicitud` es distinto, se responde **422**, porque es la misma clave con otros datos.
2. **UNIQUE en `retiro.clave_idempotencia`.**
3. **UNIQUE `(operacion_id, tipo)` en `movimiento_cuenta`.** Ni siquiera un error de programación puede cobrar o revertir dos veces.

Un proceso de limpieza borra las claves vencidas según `expira_en`. Esa tabla sí admite DELETE; la bitácora no. Por eso ninguna tabla tiene FK hacia `clave_idempotencia`.

### 3.4 Bitácora sin UPDATE ni DELETE

- **Desde la aplicación:**
  - La entidad es `@Immutable` y sus columnas son `updatable=false`.
  - Su repositorio extiende `Repository` de Spring Data, no `CrudRepository`, y solo tiene inserción y consultas.
  - La interfaz `Bitacora` solo expone `registrar`.
  - Una prueba de arquitectura impide cualquier otro acceso a la tabla.
- **En PostgreSQL:**
  - `caj_app` solo tiene `INSERT` y `SELECT` sobre `cajero.bitacora`; se le revocan `UPDATE`, `DELETE` y `TRUNCATE`.
  - Un trigger de fila `BEFORE UPDATE OR DELETE` y otro de sentencia `BEFORE TRUNCATE` lanzan una excepción, aunque alguien use el rol dueño.
- **En H2** no se replica el trigger, porque en H2 los triggers son clases Java. La protección en pruebas es la de la aplicación.
- **Qué eventos se registran:** cada intento deja su rastro en la bitácora, sea exitoso, rechazado, revertido o pendiente. Los eventos ligados a un cambio de estado se escriben en la misma transacción que ese cambio.

## 4. Árbol de paquetes

```
com.caj_app
├── CajAppApplication                   (se mueve aquí desde com.caj_app.cuenta)
├── config/seguridad/                   ← Spring Security (ADR-001, R-05): protege /api/retiros/**;
│                                         la sesión del cajero ya viene autenticada (PIN fuera de alcance)
├── compartido/                         ← núcleo mínimo: solo tipos de valor, sin beans ni BD;
│                                         módulo abierto en Modulith (@ApplicationModule(type = OPEN))
│   ├── Dinero                          (BigDecimal escala 2, HALF_EVEN)
│   └── IdOperacion
│
├── cuentas/                            ── MÓDULO 1
│   ├── api/                            ← lo único visible para otros módulos
│   │   ├── ConsultaCuentas             (resumen sin bloqueo: nombre, saldo, últimos 4, retirado hoy)
│   │   ├── OperacionesCuenta           (bloquearParaOperar, cargar, abonarReverso — MANDATORY)
│   │   ├── ResumenCuenta, CuentaBloqueada, Concepto
│   │   └── CuentaNoEncontrada, CuentaNoOperable
│   ├── dominio/
│   │   ├── Cuenta, Cliente, MovimientoCuenta, TipoMovimiento
│   │   └── CuentaRepositorio, MovimientoRepositorio        (puertos)
│   ├── aplicacion/ServicioCuentas      (implementa ambas interfaces)
│   └── infraestructura/persistencia/   (entidades JPA, Spring Data, adaptadores)
│
├── retiros/                            ── MÓDULO 2 (sin API Java: su interfaz pública es REST)
│   ├── dominio/
│   │   ├── Retiro, EstadoRetiro                    (máquina de estados)
│   │   ├── regla/ ReglaRetiro, ContextoRetiro,
│   │   │          MultiploDe200, MaximoPorOperacion, SaldoConComision,
│   │   │          LimiteDiario, EfectivoFormable
│   │   ├── CalculadoraComision, VistaPrevia
│   │   ├── Recibo, EnmascaradorCuenta
│   │   ├── ClaveIdempotencia, HashSolicitud
│   │   └── puerto/ RetiroRepositorio, ClaveIdempotenciaRepositorio,
│   │              Dispensador, ResultadoDispensacion, GeneradorFolio
│   ├── aplicacion/
│   │   ├── ConsultarMontos, PrevisualizarRetiro
│   │   ├── EjecutarRetiro              (orquestador, sin @Transactional)
│   │   ├── tx/ AplicarCargo, ConfirmarRetiro, RevertirRetiro,
│   │   │       MarcarPendienteConciliacion, RegistrarRechazo
│   │   └── BarridoRetirosHuerfanos     (@Scheduled)
│   └── infraestructura/
│       ├── web/ RetiroController, DTOs, ManejadorErrores (ProblemDetail)
│       ├── dispensador/ DispensadorSimulado, ModoSimulacion,
│       │                DispensadorConTiempoLimite (decorador, 15 s configurable)
│       ├── persistencia/
│       └── config/ PropiedadesRetiro   (@ConfigurationProperties: base 30.00, IVA 0.16,
│                                        límite diario, máximo por operación, timeout)
│
└── cajero/                             ── MÓDULO 3
    ├── api/
    │   ├── EfectivoCajero              (montosFormables, reservar, confirmarEntrega, liberar, retener)
    │   ├── Bitacora                    (registrar)
    │   └── Desglose, EventoBitacora, TipoEvento, Resultado
    ├── efectivo/
    │   ├── dominio/ InventarioBillete, ReservaEfectivo, CalculadoraDesglose
    │   ├── aplicacion/ServicioEfectivo
    │   └── infraestructura/
    └── auditoria/
        ├── dominio/RegistroBitacora
        ├── aplicacion/ServicioBitacora
        └── infraestructura/BitacoraSoloInsercion
```

- **Dependencias permitidas:** `retiros` puede usar `cuentas.api` y `cajero.api`, y todos pueden usar `compartido`. No hay ciclos.
- **`compartido` no es un cuarto módulo de negocio.** Solo contiene tipos de valor inmutables. Las tres fronteras de negocio siguen siendo `cuentas`, `retiros` y `cajero`.
- **Verificación automática:** se recomienda Spring Modulith, con una prueba de `ApplicationModules.verify()` que falla si un módulo viola sus fronteras.
  - Por defecto, Modulith solo considera público el paquete raíz de cada módulo.
  - Por eso cada `api/` se marca con `@NamedInterface`.
- **Cómo encaja TDD:**
  - **Primero:** las reglas, la comisión, el desglose y la máquina de estados se prueban con JUnit puro.
  - **Con H2:** el bloqueo, las restricciones UNIQUE y una prueba de concurrencia con 100 hilos sobre una misma cuenta (HU-14, CAJ-18). El saldo nunca queda negativo y la suma de los movimientos cuadra con el saldo final.
  - **Por módulo:** pruebas con `@ApplicationModuleTest`.
- **Dependencias que se agregarán al `pom.xml` al implementar** (todavía no se tocan):
  - `spring-boot-starter-flyway` y `flyway-database-postgresql`;
  - `spring-modulith-starter-core` y `spring-modulith-starter-test`;
  - `spring-boot-starter-validation`;
  - `spring-boot-starter-security`.
- **Diferencia con el ADR-001:** el ADR fija **Java 21**, pero el proyecto usa **Java 25**. Hay que actualizar el ADR o registrar un ADR-002 que documente el cambio.

## 5. Justificación según SOLID

| Decisión | Principio | Por qué |
|---|---|---|
| Cada regla es una clase `ReglaRetiro` sobre un `ContextoRetiro` con solo datos; el listado prueba los 30 montos posibles ($200 a $6,000) con las mismas reglas que la ejecución | OCP, SRP | Agregar una regla no toca los casos de uso, y el listado nunca ofrece un monto que luego se rechace (salvo una carrera, que el servidor vuelve a validar) |
| `ConsultaCuentas` separada de `OperacionesCuenta` | ISP | La vista previa solo lee; solo el cargo puede bloquear y escribir |
| `retiros` depende del puerto `Dispensador`, no del simulador | DIP | El simulador, un driver real o un doble de prueba son intercambiables |
| Tiempo límite como decorador | SRP, OCP, LSP | Cualquier implementación, incluido el modo "sin respuesta", cumple el mismo contrato: siempre hay un resultado en 15 s o menos |
| Cada paso transaccional en su propio bean | SRP | Un límite de transacción por clase; además evita la autoinvocación que anula `@Transactional` |
| Dominio sin Spring ni JPA; los adaptadores van en `infraestructura` | DIP | Las pruebas se escriben primero y corren en milisegundos |
| Comisión y desglose como funciones puras con la configuración inyectada | SRP | La lógica de dinero queda aislada y probada al centavo |
| `Clock` inyectado | DIP | Permite controlar "hoy" del límite diario en las pruebas |
| La bitácora solo expone inserción | ISP | La aplicación ni siquiera tiene un método para modificarla; la base de datos lo refuerza |
| Un esquema por módulo y sin FK entre módulos | SRP aplicado a los datos | Cada módulo es dueño de su información y se puede extraer sin migrar llaves |

## 6. Qué módulo se separaría primero

**La auditoría, dentro del módulo 3.**

- **Solo recibe:** no llama a nadie y nadie depende de su respuesta para continuar.
- **Es lo que más crece:** su volumen aumenta con cada intento, incluidos los rechazos.
- **El corte ya está hecho:** no tiene FK y su única puerta de entrada es `Bitacora.registrar`, así que basta con cambiar el adaptador.
  - En vez de insertar directo, escribe en una tabla *outbox* dentro de la misma transacción.
  - Un proceso reenvía esos eventos al servicio de auditoría, que los consume sin duplicar por id de evento.
  - Se conserva la atomicidad sin una transacción distribuida.

**El efectivo del cajero iría después**, porque su lugar natural es junto al equipo físico. **Las cuentas irían al final:** el bloqueo y el cargo comparten transacción con el retiro, y separarlas exigiría una saga con retención de saldo.

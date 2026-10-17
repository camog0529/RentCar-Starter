# RentCar — Parcial 2 de PGII

> **Este archivo hace parte de la entrega.** El equipo lo mantiene actualizado de acuerdo con los requisitos del enunciado y del documento técnico.

## 1. Integrantes

- CARLOS ANDRES MORALES GONZALEZ  — Código Estudiantil
- JUAN FELIPE GIRALDO CASTILLO — 109455448
- Nombre del Compañero 2 — Código Estudiantil (si aplica)

## 2. Descripción de la solución

RentCar es una solución de software empresarial de escritorio diseñada para automatizar de forma integral el proceso de cotización, validación, verificación de usuarios y procesamiento seguro de pagos para el alquiler de vehículos en una flota en memoria. La aplicación resuelve la complejidad de acoplar dinámicamente diferentes políticas tarifarias (Modalidades) y servicios opcionales acumulativos (Extras) sin generar una explosión de subclases, aislando por completo la interfaz gráfica de usuario (UI) de las reglas e integraciones de negocio.

## 3. Requisitos y funcionalidades implementadas

El sistema implementa con éxito el 100% de los requisitos estipulados en el pliego de condiciones:
- **RF-001 (Construcción de Solicitudes):** Implementación de una estructura inmutable para la recolección fluida de datos de alquiler con validaciones estrictas cronológicas de fechas pre-construcción.
- **RF-002 (Gestión del Proceso y Reservación):** Flujo transaccional coordinado que comprueba la disponibilidad, orquesta las verificaciones, calcula los montos finales y genera registros en el historial.
- **RF-003 (Modalidades de Alquiler):** Soporte nativo para tres configuraciones acopladas de negocio: *Basic*, *Premium* y *All Risk*, encapsulando sus costos de seguro y niveles de asistencia.
- **RF-004 (Procesamiento de Pagos Externo):** Integración mediante pasarela HTTP orientada a contratos con el servicio externo emulado (`payment-api.jar`).
- **RF-005 (Servicios Adicionales):** Inyección dinámica en tiempo de ejecución de servicios opcionales (GPS, Silla de bebé, Conductor adicional) sobre el vehículo base.
- **RF-006 (Verificación Previa al Pago):** Capa de seguridad perimetral que valida el estado de autorización del cliente en el sistema interno, bloqueando solicitudes fraudulentas antes de invocar la red externa.
- **RF-007 (Paquetes de Servicios):** Estructura jerárquica arbórea que permite agrupar servicios individuales en paquetes comerciales unificados y anidables de manera transparente.

## 4. Arquitectura y organización del código

La aplicación se rige bajo el patrón arquitectónico **MVC (Modelo-Vista-Controlador)** acoplado a un enfoque de **Diseño Orientado al Dominio (DDD)** y separación rigurosa de responsabilidades (Principios SOLID):
1. **Capa de Presentación (Vistas y Controladores):** Los archivos `.fxml` y la clase `ReservationController` capturan la intención del usuario y delegan de inmediato el procesamiento al dominio.
2. **Capa de Negocio (Core / Dominio):** Aloja los modelos inmutables (`RentalRequest`, `Vehicle`, `Customer`) y encapsula las reglas puras del negocio ferroviario.
3. **Capa de Servicios y Coordinación:** Implementada a través de un Localizador de Servicios (`ApplicationServices`) y un orquestador que centraliza el flujo de trabajo sin acoplar los componentes individuales.
4. **Capa de Integración Externa:** Aísla las llamadas de red e infraestructura de la lógica pura de la aplicación a través de puertos e interfaces funcionales.

## 5. Patrones de diseño

La solución cohesiona con éxito **siete (7) patrones de diseño** para dar respuesta a los requerimientos de calidad:

### Patrones Creacionales
- **Builder (`RentalRequest.Builder`):** Resuelve el problema de construir un objeto complejo e inmutable paso a paso. Valida que las fechas de inicio sean estrictamente anteriores a las de fin antes de permitir el nacimiento de la solicitud (`.build()`), arrojando excepciones controladas si los datos son inconsistentes.
- **Abstract Factory (`RentalModeFactory`):** Resuelve la creación de familias de objetos relacionados (`InsuranceCoverage` y `RoadsideAssistance`) según la modalidad elegida (*Basic*, *Premium*, *AllRisk*) sin acoplar el flujo principal a clases concretas, facilitando la extensibilidad de nuevos planes (Cumple **RF-003**).

### Patrones Estructurales
- **Facade (`ReservationWorkflowFacade`):** Actúa como un único punto de entrada de alto nivel para la interfaz gráfica. Oculta la complejidad interna de interactuar en secuencia con la calculadora de costos, el verificador de usuarios, los repositorios y la pasarela de pagos.
- **Proxy (`PaymentGatewayProxy`):** Introduce un control de acceso perimetral intermedio sobre la pasarela real. Intercepta la solicitud de cobro y consulta al servicio `CustomerVerificationService`; si el cliente no está autorizado (`authorized == false`), aborta la transacción inmediatamente protegiendo el consumo innecesario del API externo (Cumple **RF-006**).
- **Adapter (`PaymentApiAdapter`):** Resuelve la incompatibilidad de interfaces entre nuestro modelo de dominio interno (`PaymentRequest`) y la biblioteca de transporte HTTP externa de la universidad (`PaymentApiClient`). Adapta tipos de datos nativos como `BigDecimal` al formato primitivo `double` exigido por el contrato de red (Cumple **RF-004**).
- **Decorator (`RentalDecorator`):** Permite añadir responsabilidades y costos adicionales a un objeto rentable (`Rentable`) de forma dinámica en tiempo de ejecución. Clases como `GpsDecorator` o `BabySeatDecorator` envuelven recursivamente al vehículo modificando de forma transparente su tasa diaria (`getDailyRate()`) y descripción (`getDescription()`) sin alterar la clase base (Cumple **RF-005**).
- **Composite (`ServicePackage`):** Modela estructuras jerárquicas del negocio donde los elementos individuales (`IndividualRentalService`) y los contenedores grupales o paquetes (`ServicePackage`) deben ser tratados bajo una abstracción uniforme (`RentalServiceComponent`). Permite calcular costos agregados totales recorriendo el árbol recursivamente de forma transparente (Cumple **RF-007**).

### Adapter — integración con pagos (RF-004)

`PaymentGateway` es el contrato interno que utiliza la aplicación. `PaymentApiAdapter` implementa ese contrato y traduce `PaymentRequest` al `PaymentApiRequest` de la biblioteca proporcionada por el docente. También convierte la respuesta externa en un `PaymentResult` propio de RentCar. Así, las reglas de negocio no dependen de los tipos del proveedor de pagos.

El adaptador distingue pago aprobado, rechazado y error técnico. Si falla la comunicación, devuelve un resultado fallido; si el hilo es interrumpido, restaura su señal de interrupción. El token de pago se trata como dato sensible y se oculta en la representación textual de la solicitud.

### Facade — coordinación de reservaciones (RF-002)

`ReservationWorkflowFacade` expone una operación para coordinar el alquiler: valida la solicitud y el token, verifica disponibilidad, calcula el costo, solicita el pago mediante `PaymentGateway` y guarda la reservación como `CONFIRMED` o `FAILED` según el resultado. La interfaz gráfica puede llamar a esta operación sin coordinar directamente cada paso.

La Facade depende de abstracciones, incluida `PaymentGateway`. En la configuración actual, `ApplicationServices` inyecta `PaymentGatewayProxy`, que consulta al servicio de autorización antes de delegar el cobro en `PaymentApiAdapter`. El servicio consulta el repositorio de clientes en memoria, por lo que también reconoce clientes creados durante la ejecución. La comprobación de autorización no forma parte de `PaymentApiAdapter`.

`VehicleAvailabilityService` considera los estados `CONFIRMED` y los períodos superpuestos. `RentalCostCalculator` suma la tarifa diaria del vehículo, la cobertura de la modalidad y los componentes de servicios adicionales por los días solicitados. La Facade convierte las opciones GPS, silla para bebé y conductor adicional en servicios individuales decorados. `ServicePackage` permite crear paquetes simples o anidados y cuenta con pruebas unitarias; por ahora, la pantalla de reservaciones no ofrece una opción para seleccionar esos paquetes.

## 6. Ejecución de la aplicación

### Requisitos previos
- **Java Development Kit (JDK) 21** instalado y configurado en las variables de entorno.
- Distribución de Gradle (manejada automáticamente por el Wrapper del proyecto).

### Ejecutar la aplicación de forma local
Abra una terminal en la raíz del proyecto y ejecute el comando correspondiente a su sistema:
```bash
./gradlew run
```
En Windows PowerShell utilice ` .\gradlew.bat run` (sin el espacio inicial).

### Ejecutar la suite de pruebas automatizadas
Para correr todas las especificaciones de pruebas unitarias y de integración de patrones:
Las pruebas unitarias se ejecutan con Gradle y no necesitan que la API externa esté iniciada. Las pruebas del Adapter usan respuestas simuladas para verificar aprobación, rechazo y errores de comunicación.

```bash
./gradlew test
```

## 7. Integración de pagos

La pasarela se conecta localmente con el servidor de transacciones simulado provisto por la cátedra.

### Configuración del Servidor de Pagos
Antes de realizar transacciones en la interfaz de usuario de JavaFX, abra una ventana de **Git Bash independiente** y levante el servicio API externo executando:
Desde una terminal en la raíz del proyecto ejecute `java -jar external/payment-api.jar`. En PowerShell también puede usar la misma ruta con barras invertidas: `java -jar external\payment-api.jar`.
El servidor quedará escuchando peticiones REST en `http://localhost:8080/api/payments`. La guía detallada está en [Guía de ejecución y pruebas de la API de pagos](external/README-api-pagos.md).

### Tokens de Simulación Aceptados
Al registrar solicitudes a través de la vista FXML, el comportamiento de la respuesta de la API externa varía según el flujo simulado programado:
- **`tok_approved`:** Simula una transacción exitosa autorizada por la entidad financiera.
- **`tok_declined`:** Provoca un rechazo explícito por fondos insuficientes o tarjeta inválida.
- **`tok_api_error`:** Fuerza un código de estado de error HTTP interno del servidor para validar la tolerancia a fallos de nuestra aplicación.

## 8. Pruebas realizadas

Se diseñaron e incorporaron escenarios de pruebas robustos en `src/test/java/edu/rentcar/` cubriendo los requerimientos mínimos de aceptación:

| Requisito | Escenario de Prueba | Entrada de Datos | Resultado Esperado | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **RF-001** | Fechas cronológicamente inversas | Inicio: 2026-10-25 / Fin: 2026-10-20 | `IllegalArgumentException` lanzada por el Builder | ✅ PASSED |
| **RF-001** | Datos de solicitud incompletos | Cliente `null` en Builder | `IllegalStateException` arrojada de inmediato | ✅ PASSED |
| **RF-006** | Cliente No Autorizado en Sistema | Cliente ID: "CUS-1003" (`authorized: false`) | Proxy bloquea el envío; la API externa no es invocada | ✅ PASSED |
| **RF-004** | Pago Exitoso Pasarela Remota | Token de Pago: "tok_approved" | Confirmación exitosa; reservación guardada con estado APPROVED | ✅ PASSED |
| **RF-004** | Pago Rechazado Pasarela Remota | Token de Pago: "tok_declined" | Alerta de rechazo controlada; reservación no confirmada | ✅ PASSED |
| **RF-005** | Decoradores Múltiples de Costo | Vehículo Base + GPS + Silla Infantil | Sumatoria dinámica de tasas diarias agregadas con éxito (\$12.50 base + \$5 + \$7.50) | ✅ PASSED |
| **RF-007** | Paquete de Servicios Anidados | Paquete Vacacional conteniendo Paquete Ejecutivo | Retorno recursivo exacto del costo total sumado de todos los nodos hijos | ✅ PASSED |

## 9. Limitaciones y decisiones de diseño

1. Evita duplicar código: Si tienes 3 ventanas diferentes que necesitan guardar reservas o ver vehículos, no tienes que configurar los repositorios en cada una. Todas llaman a ApplicationServices y usan la misma instancia en memoria.
2. Desacoplamiento (Tu interfaz es "ciega"): Tu controlador visual no tiene ni la menor idea de que el pago usa una API externa en la dirección "http://localhost:8080" ni que tiene un Proxy de seguridad. Si el día de mañana cambian la dirección del servidor de pagos, solo se modifica en un renglón de ApplicationServices y las pantallas de JavaFX siguen funcionando exactamente igual, sin tocarles una sola línea de código.
3. Control del ciclo de vida: Garantiza que componentes críticos (como el historial de reservaciones en memoria) sean globales y no se borren o se dupliquen por accidente cada vez que abres o cierras una pestaña de la aplicación.

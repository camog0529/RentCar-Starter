# RentCar — Parcial 2 de PGII

> **Este archivo hace parte de la entrega.** El equipo debe completarlo y mantenerlo actualizado de acuerdo con los requisitos del enunciado y del documento técnico.

## 1. Integrantes

- Nombre completo — código estudiantil
- Nombre completo — código estudiantil
- Nombre completo — código estudiantil (si aplica)

## 2. Descripción de la solución

<!-- Describan brevemente qué resuelve la aplicación y qué funcionalidades implementó el equipo. -->

## 3. Requisitos y funcionalidades implementadas

<!-- Resuman las funcionalidades implementadas y relacionen cada una con los requisitos del enunciado. -->

## 4. Arquitectura y organización del código

<!-- Expliquen la arquitectura utilizada, la responsabilidad de las capas y cómo se relacionan. -->

## 5. Patrones de diseño

<!-- Para cada patrón requerido, documenten el problema que resuelve, las clases participantes y dónde se utiliza. -->

### Adapter — integración con pagos (RF-004)

`PaymentGateway` es el contrato interno que utiliza la aplicación. `PaymentApiAdapter` implementa ese contrato y traduce `PaymentRequest` al `PaymentApiRequest` de la biblioteca proporcionada por el docente. También convierte la respuesta externa en un `PaymentResult` propio de RentCar. Así, las reglas de negocio no dependen de los tipos del proveedor de pagos.

El adaptador distingue pago aprobado, rechazado y error técnico. Si falla la comunicación, devuelve un resultado fallido; si el hilo es interrumpido, restaura su señal de interrupción. El token de pago se trata como dato sensible y se oculta en la representación textual de la solicitud.

### Facade — coordinación de reservaciones (RF-002)

`ReservationWorkflowFacade` expone una operación para coordinar el alquiler: valida la solicitud y el token, verifica disponibilidad, calcula el costo, solicita el pago mediante `PaymentGateway` y guarda la reservación como `CONFIRMED` o `FAILED` según el resultado. La interfaz gráfica puede llamar a esta operación sin coordinar directamente cada paso.

La Facade depende de abstracciones, incluida `PaymentGateway`. Cuando se integre el Proxy de RF-006, se debe inyectar el Proxy como `PaymentGateway` para que la verificación de autorización ocurra antes de que la solicitud llegue al adaptador externo. La comprobación de autorización no forma parte de `PaymentApiAdapter`.

`VehicleAvailabilityService` considera los estados `CONFIRMED` y los períodos superpuestos. `RentalCostCalculator` calcula la tarifa diaria del vehículo y la cobertura de la modalidad por los días solicitados. Los servicios adicionales y paquetes se incorporarán cuando se integren Decorator y Composite (RF-005 y RF-007).

## 6. Ejecución de la aplicación

### Requisitos

- JDK 21.
- Conexión a Internet la primera vez que Gradle necesite descargar la distribución o dependencias.

### Ejecutar

Desde la raíz del proyecto:

- **Windows PowerShell:** ` .\gradlew.bat run `
- **Git Bash / macOS / Linux:** ` ./gradlew run `

En IntelliJ IDEA también se puede ejecutar la tarea `application > run` desde la ventana Gradle.

### Ejecutar las pruebas

- **Windows PowerShell:** ` .\gradlew.bat test `
- **Git Bash / macOS / Linux:** ` ./gradlew test `

## 7. Integración de pagos

<!-- Documenten cómo configurar y probar la integración que implementó el equipo, según el enunciado. No incluyan claves, contraseñas ni tokens reales. -->

## 8. Pruebas realizadas

<!-- Describan las pruebas unitarias y funcionales, cómo ejecutarlas y qué resultados obtuvieron. -->

## 9. Limitaciones y decisiones de diseño

<!-- Registren las limitaciones conocidas y justifiquen las decisiones relevantes. -->

## 10. Evidencias

<!-- Agreguen enlaces o referencias a las evidencias solicitadas en el enunciado. -->
# RentCar-Starter

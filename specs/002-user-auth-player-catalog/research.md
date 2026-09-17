# Research — Autenticacion de usuarios y catalogo de jugadores

## Decision: Spring Boot 4.x con Java 21 y Gradle

- **Chosen**: Java 21 con la version 4.x mas reciente de Spring Boot disponible al implementar, gestionada con Gradle.
- **Rationale**: Coincide con el contexto tecnico solicitado, ofrece soporte maduro para HTTP, validacion, persistencia y testing, y permite mantener una estructura de servicio web convencional.
- **Alternatives considered**: Spring Boot 3.x, Maven y otros frameworks web. Se descartan porque no coinciden con la pauta tecnica de la entrega.

## Decision: PostgreSQL como almacenamiento persistente

- **Chosen**: PostgreSQL para usuarios, estado de autenticación JWT, jugadores y estado de actualización.
- **Rationale**: Es el almacenamiento solicitado y permite mantener el estado de sesión necesario para invalidar inmediatamente un JWT anterior.
- **Alternatives considered**: H2 o archivos locales. Se descartan porque no representan el entorno objetivo y pueden ocultar diferencias de SQL o concurrencia.

## Decision: JWT fuera del dominio

- **Chosen**: Emitir un JWT por usuario dentro del módulo de autenticación; el registro lo entrega y cada login reemplaza el anterior. Las consultas protegidas reciben `Authorization: Bearer <jwt>`; el catálogo es público.
- **Rationale**: JWT permite transportar una identidad firmada sin mezclar la credencial con las reglas de Usuario o Jugador. Como la especificación exige invalidación inmediata, el módulo de autenticación mantiene el `jti` vigente o una versión de token y rechaza tokens anteriores aunque su firma no haya expirado.
- **Alternatives considered**: ApiKey opaca, múltiples tokens simultáneos y autenticación externa. Se descartan porque la decisión del proyecto requiere JWT, una sola sesión activa y no agrega proveedores externos.

## Decision: Validacion distribuida por responsabilidad

- **Chosen**: DTOs validan tipos, presencia, trimming y formato; services validan duplicados, credenciales y existencia; modelos mantienen solo invariantes de dominio. La autenticación firma y valida JWT fuera de los modelos de negocio.
- **Rationale**: Cumple los principios II y III de la constitución y permite tests unitarios sin infraestructura. La separación evita que una credencial técnica contamine el modelo de negocio.
- **Alternatives considered**: Validar todo en controllers o services. Se descartan porque duplican reglas y debilitan la independencia del modelo.

## Decision: Actualizacion semanal con ultimo snapshot valido

- **Chosen**: Un importador semanal actualiza jugadores a traves de una interfaz de fuente externa. Si falla, conserva el último snapshot válido y registra la fecha de última actualización sin presentarlo como actualizado.
- **Rationale**: Cumple la especificación y separa la integración externa del catálogo consultable; permite usar fixtures deterministas en tests.
- **Alternatives considered**: Actualización bajo demanda, datos estáticos permanentes o fallar toda consulta ante indisponibilidad externa. Se descartan por no cumplir la frecuencia definida o por degradar innecesariamente la disponibilidad.

## Decision: Pruebas con Testcontainers y MockMvc

- **Chosen**: Unit tests del dominio sin base de datos, integración de repositorios con PostgreSQL real en Testcontainers y E2E HTTP con MockMvc.
- **Rationale**: Es obligatorio por la constitución y cubre invariantes, persistencia y contratos HTTP de manera separada.
- **Alternatives considered**: H2, mocks de repositorio para integración y pruebas manuales. Se descartan porque no satisfacen la definición de terminado.

## Decision: Datos de catálogo y fuentes externas

- **Chosen**: Mantener un puerto interno para importar datos de las fuentes autorizadas del proyecto; la implementación concreta y credenciales quedan en configuración local y no se requieren para las pruebas deterministas.
- **Rationale**: Aísla cambios de fuente, permite simular errores y evita hacer depender el arranque o los tests de servicios externos.
- **Alternatives considered**: Llamar a fuentes externas desde el controller o cargar datos directamente desde el frontend. Se descartan por acoplamiento y por violar la arquitectura en capas.

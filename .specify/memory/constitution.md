<!--
Sync Impact Report
- Version change: no version previa -> 0.1.0
- Modified principles: scaffold placeholders -> I-VII, principios de Pronta Entrega
- Added sections: Restricciones de implementación; Flujo de trabajo y calidad
- Removed sections: ninguna
- Follow-up TODOs: confirmar la fecha histórica de ratificación
-->

# ProntaEntrega Constitution

## Core Principles

### I. Arquitectura en capas
El backend MUST separar controller, servicio, modelo y persistencia. El controller
solo MUST comunicarse con el servicio; el servicio MUST orquestar modelo y persistencia;
el modelo MUST permanecer independiente de las demás capas; y persistencia MUST conocer
al modelo sin realizar llamadas sobre él. El frontend MUST organizarse en carpetas
funcionales `api`, `styles`, `pages` y `components`. Esta separación limita el
acoplamiento y hace verificables las responsabilidades de cada capa.

### II. Lógica de negocio solo en modelo
La lógica de negocio MUST vivir en los objetos de modelo. Ninguna otra capa MUST
duplicar, trasladar o decidir reglas propias del dominio. Esta regla mantiene las
invariantes en un único lugar y permite probarlas sin infraestructura externa.

### III. Cada validación en su nivel
El DTO de request MUST validar forma, tipos, trimming y sanitización de inputs. El
service MUST validar existencia de recursos y posibilidad de la acción solicitada. Los
objetos de modelo MUST validar invariantes del dominio y lanzar excepciones propias del
dominio. El frontend MUST ejecutar validaciones adicionales de inputs antes de enviar
la request. La distribución evita que una capa asuma responsabilidades ajenas.

### IV. Tests no negociables
El backend MUST incluir tests unitarios de dominio sin base de datos ni service, tests
de integración y repositorios contra bases de datos reales levantadas con Testcontainers,
y tests end-to-end con MockMvc en su propio paquete. Cada conjunto MUST cubrir casos
base, casos borde y excepciones. Ningún test existente MUST modificarse ni eliminarse
sin permiso y confirmación explícita del responsable del proyecto.

### V. Definición de terminado y entregable
Un requisito MUST considerarse terminado solo cuando cuenta con tests unitarios, de
integración y end-to-end para casos felices y borde, todos pasando; la aplicación de
frontend y backend compila y levanta con la configuración local; y la colección de
Postman incluye los endpoints nuevos. Estos criterios hacen que la entrega sea
reproducible y verificable.

### VI. Documentación
Cada clase y cada método MUST documentar su responsabilidad y funcionamiento. La
documentación MUST mantenerse junto con el código que describe para que las decisiones
de diseño y los contratos permanezcan comprensibles.

### VII. Idioma
Los documentos y mensajes de error MUST estar en español. Los nombres de dominio MUST
estar en español y no incluir acentos ni `ñ` en identificadores. Los términos técnicos
y normativos MUST permanecer en inglés cuando corresponda. En el frontend, todo texto
visible MUST estar en español; nombres de archivos, carpetas, funciones y código MUST
estar en inglés.

## Restricciones de implementación

Las decisiones de implementación MUST respetar la arquitectura en capas, la ubicación
de la lógica de negocio y la distribución de validaciones definidas en los principios.
Las dependencias, persistencia, configuración local y colección de Postman MUST permitir
compilar, levantar y verificar el sistema sin modificar las reglas del dominio.

## Flujo de trabajo y calidad

Cada cambio MUST demostrar cumplimiento de los principios mediante tests adecuados y
revisión de la documentación correspondiente. La revisión MUST comprobar cobertura de
casos felices, bordes y excepciones, separación de paquetes y ausencia de modificaciones
no autorizadas en tests existentes. Un cambio que no cumpla la definición de terminado
NO DEBE considerarse entregable.

## Governance

Esta constitución prevalece sobre prácticas locales incompatibles. Toda enmienda MUST
documentar el cambio en un Sync Impact Report, actualizar la versión y conservar la
información vigente. La versión MUST seguir Semantic Versioning: MAJOR para eliminar o
redefinir de forma incompatible un principio; MINOR para agregar o ampliar materialmente
un principio o sección; y PATCH para aclaraciones, correcciones de redacción o cambios
no semánticos.

Las revisiones de código y de requisitos MUST comprobar el cumplimiento de esta
constitución. Cada cambio MUST incluir la evidencia de validación aplicable y cualquier
excepción MUST quedar justificada y aprobada antes de incorporarse. La fecha de
ratificación original aún no está registrada y requiere confirmación del responsable.

**Version**: 0.1.0 | **Ratified**: 2026-09-05 | **Last Amended**: 2026-09-05

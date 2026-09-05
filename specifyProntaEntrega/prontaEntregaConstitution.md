# ProntaEntrega Constitution

## Core Principles

### I. Arquitectura en capas 
Para el backend: La arquitectura del proyecto utilizara una division en capas tradicional, con controller, servicio, modelo, y persistencia.
Controller solo se habla con servicio,
Servicio orquesta entrre modelo y persistencia.
Modelo no conoce ni interactua con ninguna otra capa.
Persistencia conoce a modelo pero no realiza llamadas sobre el.
Para el Frontend: La arquitectura debe estar dividida en carpetas segun su funcionalidad, con api, styles, pages, y components respectivamente.

### 2. Logica de negocio solo en modelo
La logica de modelo vive en los objetos de modelo.

### 3. Cada validación en su nivel
- Forma y tipos del request en el DTO del request, el trimming y la sanitización de inputs tambien se realizan aca.
- Que lo pedido exista y la accion sea posible (los ids resuelvan, las entidaddes se encuentren) en el service.
- Invariantes del dominio en los objetos de modelo, lanzando excepciones propias del dominio.
- Validacion extra de inputs, orms del lado del Frontend antes de que la request llegue al backend.

### 4. Tests (NO-NEGOCIABLE)
Solo para el backend:
- Test unitarios de dominio, sin base de datos ni service.
- Test de integracion y repositorios contra las bases de datos real levantado con Testcontainers.
- End-to-end con MockMvc. Solo en su propio paquete, nunca dentro de un test de service.
- Siempre casos base, casos borde, y validacion de excepciones.
- NO se modifica ni se borra un test ya existente, en ninguna fase de flujo, sin antes pedirme permiso y recibir una confirmación explicita  

### 5. Definición de terminado y entregable
Un requisito/funcionalidad esta terminado cuando:
- Tiene tests unitarios, de integracion y de end-to-end, con casos felices y borde, y pasando.
- La aplicacion, tanto front y back compila y levanta con la configuración local.
- La colección de Postman del proyecto quedo actualizada con los endpoint nuevos.

### 6. Documentación
Cada metodo y cada clase debe contar con una documentación dejando en claro su responsabilidad y funcionamiento.

### 7. Idioma
Documentos y mensajes de error en español. Nombres de dominio en español, sin acentos ni ñ en identificadores. Terminos tecnicos y normativos en inglés.
Del lado del front, cualquier texto que el usuario vea debe estar en español. El resto, los archivos, carpetas, funciones, codigo, en inglés.
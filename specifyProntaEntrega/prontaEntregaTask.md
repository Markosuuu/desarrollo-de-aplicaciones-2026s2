1. Ejecuta `.specify/scripts/python/check-prerequisites.py -Json` desde la raíz del repositorio y analiza FEATURE DIR y AVAILABLE. 
    DOCS list. Todas las rutas deben ser absolutas.

2. Carga y analiza los documentos de diseño disponibles:
Del directorio `specs/002-user-auth-player-catalog/`
- Siempre lee plan.md para el stack tecnológico y librerías
- SI EXISTE: Lee data-model.md para entidades
- SI EXISTE: Lee contracts/ para endpoints de API
- SI EXISTE: Lee research.md para decisiones técnicas
- SI EXISTE: Lee quickstart.md para escenarios de prueba
Nota: No todos los proyectos tienen todos los documentos. 
Por ejemplo:
- Las herramientas de CLI podrían no tener contracts/
- Las bibliotecas simples podrían no necesitar data-model.md
- Genera tareas basadas en lo que esté disponible

3. Generá task siguiendo la plantilla:
   - Usa `.specify/templates/tasks-template.md” como base
   * **Tareas de configuración**: Inicialización del proyecto, dependencias, linting
   * **Tareas de prueba [P]**: Una por contrato, una por escenario de integración
   * **Tareas principales**: Una por entidad, servicio, comando CLI, endpoint
   * **Tareas de integración**: Conexiones a DB, middleware, logging
   * **Tareas de pulido [P]**: Pruebas unitarias, rendimiento, documentación

4. Task generation rules:
   Each contract file * contract test task marked [P]
   Each entity in data-model * model creation task marked [P]
   Each endpoint s implementation task (not parallel if shared files)
- Each user story * integration test marked [P]
- Different files - can be parallel [P]
  Same file - seguential (no [P])

4. Reglas de generación de tareas:
- Cada archivo de contrato * tarea de prueba de contrato marcada [P]
- Cada entidad en data-model * tarea de creación de modelo marcada [P]
- Cada endpoint * tarea de implementación (no paralela si comparte archivos)
- Cada historia de usuario * tarea de prueba de integración marcada [P]
Diferencia de archivos - puede ser paralela [P]
- Mismo archivo - secuencial (no [P])

5. Ordenar las tareas por dependencias:
- Configuración antes que todo
- Pruebas antes de la implementación (TDD)
- Modelos antes de servicios
- Servicios antes de endpoints
- Núcleo antes de integración
- Todo antes de pulido

6. Tnclude parallel execution examples:
- Group [P] tasks that can run together
- Show actual Task agent commands

6. Incluir ejemplos de ejecución paralela:
- Agrupar tareas [P] que pueden ejecutarse juntas
- Mostrar comandos reales del agente de tareas

7. Crear FEATURE DIR/tasks.md con:
- Nombre correcto de la característica del plan de implementación
- Tareas numeradas (T001, T002, etc.)
- Rutas de archivo claras para cada tarea
- Notas de dependencia
- Guía de ejecución paralela

Context for task generation: $ARGUMENTS
The tasks.md should be immediately executable - each task must be specific enough that an LLM can complete it
additional context.

Contexto para la generación de tareas: $ARGUMENTS
El tasks.md debe ser ejecutable de inmediato: cada tarea debe ser lo suficientemente específica para que un LLM pueda completarla sin contexto adicional
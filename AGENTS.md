# AGENTS.md

## Java baseline

- Java 21.
- Antes de ejecutar Maven, usar `./mvn-java.ps1`.
  El script local selecciona el JDK declarado para ese proceso y no modifica
  las variables globales del sistema.

## Pre-flight obligatorio

La primera lectura de cada tarea debe ser este archivo desde la rama por defecto; después, cualquier regla referenciada. No se permite escribir antes de completar ese pre-flight.

## Trazabilidad Jira obligatoria

Todo cambio requiere una tarea Jira: identificarla o crearla, pasarla por `Por hacer -> En curso -> Finalizado`, usar su clave en rama, commits, PR y CHANGELOG, y dejar evidencia de progreso y cierre.

## Flujo obligatorio

1. Partir de `main` actualizado y crear una rama dedicada.
2. Determinar y aplicar SemVer sobre `revision`.
3. Actualizar `CHANGELOG.md`, `README.md`, `pom.xml` y documentación de versión cuando corresponda.
4. Ejecutar como mínimo `./mvn-java.ps1 -B test` y los checks aplicables.
5. Abrir/actualizar PR a `main`, corregir checks y fusionar solo en verde sobre el SHA actual.
6. Eliminar la rama origen únicamente tras el merge y verificar la limpieza.

No se puede escribir, commitear ni pushear directamente a `main`.

## Automatización obligatoria del ciclo de entrega

Tras cada cambio, el agente debe completar sin pedir confirmaciones intermedias: commit, push, creación o actualización de la Pull Request, comprobación de los checks del SHA actual, merge automático cuando todos los checks requeridos estén en verde y eliminación de la rama origen. Solo debe detener el merge si GitHub informa de una protección bloqueante, un check fallido o un conflicto que requiera corrección.

Antes de **cada commit**, el agente debe volver a leer completamente el `AGENTS.md` vigente de la rama de trabajo y verificar que el commit incluye el `CHANGELOG.md`, el versionado y las pruebas requeridas. No puede crear el commit si falta alguno de esos elementos aplicables.

## Versionado Maven CI-friendly

```xml
<version>${revision}${sha1}${changelist}</version>
```

- `revision`: SemVer funcional; `sha1` lo genera CI; `changelist` es vacío o `-SNAPSHOT`.
- `patch`: `X.Y.Z` -> `X.Y.(Z+1)`; `minor`: `X.Y.Z` -> `X.(Y+1).0`; `major`: `X.Y.Z` -> `(X+1).0.0`.

## Tests y seguridad operativa

- Baseline: JDK 21. Usar siempre `./mvn-java.ps1`, que lee este `AGENTS.md` y selecciona el JDK solo para Maven.
- Los cambios funcionales o de configuración requieren pruebas actualizadas.
- Toda decisión de merge se toma sobre el SHA actual de la PR; si una instrucción contradice estas reglas, detener solo esa operación incompatible.

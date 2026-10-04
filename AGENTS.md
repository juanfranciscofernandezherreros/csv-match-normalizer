# AGENTS.md

## Java baseline

- Java 21.
- Antes de ejecutar Maven desde el workspace, usar
  `../Invoke-MavenForProject.ps1 -ProjectPath . -MavenArguments <args>`.
  El lanzador selecciona el JDK declarado para ese proceso y no modifica las
  variables globales del sistema.

[CmdletBinding()]
param([Parameter(ValueFromRemainingArguments = $true)][string[]]$MavenArguments = @('test'))

$agents = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'AGENTS.md') -Raw
$match = [regex]::Match($agents, '(?im)\b(?:baseline\s+(?:soportado\s+es\s+)?|java|jdk)\s*(?:version\s*)?(\d+)\b')
if (-not $match.Success) { throw 'Java version is not declared in AGENTS.md.' }
$version = $match.Groups[1].Value
$jdkHome = [Environment]::GetEnvironmentVariable("JAVA$version`_HOME", 'User')
if (-not $jdkHome) { $jdkHome = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue | Where-Object Name -match "^jdk-$version(?:\.|-)" | Sort-Object Name -Descending | Select-Object -First 1 -ExpandProperty FullName }
if (-not $jdkHome -or -not (Test-Path (Join-Path $jdkHome 'bin\java.exe'))) { throw "JDK $version required by AGENTS.md was not found." }
$oldHome, $oldPath = $env:JAVA_HOME, $env:PATH
try { $env:JAVA_HOME = $jdkHome; $env:PATH = "$(Join-Path $jdkHome 'bin');$oldPath"; & mvn @MavenArguments; exit $LASTEXITCODE } finally { $env:JAVA_HOME, $env:PATH = $oldHome, $oldPath }

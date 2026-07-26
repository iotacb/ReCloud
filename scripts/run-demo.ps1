param(
    [Parameter(Position = 0)]
    [ValidateSet("first_window", "balls_demo", "camera_demo", "custom_shader", "drawing_stuff", "flappy_bird_clone", "particle_system_demo", "physics_demo", "ui_system")]
    [string]$Demo = "first_window",

    [switch]$List
)

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$ToolsDir = Join-Path $RepoRoot ".tools"
$JdkRoot = Join-Path $ToolsDir "jdk-25"
$MavenRoot = Join-Path $ToolsDir "apache-maven-3.9.9"
$MavenCmd = Join-Path $MavenRoot "bin\mvn.cmd"

$DemoMainClasses = @{
    first_window      = "first_window.Main"
    balls_demo        = "balls_demo.Main"
    camera_demo       = "camera_demo.Main"
    custom_shader     = "custom_shader.Main"
    drawing_stuff     = "drawing_stuff.Main"
    flappy_bird_clone = "flappy_bird_clone.Main"
    particle_system_demo = "particle_system_demo.Main"
    physics_demo      = "physics_demo.Main"
    ui_system         = "ui_system.Main"
}

if ($List) {
    $DemoMainClasses.Keys | Sort-Object
    exit 0
}

if (-not (Test-Path -LiteralPath (Join-Path $JdkRoot "bin\java.exe")) -or
    -not (Test-Path -LiteralPath $MavenCmd)) {
    & (Join-Path $PSScriptRoot "setup-tools.ps1") -Quiet
}

$env:JAVA_HOME = $JdkRoot
$env:MAVEN_HOME = $MavenRoot
$env:Path = "$JdkRoot\bin;$MavenRoot\bin;$env:Path"

$MainClass = $DemoMainClasses[$Demo]

Push-Location $RepoRoot
try {
    & $MavenCmd compile exec:java "-Dexec.mainClass=$MainClass"
}
finally {
    Pop-Location
}

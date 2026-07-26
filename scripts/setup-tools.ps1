param(
    [switch]$Quiet
)

$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$ToolsDir = Join-Path $RepoRoot ".tools"
$DownloadsDir = Join-Path $ToolsDir "downloads"
$JdkRoot = Join-Path $ToolsDir "jdk-25"
$MavenVersion = "3.9.9"
$MavenRoot = Join-Path $ToolsDir "apache-maven-$MavenVersion"

function Write-Step($Message) {
    if (-not $Quiet) {
        Write-Host $Message
    }
}

function Download-File($Url, $Destination) {
    if (Test-Path -LiteralPath $Destination) {
        return
    }

    Write-Step "Downloading $Url"
    Invoke-WebRequest -UseBasicParsing -Uri $Url -OutFile $Destination
}

function Expand-SingleRootZip($ZipPath, $FinalPath) {
    if (Test-Path -LiteralPath $FinalPath) {
        return
    }

    $TempExtract = Join-Path $ToolsDir ("extract-" + [Guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Force -Path $TempExtract | Out-Null

    try {
        Expand-Archive -LiteralPath $ZipPath -DestinationPath $TempExtract -Force
        $RootFolders = @(Get-ChildItem -LiteralPath $TempExtract -Directory)

        if ($RootFolders.Count -ne 1) {
            throw "Expected one root folder in $ZipPath, found $($RootFolders.Count)."
        }

        Move-Item -LiteralPath $RootFolders[0].FullName -Destination $FinalPath
    }
    finally {
        if (Test-Path -LiteralPath $TempExtract) {
            Remove-Item -LiteralPath $TempExtract -Recurse -Force
        }
    }
}

New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null
New-Item -ItemType Directory -Force -Path $DownloadsDir | Out-Null

$JdkZip = Join-Path $DownloadsDir "temurin-jdk-25-windows-x64.zip"
$MavenZip = Join-Path $DownloadsDir "apache-maven-$MavenVersion-bin.zip"

Download-File "https://api.adoptium.net/v3/binary/latest/25/ga/windows/x64/jdk/hotspot/normal/eclipse" $JdkZip
Download-File "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip" $MavenZip

Write-Step "Installing local JDK into $JdkRoot"
Expand-SingleRootZip $JdkZip $JdkRoot

Write-Step "Installing local Maven into $MavenRoot"
Expand-SingleRootZip $MavenZip $MavenRoot

$env:JAVA_HOME = $JdkRoot
$env:MAVEN_HOME = $MavenRoot
$env:Path = "$JdkRoot\bin;$MavenRoot\bin;$env:Path"

Write-Step "Tooling is ready."
if (-not $Quiet) {
    & "$JdkRoot\bin\java.exe" -version
    & "$MavenRoot\bin\mvn.cmd" -version
}

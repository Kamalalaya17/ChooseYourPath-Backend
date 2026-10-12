$ErrorActionPreference = 'Stop'

Write-Host "ChooseYourPath backend setup" -ForegroundColor Cyan
Write-Host "Make sure MySQL is running and the 'chooseyourpath' database exists."

if (-not $env:DB_USERNAME) { $env:DB_USERNAME = 'root' }
$dbPasswordSecure = Read-Host "Enter the MySQL password for $env:DB_USERNAME" -AsSecureString
$dbPasswordPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPasswordSecure)
try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($dbPasswordPtr)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($dbPasswordPtr)
}

if (-not $env:DB_HOST) { $env:DB_HOST = 'localhost' }
if (-not $env:DB_PORT) { $env:DB_PORT = '3306' }
if (-not $env:DB_NAME) { $env:DB_NAME = 'chooseyourpath' }
if (-not $env:DB_USERNAME) { $env:DB_USERNAME = 'root' }
if (-not $env:JWT_SECRET) {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
}
if (-not $env:APP_CORS_ALLOWED_ORIGINS) { $env:APP_CORS_ALLOWED_ORIGINS = 'http://localhost:5173' }

Write-Host "Starting Spring Boot on http://localhost:8080 ..." -ForegroundColor Green
mvn spring-boot:run

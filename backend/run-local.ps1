$envFile = Join-Path $PSScriptRoot '.env'

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#')) {
            return
        }

        $parts = $line -split '=', 2
        if ($parts.Count -eq 2) {
            Set-Item -Path "Env:$($parts[0].Trim())" -Value $parts[1]
        }
    }
}

Set-Location $PSScriptRoot
& .\mvnw.cmd spring-boot:run
# Coffee Analyze Development Environment Starter
Write-Host "Starting Coffee Analyze Development Environment..." -ForegroundColor Green
Write-Host ""

# Check and install dependencies
if (-not (Test-Path "node_modules")) {
    Write-Host "Installing root dependencies..." -ForegroundColor Yellow
    npm install
}

if (-not (Test-Path "frontend\node_modules")) {
    Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
    Set-Location frontend
    npm install
    Set-Location ..
}

Write-Host ""
Write-Host "Starting backend and frontend simultaneously..." -ForegroundColor Cyan
Write-Host "Backend will run on: http://localhost:8080" -ForegroundColor White
Write-Host "Frontend will run on: http://localhost:5173" -ForegroundColor White
Write-Host ""

# Start backend in background
$backendJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    .\gradlew bootRun
}

# Wait a moment for backend to start
Start-Sleep -Seconds 3

# Start frontend in background
$frontendJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD\frontend
    npm run dev
}

Write-Host ""
Write-Host "Both servers are starting..." -ForegroundColor Green
Write-Host "Press Ctrl+C to stop all servers" -ForegroundColor Red
Write-Host ""

try {
    # Keep the script running and show status
    while ($true) {
        $backendStatus = $backendJob.State
        $frontendStatus = $frontendJob.State
        
        Write-Host "Backend Status: $backendStatus | Frontend Status: $frontendStatus" -ForegroundColor Gray
        Start-Sleep -Seconds 10
    }
}
catch {
    Write-Host ""
    Write-Host "Stopping all servers..." -ForegroundColor Yellow
    
    # Stop the jobs
    Stop-Job $backendJob -ErrorAction SilentlyContinue
    Stop-Job $frontendJob -ErrorAction SilentlyContinue
    Remove-Job $backendJob -ErrorAction SilentlyContinue
    Remove-Job $frontendJob -ErrorAction SilentlyContinue
    
    # Kill any remaining processes
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
    Get-Process -Name "node" -ErrorAction SilentlyContinue | Stop-Process -Force
    
    Write-Host "Servers stopped." -ForegroundColor Green
} 
@echo off
echo Starting Coffee Analyze Development Environment...
echo.

echo Installing dependencies if needed...
if not exist "node_modules" (
    echo Installing root dependencies...
    npm install
)

if not exist "frontend\node_modules" (
    echo Installing frontend dependencies...
    cd frontend
    npm install
    cd ..
)

echo.
echo Starting backend and frontend simultaneously...
echo Backend will run on: http://localhost:8080
echo Frontend will run on: http://localhost:5173
echo.

start "Backend Server" cmd /k "gradlew bootRun"
timeout /t 3 /nobreak > nul
start "Frontend Server" cmd /k "cd frontend && npm run dev"

echo.
echo Both servers are starting...
echo Press any key to close all servers...
pause > nul

echo Stopping all servers...
taskkill /f /im java.exe > nul 2>&1
taskkill /f /im node.exe > nul 2>&1
echo Servers stopped. 
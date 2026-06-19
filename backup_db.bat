@echo off
REM backup_db.bat — Backup simple de la base de datos MySQL del proyecto Paujil
REM
REM Uso:
REM   backup_db.bat
REM
REM Ejecutalo desde la raiz del proyecto Paujil (donde esta el archivo .env)
REM Requiere: mysqldump.exe accesible en el PATH (viene con MySQL Server o MySQL Workbench)

setlocal enabledelayedexpansion

set ENV_FILE=.env
set BACKUP_DIR=backups
set DB_NAME=finca_pajuil
set DB_USER=root
set DB_HOST=localhost
set DB_PORT=3306

REM --- Verificar que exista el .env ---
if not exist "%ENV_FILE%" (
    echo Error: no se encontro %ENV_FILE%. Ejecuta este script desde la raiz del proyecto.
    exit /b 1
)

REM --- Leer DB_PASS desde el .env ---
for /f "tokens=2 delims==" %%a in ('findstr /b "DB_PASS=" "%ENV_FILE%"') do set DB_PASS=%%a

REM --- Crear carpeta de backups si no existe ---
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM --- Generar nombre de archivo con fecha y hora ---
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set TIMESTAMP=%dt:~0,4%-%dt:~4,2%-%dt:~6,2%_%dt:~8,2%-%dt:~10,2%-%dt:~12,2%
set OUTFILE=%BACKUP_DIR%\%DB_NAME%_%TIMESTAMP%.sql

echo Generando backup de la base de datos '%DB_NAME%'...

mysqldump --host=%DB_HOST% --port=%DB_PORT% --user=%DB_USER% --password=%DB_PASS% --single-transaction --routines --triggers %DB_NAME% > "%OUTFILE%"

if %errorlevel% neq 0 (
    echo Error al generar el backup.
    exit /b 1
)

echo Backup guardado en: %OUTFILE%
endlocal

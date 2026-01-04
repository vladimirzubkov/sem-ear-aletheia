@echo off
setlocal enabledelayedexpansion

REM --- Settings ---
set "SOURCE_DIR=%CD%"
set "GITIGNORE_FILE=%SOURCE_DIR%\.gitignore"
for %%i in ("%SOURCE_DIR%") do set "ROOT_NAME=%%~ni"
set "OUTPUT_FILE=%ROOT_NAME%_contents.txt"

REM --- Delete old output file if exists ---
if exist "%OUTPUT_FILE%" del "%OUTPUT_FILE%"

REM --- Header ---
echo // ----- Root Directory: %ROOT_NAME% ----- >> "%OUTPUT_FILE%"
echo Current root directory: %SOURCE_DIR%
echo. >> "%OUTPUT_FILE%"

REM --- Load .gitignore if present ---
set "IGNORES="
if exist "%GITIGNORE_FILE%" (
    echo Loading .gitignore patterns...
    for /f "usebackq tokens=* delims=" %%g in ("%GITIGNORE_FILE%") do (
        set "line=%%g"
        REM Skip empty lines and comments
        if not "!line!"=="" (
            if not "!line:~0,1!"=="#" (
                REM Remove leading/trailing spaces
                for /f "tokens=* delims= " %%t in ("!line!") do set "line=%%t"
                REM Replace / with \ for Windows paths
                set "pattern=!line:/=\!"
                REM Append pattern to ignore list
                if not "!pattern!"=="" set "IGNORES=!IGNORES!;!pattern!"
            )
        )
    )
    echo Ignore patterns loaded: !IGNORES!
)

REM --- Process files recursively ---
set "LAST_DIR="
for /r "%SOURCE_DIR%" %%f in (*.java *.iml *.csv *.dbcsv *.policy *.kt *.kts *.xml *.md *.markdown *.js *.json *.css *.html *.yaml) do (
    set "SKIP_FILE=false"
    set "FILE_FULL=%%~f"
    set "FILE_REL=!FILE_FULL:%SOURCE_DIR%\=!"
    
    REM Check if file matches any ignored pattern
    if not "!IGNORES!"=="" (
        for %%p in (!IGNORES!) do (
            set "pat=%%~p"
            if not "!pat!"=="" (
                echo !FILE_REL! | findstr /I /C:"!pat!" >nul
                if !errorlevel! equ 0 (
                    set "SKIP_FILE=true"
                )
            )
        )
    )
    
    if "!SKIP_FILE!"=="true" (
        echo Skipped ignored: !FILE_REL!
    ) else (
        REM Get directory path
        set "DIR_FULL=%%~dpf"
        set "DIR_REL=!DIR_FULL:%SOURCE_DIR%\=!"
        set "DIR_REL=!DIR_REL:~0,-1!"
        
        REM Print directory header if changed
        if not "!DIR_REL!"=="!LAST_DIR!" (
            if not "!LAST_DIR!"=="" echo. >> "%OUTPUT_FILE%"
            echo Processing directory: !DIR_REL!
            echo // ----- Directory: %ROOT_NAME%\!DIR_REL! ----- >> "%OUTPUT_FILE%"
            set "LAST_DIR=!DIR_REL!"
        )
        
        REM Add file content
        echo Adding: %%~nxf
        echo // File: %ROOT_NAME%\!DIR_REL!\%%~nxf >> "%OUTPUT_FILE%"
        echo // Content: >> "%OUTPUT_FILE%"
        type "%%f" >> "%OUTPUT_FILE%" 2>nul || echo // [Binary or inaccessible file] >> "%OUTPUT_FILE%"
        echo. >> "%OUTPUT_FILE%"
    )
)

echo.
echo Done! Output written to %OUTPUT_FILE%
pause
@echo off
setlocal

echo.
echo -----------------------------------------------
echo [INFO] Application name           : %1
echo [INFO] Version                    : %2
echo [INFO] Organization               : %3
echo [INFO] Main Class                 : %4
echo -----------------------------------------------
echo.
echo [0/6] Cleaning and packaging application...
call mvn clean package -DskipTests

echo.
echo [1/6] Cleaning previous config...
rmdir /S /Q config 2>nul
mkdir config

echo.
echo [2/6] Preparing distribution and output folders...
rmdir /S /Q dist 2>nul
mkdir dist
rmdir /S /Q output 2>nul
mkdir output
cd dist

echo.
echo [3/6] Simulating runtime usage to generate native-image config...
java -agentlib:native-image-agent=config-output-dir=../config ^
     -Dflatlaf.uiScale=1 ^
     -Djava.awt.headless=false ^
     -cp "../target/canscan-%2.jar;../target/test-classes" ^
          fr.softsf.canscan.NativeImageConfigSimulator

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] La simulation a echoue avec le code %ERRORLEVEL%
    exit /b 1
)

echo.
echo [4/6] Building native image...
echo       Les options SUBSYSTEM:WINDOWS et ENTRY:mainCRTStartup suppriment la console
echo       (retirer ces 2 lignes pour afficher la console pendant l'exécution)
call native-image --no-fallback ^
                  --no-server ^
                  --verbose ^
                  --enable-preview ^
                  -H:ConfigurationFileDirectories=../config ^
                  -H:+UnlockExperimentalVMOptions ^
                  -H:+ReportUnsupportedElementsAtRuntime ^
                  -H:+ReportExceptionStackTraces ^
                  -H:Name=canscan ^
                  -H:Class=%4 ^
                  -H:IncludeResources=".*\\.properties|.*\\.png|.*\\.svg|.*\\.ttf|.*\\.json" ^
                  -H:ReflectionConfigurationFiles=../config\reflect-config.json ^
                  -H:ResourceConfigurationFiles=../config\resource-config.json ^
                  -H:JNIConfigurationFiles=../config\jni-config.json ^
                  -H:DynamicProxyConfigurationFiles=../config\proxy-config.json ^
                  -H:NativeLinkerOption=/SUBSYSTEM:WINDOWS ^
                  -H:NativeLinkerOption=/ENTRY:mainCRTStartup ^
                  -Dsun.java2d.d3d=false ^
                  -Dsun.java2d.noddraw=true ^
                  -Djava.awt.headless=false ^
                  -J-Dsun.java2d.d3d=false ^
                  -J-Dsun.java2d.noddraw=true ^
                  -J-Djava.awt.headless=false ^
                  -J-Xmx7G ^
                  -jar ../target/canscan-%2.jar

cd ..

echo.
echo [5/6] Native image build complete in /dist

echo.
echo [6/6] Building installer...

if not defined INNOSETUP (
  echo.
  echo Missing environment variable: INNOSETUP. Please define a user-level variable pointing to the path of ISCC.exe.
  exit /b 1
)
"%INNOSETUP%" ^
  /DAppName=%1 ^
  /DAppVersion=%2 ^
  /DOrganization=%3 ^
  .myresources/scripts/canscan.iss

echo.
echo [INFO] Inno Setup compilation with :
echo   AppName      = %1
echo   AppVersion   = %2
echo   Organization = %3

echo.
echo [INFO] Setup file(s) available in the output directory.

endlocal
exit /b 0

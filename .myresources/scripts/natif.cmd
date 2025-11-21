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
echo   -agentlib:native-image-agent  : Agent GraalVM pour tracer reflexion/ressources/JNI
echo   config-output-dir             : Repertoire de sortie des fichiers de config
echo   -Djava.awt.headless=false     : Active l'interface graphique Swing/AWT
echo   -cp                           : Classpath (JAR + classes de test)
echo   NativeImageConfigSimulator    : Classe simulant l'usage runtime de l'app
echo.
java -agentlib:native-image-agent=config-output-dir=../config ^
     -Djava.awt.headless=false ^
     -Duser.language=fr ^
     -Duser.country=FR ^
     -Duser.region=FR ^
     -cp "../target/canscan-%2.jar;../target/test-classes" ^
          fr.softsf.canscan.NativeImageConfigSimulator

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Configuration simulation failed
    exit /b 1
)

echo.
echo [4/6] Building native image...
echo   --no-fallback                 : Force la compilation native (pas de fallback JVM)
echo   --strict-image-heap           : Prepare pour les prochaines versions GraalVM
echo   -H:+UnlockExperimentalVMOptions : Deverrouille les options experimentales
echo   -H:ConfigurationFileDirectories : Charge les configs de reflexion/ressources/JNI
echo   -H:Name                       : Nom de l'executable (canscan.exe)
echo   -H:Class                      : Classe main a executer
echo   -H:NativeLinkerOption=SUBSYSTEM : Supprime la console Windows
echo   -H:NativeLinkerOption=ENTRY   : Point d'entree sans console
echo   -Djava.awt.headless=false     : Active l'interface graphique Swing/AWT
echo   -Dsun.java2d.d3d=false        : Desactive Direct3D (stabilite Windows)
echo   -J-Xmx7G                      : Memoire max pour la compilation (7 GB)
echo.
call native-image --no-fallback ^
                  --strict-image-heap ^
                  -H:+UnlockExperimentalVMOptions ^
                  -H:ConfigurationFileDirectories=../config ^
                  -H:Name=canscan ^
                  -H:Class=%4 ^
                  -H:NativeLinkerOption=/SUBSYSTEM:WINDOWS ^
                  -H:NativeLinkerOption=/ENTRY:mainCRTStartup ^
                  -Duser.language=fr ^
                  -Duser.country=FR ^
                  -Duser.region=FR ^
                  -Djava.awt.headless=false ^
                  -Dsun.java2d.d3d=false ^
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

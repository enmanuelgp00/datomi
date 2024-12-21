@echo off

call :existManifest

set "build=app\build"
set "main=app\src\main"
set "classes=%build%\bin\java\classes"

set build-tool=D:\users\enmanuel\.software\profession\dev\sdk\android\build-tools\30.0.0
set android=D:\users\enmanuel\.software\profession\dev\sdk\android\platforms\android-30
set root=%~dp0

call :getAppNameFromFolder appName
call :getPackageName package
call :settingUp

%build-tool%\aapt package -f -m -J %build%\gen -M %main%\AndroidManifest.xml -S %main%\res -I %android%\android.jar  

if %ERRORLEVEL% equ 1 (
  goto:eof
)

echo Compiling to classes ...
rem 2>&1 converts the error level in standar output

rem -Xlint:unchecked 
rem  -Xlint:deprecation
javac -Xlint:-options -Xlint:deprecation -source 8 -target 8 -d %classes% -classpath %main%\java;%build%\gen -bootclasspath %android%\android.jar %main%\java\%package%\ActivityMain.java 

if %ERRORLEVEL% equ 1 (
  goto:eof
)

call "%build-tool%\dx.bat" --dex --output %build%\bin %classes%
%build-tool%\aapt package -f -m -F %build%\bin\unaligned.apk -M %main%\AndroidManifest.xml -S %main%\res -I %android%\android.jar 

cd %build%\bin
%build-tool%\aapt add "unaligned.apk" classes.dex 1> nul
%build-tool%\zipalign -f 4 unaligned.apk aligned.apk 
call %build-tool%\apksigner sign --ks "keystore\demo.keystore" -v1-signing-enabled true -v2-signing-enabled true --ks-pass pass:password --out "%appName%.apk" "aligned.apk" 

cd %root%
echo Success
goto:exit

:settingUp
  if exist %classes% ( 
    for /d %%d in (%classes%) do (
    if not "%%d" equ "%classes%\%package%" (
      rmdir /s /q %%d
      )  
    )
  ) else (
    mkdir %classes%\%package%
  )
  if not exist %main% (
     mkdir %main%
  )
goto:eof

:getPackageName %1
	setlocal enableDelayedExpansion
	for /f "delims=" %%f in ('findstr "package" %main%\AndroidManifest.xml') do (
		set str=%%f
	)
	set sign=0
	for %%s in (%str%) do (
		if !sign! equ 1 (
			set str0=%%~s
			set sign=0
		)
		if "%%s" equ "package" (
			set sign=1
		)
	)
	set p=%str0%
	if not exist %main%\java\%p% (
		echo %main%\java\%p% does not exist
		set p=%str0:.=\%
	)
	echo %main%\java\%p%
	endlocal & set %1=%p%
goto:eof

:getAppNameFromFolder
	for /f %%i in ('cd') do (set appName=%%~ni)
goto:eof

:existManifest
	if not exist %main%\AndroidManifest.xml (
		echo There is no AndroidManifiest
		goto:exit
	)
goto:eof

:exit

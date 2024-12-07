call :existManifest
set build-tool=D:\users\enmanuel\.software\profession\dev\sdk\android\build-tools\30.0.0
set android=D:\users\enmanuel\.software\profession\dev\sdk\android\platforms\android-30
set root=%~dp0
call :getAppNameFromFolder appName
call :getPackageName package

%build-tool%\aapt package -f -m -J app\build\gen -M app/src/main/AndroidManifest.xml -S app\src\main\res -I %android%\android.jar  
javac -source 8 -target 8 -d app\build\bin\java\classes -classpath app\src\main\java;app\build\gen -bootclasspath %android%\android.jar app\src\main\java\%package%\ActivityMain.java 

call "%build-tool%\dx.bat" --dex --output app\build\bin app\build\bin\java\classes
@echo on
%build-tool%\aapt package -f -m -F app\build\bin\unaligned.apk -M app\src\main\AndroidManifest.xml -S app\src\main\res -I %android%\android.jar 

cd app\build\bin
%build-tool%\aapt add "unaligned.apk" classes.dex
%build-tool%\zipalign -f 4 unaligned.apk aligned.apk 
call %build-tool%\apksigner sign --ks "keystore\demo.keystore" -v1-signing-enabled true -v2-signing-enabled true --ks-pass pass:password --out "%appName%.apk" "aligned.apk" 

cd %root%

:exit
goto:eof

:getPackageName %1
	@echo off
	setlocal enableDelayedExpansion
	for /f "delims=" %%f in ('findstr "package" app\src\main\AndroidManifest.xml') do (
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
	if not exist app\src\main\java\%p% (
		echo %p% does not exist
		set p=%str0:.=\%
	)
	echo %p%
	endlocal & set %1=%p%
echo on
goto:eof
:getAppNameFromFolder
	for /f %%i in ('cd') do (set appName=%%~ni)
goto:eof
:existManifest
	if not exist app\src\main\AndroidManifest.xml (
		echo There is no AndroidManifiest
		goto:exit
	)
goto:eof

@echo off
call :getPackageName package
for /f "delims=" %%f in ('adb shell pidof %package%') do (
   if "%%f" equ "" (
      echo %package% is not running
   ) else (
      adb logcat --pid %%f
   )
adb logcat --pid %%f
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
	endlocal & set %1=%p%
goto:eof
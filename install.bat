@echo off
set "arr=aligned unaligned"
for %%i in (app\build\bin\*.apk) do (
   if "%%~ni" neq "aligned" (
      if "%%~ni" neq "unaligned" ( 
        set apk=%%i
      )
   )
)
adb install %apk%
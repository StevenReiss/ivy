@ rem callable script to setup environment for ivy

@echo off

rem setlocal enableextensions

if defined BROWN_IVY_IVY goto :donesetvar

set ivy_x1=%~dp$Path:0

if exist %ivy_x1%bin\ivysetupenv (
   set ivy_y1=%ivy_x1%
) else if exist %x1%..\bin\ivysetupenv (
   set ivy_y1=%ivy_x1%..\
)

call %ivy_y1%bin\winsetpathvar BROWN_IVY_ROOT %ivy_y1%..\

call %ivy_y1%bin\winsetpathvar BROWN_IVY_IVY %ivy_y1%

set BROWN_IVY_ARCH=%PROCESSOR_ARCHITECTURE%

set Path=%Path%;%BROWN_IVY_IVY%bin;%BROWN_IVY_IVY%bin


:donesetvar

if defined BROWN_JAVA_HOME goto :donesetjava


set ivy_x1=

for /D %%a in ("C:\Program Files\Java\j2re*") do set ivy_x1="%%a"

if defined ivy_x1 goto :foundjava

for /D %%a in ("C:\j2sdk*") do set ivy_x1="%%a"

if defined ivy_x1 goto :foundjava

echo Can't locate java run time.
goto :done

:foundjava
set BROWN_JAVA_HOME=%ivy_x1%
set BROWN_JAVA_ARCH=i386
set BROWN_JAVA_THREADS=native_threads
set BROWN_JAVA_OS=%OS%


:donesetjava

if defined BROWN_IVY_SETUP goto :done

call winaddclasspath %BROWN_IVY_IVY%\java
call winaddclasspath %BROWN_IVY_IVY%\javasrc
call winaddclasspath %BROWN_IVY_IVY%\lib\jikesbt.jar

set BROWN_IVY_SETUP=1


:done

set ivy_x1=
set ivy_y1=



rem endlocal


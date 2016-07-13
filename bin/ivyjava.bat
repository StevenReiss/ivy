@echo off

setlocal

set ivy_x1=%~dp$Path:0

if exist %ivy_x1%\bin\ivysetupenv (
   call %ivy_x1%\bin\ivysetupenv
)


set CLASSPATH=%BROWN_IVY_IVY%\java;%BROWN_IVY_IVY%\lib\jikesbt.jar

set args=
set croot= -Dedu.brown.cs.ivy.IVY=%BROWN_IVY_IVY%

java %args% %croot% %*


endlocal

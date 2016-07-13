
set taiga_cp1=

:retry

if "%2" == "" goto :done

if %1 == %2 set taiga_cp1=1 else (
shift /2
goto :retry
)


:done

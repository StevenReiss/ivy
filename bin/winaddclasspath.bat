rem winaddclasspath <dir>

if not exist %1 goto :done

call winsetpathvar taiga_acp %1

if not defined CLASSPATH (
   set CLASSPATH=%taiga_acp%
   goto :done
)


call wintestpath %taiga_acp% %CLASSPATH%

if not defined taiga_cp1 (
   set CLASSPATH=%CLASSPATH%;%taiga_acp%
)



:done

set taiga_acp=

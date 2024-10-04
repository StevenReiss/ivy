#! /bin/tcsh -f

set nonomatch

   set eclipse = 'jee-2022-09'

   set target = /pro/ivy/lib/eclipsejartest
   set plugins = $eclipse/plugins
   set plugins = ~/.p2/pool/plugins
   rm -rf $target
   mkdir $target

   set swt = swt.gtk.linux.x86_64
   if ($BROWN_IVY_ARCH == i386 || $BROWN_IVY_ARCH == ppc) then
      set swt = swt.carbon.macosx
   endif
   if ($BROWN_IVY_ARCH == mac64) then
      set swt = swt.cocoa.macosx.x86_64
      cp /pro/ivy/lib/eclipsejar/org.eclipse.swt.gtk.linux.x86_64.jar $target
   endif

   set pfx = 'org.eclipse'
   foreach i (jdt.core core.filebuffers core.resources core.runtime core.jobs \
		core.contenttype equinox.common equinox.preferences equinox.registry \
		equinox.app \
		debug.core debug.ui jdt.debug jdt.launching \
		text \
		core.filesystem search \
		jface ui.workbench swt $swt \
		jface.text jdt.ui \
		core.commands core.net jdt.core.manipulation \
		ltk.core.refactoring ui.ide \
		wst.jsdt.core wst.jsdt.debug.core \
#		pde.ui pde.api.tools.ui \
		osgi osgi.util osgi.services)
      set f1 = `echo $plugins/${pfx}.${i}_*.jar`
      set f2 = `echo $plugins/${pfx}.${i}_*/*.jar`
      if ("$f1" != "$plugins/${pfx}.${i}_*.jar") then
	 set f = `ls -1 -t -r $plugins/${pfx}.${i}_*.jar | tail -1`
	 cp $f $target/${pfx}.${i}.jar
      else if ("$f2" != "$plugins/${pfx}.${i}_*/*.jar") then
	 set f = `ls -1d -t -r $plugins/${pfx}.${i}_* | tail -1`
	 foreach j ($f/*.jar)
	    set x1 = ${j:t}
	    cp $j $target/${pfx}.${i}.${x1}
	 end
      else
	 echo Problem with $f1 $f2
      endif
   end

   set pfx = 'com.google'
   foreach i (guava javascript)
      set f1 = `echo $plugins/${pfx}.${i}_*.jar`
      set f2 = `echo $plugins/${pfx}.${i}_*/*.jar`
      if ("$f1" != "$plugins/${pfx}.${i}_*.jar") then
	 set f = `ls -1 -t -r $plugins/${pfx}.${i}_*.jar | tail -1`
	 cp $f $target/${pfx}.${i}.jar
      else if ("$f2" != "$plugins/${pfx}.${i}_*/*.jar") then
	 set f = `ls -1d -t -r $plugins/${pfx}.${i}_* | tail -1`
	 foreach j ($f/*.jar)
	    set x1 = ${j:t}
	    cp $j $target/${pfx}.${i}.${x1}
	 end
      else
	 echo Problem with $f1 $f2
      endif
   end

   set pfx = 'org.python.pydev'
   foreach i (parser core)
      set f1 = `echo $plugins/${pfx}.${i}_*.jar`
      set f2 = `echo $plugins/${pfx}.${i}_*/*.jar`
      if ("$f1" != "$plugins/${pfx}.${i}_*.jar") then
	 set f = `ls -1 -t -r $plugins/${pfx}.${i}_*.jar | tail -1`
	 cp $f $target/${pfx}.${i}.jar
      else if ("$f2" != "$plugins/${pfx}.${i}_*/*.jar") then
	 set f = `ls -1d -t -r $plugins/${pfx}.${i}_* | tail -1`
	 foreach j ($f/*.jar)
	    set x1 = ${j:t}
	    cp $j $target/${pfx}.${i}.${x1}
	 end
      else
	 echo Problem with $f1 $f2
      endif
   end

   echo "$eclipse" >&! $target/version

unset nonomatch

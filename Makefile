#
# IVY
#
# Copyright 2003 Steven P. Reiss, Brown University - All rights reserved.
#
#

SYSTEM= ivy
HOME = $(BROWN_IVY_IVY)
GMAKE= gnumake

VERSION = 1
SUBVER = 0
RCSVERSION = 0.0
SVER= $(VERSION).$(SUBVER)




INSTALL_TOP= $(BROWN_IVY_ROOT)
ifneq ("$(wildcard /research/people/spr)","")
   CPRO=/research/people/spr
else
   CPRO= $(PRO)
endif


#
#	Distribution information
#

DISTRIB_TOP= /pro/spr_distrib/files
DISTRIB_BIN= /pro/spr_distrib/binary/$(SYSTEM)
DISTRIB_DIR= $(DISTRIB_TOP)/$(SYSTEM)
DISTRIB= $(DISTRIB_DIR)
DMAKE= $(MAKE) DISTRIB_BIN=$(DISTRIB_BIN)

SHARED_LIB = $(BROWN_TEA_ROOT)/lib

ACTIVE= Makefile


#
#	Component definitions
#

FILES= Makefile

SETUPCOMPS= lib bin data
OTHERCOMPS= include

C++_COMPONENTS= stdlib mince native
JCOMPONENTS= file exec swing xml petal pebble mint jcode jcomp jannot limbo leash project

COMPONENTS= $(JCOMPONENTS) $(C++_COMPONENTS)


ALL_COMPONENTS= $(SETUPCOMPS) $(COMPONENTS) $(OTHERCOMPS)
DIST_COMPONENTS=

#
#	Command definitions
#

GENERIC_COMMANDS= print pribm prps clean newlib create prim rcssetup realclean cleanall
ACTIVE_COMMANDS= all links checkin copyright binshare shareclean count
DISTRIB_COMMANDS= distrib_dir
C++_COMMANDS= opt prof makedep
JAVA_COMMANDS = xjavadep


#
#	Build rules
#

COPYVALUES= BROWN_IVY_ROOT=$(BROWN_IVY_ROOT)


default: all opt jar

$(ACTIVE_COMMANDS):
	touch .DUMMY
	$(MAKE) $(COMPONENTS) 'COMMAND=$@' $(COPYVALUES)

$(GENERIC_COMMANDS):
	touch .DUMMY
	$(MAKE) $(ALL_COMPONENTS) 'COMMAND=$@' $(COPYVALUES)

$(DISTRIB_COMMANDS):
	touch .DUMMY
	$(GMAKE) $(ALL_COMPONENTS) $(DIST_COMPONENTS) 'COMMAND=$@' BROWN_TEA_ROOT=$(BROWN_TEA_ROOT) \
		'DISTRIB=$(DISTRIB)' 'DISTRIB_MACH=$(DISTRIB_MACH)' \
		'DISTRIB_TOP=$(DISTRIB_TOP)' 'DISTRIB_DIR=$(DISTRIB_DIR)'

$(C++_COMMANDS):
	touch .DUMMY
	$(MAKE) $(C++_COMPONENTS) 'COMMAND=$@' $(COPYVALUES)

$(JAVA_COMMANDS):
	touch .DUMMY
	$(MAKE) $(JCOMPONENTS) 'COMMAND=$@' $(COPYVALUES)



.PHONY: $(GENERIC_COMMANDS) $(ACTIVE_COMMANDS) $(DISTRIB_COMMANDS)

.PHONY: $(ALL_COMPONENTS) $(DIST_COMPONENTS)

$(ALL_COMPONENTS) $(DIST_COMPONENTS): .DUMMY

.DUMMY:

.PRECIOUS: $(ALL_COMPONENTS) $(DIST_COMPONENTS)

$(COMPONENTS) $(DIST_COMPONENTS):
	(cd $@/src; $(MAKE) $(COMMAND))

$(SETUPCOMPS) $(OTHERCOMPS):
	(cd $@; $(MAKE) $(COMMAND) )

cleanlib:
	(cd lib; $(MAKE) cleanlib)
	$(MAKE) newlib
	(cd lib; $(MAKE) ranlib)

fullclean:
	-rm -rf SB/*
	$(GMAKE) clean
	$(GMAKE) repoclean

fullmake:
	$(GMAKE) fullclean
	$(GMAKE) -k all

cvsco:
	$(GMAKE) setup
	$(GMAKE) create
	$(GMAKE) links

newmachine:
	$(GMAKE) setup
	$(GMAKE) create
	$(GMAKE) links
	$(GMAKE) all

setmachine:
	$(GMAKE) -k all

wc:
	@rm -rf count.out
	@$(GMAKE) count | tee count.out
	@awk -f ../forest/data/total.awk count.out
	@rm -rf count.out


distrib:
	rm -rf $(DISTRIB_DIR)
	- (cd $(DISTRIB_TOP) ; cvs co -P ivy )
	- (cd $(DISTRIB_DIR) ; find . -name CVS -exec rm -rf {} \; )



# REMFLAGS= REMOTE=12
# REMSET= FREEX=$(BROWN_TEA_ROOT)/bin/field/freex FIELD_DIR=$(BROWN_TEA_ROOT)


#
#	Install scripts
#

INSTALL_BIN= $(INSTALL_TOP)/bin/ivy	      # location for ivy binaries
INSTALL_LIB= $(INSTALL_TOP)/lib/ivy	      # location for libraries
INSTALL_DAT= $(INSTALL_LIB)/data		# location for tea data files
INSTALL_USR= $(INSTALL_TOP)/bin

BINFILES=
SCRIPTFILES=
TOPFILES=
UTILBIN=
DATAFILES=
LIBFILES=


shareinstall:
	@echo nothing to do

install:
	$(MAKE) realinstall TOPBIN=$(INSTALL_BIN) TOPLIB=$(INSTALL_LIB) TOPDAT=$(INSTALL_DAT) \
		TOPFRM=$(INSTALL_FRM) TOPBUF=$(INSTALL_BUF) TOPDIR=$(INSTALL_TOP) \
		TOPDATA=$(INSTALL_DATA)

realinstall:
	@echo checking directories
	- if [ ! -d $(TOPBIN) ]; then mkdir $(TOPBIN); else true; fi
	- if [ ! -d $(TOPLIB) ]; then mkdir $(TOPLIB); else true; fi
	- if [ ! -d $(TOPDAT) ]; then mkdir $(TOPDAT); else true; fi
	- if [ ! -d $(TOPBUF) ]; then mkdir $(TOPBUF); else true; fi
	- if [ ! -d $(TOPDATA) ]; then mkdir $(TOPDATA); else true; fi
	@echo copying binaries
	( cd bin/sol; cp $(BINFILES) $(SCRIPTFILES) $(UTILBIN) $(TOPBIN) )
	@echo copying libraries
	( cd lib/$(ARCH); cp $(LIBFILES) $(TOPLIB) )
	@echo copying data files
	( cd lib/sol/data; cp $(DATAFILES) $(TOPDAT) )
	@echo doing configuration
	@echo installation done


jar:
	rm -rf jar.files ivy.jar
	(cd java; find . -follow -name '*.class' -print | \
		grep -F -v .AppleDouble | \
		grep -F -v tea/iced > ../jar.files )
	cp javasrc/fait.xml java
	cp lib/words java
	(cd java; jar cf ../lib/ivy.jar `cat ../jar.files` fait.xml words )
	(cd java; jar cfm ../lib/ivyfull.jar ../setupmanifest.mf `cat ../jar.files` fait.xml words )
	rm -rf jar.files java/fait.xml
	-cp lib/ivy.jar /pro/sharpFix/lib

world:
	$(MAKE) realclean
	$(MAKE) links
	$(MAKE) all


setup:
	bin/ivysetup
	-unlink src
	ln -s javasrc/edu/brown/cs/ivy src
	for x in $(C++_COMPONENTS); do\
	   unlink $$x/src/Makefile; \
	   cp data/Make.pass $$x/src/Makefile; \
	   bin/ivycreatec++ $$x; \
	 done
	for x in $(JCOMPONENTS); do \
	   bin/ivycreatejava $$x; \
	 done
	-cp lib/words java



javadoc:
	javadoc -d $(PRO)/ivy/doc -protected -sourcepath $(PRO)/ivy/javasrc \
		edu.brown.cs.ivy.exec \
		edu.brown.cs.ivy.file \
		edu.brown.cs.ivy.swing \
		edu.brown.cs.ivy.xml \
		edu.brown.cs.ivy.petal \
		edu.brown.cs.ivy.pebble \
		edu.brown.cs.ivy.mint \
		edu.brown.cs.ivy.jcomp \
		edu.brown.cs.ivy.jcode \
		edu.brown.cs.ivy.limbo \
		edu.brown.cs.ivy.project


bubbles:
	make jar
	rm -rf src.files ivysrc.jar
	(cd javasrc; find . -follow -name '*.java' -print | grep -F -v 'jflow'| grep -F -v 'cinder' | grep -F -v 'project' | grep -F -v 'pebble' > ../src.files )
	(cd javasrc; jar cf ../ivysrc.jar `cat ../src.files` )
	rm -rf src.files
	mv ivysrc.jar $(PRO)/bubbles/
	(cd java; find . -follow -name '*.class' -print | grep -F -v 'jflow'| grep -F -v 'cinder' | grep -F -v 'project' | grep -F -v 'pebble'  > ../bin.files )
	(cd java; jar cf ../ivybin.jar `cat ../bin.files` )
	rm -rf bin.files
	-cp ivybin.jar $(PRO)/bubbles/suds/lib/ivy.jar
	cp ivybin.jar $(PRO)/bubbles/lib/ivy.jar
	jar cf ivylib.jar lib/*.props lib/*.jar lib/androidjar lib/eclipsejar lib/*.xml
	-cp ivylib.jar $(PRO)/bubbles/
	-mkdir eclipsejar
	-cp lib/eclipsejar/*.jar $(PRO)/bubbles/eclipsejar
	-cp lib/cocker.jar $(PRO)/bubbles/lib
	-cp lib/asm.jar $(PRO)/bubbles/lib
	-cp ivybin.jar $(CPRO)/cocker/lib/ivy.jar
	-cp lib/asm.jar $(CPRO)/cocker/lib/asm.jar
	-cp lib/eclipsejar/*.jar $(CPRO)/cocker/lib/eclipsejar
	-cp ivybin.jar $(CPRO)/iot/signmaker/lib/ivy.jar
	-cp lib/json.jar lib/mysql.jar lib/postgresql.jar $(CPRO)/iot/signmaker/lib
	rm ivybin.jar
	rm ivylib.jar


#	cp lib/eclipsejar/version /pro/bubbles/eclipsejar
#	   rm -rf javasrc/edu/brown/cs/ivy/$$x/Makefile; \
#	   ln -s ../../../../../data/Make.pass javasrc/edu/brown/cs/ivy/$$x/Makefile; \
#	   done


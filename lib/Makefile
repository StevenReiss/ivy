#
# IVY Library makefile
#
# Copyright 1994 Steven P. Reiss, Brown University - All rights reserved.
#
#

SYSTEM= ivy

ALL=

PFILES= Makefile

ACTIVE= $(PFILES) jdeps.jar

SHELL= /bin/sh


all .DEFAULT:
	@echo $@ Nothing to do.

create:
	-if [ ! -d $(BROWN_IVY_ARCH) ] ; then mkdir $(BROWN_IVY_ARCH); else true; fi

print:
	pr $(PFILES) | lpr -low

pribm:
	/bin/pr -l62 -f $(PFILES) | lpr -Pibm

prim:
	imprint -Pim0 -2 $(PFILES)

distrib_dir:
	rm -rf $(DISTRIB_DIR)/lib
	mkdir $(DISTRIB_DIR)/lib
	cp $(ACTIVE) $(DISTRIB_DIR)/lib



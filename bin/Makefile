#
# IVY bin makefile
#
# Copyright 1993 Steven P. Reiss, Brown University - All rights reserved.
#
#


SYSTEM= ivy

NAME= bin
PFILES= Makefile ivyarch ivybasepath ivybinpath ivycreatejava ivydatapath \
	ivyjava ivyjavaos ivyjavapath ivyjavatrace ivyjive ivymake \
	ivypath ivysetup ivysetupenv ivysrcpath


ACTIVE= $(PFILES)

.PHONY: all opt create newlib print pribm prim clean transfer distrib

all:

create:
	rm -rf $(BROWN_IVY_ARCH)
	mkdir $(BROWN_IVY_ARCH)

all .DEFAULT:
	@echo $@ nothing to do

print:
	pr $(PFILES) | lpr -low

pribm:
	/bin/pr -l62 -f $(PFILES) | lpr -Pibm

prim:
	imprint -Pim0 -2 $(PFILES)


transfer:
	(cd src; $(MAKE) $@)
	rcp $(ACTIVE) $(TOMACH):$(SRC)

distrib_dir:
	rm -rf $(DISTRIB_DIR)/bin
	mkdir $(DISTRIB_DIR)/bin
	cp $(ACTIVE) $(DISTRIB_DIR)/bin


bindistrib:
	rm -rf $(DISTRIB_BIN)/bin
	mkdir $(DISTRIB_BIN)/bin
	cp $(ACTIVE) $(DISTRIB_BIN)/bin
	mkdir $(DISTRIB_BIN)/bin/i686
	cp i686/* $(DISTRIB_BIN)/bin/i686
	mkdir $(DISTRIB_BIN)/bin/sol
	cp sol/* $(DISTRIB_BIN)/bin/sol

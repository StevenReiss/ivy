#
#   IVY/DATA Makefile
#
# Copyright 1990 Steven P. Reiss, Brown University - All rights reserved.
#
#



PFILES= Makefile Make.pass Make.template Make.defs Make.global

ACTIVE= $(PFILES)

all .DEFAULT:
	@echo $@ nothing to do.


print:
	pr $(PFILES) | lpr -low

pribm:
	/bin/pr -l62 -f $(PFILES) | lpr -Pibm

prim:
	imprint -Pim0 -2 $(PFILES)


create:

distrib_dir:
	rm -rf $(DISTRIB_DIR)/data
	mkdir $(DISTRIB_DIR)/data
	cp $(ACTIVE) $(DISTRIB_DIR)/data


bindistrib:
	mkdir $(DISTRIB_BIN)/data
	cp $(ACTIVE) $(DISTRIB_BIN)/data

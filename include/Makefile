#
#   include Makefile
#
# Copyright 1994 Steven P. Reiss, Brown University - All rights reserved.
#
#


SYSTEM= ivy

PFILES= Makefile
ACTIVE= $(PFILES)


all .DEFAULT:
	echo $@ nothing to do

print:
	pr $(PFILES) | lpr -low

pribm:
	/bin/pr -l62 -f $(PFILES) | lpr -Pibm

prim:
	imprint -Pim0 -2 $(PFILES)

distrib_dir:
	rm -rf $(DISTRIB_DIR)/include
	mkdir $(DISTRIB_DIR)/include
	cp $(ACTIVE) $(DISTRIB_DIR)/include
	mkdir $(DISTRIB_DIR)/include/xml
	cp -r xml/* $(DISTRIB_DIR)/include/xml

csdistrib_dir:
	rm -rf $(CSDISTRIB)/include
	mkdir $(CSDISTRIB)/include
	cp $(ACTIVE) $(CSDISTRIB)/include


create:
	- if [ ! -d xml ]; then mkdir xml; else true; fi

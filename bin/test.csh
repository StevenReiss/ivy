#! /bin/csh -f


# perl -e 'print (10000+(20000*rand)),"\n\n"'


# perl -e 'printf "%5.0f\n", (10000+(20000*rand))'

set port = `perl -e 'printf "%5.0f", (10000+(20000*rand))'`

echo PORT=$port

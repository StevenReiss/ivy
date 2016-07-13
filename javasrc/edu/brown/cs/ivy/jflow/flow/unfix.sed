#! /bin/csh -f

foreach i (*.java)

   mv $i.save $i

end

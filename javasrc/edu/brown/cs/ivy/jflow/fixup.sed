#! /bin/csh -f

foreach i (*.java)

   cp $i $i.save
   sed -e 's/JflowFactory/JflowControl/g' $i.save \
   >! $i

end



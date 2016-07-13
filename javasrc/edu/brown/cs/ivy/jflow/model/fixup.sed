#! /bin/csh -f

foreach i (*.java)

   sed -e 's/ChetSet/JflowSourceSet/g' $i \
   | sed -e 's/edu.brown.clime.chet/edu.brown.cs.ivy.jflow/g' \
   | sed -e 's/ChetState/JflowState/g' \
   | sed -e 's/ChetFlow/JflowMaster/g' \
   | sed -e 's/ChetValue/ModelValue/g' \
   | sed -e 's/ChetSource/JflowSource/g' \
   | sed -e 's/ChetConstants/JflowConstants/g' \
   | sed -e 's/ChetMethod/JflowMethod/g' \
   | sed -e 's/ChetMain/JflowMaster/g' \
   | sed -e 's/ChetModel/JflowModel/g' \
   | sed -e 's/ChetEvent/JflowEvent/g' \
   | sed -e 's/JflowValue.Factory/ValueFactory/g' \
   | sed -e 's/ChetFlow.CallSite/FlowCallSite/g' \
   | sed -e 's/debugGen/doDebug/g' \
   | sed -e 's/debugEval/doDebug/g' \
   | sed -e 's/JflowMethod.Factory\./jflow_master./g' \
   >! xxx
   cp $i $i.save
   mv xxx $i

end



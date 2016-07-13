#! /bin/csh -f

foreach i (FlowCleanup.java)

   sed -e 's/ChetSet/SourceSet/g' $i \
   | sed -e 's/JflowState/StateBase/g' \
   | sed -e 's/ChetState/StateBase/g' \
   | sed -e 's/JflowProto/ProtoBase/g' \
   | sed -e 's/ChetFlow/FlowControl/g' \
   | sed -e 's/ChetProto/ProtoBase/g' \
   | sed -e 's/ChetValue/ValueBase/g' \
   | sed -e 's/ChetSource/SourceBase/g' \
   | sed -e 's/ChetConstants/JflowConstants/g' \
   | sed -e 's/JflowSet/SourceSet/g' \
   | sed -e 's/ChetMethod/MethodBase/g' \
   | sed -e 's/ProtoBasetype/ProtoBase/g' \
   | sed -e 's/ChetMain/JflowMasterImpl/g' \
   | sed -e 's/JflowValue.Factory/ValueFactory/g' \
   | sed -e 's/JflowSource.Factory/SourceFactory/g' \
   | sed -e 's/MethodBaseFactory/MethodFactory/g' \
   | sed -e 's/ChetFlow.CallSite/FlowCallSite/g' \
   | sed -e 's/debugFlow/doDebug/g' \
   | sed -e 's/SourceSet.Factory\./jflow_master.create/g' \
   | sed -e 's/MethodBase.Factory\./jflow_master./g' \
   >! xxx
   cp $i $i.save
   mv xxx $i

end



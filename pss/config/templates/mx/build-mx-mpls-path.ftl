<#-- @ftlvariable name="paths" type="java.util.List<net.es.oscars.dto.pss.params.MplsPath>" -->

<#list paths as path>
edit protocols mpls path "${path.name}"
<#list path.hops as hop>
set ${hop.address} strict
</#list>
top
</#list>

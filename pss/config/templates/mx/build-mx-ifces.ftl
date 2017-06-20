<#-- @ftlvariable name="ifces" type="java.util.List<net.es.oscars.dto.pss.params.mx.MxIfce>" -->


<#list ifces as ifce>
edit interfaces ${ifce.port}
edit unit ${ifce.vlan}
set description "${ifce.description}"
set encapsulation vlan-vpls
set vlan-id ${ifce.vlan}
set output-vlan-map swap
set family vpls filter input ${ifce.statsFilter}
set family vpls filter output ${ifce.statsFilter}
top
</#list>

<#-- @ftlvariable name="qoses" type="java.util.List<net.es.oscars.pss.params.mx.MxQos>" -->
<#list qoses as qos>
delete firewall family any filter "${qos.filterName}"
<#if qos.createPolicer>
delete firewall policer "${qos.policerName}"
</#if>
</#list>

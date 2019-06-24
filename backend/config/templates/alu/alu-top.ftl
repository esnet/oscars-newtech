<#-- @ftlvariable name="fragments" type="java.util.List<java.lang.String>" -->

<#list fragments as fragment>
exit all
${fragment}
exit all
</#list>


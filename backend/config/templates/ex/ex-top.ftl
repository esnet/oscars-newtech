<#-- @ftlvariable name="fragments" type="java.util.List<java.lang.String>" -->
configure private

<#list fragments as fragment>
top
${fragment}
top
</#list>

commit and-quit
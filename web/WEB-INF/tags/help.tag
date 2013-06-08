<%--
--%><%@include file="/WEB-INF/tags/decl.tagf"%><%--
--%><%@tag body-content="empty"%><%--
--%><%@attribute name="id" rtexprvalue="true" %><%--
--%><tag:img png="help" title="common.help" style="display:inline" onclick="help('${id}', this);"/>
<%--
--%><%@include file="/WEB-INF/tags/decl.tagf"%><%--
--%><%@tag body-content="empty"%><%--
--%><%@attribute name="id" %><%--
--%><%@attribute name="value" rtexprvalue="true" %><%--
--%><sst:select id="${id}" value="${value}">
  <c:forEach begin="0" end="23" var="i"><sst:option value="${i}">${m2m2:padZeros(i, 2)}</sst:option></c:forEach>
</sst:select>
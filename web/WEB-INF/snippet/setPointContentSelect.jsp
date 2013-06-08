<%--
--%><%@ include file="/WEB-INF/snippet/common.jsp" %>
<sst:select value="${rawText}" id="setPointValue${idSuffix}">
  <c:forEach items="${point.textRenderer.multistateValues}" var="valueDef">
    <sst:option value="${valueDef.key}">${valueDef.text}</sst:option>
  </c:forEach>
</sst:select>
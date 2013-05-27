<%--
--%><%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<tag:page dwr="DataPointEditDwr">
  <%@ include file="/WEB-INF/jsp/pointEdit/pointName.jsp" %>
  
  <form action="" method="post">
    <input type="hidden" id="taskName" name="asdf" value=""/>
    <table width="100%" cellpadding="0" cellspacing="0">
      <tr>
        <td valign="top">
          <%@ include file="/WEB-INF/jsp/pointEdit/pointProperties.jsp" %>
          <%@ include file="/WEB-INF/jsp/pointEdit/loggingProperties.jsp" %>
          <%@ include file="/WEB-INF/jsp/pointEdit/valuePurge.jsp" %>
          <%@ include file="/WEB-INF/jsp/pointEdit/textRenderer.jsp" %>
          <%@ include file="/WEB-INF/jsp/pointEdit/chartRenderer.jsp" %>
        </td>
        <td valign="top">
          <%@ include file="/WEB-INF/jsp/pointEdit/eventDetectors.jsp" %>
        </td>
      </tr>
    </table>
  
    <%@ include file="/WEB-INF/jsp/pointEdit/buttons.jsp" %>
  </form>
</tag:page>

<script type="text/javascript">
  var pointList = [
    <c:forEach items="${userPoints}" var="point">{id:${point.id},name:"${sst:dquotEncode(point.extendedName)}"},
    </c:forEach>
  ];
</script>
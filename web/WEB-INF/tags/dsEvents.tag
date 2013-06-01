<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%><%@include file="/WEB-INF/tags/decl.tagf"%><%--
--%><%@tag body-content="empty"%><%--
--%><table width="100%">
  <tr><td class="smallTitle">
    <tag:img png="flag_white"/>
    <fmt:message key="dsEdit.events.alarmLevels"/>
  </td></tr>
</table>
<table cellspacing="1">
  <c:choose>
    <c:when test="${empty dataSource.eventTypes}">
      <tr><td><b><fmt:message key="dsEdit.events.noEvents"/></b></td></tr>
    </c:when>
    <c:otherwise>
      <c:forEach items="${dataSource.eventTypes}" var="type">
        <tr>
          <td><b><m2m2:translate message="${type.description}"/></b></td>
          <td>
            <tag:alarmLevelOptions id="alarmLevel${type.typeRef2}" onchange="alarmLevelChanged(${type.typeRef2})" value="${type.alarmLevel}"/>
            <tag:img id="alarmLevelImg${type.typeRef2}" png="flag_green" style="display:none;"/>
            <script type="text/javascript">setAlarmLevelImg(${type.alarmLevel}, 'alarmLevelImg${type.typeRef2}')</script>
          </td>
        </tr>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</table>
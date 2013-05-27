<%--
--%><%@include file="/WEB-INF/tags/decl.tagf" %><%--
--%><%@tag body-content="empty" %><%--
--%><%@attribute name="event" type="com.serotonin.m2m2.rt.event.EventInstance" required="true" rtexprvalue="true" %><%--
--%><c:if test="${event.userNotified}">
  <c:choose>
    <c:when test="${event.acknowledged}"><tag:img png="tick_off" title="events.acknowledged" style="display:inline;"/></c:when>
    <c:otherwise>
      <tag:img png="tick" id="ackImg${event.id}" onclick="ackEvent(${event.id})" title="events.acknowledge" style="display:inline;"/>
      <c:choose>
        <c:when test="${event.silenced}">
          <tag:img png="sound_mute_small" id="silenceImg${event.id}" onclick="toggleSilence(${event.id})" title="events.unsilence" style="display:inline;"/>
        </c:when>
        <c:otherwise>
          <tag:img png="sound_none_small" id="silenceImg${event.id}" onclick="toggleSilence(${event.id})" title="events.silence" style="display:inline;"/>
        </c:otherwise>
      </c:choose>
    </c:otherwise>
  </c:choose>
</c:if>
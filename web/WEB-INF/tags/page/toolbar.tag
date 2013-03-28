<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%><%@include file="/WEB-INF/tags/decl.tagf"%>
<%@tag import="com.serotonin.m2m2.module.ModuleRegistry"%>
<%@tag import="com.serotonin.m2m2.module.MenuItemDefinition"%>

<c:if test="${!simple}">
  <table width="100%" cellspacing="0" cellpadding="0" border="0" id="subHeader">
    <tr>
      <td style="cursor:default">
        <c:if test="${!empty sessionUser}">
          <c:forEach items="<%= ModuleRegistry.getMenuItems().get(MenuItemDefinition.Visibility.USER) %>" var="mi">
            <m2m2:menuItem def="${mi}"/>
          </c:forEach>
                
          <c:if test="${sessionUser.dataSourcePermission}">
            <img src="/images/menu_separator.png"/>
            <c:forEach items="<%= ModuleRegistry.getMenuItems().get(MenuItemDefinition.Visibility.DATA_SOURCE) %>" var="mi">
              <m2m2:menuItem def="${mi}"/>
            </c:forEach>
          </c:if>
          
          <img src="/images/menu_separator.png"/>
          <m2m2:menuItem href="/users.shtm" png="user" key="header.users"/>
          
          <c:if test="${sessionUser.admin}">
            <c:forEach items="<%= ModuleRegistry.getMenuItems().get(MenuItemDefinition.Visibility.ADMINISTRATOR) %>" var="mi">
              <m2m2:menuItem def="${mi}"/>
            </c:forEach>
          </c:if>
          
          <img src="/images/menu_separator.png"/>
          <c:forEach items="<%= ModuleRegistry.getMenuItems().get(MenuItemDefinition.Visibility.ANONYMOUS) %>" var="mi">
            <m2m2:menuItem def="${mi}"/>
          </c:forEach>
        </c:if>
        <c:if test="${empty sessionUser}">
          <m2m2:menuItem href="/login.htm" png="login" key="header.login"/>
          <c:forEach items="<%= ModuleRegistry.getMenuItems().get(MenuItemDefinition.Visibility.ANONYMOUS) %>" var="mi">
            <m2m2:menuItem def="${mi}"/>
          </c:forEach>
        </c:if>
        <div id="headerMenuDescription" class="labelDiv" style="position:absolute;display:none;"></div>
      </td>
      
      <td align="right">
        <c:if test="${!empty sessionUser}">
          <span class="copyTitle"><fmt:message key="header.user"/>: <b>${sessionUser.username}</b></span>
          <m2m2:menuItem href="/logout.htm" png="logout" key="header.logout"/>
          <tag:img id="userMutedImg" onclick="MiscDwr.toggleUserMuted(setUserMuted)" onmouseover="hideLayersIgnoreMissing('userHome', 'localeEdit')"/>
          <div style="display: inline;" onmouseover="hideLayersIgnoreMissing('localeEdit'); showMenu('userHome', null, 10, 10);">
            <tag:img png="home" title="header.goHomeUrl" onclick="goHomeUrl();"/>
            <div id="userHome" style="visibility:hidden;left:0px;top:15px;" class="labelDiv" onmouseout="hideLayer(this)">
              <tag:img png="house_link" title="header.setHomeUrl" onclick="setHomeUrl()" onmouseover="hideLayersIgnoreMissing('localeEdit')"/>
              <tag:img png="house_delete" title="header.deleteHomeUrl" onclick="deleteHomeUrl()" onmouseover="hideLayersIgnoreMissing('localeEdit')"/>
            </div>
          </div>
        </c:if>
        <c:if test="${fn:length(availableLanguages) > 1}">
          <div style="display:inline;" onmouseover="hideLayersIgnoreMissing('userHome'); showMenu('localeEdit', null, 10, 10);">
            <tag:img png="locale" title="header.changeLanguage"/>
            <div id="localeEdit" style="visibility:hidden;left:0px;top:15px;" class="labelDiv" onmouseout="hideLayer(this)">
              <c:forEach items="${availableLanguages}" var="lang">
                <a class="ptr" onclick="setLocale('${lang.key}')">${lang.value}</a><br/>
              </c:forEach>
            </div>
          </div>
        </c:if>
      </td>
    </tr>
  </table>
</c:if>
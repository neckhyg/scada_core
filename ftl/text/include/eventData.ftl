<#include "alarmLevel.ftl">${evt.prettyActiveTimestamp} - <@fmt message=evt.message/>

<#if evt.eventComments??>
<#list evt.eventComments as comment>

********** <@fmt key="notes.note"/> - ${comment.prettyTime} <@fmt key="notes.by"/> <#if comment.username??>${comment.username}<#else><@fmt key="common.deleted"/></#if>
${comment.comment}

</#list>
</#if>

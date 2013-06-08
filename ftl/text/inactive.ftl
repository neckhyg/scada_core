<#ftl strip_whitespace=false>
${instanceDescription}
${evt.prettyRtnTimestamp} <@fmt key="ftl.eventInactive"/>: <@fmt message=evt.rtnMessage/>


<@fmt key="ftl.originalInformation"/>

*******************************************************
<#include "include/eventData.ftl">

*******************************************************

<#include "include/footer.ftl">
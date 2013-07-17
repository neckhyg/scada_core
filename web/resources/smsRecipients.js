mango.srecip = {};

mango.srecip.SmsRecipients = function(prefix, testSmsMessage, users) {
    this.prefix = prefix;
    this.testSmsMessage = testSmsMessage;
    this.nextMobileId = 1;
    this.users = users;
    
    this.write = function(node, varName, id, label) {
        node = getNodeIfString(node);
        var tr = appendNewElement("tr", node);
        if (id)
            tr.id = id;
        
        var td = appendNewElement("td", tr);
        td.className = "formLabelRequired";
        var cnt = label +'<br/>';
        cnt += '<img id="'+ this.prefix +'SendTestImg" src="images/sms_go.png" class="ptr"';
        cnt += '        onclick="'+ varName +'.sendTestSms()" title="'+ mango.i18n["common.sendTestSms"] +'"/>';
        td.innerHTML = cnt;
        
        td = appendNewElement("td", tr);
        td.className = "formField";
        cnt  = '<table>';
        cnt += '  <tr>';
        cnt += '    <td class="formLabel">'+ mango.i18n["js.sms.addUser"] +'</td>';
        cnt += '    <td>';
        cnt += '      <select id="'+ this.prefix +'Users"></select>';
        cnt += '      <img src="images/add.png" class="ptr" onclick="'+ varName +'.addUser()"/>';
        cnt += '    </td>';
        cnt += '  </tr>';
        cnt += '  <tr>';
        cnt += '    <td class="formLabel">'+ mango.i18n["js.sms.addMobile"] +'</td>';
        cnt += '    <td>';
        cnt += '      <input id="'+ this.prefix +'Mobile" type="text"/>';
        cnt += '      <img src="images/add.png" class="ptr" onclick="'+ varName +'.addMobile()"/>';
        cnt += '    </td>';
        cnt += '  </tr>';
        cnt += '</table>';
        cnt += '<div class="borderDivPadded">';
        cnt += '  <table width="100%">';
        cnt += '    <tr id="'+ this.prefix +'ListEmpty"><td colspan="3">'+ mango.i18n["js.sms.noRecipients"] +'</td></tr>';
        cnt += '    <tr id="'+ this.prefix +'_TEMPLATE_" style="display:none;">';
        cnt += '        <td width="16"><img id="'+ this.prefix +'_TEMPLATE_Img"/></td>';
        cnt += '        <td id="'+ this.prefix +'_TEMPLATE_Description"></td>';
        cnt += '        <td width="16"><img src="images/bullet_delete.png" class="ptr"';
        cnt += '                onclick="'+ varName +'.deleteRecipient(getMangoId(this))"/></td>';
        cnt += '    </tr>';
        cnt += '    <tbody id="'+ this.prefix +'List"></tbody>';
        cnt += '  </table>';
        cnt += '</div>';
        cnt += '<span id="'+ this.prefix +'Error" class="formError"></span>';
        td.innerHTML = cnt;
    }
    
    this.repopulateLists = function() {
        dwr.util.removeAllOptions(this.prefix +"Users");
        dwr.util.addOptions(this.prefix +"Users", this.users, "id", "username");
    }

    this.sendTestSms = function() {
        this.setErrorMessage();
        var smsList = this.createRecipientArray();
        if (smsList.length == 0)
            this.setErrorMessage(mango.i18n["js.sms.noRecipForSms"]);
        else {
//            MiscDwr.sendTestSms(smsList, this.prefix, this.testSmsMessage, dojo.hitch(this, "sendTestSmsCB"));
            startImageFader($(this.prefix +"SendTestImg"));
        }
    }

    this.sendTestSmsCB = function(response) {
        stopImageFader($(this.prefix +"SendTestImg"));
        if (response.messages.length > 0)
            this.setErrorMessage(response.messages[0]);
        else
            this.setErrorMessage(mango.i18n["js.sms.testSent"]);
    }

    this.deleteRecipient = function(id) {
        // Delete the visual entry.
        $(this.prefix +"List").removeChild($(this.prefix + id));
        this.updateOptions(this.prefix +"Users", this.users, this.prefix +"List", "U", "username");
        this.checkListEmptyMessage();
    }
    

    this.addUser = function(uid) {
        if (!uid)
            uid = $get(this.prefix +"Users");
        if (!uid)
            return;
        var user = this.getUser(uid);
        if (user) {
            this.addListEntry("U"+ uid, "images/user.png", user.username);
            setUserImg(user.admin, user.disabled, $(this.prefix +"U"+ uid +"Img"));
            this.updateOptions(this.prefix +"Users", this.users, this.prefix +"List", "U", "username");
            this.checkListEmptyMessage();
        }
    }
    
    this.addMobile = function(mobile) {
        if (!mobile)
            mobile = $get(this.prefix +"Mobile");
        if (mobile == "")
            return;
        this.addListEntry("M"+ this.nextMobileId++, "images/sms.png", mobile);
        this.checkListEmptyMessage();
    }
    
    this.createRecipientArray = function() {
        var recipientList = $(this.prefix +"List");
        var list = new Array();
        var id;
        for (j=0; j<recipientList.childNodes.length; j++) {
            if (recipientList.childNodes[j].mangoId) {
                id = recipientList.childNodes[j].mangoId;
                if (id.startsWith("U"))
                    list[list.length] = {
                            recipientType: 1, // SmsRecipient.TYPE_USER
                            referenceId: id.substring(1)};
                else if (id.startsWith("M"))
                    list[list.length] = {
                            recipientType: 2, // SmsRecipient.TYPE_MOBILE
                            referenceMobile: $(this.prefix + id +"Description").innerHTML};
                else
                    dojo.debug("Unknown recipient mango id: "+ id);
            }
        }
        return list;
    }
    
    this.updateOptions = function(selectId, itemList, recipientListId, prefix, descriptionField) {
        var select = $(selectId);
        var recipientList = $(recipientListId);
    
        dwr.util.removeAllOptions(select);
        var addOptions = new Array();
        var i, j, item, found;
        for (i=0; i<itemList.length; i++) {
            item = itemList[i];
            found = false;
            
            for (j=0; j<recipientList.childNodes.length; j++) {
                if (recipientList.childNodes[j].mangoId && prefix + item.id == recipientList.childNodes[j].mangoId) {
                    found = true;
                    break;
                }
            }
            
            if (!found)
                addOptions[addOptions.length] = item;
        }
        dwr.util.addOptions(select, addOptions, "id", descriptionField);
    }
    
    this.addListEntry = function(id, imgName, description) {
        createFromTemplate(this.prefix +"_TEMPLATE_", id, this.prefix +"List");
        $(this.prefix + id +"Img").src = imgName;
        $(this.prefix + id +"Description").innerHTML = description;
    }

    this.getUser = function(id) {
        return getElement(this.users, id);
    }
    
    this.checkListEmptyMessage = function() {
        var recipientList = $(this.prefix +"List");
        // Check if the empty list message should be displayed or not.
        var found = false;
        for (var i=0; i<recipientList.childNodes.length; i++) {
            if (recipientList.childNodes[i].mangoId) {
                found = true;
                break;
            }
        }
        display(this.prefix +"ListEmpty", !found);
    }
    
    this.updateRecipientList = function(recipientList) {
        this.setErrorMessage();
        this.repopulateLists();
        dwr.util.removeAllRows(this.prefix +"List");
        if (!recipientList || recipientList.length == 0)
            this.checkListEmptyMessage();
        else {
            for (var i=0; i<recipientList.length; i++) {
                var recip = recipientList[i];
                if (recip.recipientType == 1) // SmsRecipient.TYPE_USER
                    this.addUser(recip.referenceId);
                else if (recip.recipientType == 2) // SmsRecipient.TYPE_MOBILE
                    this.addMobile(recip.referenceMobile);
                else
                    dojo.debug("Unknown recipient type id: "+ recip.recipientType);
            }
        }
    }
    
    this.setErrorMessage = function(msg) {
        showMessage(this.prefix +"Error", msg);
    }
}

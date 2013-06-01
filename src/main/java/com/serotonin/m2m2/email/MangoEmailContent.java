package com.serotonin.m2m2.email;

import java.io.IOException;
import java.util.Map;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.web.mail.TemplateEmailContent;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MangoEmailContent extends TemplateEmailContent {
    public static final int CONTENT_TYPE_BOTH = 0;
    public static final int CONTENT_TYPE_HTML = 1;
    public static final int CONTENT_TYPE_TEXT = 2;

    private final String defaultSubject;
    private final SubjectDirective subjectDirective;

    public MangoEmailContent(String templateName, Map<String, Object> model, Translations translations,
            String defaultSubject, String encoding) throws TemplateException, IOException {
        super(encoding);

        int type = SystemSettingsDao.getIntValue(SystemSettingsDao.EMAIL_CONTENT_TYPE);

        this.defaultSubject = defaultSubject;
        this.subjectDirective = new SubjectDirective(translations);

        model.put("fmt", new MessageFormatDirective(translations));
        model.put("subject", subjectDirective);

        if (type == CONTENT_TYPE_HTML || type == CONTENT_TYPE_BOTH)
            setHtmlTemplate(getTemplate(templateName, true), model);

        if (type == CONTENT_TYPE_TEXT || type == CONTENT_TYPE_BOTH)
            setPlainTemplate(getTemplate(templateName, false), model);
    }

    public String getSubject() {
        String subject = subjectDirective.getSubject();
        if (subject == null)
            return defaultSubject;
        return subject;
    }

    private Template getTemplate(String name, boolean html) throws IOException {
        if (html)
            name = "html/" + name + ".ftl";
        else
            name = "text/" + name + ".ftl";

        return Common.freemarkerConfiguration.getTemplate(name);
    }
}

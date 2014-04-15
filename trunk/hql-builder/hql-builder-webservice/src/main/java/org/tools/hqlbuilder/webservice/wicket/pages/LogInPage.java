package org.tools.hqlbuilder.webservice.wicket.pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.core.Authentication;
import org.tools.hqlbuilder.webservice.wicket.DefaultWebPage;
import org.tools.hqlbuilder.webservice.wicket.MountedPage;
import org.tools.hqlbuilder.webservice.wicket.WicketApplication;

@MountedPage("${wicket.login.mount}")
public class LogInPage extends DefaultWebPage {
    private static final long serialVersionUID = -959095871171401454L;

    public LogInPage(PageParameters parameters) {
        super(parameters);
        setStatelessHint(true);
    }

    @Override
    protected void addComponents() {
        super.addComponents();
        Authentication authentication = WicketApplication.getSecurityContext().getAuthentication();
        add(new LogInPanel(authentication, securityProperties));
    }
}

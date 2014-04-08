package org.tools.hqlbuilder.webservice.wicket.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.core.Authentication;
import org.tools.hqlbuilder.webservice.security.SecurityConstants;
import org.tools.hqlbuilder.webservice.wicket.DefaultWebPage;
import org.tools.hqlbuilder.webservice.wicket.WicketApplication;

public class LogOutPage extends DefaultWebPage {
    private static final long serialVersionUID = -1844173741599209281L;

    public LogOutPage(PageParameters parameters) {
        super(parameters);

        Authentication authentication = WicketApplication.getSecurityContext().getAuthentication();

        ExternalLink logout = new ExternalLink("logout", getRequest().getContextPath() + SecurityConstants.$LOGOUT$);
        add(logout);

        Label username = new Label("username", authentication == null ? "?" : authentication.getName());
        add(username);
    }
}
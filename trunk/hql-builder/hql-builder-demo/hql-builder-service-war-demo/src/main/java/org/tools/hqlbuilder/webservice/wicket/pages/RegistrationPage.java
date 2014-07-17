package org.tools.hqlbuilder.webservice.wicket.pages;

import static org.tools.hqlbuilder.webservice.wicket.WebHelper.proxy;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tools.hqlbuilder.test.Registration;
import org.tools.hqlbuilder.webclient.HqlWebServiceClient;
import org.tools.hqlbuilder.webservice.wicket.MountedPage;
import org.tools.hqlbuilder.webservice.wicket.forms.DefaultFormActions;
import org.tools.hqlbuilder.webservice.wicket.forms.FormElementSettings;
import org.tools.hqlbuilder.webservice.wicket.forms.FormPanel;
import org.tools.hqlbuilder.webservice.wicket.forms.FormSettings;

@SuppressWarnings("serial")
@MountedPage("/form/registration")
public class RegistrationPage extends BasePage {
    @SpringBean
    protected transient HqlWebServiceClient hqlWebClient;

    @SpringBean
    protected transient PasswordEncoder passwordEncoder;

    public RegistrationPage(PageParameters parameters) {
        super(parameters);

        DefaultFormActions<Registration> formActions = new DefaultFormActions<Registration>() {
            @Override
            public void submitModel(IModel<Registration> model) {
                Registration object = model.getObject();
                object.setPassword(passwordEncoder.encode(object.getPassword()));
                object.setVerification(new LocalDateTime());
                Serializable id = hqlWebClient.save(object);
                object = hqlWebClient.get(object.getClass(), id);
                model.setObject(object);
            }
        };
        FormPanel<Registration> formPanel = new FormPanel<Registration>("registrationform", formActions, new FormSettings());
        add(formPanel);

        Registration proxy = proxy(Registration.class);
        formPanel.addTextField(proxy.getUsername(), new FormElementSettings(true));
        formPanel.addTextField(proxy.getFirstName(), new FormElementSettings(true));
        formPanel.addTextField(proxy.getLastName(), new FormElementSettings(true));
        formPanel.addEmailTextField(proxy.getEmail(), new FormElementSettings(true));
        formPanel.addDatePicker(proxy.getDateOfBirth(), new FormElementSettings(false));
        formPanel.addPasswordTextField(proxy.getPassword(), new FormElementSettings(true));
    }
}

package org.tools.hqlbuilder.webservice.wicket.forms;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.tools.hqlbuilder.webservice.wicket.forms.FormPanel.FormComponentSettings;
import org.tools.hqlbuilder.webservice.wicket.forms.FormPanel.FormRowPanel;
import org.tools.hqlbuilder.webservice.wicket.forms.FormPanel.FormSettings;

public class PasswordTextFieldPanel extends FormRowPanel<String, PasswordTextField> {
    private static final long serialVersionUID = -7993592150932306594L;

    public PasswordTextFieldPanel(final IModel<?> model, final String property, FormSettings formSettings, FormComponentSettings componentSettings) {
        super(model, property, String.class, formSettings, componentSettings);
    }

    @Override
    protected PasswordTextField createComponent() {
        return new PasswordTextField(VALUE, getValueModel()) {
            private static final long serialVersionUID = -7265008846653720072L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                onFormComponentTag(tag);
            }
        };
    }
}

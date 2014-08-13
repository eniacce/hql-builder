package org.tools.hqlbuilder.webservice.wicket.forms;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

public class TextFieldPanel<T extends Serializable> extends DefaultFormRowPanel<T, TextField<T>> {
    private static final long serialVersionUID = -7993592150932306594L;

    public TextFieldPanel(String id, final IModel<?> model, final T propertyPath, FormSettings formSettings, FormElementSettings componentSettings) {
        super(id, model, propertyPath, formSettings, componentSettings);
    }

    @Override
    protected TextField<T> createComponent(IModel<T> model, Class<T> valueType) {
        return new TextField<T>(VALUE, model, valueType) {
            private static final long serialVersionUID = -3231896888772971388L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                onFormComponentTag(tag);
            }
        };
    }
}

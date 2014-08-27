package org.tools.hqlbuilder.webservice.wicket.forms;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

public class NumberTextFieldPanel<N extends Number & Comparable<N>> extends DefaultFormRowPanel<N, TextField<N>, FormElementSettings> {
    private static final long serialVersionUID = 2490571767214451220L;

    public NumberTextFieldPanel(IModel<?> model, N propertyPath, FormSettings formSettings, NumberFieldSettings<N> componentSettings) {
        super(model, propertyPath, formSettings, componentSettings);
    }

    @Override
    protected TextField<N> createComponent(IModel<N> model, Class<N> valueType) {
        return new TextField<N>(VALUE, model, valueType) {
            private static final long serialVersionUID = -8892429029495702023L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                onFormComponentTag(tag);
                @SuppressWarnings("unchecked")
                NumberFieldSettings<N> settings = (NumberFieldSettings<N>) getComponentSettings();
                tag(tag, "min", settings.getMinimum());
                tag(tag, "max", settings.getMaximum());
                tag(tag, "step", settings.getStep());
            }
        };
    }

    @Override
    protected void setupPlaceholder(ComponentTag tag) {
        //
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (!isEnabledInHierarchy()) {
            return;
        }
        response.render(new JavaScriptContentHeaderItem("$(function() { $( \"#" + getComponent().getMarkupId() + "\" ).puispinner(); });", "js_"
                + getComponent().getMarkupId() + "_" + System.currentTimeMillis(), null));
    }
}
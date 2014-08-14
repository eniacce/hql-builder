package org.tools.hqlbuilder.webservice.wicket.forms;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tools.hqlbuilder.common.CommonUtils;
import org.tools.hqlbuilder.webservice.css.WicketCSSRoot;
import org.tools.hqlbuilder.webservice.resources.PocketGrid.PocketGrid;
import org.tools.hqlbuilder.webservice.resources.weloveicons.WeLoveIcons;
import org.tools.hqlbuilder.webservice.wicket.WebHelper;
import org.tools.hqlbuilder.webservice.wicket.converter.Converter;
import org.tools.hqlbuilder.webservice.wicket.zuss.ZussResourceReference;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;

public class FormPanel<T extends Serializable> extends Panel implements FormConstants {
    private static final long serialVersionUID = -6387604067134639316L;

    protected static final Logger logger = LoggerFactory.getLogger(FormPanel.class);

    protected RepeatingView rowRepeater;

    protected RepeatingView componentRepeater;

    protected FormSettings formSettings;

    protected FormActions<T> formActions;

    protected Form<T> form;

    protected int count = 0;

    protected StringBuilder css = new StringBuilder();

    protected Map<String, String> cssTypes = new HashMap<String, String>();

    public FormPanel(String id) {
        this(id, null, null);
    }

    public FormPanel(String id, FormActions<T> formActions, FormSettings formSettings) {
        super(id);
        WebHelper.show(this);
        setFormActions(formActions != null ? formActions : new DefaultFormActions<T>() {
            private static final long serialVersionUID = -6135914559717102175L;

            @Override
            public Class<T> forObjectClass() {
                return CommonUtils.<T> getImplementation(FormPanel.this, FormPanel.class);
                // Class<? extends FormPanel<T>> clazz = (Class<? extends FormPanel<T>>) FormPanel.this.getClass();
                // return (Class<T>) ((java.lang.reflect.ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
            }
        });
        setFormSettings(formSettings == null ? new FormSettings() : formSettings);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new ZussResourceReference(WicketCSSRoot.class, "form.css")));
        response.render(CssHeaderItem.forReference(PocketGrid.POCKET_GRID));
        response.render(CssHeaderItem.forReference(WeLoveIcons.WE_LOVE_ICONS_CSS));
        renderColumnsCss(response);
    }

    protected void renderColumnsCss(IHeaderResponse response) {
        response.render(CssHeaderItem.forCSS(css.toString(),//
                "css_form_" + getId() + "_" + System.currentTimeMillis()));//
    }

    protected FormActions<T> getFormActions() {
        if (formActions == null) {
            throw new RuntimeException("FormActions required");
        }
        return this.formActions;
    }

    public void setFormActions(FormActions<T> formActions) {
        if (formActions == null) {
            throw new RuntimeException("FormActions required");
        }
        this.formActions = formActions;
    }

    public FormSettings getFormSettings() {
        if (formSettings == null) {
            throw new RuntimeException("FormSettings required");
        }
        return this.formSettings;
    }

    public void setFormSettings(FormSettings formSettings) {
        if (formSettings == null) {
            throw new RuntimeException("FormSettings required");
        }
        this.formSettings = formSettings;
    }

    public Form<T> getForm() {
        if (form == null) {
            getFormActions();
            getFormSettings();
            IModel<T> formModel = new LoadableDetachableModel<T>() {
                private static final long serialVersionUID = -5489467484161698560L;

                @Override
                protected T load() {
                    return getFormActions().loadObject();
                }
            };
            this.form = new Form<T>(FORM, formModel) {
                private static final long serialVersionUID = -5899425422548211723L;

                @Override
                protected void onSubmit() {
                    onBeforeSubmit();
                    getFormActions().submitModel(getFormModel());
                    onAfterSubmit();
                }
            };

            if (getFormSettings().isInheritId()) {
                form.setMarkupId(form.getId());
            }

            WebHelper.show(form);
            add(form);

            WebMarkupContainer formHeader = new WebMarkupContainer(FORM_HEADER);
            form.add(formHeader);

            WebMarkupContainer formBody = new WebMarkupContainer(FORM_BODY);
            form.add(formBody);

            formBody.add(getRowRepeater());

            ResourceModel submitModel = new ResourceModel(SUBMIT_LABEL);
            ResourceModel resetModel = new ResourceModel(RESET_LABEL);
            ResourceModel cancelModel = new ResourceModel(CANCEL_LABEL);

            Component submit;

            if (getFormSettings().isAjax()) {
                submit = new AjaxSubmitLink(FORM_SUBMIT, form) {
                    private static final long serialVersionUID = 1046494045754727027L;

                    @SuppressWarnings("unchecked")
                    @Override
                    protected void onAfterSubmit(AjaxRequestTarget target, Form<?> f) {
                        getFormActions().afterSubmit(target, (Form<T>) f, getFormModel());
                    }
                };
                submit.setDefaultModel(submitModel);
            } else {
                submit = new Button(FORM_SUBMIT, submitModel);
            }

            Button reset = new Button(FORM_RESET, resetModel);

            // https://cwiki.apache.org/confluence/display/WICKET/Multiple+submit+buttons
            Component cancel;
            if (getFormSettings().isAjax()) {
                cancel = new AjaxSubmitLink(FORM_CANCEL, form) {
                    private static final long serialVersionUID = -8816675271842238444L;

                    @SuppressWarnings("unchecked")
                    @Override
                    protected void onAfterSubmit(AjaxRequestTarget target, Form<?> f) {
                        getFormActions().afterCancel(target, (Form<T>) f, getFormModel());
                    }
                };
                cancel.setDefaultModel(cancelModel);
                ((AjaxSubmitLink) cancel).setDefaultFormProcessing(false);
            } else {
                cancel = new Button(FORM_CANCEL, cancelModel);
                ((Button) cancel).setDefaultFormProcessing(false);
            }
            cancel.setVisible(getFormSettings().isCancelable());

            if (getFormSettings().isInheritId()) {
                submit.setMarkupId(getId() + "." + FORM_SUBMIT);
                reset.setMarkupId(getId() + "." + FORM_RESET);
                cancel.setMarkupId(getId() + "." + FORM_CANCEL);
            }

            WebMarkupContainer formActionsContainer = new WebMarkupContainer(FORM_ACTIONS);
            form.add(formActionsContainer);
            formActionsContainer.add(submit);
            formActionsContainer.add(reset);
            formActionsContainer.add(cancel);

            WebMarkupContainer formFooter = new WebMarkupContainer(FORM_FOOTER);
            form.add(formFooter);
        }
        return form;
    }

    public IModel<T> getFormModel() {
        return getForm().getModel();
    }

    @SuppressWarnings("rawtypes")
    protected void onBeforeSubmit() {
        getRowRepeater().visitChildren(FormRowPanel.class, new IVisitor<FormRowPanel, Void>() {
            @Override
            public void component(FormRowPanel object, IVisit<Void> visit) {
                if (object instanceof FormSubmitInterceptor) {
                    FormSubmitInterceptor.class.cast(object).onBeforeSubmit();
                }
            }
        });
    }

    @SuppressWarnings("rawtypes")
    protected void onAfterSubmit() {
        getRowRepeater().visitChildren(FormRowPanel.class, new IVisitor<FormRowPanel, Void>() {
            @Override
            public void component(FormRowPanel object, IVisit<Void> visit) {
                if (object instanceof FormSubmitInterceptor) {
                    FormSubmitInterceptor.class.cast(object).onAfterSubmit();
                }
            }
        });
    }

    public <PropertyType extends Serializable, ComponentType extends FormComponent<PropertyType>, RowPanel extends DefaultFormRowPanel<PropertyType, ComponentType>> RowPanel addDefaultRow(
            RowPanel rowpanel) {
        return addRow(rowpanel);
    }

    public <PropertyType, ModelType, ComponentType extends FormComponent<ModelType>, RowPanel extends FormRowPanel<PropertyType, ModelType, ComponentType>> RowPanel addCustomRow(
            RowPanel rowpanel) {
        return addRow(rowpanel);
    }

    /**
     * single lazy creation
     */
    protected RepeatingView getRowRepeater() {
        if (rowRepeater == null) {
            RepeatingView repeater = new RepeatingView(FORM_ROW_REPEATER);
            rowRepeater = WebHelper.show(repeater);
        }
        return this.rowRepeater;
    }

    protected String renderColumnsCss(FormPanelVariation variation, int columnCount, String labelWidth) {
        String mapKey = getId() + '_' + columnCount + '_' + variation + '_' + labelWidth;
        String cssClass = cssTypes.get(mapKey);
        if (cssClass == null) {
            cssClass = "c_" + getId() + '_' + getFormSettings().getColumns() + '_'
                    + (getFormSettings().getVariation() == FormPanelVariation.label ? new String(Hex.encodeHex(labelWidth.getBytes())) : "");
            cssTypes.put(mapKey, cssClass);
            StringBuilder sbColumnsCss = new StringBuilder();
            if (variation == FormPanelVariation.label) {
                {
                    for (int i = 0; i < columnCount; i++) {
                        sbColumnsCss.append(".").append(cssClass).append(" ");
                        sbColumnsCss.append(".block:nth-child(").append((i * 2) + 1).append(")");
                        if (i < columnCount - 1) {
                            sbColumnsCss.append(",");
                        }
                    }
                    sbColumnsCss.append("{width:").append(labelWidth).append(";}");
                    sbColumnsCss.append("\n");
                }
                {
                    for (int i = 0; i < columnCount; i++) {
                        sbColumnsCss.append(".").append(cssClass).append(" ");
                        sbColumnsCss.append(".block:nth-child(").append((i * 2) + 2).append(")");
                        if (i < columnCount - 1) {
                            sbColumnsCss.append(",");
                        }
                    }
                    sbColumnsCss.append("{width:");
                    if (columnCount == 1) {
                        sbColumnsCss.append("calc(100% - ").append(labelWidth).append(")");
                    } else {
                        sbColumnsCss.append("calc((100% - (").append(labelWidth).append(" * ").append(columnCount).append("))").append(" / ")
                        .append(columnCount).append(")");
                    }
                    sbColumnsCss.append(";}");
                    sbColumnsCss.append("\n");
                }
            } else {
                for (int i = 0; i < columnCount; i++) {
                    sbColumnsCss.append(".").append(cssClass).append(" ");
                    sbColumnsCss.append(".block:nth-child(").append(i + 1).append(")");
                    if (i < columnCount - 1) {
                        sbColumnsCss.append(",");
                    }
                }
                sbColumnsCss.append("{width:");
                if (columnCount == 1) {
                    sbColumnsCss.append("100%");
                } else {
                    sbColumnsCss.append("calc(100%").append(" / ").append(columnCount).append(")");
                }
                sbColumnsCss.append(";}");
                sbColumnsCss.append("\n");
            }
            System.out.println(mapKey);
            System.out.println(cssClass);
            System.out.println(sbColumnsCss);
            css.append(sbColumnsCss.toString());
        }
        return cssClass;
    }

    protected RepeatingView getComponentRepeater() {
        // only create a new row when needed
        if (componentRepeater == null) {
            WebMarkupContainer rowContainer = new WebMarkupContainer(getRowRepeater().newChildId());

            rowContainer.add(new CssClassNameAppender(renderColumnsCss(getFormSettings().getVariation(), getFormSettings().getColumns(),
                    getFormSettings().getLabelWidth())));
            getRowRepeater().add(rowContainer);

            RepeatingView repeater = new RepeatingView(FORM_ELEMENT_REPEATER);
            componentRepeater = WebHelper.show(repeater);
            rowContainer.add(componentRepeater);
        }
        return this.componentRepeater;
    }

    protected <PropertyType, ModelType, ComponentType extends FormComponent<ModelType>, RowPanel extends FormRowPanel<PropertyType, ModelType, ComponentType>> RowPanel addRow(
            RowPanel rowpanel) {
        getForm();

        WebMarkupContainer elementContainer = new WebMarkupContainer(getComponentRepeater().newChildId());
        getComponentRepeater().add(elementContainer);

        // rowpanel is already created
        elementContainer.add(rowpanel);

        // components are created in "rowpanel.addComponents" method
        rowpanel.addComponents();

        // some post creation stuff
        rowpanel.afterAddComponents();

        count++;
        if (count == formSettings.getColumns()) {
            count = 0; // reset count
            componentRepeater = null; // so that a new one is created when needed
        }

        return rowpanel;
    }

    public <F extends Serializable> HiddenFieldPanel<F> addHidden(F propertyPath) {
        return addDefaultRow(new HiddenFieldPanel<F>(getFormModel(), propertyPath));
    }

    public ColorPickerPanel addColorPicker(String propertyPath, ColorPickerSettings componentSettings) {
        return addDefaultRow(new ColorPickerPanel(getFormModel(), propertyPath, getFormSettings(), componentSettings));
    }

    public <F extends Serializable> RadioButtonsPanel<F> addRadioButtons(F propertyPath, FormElementSettings componentSettings, ListModel<F> choices,
            IChoiceRenderer<F> renderer) {
        return addDefaultRow(new RadioButtonsPanel<F>(getFormModel(), propertyPath, getFormSettings(), componentSettings, choices, renderer));
    }

    public <F extends Serializable> MultiSelectCheckBoxPanel<F> addMultiSelectCheckBox(Collection<F> propertyPath,
            FormElementSettings componentSettings, ListModel<F> choices, IChoiceRenderer<F> renderer) {
        return addCustomRow(new MultiSelectCheckBoxPanel<F>(getFormModel(), propertyPath, getFormSettings(), componentSettings, choices, renderer));
    }

    public <F extends Serializable> DropDownPanel<F> addDropDown(F propertyPath, FormElementSettings componentSettings, ListModel<F> choices,
            IChoiceRenderer<F> renderer) {
        return addDefaultRow(new DropDownPanel<F>(getFormModel(), propertyPath, getFormSettings(), componentSettings, choices, renderer));
    }

    public <F extends Serializable> TextFieldPanel<F> addTextField(F propertyPath, FormElementSettings componentSettings) {
        return addDefaultRow(new TextFieldPanel<F>(getFormModel(), propertyPath, getFormSettings(), componentSettings));
    }

    public <F extends Serializable> TextAreaPanel<F> addTextArea(F propertyPath, TextAreaSettings componentSettings) {
        return addDefaultRow(new TextAreaPanel<F>(getFormModel(), propertyPath, getFormSettings(), componentSettings));
    }

    public <N extends Number & Comparable<N>> NumberFieldPanel<N> addNumberField(N propertyPath, NumberFieldSettings<N> componentSettings) {
        return addDefaultRow(new NumberFieldPanel<N>(getFormModel(), propertyPath, getFormSettings(), componentSettings));
    }

    public <N extends Number & Comparable<N>> RangeFieldPanel<N> addRangeField(N propertyPath, RangeFieldSettings<N> componentSettings) {
        return addDefaultRow(new RangeFieldPanel<N>(getFormModel(), propertyPath, getFormSettings(), componentSettings));
    }

    public CheckBoxPanel addCheckBox(Boolean propertyPath, FormElementSettings componentSettings) {
        return addDefaultRow(new CheckBoxPanel(getFormModel(), propertyPath, getFormSettings(), componentSettings));
    }

    public EmailTextFieldPanel addEmailTextField(String propertyPath, FormElementSettings componentSettings) {
        return addDefaultRow(new EmailTextFieldPanel(getFormModel(), propertyPath, getFormSettings(), componentSettings));
    }

    public PasswordTextFieldPanel addPasswordTextField(String propertyPath, FormElementSettings componentSettings) {
        return addDefaultRow(new PasswordTextFieldPanel(getFormModel(), propertyPath, getFormSettings(), componentSettings));
    }

    /**
     * also "form.setMultiPart(true);" and "form.setMaxSize(Bytes.megabytes(1));"
     */
    public <F> FilePickerPanel<F> addFilePicker(F propertyPath, FilePickerSettings componentSettings, FilePickerHook hook) {
        return addCustomRow(new FilePickerPanel<F>(getFormModel(), propertyPath, getFormSettings(), componentSettings, hook));
    }

    @SuppressWarnings("unchecked")
    public <F extends Serializable> DatePickerPanel<F> addDatePicker(F propertyPath, FormElementSettings componentSettings,
            Converter<F, Date> dateConverter) {
        return addDefaultRow(new DatePickerPanel<F>(getFormModel(), propertyPath, dateConverter, getFormSettings(), componentSettings));
    }

    public DatePickerPanel<Date> addDatePicker(Date propertyPath, FormElementSettings componentSettings) {
        return addDatePicker(propertyPath, componentSettings, (Converter<Date, Date>) null);
    }
}

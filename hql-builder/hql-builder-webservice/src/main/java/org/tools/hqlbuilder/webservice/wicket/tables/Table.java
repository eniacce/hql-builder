package org.tools.hqlbuilder.webservice.wicket.tables;

import static org.tools.hqlbuilder.webservice.wicket.WebHelper.name;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see https://www.packtpub.com/article/apache-wicket-displaying-data-using-datatable
 */
public class Table<T extends Serializable> extends DefaultDataTable<T, String> {
    private static final long serialVersionUID = -5074672726454953465L;

    private static final Logger logger = LoggerFactory.getLogger(Table.class);

    public static final String ID = "table";

    public static final String ACTIONS_ID = "table.actions";

    public Table(List<IColumn<T, String>> columns, final DataProvider<T> dataProvider) {
        super(ID, columns, dataProvider, dataProvider.getRowsPerPage());
        setOutputMarkupId(true);
    }

    public static <D> IColumn<D, String> newColumn(Component parent, Object argument) {
        return new PropertyColumn<D, String>(labelModel(parent, argument), name(argument), name(argument));
    }

    public static <D> IColumn<D, String> newEmailColumn(Component parent, Object argument) {
        return new EmailColumn<D>(labelModel(parent, argument), name(argument));
    }

    public static <D> IColumn<D, String> newTimeColumn(Component parent, Object argument) {
        return newDateOrTimeColumn(parent, argument, new DateConverter(true) {
            private static final long serialVersionUID = 2343896464273737921L;

            @Override
            protected DateTimeFormatter getFormat(Locale locale) {
                return DateTimeFormat.longTime().withLocale(locale);
            }

            @Override
            public String getDatePattern(Locale locale) {
                return getFormat(locale).toString();
            }
        });
    }

    public static <D> IColumn<D, String> newDateColumn(Component parent, Object argument) {
        return newDateOrTimeColumn(parent, argument, new DateConverter(true) {
            private static final long serialVersionUID = 7740123878863398108L;

            @Override
            protected DateTimeFormatter getFormat(Locale locale) {
                return DateTimeFormat.longDate().withLocale(locale);
            }

            @Override
            public String getDatePattern(Locale locale) {
                return getFormat(locale).toString();
            }
        });
    }

    public static <D> IColumn<D, String> newDateTimeColumn(Component parent, Object argument) {
        return newDateOrTimeColumn(parent, argument, new DateConverter(true) {
            private static final long serialVersionUID = 3387299335035913335L;

            @Override
            protected DateTimeFormatter getFormat(Locale locale) {
                return DateTimeFormat.longDateTime().withLocale(locale);
            }

            @Override
            public String getDatePattern(Locale locale) {
                return getFormat(locale).toString();
            }
        });
    }

    public static <D> IColumn<D, String> newDateOrTimeColumn(Component parent, Object argument, final DateConverter dateConverter) {
        return new PropertyColumn<D, String>(labelModel(parent, argument), name(argument), name(argument)) {
            private static final long serialVersionUID = -4327523236766929509L;

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void populateItem(Item<ICellPopulator<D>> item, String componentId, IModel<D> rowModel) {
                item.add(new DateLabel(componentId, (IModel) getDataModel(rowModel), dateConverter));
            }
        };
    }

    public static Model<String> labelModel(Component parent, Object argument) {
        String property = name(argument);
        return labelModel(parent, property);
    }

    public static Model<String> labelModel(Component parent, String property) {
        String label;
        try {
            label = parent.getString(property);
        } catch (MissingResourceException ex) {
            logger.error("no translation for " + property);
            label = "${" + property + "}";
        }
        return new Model<String>(label);
    }

    public static class EmailColumn<D> extends PropertyColumn<D, String> {
        private static final long serialVersionUID = 4348693361586305371L;

        public EmailColumn(IModel<String> displayModel, String sortProperty, String propertyExpression) {
            super(displayModel, sortProperty, propertyExpression);
        }

        public EmailColumn(IModel<String> displayModel, String propertyExpression) {
            super(displayModel, propertyExpression, propertyExpression);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void populateItem(Item<ICellPopulator<D>> item, String componentId, IModel<D> rowModel) {
            item.add(new LinkPanel(componentId, (IModel) getDataModel(rowModel)));
        }

        private class LinkPanel extends Panel {
            private static final long serialVersionUID = -9215479986488566139L;

            public LinkPanel(String id, final IModel<String> model) {
                super(id);
                add(new ExternalLink("link", new AbstractReadOnlyModel<String>() {
                    private static final long serialVersionUID = 11942951671862663L;

                    @Override
                    public String getObject() {
                        return "mailto:" + model.getObject();
                    }
                }, model));
            }
        }
    }

    public static <T extends Serializable> IColumn<T, String> getActionsColumn(Component parent, final DataProvider<T> deleter) {
        return new AbstractColumn<T, String>(labelModel(parent, ACTIONS_ID)) {
            private static final long serialVersionUID = 3528122666825952597L;

            @Override
            @SuppressWarnings({ "rawtypes", "unchecked" })
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                T object = ((T) rowModel.getObject());
                cellItem.add(new ActionsPanel<T>(componentId, object) {
                    private static final long serialVersionUID = -601177222898722169L;

                    @Override
                    public void onDelete(AjaxRequestTarget target, T o) {
                        deleter.delete(o);
                        deleter.updateUI(target);
                        info("Row deleted");
                    }
                });
            }
        };
    }

    public static abstract class ActionsPanel<T extends Serializable> extends Panel {
        private static final long serialVersionUID = 7542462926954803874L;

        public static final String ACTIONS_DELETE_ID = "delete";

        public static final String ACTIONS_EDIT_ID = "edit";

        public static final String ACTIONS_FORM_ID = "form";

        public ActionsPanel(String id, final T object) {
            super(id);
            setOutputMarkupId(true);
            final Form<T> form = new Form<T>(ACTIONS_FORM_ID);
            AjaxSubmitLink editLink = new AjaxSubmitLink(ACTIONS_EDIT_ID) {
                private static final long serialVersionUID = 2542930376888979931L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
                    System.out.println("EDIT"); // TODO
                }
            };
            form.add(editLink);
            AjaxSubmitLink deleteLink = new AjaxSubmitLink(ACTIONS_DELETE_ID) {
                private static final long serialVersionUID = 2542930376888979931L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
                    onDelete(target, object);
                }
            };
            form.add(deleteLink);
            add(form);
        }

        /**
         * add table to AjaxRequestTarget and make service call to delete object
         */
        public abstract void onDelete(AjaxRequestTarget target, T object);
    }

    public static interface DataProvider<T extends Serializable> extends ISortableDataProvider<T, String> {
        /** service call to delete object */
        public void delete(T object);

        /** target.add( feedback ); target.add( table ); */
        public void updateUI(AjaxRequestTarget target);

        /** default 10 */
        public int getRowsPerPage();
    }

    public static abstract class DefaultDataProvider<T extends Serializable> implements DataProvider<T> {
        private static final long serialVersionUID = 2267212289729092246L;

        protected SingleSortState<String> sortState = new SingleSortState<String>();

        protected int rowsPerPage = 10;

        @Override
        public IModel<T> model(T object) {
            return Model.of(object);
        }

        @Override
        public void detach() {
            //
        }

        @Override
        public ISortState<String> getSortState() {
            return sortState;
        }

        @Override
        public int getRowsPerPage() {
            return rowsPerPage;
        }

        public void setRowsPerPage(int rowsPerPage) {
            this.rowsPerPage = rowsPerPage;
        }
    }
}

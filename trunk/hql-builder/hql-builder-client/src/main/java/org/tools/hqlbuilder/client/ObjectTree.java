package org.tools.hqlbuilder.client;

import java.awt.BorderLayout;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.swingeasy.ETree;
import org.swingeasy.ETreeConfig;
import org.swingeasy.ETreeNode;

public class ObjectTree extends JFrame {
    /** serialVersionUID */
    private static final long serialVersionUID = 3880395325775694814L;

    private final JPanel propertypanel = new JPanel(new BorderLayout());

    public ObjectTree(Object bean, final boolean editable) {
        bean = initialize(bean);
        TreeNode rootNode = new TreeNode(bean);
        final ETree<Object> tree = new ETree<Object>(new ETreeConfig(), rootNode);
        tree.setEditable(false);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), propertypanel);
        getContentPane().add(split, BorderLayout.CENTER);
        setTitle(String.valueOf(bean));
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                try {
                    Object data = tree.getSelectionModel().getSelectionPath().getLastPathComponent();
                    propertypanel.setVisible(false);
                    propertypanel.removeAll();
                    if (data != null) {
                        data = TreeNode.class.cast(data).bean;
                        // if (hqlBuilderHelper.accept(data.getClass())) {
                        data = initialize(data);
                        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
                        for (PropertyDescriptor descriptor : propertyUtilsBean.getPropertyDescriptors(data)) {
                            try {
                                propertyUtilsBean.getProperty(data, descriptor.getName().toString());
                            } catch (Exception ex) {
                                //
                            }
                        }
                        JComponent propertyFrame = DefaultHqlBuilderHelper.getPropertyFrame(data, editable);
                        DefaultHqlBuilderHelper.font(propertyFrame, 12);
                        propertypanel.add(propertyFrame, BorderLayout.CENTER);
                        // }
                    }
                    propertypanel.setVisible(true);
                } catch (NullPointerException ex) {
                    //
                }
            }
        });
        split.setDividerLocation(500);
        DefaultHqlBuilderHelper.font(tree, 12);
        setVisible(true);
    }

    private class TreeNode extends ETreeNode<Object> {
        private static final long serialVersionUID = 4389106694997553842L;

        private Object bean;

        private String name;

        private String toString;

        private TreeNode(Object bean) {
            super(bean);
            this.bean = bean;
            String classname = bean.getClass().getSimpleName();
            try {
                toString = classname + ":" + bean;
            } catch (Exception ex) {
                ex.printStackTrace();
                if (name != null) {
                    toString = classname + "." + name + "?";
                } else {
                    toString = classname + "?";
                }
            }
        }

        private TreeNode(String name, Object bean) {
            super(name);
            this.bean = bean;
            this.name = name;
            toString = "[" + name;
        }

        /**
         * 
         * @see org.swingeasy.ETreeNode#getStringValue()
         */
        @Override
        public String getStringValue() {
            return toString();
        }

        /**
         * 
         * @see org.swingeasy.ETreeNode#toString()
         */
        @Override
        public String toString() {
            return toString;
        }

        /**
         * 
         * @see org.swingeasy.ETreeNode#initChildren(java.util.List)
         */
        @Override
        protected void initChildren(List<ETreeNode<Object>> list) {
            try {
                PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
                Object _bean = initialize(bean);
                if (name != null) {
                    for (Object sub : Collection.class.cast(propertyUtilsBean.getProperty(_bean, name))) {
                        sub = initialize(sub);
                        list.add(new TreeNode(sub));
                    }
                } else {
                    for (PropertyDescriptor descriptor : propertyUtilsBean.getPropertyDescriptors(_bean)) {
                        try {
                            Object _name = descriptor.getName();
                            Object _value = propertyUtilsBean.getProperty(_bean, _name.toString());
                            if (_value != null) {
                                if (_value instanceof Collection) {
                                    list.add(new TreeNode(_name.toString(), _bean));
                                } else /* if (hqlBuilderHelper.accept(_value.getClass())) */{
                                    _value = initialize(_value);
                                    TreeNode e = new TreeNode(_value);
                                    list.add(e);
                                }
                            }
                        } catch (Exception ex) {
                            //
                        }

                    }
                }
            } catch (Exception ex) {
                //
            }
        }
    }

    private Object initialize(Object o) {
        // o = sessionFactory.openSession().merge(o);
        // if (o instanceof HibernateProxy) {
        // return ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation();
        // }
        // Hibernate.initialize(o);
        return o;
    }
}

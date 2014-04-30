package org.tools.hqlbuilder.client;

import groovy.util.Eval;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.xml.parsers.DocumentBuilderFactory;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.LoggerFactory;
import org.swingeasy.CustomizableOptionPane;
import org.swingeasy.DocumentKeyListener;
import org.swingeasy.EButton;
import org.swingeasy.ECheckBox;
import org.swingeasy.EComponentPopupMenu;
import org.swingeasy.EFontChooser;
import org.swingeasy.ELabel;
import org.swingeasy.ELabeledTextFieldButtonComponent;
import org.swingeasy.EList;
import org.swingeasy.EListConfig;
import org.swingeasy.EListRecord;
import org.swingeasy.ETable;
import org.swingeasy.ETableConfig;
import org.swingeasy.ETableHeaders;
import org.swingeasy.ETableRecord;
import org.swingeasy.ETableRecordCollection;
import org.swingeasy.ETextArea;
import org.swingeasy.ETextAreaBorderHighlightPainter;
import org.swingeasy.ETextAreaConfig;
import org.swingeasy.ETextAreaFillHighlightPainter;
import org.swingeasy.ETextField;
import org.swingeasy.ETextFieldConfig;
import org.swingeasy.EToolBarButtonCustomizer;
import org.swingeasy.EventHelper;
import org.swingeasy.EventModifier;
import org.swingeasy.ListOptionPaneCustomizer;
import org.swingeasy.MessageType;
import org.swingeasy.ObjectWrapper;
import org.swingeasy.OptionType;
import org.swingeasy.ProgressGlassPane;
import org.swingeasy.ResultType;
import org.swingeasy.UIUtils;
import org.swingeasy.system.SystemSettings;
import org.tools.hqlbuilder.client.HqlWizard.HqlWizardListener;
import org.tools.hqlbuilder.common.ExecutionResult;
import org.tools.hqlbuilder.common.HibernateWebResolver;
import org.tools.hqlbuilder.common.QueryParameter;
import org.tools.hqlbuilder.common.exceptions.ServiceException;
import org.tools.hqlbuilder.common.exceptions.SqlException;
import org.tools.hqlbuilder.common.exceptions.SyntaxException;
import org.tools.hqlbuilder.common.exceptions.SyntaxException.SyntaxExceptionType;

/**
 * @author Jurgen
 */
public class HqlBuilderFrame implements HqlBuilderFrameConstants {
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private static final String PERSISTENT_LOCALE = "locale";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(HqlBuilderFrame.class);

    /** do not remove */
    ca.odell.glazedlists.swing.EventSelectionModel<?> wtf;

    private static Preferences preferences;

    private static final File USER_HOME_DIR = new File(System.getProperty("user.home"));

    private static final File PROGRAM_DIR = new File(USER_HOME_DIR, "hqlbuilder");

    private static final File FAVORITES_DIR = new File(PROGRAM_DIR, "favorites");

    @SuppressWarnings("unused")
    private org.swingeasy.UIExceptionHandler uiExceptionHandler = org.swingeasy.UIExceptionHandler.getInstance();

    private final JFrame frame = new JFrame();

    private final JPanel hql_sql_tabs_panel = new JPanel(new BorderLayout());

    private final JTabbedPane hql_sql_tabs = new JTabbedPane();

    private final ELabel resultsInfo;

    private final JPanel propertypanel = new JPanel(new BorderLayout());

    private final ETextField parameterBuilder;

    private final ETextField parameterName;

    private final ETextField parameterValue;

    private final ETextArea hql;

    private final ETextArea sql;

    // not EDT safe
    private final EList<QueryParameter> parametersUnsafe;

    // EDT safe
    private final EList<QueryParameter> parametersEDT;

    // not EDT safe
    private final ETable<List<Object>> resultsUnsafe;

    // EDT safe
    private final ETable<List<Object>> resultsEDT;

    // results
    //
    private final HqlBuilderAction hibernatePropertiesAction;

    private final HqlBuilderAction objectTreeAction;

    private final HqlBuilderAction deleteObjectAction;

    private final HqlBuilderAction copyCellAction;

    private final HqlBuilderAction executeScriptOnColumnAction;

    // parameters
    //
    private final HqlBuilderAction downAction;

    private final HqlBuilderAction removeAction;

    private final HqlBuilderAction saveAction;

    private final HqlBuilderAction setNullAction;

    private final HqlBuilderAction toTextAction;

    private final HqlBuilderAction upAction;

    private final HqlBuilderAction addParameterAction;

    private final HqlBuilderAction importParametersAction;

    // hql
    //
    private final HqlBuilderAction wizardAction;

    private final HqlBuilderAction clearAction;

    private final HqlBuilderAction findParametersAction;

    private final HqlBuilderAction favoritesAction;

    private final HqlBuilderAction addToFavoritesAction;

    private final HqlBuilderAction startQueryAction;

    private final HqlBuilderAction formatAction;

    private final HqlBuilderAction namedQueryAction;

    private final HqlBuilderAction clipboardExportAction;

    private final HqlBuilderAction clipboardImportAction;

    private final HqlBuilderAction helpInsertAction;

    private final HqlBuilderAction remarkToggleAction;

    private final HqlBuilderAction deleteInvertedSelectionAction;

    // existing
    //
    private final HqlBuilderAction helpAction = new HqlBuilderAction(null, this, HELP, true, HELP, "help16.png", HELP, HELP, true, 'h', "F1");

    private final HqlBuilderAction exitAction = new HqlBuilderAction(null, this, EXIT, true, EXIT, "sc_quit.png", EXIT, EXIT, true, 'x', "alt X");

    // not accelerated
    //
    private final HqlBuilderAction helpHibernateAction = new HqlBuilderAction(null, this, HIBERNATE_DOCUMENTATION, true, HIBERNATE_DOCUMENTATION,
            "help16.png", HIBERNATE_DOCUMENTATION, HIBERNATE_DOCUMENTATION, true, null, null);

    private final HqlBuilderAction helpHqlAction = new HqlBuilderAction(null, this, HQL_DOCUMENTATION, true, HQL_DOCUMENTATION, "help16.png",
            HQL_DOCUMENTATION, HQL_DOCUMENTATION, true, null, null);

    private final HqlBuilderAction luceneQuerySyntaxAction = new HqlBuilderAction(null, this, LUCENE_QUERY_SYNTAX, true, LUCENE_QUERY_SYNTAX,
            "help16.png", LUCENE_QUERY_SYNTAX, LUCENE_QUERY_SYNTAX, true, null, null);

    private final HqlBuilderAction forceExitAction = new HqlBuilderAction(null, this, FORCE_EXIT, true, FORCE_EXIT, "exit.png", FORCE_EXIT,
            FORCE_EXIT, true, 't', null);

    private final HqlBuilderAction removeJoinsAction = new HqlBuilderAction(null, this, IMMEDIATE_QUERY, true, REMOVE_JOINS, null, REMOVE_JOINS,
            REMOVE_JOINS, true, null, null, PERSISTENT_ID);

    private final HqlBuilderAction formatLinesAction = new HqlBuilderAction(null, this, IMMEDIATE_QUERY, true, FORMAT_LINES, null, FORMAT_LINES,
            FORMAT_LINES, true, null, null, PERSISTENT_ID);

    private final HqlBuilderAction replacePropertiesAction = new HqlBuilderAction(null, this, IMMEDIATE_QUERY, true, REPLACE_PROPERTIES, null,
            REPLACE_PROPERTIES, REPLACE_PROPERTIES, true, null, null, PERSISTENT_ID);

    private final HqlBuilderAction resizeColumnsAction = new HqlBuilderAction(null, this, RESIZE_COLUMNS, true, RESIZE_COLUMNS, null, RESIZE_COLUMNS,
            RESIZE_COLUMNS, true, null, null, PERSISTENT_ID);

    private final HqlBuilderAction formatSqlAction = new HqlBuilderAction(null, this, FORMAT_SQL, true, FORMAT_SQL, null, FORMAT_SQL, FORMAT_SQL,
            true, null, null, PERSISTENT_ID);

    private final HqlBuilderAction maximumNumberOfResultsAction = new HqlBuilderAction(null, this, MAXIMUM_NUMBER_OF_RESULTS, true,
            MAXIMUM_NUMBER_OF_RESULTS, "scr.png", MAXIMUM_NUMBER_OF_RESULTS, MAXIMUM_NUMBER_OF_RESULTS, true, null, null, PERSISTENT_ID,
            Integer.class, 100);

    private HqlBuilderAction fontAction;

    private final HqlBuilderAction systrayAction = new HqlBuilderAction(null, this, null, true, SYSTEM_TRAY, null, SYSTEM_TRAY, SYSTEM_TRAY, true,
            null, null, PERSISTENT_ID);

    private final HqlBuilderAction canExecuteSelectionAction = new HqlBuilderAction(null, this, null, true, CAN_EXECUTE_SELECTION, null,
            CAN_EXECUTE_SELECTION, CAN_EXECUTE_SELECTION, true, null, null, PERSISTENT_ID);

    private final HqlBuilderAction hiliAction = new HqlBuilderAction(null, this, null, true, HIGHLIGHT_SYNTAX, null, HIGHLIGHT_SYNTAX,
            HIGHLIGHT_SYNTAX, false, null, null, PERSISTENT_ID);

    private final HqlBuilderAction hiliColorAction = new HqlBuilderAction(null, this, HIGHLIGHT_COLOR, true, HIGHLIGHT_COLOR, null, HIGHLIGHT_COLOR,
            HIGHLIGHT_COLOR, false, null, null, PERSISTENT_ID, Color.class, null);

    private final HqlBuilderAction searchColorAction = new HqlBuilderAction(null, this, SEARCH_COLOR, true, SEARCH_COLOR, null, SEARCH_COLOR,
            SEARCH_COLOR, false, null, null, PERSISTENT_ID, Color.class, null);

    private final HqlBuilderAction alwaysOnTopAction = new HqlBuilderAction(null, this, ALWAYS_ON_TOP, true, ALWAYS_ON_TOP, null, ALWAYS_ON_TOP,
            ALWAYS_ON_TOP, false, null, null, PERSISTENT_ID);

    private final HqlBuilderAction editableResultsAction = new HqlBuilderAction(null, this, null, true, EDITABLE_RESULTS, null, EDITABLE_RESULTS,
            EDITABLE_RESULTS, false, null, null, PERSISTENT_ID);

    private final HqlBuilderAction switchLayoutAction = new HqlBuilderAction(null, this, SWITCH_LAYOUT, true, SWITCH_LAYOUT, "layout_content.png",
            SWITCH_LAYOUT, SWITCH_LAYOUT, false, 'w', null, PERSISTENT_ID);

    //

    private final ELabel maxResults;

    private final LinkedList<QueryFavorite> favorites = new LinkedList<QueryFavorite>();

    private final JComponent values = ClientUtils.getPropertyFrame(new Object(), false);

    /** aliases query */
    private Map<String, String> aliases = new HashMap<String, String>();

    /** selected query parameter */
    private QueryParameter selectedQueryParameter = new QueryParameter();

    /** scripts being executed on column */
    private Map<Integer, String> scripts = new HashMap<Integer, String>();

    /** record count query */
    private int recordCount = 0;

    private final JPanel resultPanel = new JPanel(new BorderLayout());

    private final JPanel parameterspanel = new JPanel(new MigLayout("wrap 3", "[]rel[grow]rel[]", "[][][][shrink][shrink][shrink][shrink][grow]"));

    private final ETextField importParametersFromTextF = new ETextField(new ETextFieldConfig());

    private final JPanel normalContentPane = new JPanel(new BorderLayout());

    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private final JMenu formatSqlOptionsMenu = new JMenu(HqlResourceBundle.getMessage("format sql options"));

    private HibernateWebResolver hibernateWebResolver;

    private EList<String> insertPropertyHelper;

    private JPopupMenu insertClassHelper;

    private EList<String> insertHelperList;

    private JPopupMenu insertHelper;

    private JSplitPane split0;

    private JSplitPane split1;

    private JSplitPane split2;

    private ProgressGlassPane glass = null;

    private ETextAreaBorderHighlightPainter syntaxHighlight = new ETextAreaBorderHighlightPainter(getHighlightColor());

    private ETextAreaBorderHighlightPainter bracesHighlight = new ETextAreaBorderHighlightPainter(getHighlightColor());

    private ETextAreaBorderHighlightPainter syntaxErrorsHighlight = new ETextAreaBorderHighlightPainter(Color.RED);

    private TrayIcon trayIcon;

    private HqlServiceClient hqlService;

    private boolean ingoreParameterListSelectionListener = false;

    private HqlBuilderFrame() {
        // needs to be first to init font
        fontAction = new HqlBuilderAction(null, this, FONT, true, FONT, "font.png", FONT, FONT, true, null, null, PERSISTENT_ID, Font.class,
                ClientUtils.getDefaultFont());
        fontAction.setWarnRestart(true);

        resultsInfo = font(new ELabel(""), null);
        parameterBuilder = font(new ETextField(new ETextFieldConfig()), null);
        parameterName = font(new ETextField(new ETextFieldConfig()), null);
        parameterValue = font(new ETextField(new ETextFieldConfig(false)), null);
        hql = font(new ETextArea(new ETextAreaConfig()), null);
        EComponentPopupMenu.debug(hql);
        sql = font(new ETextArea(new ETextAreaConfig(false)), null);
        maxResults = font(new ELabel(" / " + maximumNumberOfResultsAction.getValue()), null);
        parametersUnsafe = font(new EList<QueryParameter>(new EListConfig()), null);
        parametersEDT = parametersUnsafe.stsi();
        resultsUnsafe = font(new ETable<List<Object>>(new ETableConfig(true)), null);
        resultsEDT = resultsUnsafe.stsi();
        hibernatePropertiesAction = new HqlBuilderAction(resultsUnsafe, this, HIBERNATE_PROPERTIES, true, HIBERNATE_PROPERTIES,
                "page_white_stack.png", HIBERNATE_PROPERTIES, HIBERNATE_PROPERTIES, true, null, "shift alt F8");
        objectTreeAction = new HqlBuilderAction(resultsUnsafe, this, OBJECT_TREE, true, OBJECT_TREE, "application_side_tree.png", OBJECT_TREE,
                OBJECT_TREE, true, null, "shift alt F9");
        deleteObjectAction = new HqlBuilderAction(resultsUnsafe, this, DELETE_OBJECT, true, DELETE_OBJECT, "bin_empty.png", DELETE_OBJECT,
                DELETE_OBJECT, true, null, "shift alt F10");
        copyCellAction = new HqlBuilderAction(resultsUnsafe, this, COPY_SELECTED_CELL, true, COPY_SELECTED_CELL, "cell_layout.png",
                COPY_SELECTED_CELL, COPY_SELECTED_CELL, true, null, "shift alt F11");
        executeScriptOnColumnAction = new HqlBuilderAction(resultsUnsafe, this, EXECUTE_SCRIPT_ON_COLUMN, true, EXECUTE_SCRIPT_ON_COLUMN,
                "groovy.png", EXECUTE_SCRIPT_ON_COLUMN, EXECUTE_SCRIPT_ON_COLUMN, true, null, "shift alt F12");
        downAction = new HqlBuilderAction(parametersUnsafe, this, DOWN, true, DOWN, "bullet_arrow_down.png", DOWN, DOWN, false, null, null);
        removeAction = new HqlBuilderAction(parametersUnsafe, this, REMOVE, true, REMOVE, "bin_empty.png", REMOVE, REMOVE, false, null, null);
        saveAction = new HqlBuilderAction(parametersUnsafe, this, SAVE, true, SAVE, "disk.png", SAVE, SAVE, false, null, null);
        setNullAction = new HqlBuilderAction(parametersUnsafe, this, SET_NULL, true, SET_NULL, "page (2).png", SET_NULL, SET_NULL, false, null, null);
        toTextAction = new HqlBuilderAction(parametersUnsafe, this, TO_TEXT, true, TO_TEXT, "textfield.png", TO_TEXT, TO_TEXT, false, null, null);
        upAction = new HqlBuilderAction(parametersUnsafe, this, UP, true, UP, "bullet_arrow_up.png", UP, UP, false, null, null);
        addParameterAction = new HqlBuilderAction(parametersUnsafe, this, ADD_PARAMETER, true, ADD_PARAMETER, "add.png", ADD_PARAMETER,
                ADD_PARAMETER, false, null, null);
        importParametersAction = new HqlBuilderAction(parametersUnsafe, this, IMPORT_PARAMETERS, true, IMPORT_PARAMETERS, "cog.png",
                IMPORT_PARAMETERS, IMPORT_PARAMETERS, false, null, null);
        wizardAction = new HqlBuilderAction(hql, this, WIZARD, true, WIZARD, "wizard.png", WIZARD, WIZARD, true, null, "alt F1");
        clearAction = new HqlBuilderAction(hql, this, CLEAR, true, CLEAR, "bin_empty.png", CLEAR, CLEAR, true, null, "alt F2");
        findParametersAction = new HqlBuilderAction(hql, this, FIND_PARAMETERS, true, FIND_PARAMETERS, "book_next.png", FIND_PARAMETERS,
                FIND_PARAMETERS, true, null, "alt F3");
        favoritesAction = new HqlBuilderAction(hql, this, FAVORITES, true, FAVORITES, "favb16.png", FAVORITES, FAVORITES, true, null, "alt F4");
        addToFavoritesAction = new HqlBuilderAction(hql, this, ADD_TO_FAVORITES, true, ADD_TO_FAVORITES, "favbadd16.png", ADD_TO_FAVORITES,
                ADD_TO_FAVORITES, true, null, "alt F5");
        // alt F6 not taken
        // alt F7 not taken
        // alt F8 not taken
        formatAction = new HqlBuilderAction(hql, this, FORMAT, true, FORMAT, "text_align_justify.png", FORMAT, FORMAT, true, null, "alt F9");
        namedQueryAction = new HqlBuilderAction(hql, this, LOAD_NAMED_QUERY, true, LOAD_NAMED_QUERY, "package_go.png", LOAD_NAMED_QUERY,
                LOAD_NAMED_QUERY, true, null, "alt F10");
        clipboardExportAction = new HqlBuilderAction(hql, this, EXPORT_COPY_HQL_AS_JAVA_TO_CLIPBOARD, true, EXPORT_COPY_HQL_AS_JAVA_TO_CLIPBOARD,
                "sc_arrowshapes.striped-right-arrow.png", EXPORT_COPY_HQL_AS_JAVA_TO_CLIPBOARD, EXPORT_COPY_HQL_AS_JAVA_TO_CLIPBOARD, true, null,
                "alt F11");
        clipboardImportAction = new HqlBuilderAction(hql, this, IMPORT_PASTE_HQL_AS_JAVA_FROM_CLIPBOARD, true,
                IMPORT_PASTE_HQL_AS_JAVA_FROM_CLIPBOARD, "sc_arrowshapes.striped-left-arrow.png", IMPORT_PASTE_HQL_AS_JAVA_FROM_CLIPBOARD,
                IMPORT_PASTE_HQL_AS_JAVA_FROM_CLIPBOARD, true, null, "alt F12");
        helpInsertAction = new HqlBuilderAction(hql, this, HELP_INSERT, true, HELP_INSERT, "attach.png", HELP_INSERT, HELP_INSERT, true, null,
                "ctrl shift SPACE");
        remarkToggleAction = new HqlBuilderAction(hql, this, REMARK_TOGGLE, true, REMARK_TOGGLE, "text_indent.png", REMARK_TOGGLE, REMARK_TOGGLE,
                true, null, "ctrl shift SLASH");
        startQueryAction = new HqlBuilderAction(hql, this, START_QUERY, true, START_QUERY, "control_play_blue.png", START_QUERY, START_QUERY, true,
                null, "ctrl ENTER");
        deleteInvertedSelectionAction = new HqlBuilderAction(hql, this, DELETE_INVERTED_SELECTION, true, DELETE_INVERTED_SELECTION,
                "table_row_delete.png", DELETE_INVERTED_SELECTION, DELETE_INVERTED_SELECTION, true, null, "ctrl DELETE");
    }

    protected void down() {
        parametersEDT.moveSelectedDown();
    }

    protected void up() {
        parametersEDT.moveSelectedUp();
    }

    protected void clear() {
        aliases.clear();
        resultsInfo.setText("");
        parametersEDT.removeAllRecords();
        clearParameter();
        hql.setText("");
        sql.setText("");
        scripts.clear();
        clearResults();
        propertypanel.add(ClientUtils.getPropertyFrame(new Object(), false), BorderLayout.CENTER);
        propertypanel.revalidate();
        hql_sql_tabs.setForegroundAt(1, Color.gray);
    }

    private void clearParameter() {
        selectedQueryParameter = new QueryParameter();
        parameterBuilder.setText("");
        parameterName.setText("");
        parameterValue.setText("");
    }

    private void clearResults() {
        recordCount = 0;
        resultsEDT.setHeaders(new ETableHeaders<List<Object>>());
        resultsEDT.removeAllRecords();
    }

    protected void favorites() {
        try {
            FavoritesDialog dialog = new FavoritesDialog(frame, favorites);
            dialog.setTitle(HqlResourceBundle.getMessage("favorites.long"));
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            QueryFavorite selection = dialog.getSelection();

            if (selection != null) {
                importFromFavorites(selection);
            }
        } catch (Exception ex) {
            log(ex);
        }
    }

    private void log(Object message) {
        ClientUtils.log(message);
    }

    private void importFromFavorites(QueryFavorite selection) {
        clear();
        importFromFavoritesNoQ(selection);
    }

    private void importFromFavoritesNoQ(QueryFavorite selection) {
        hql.setText(selection.getFull());

        if (selection.getParameters() != null) {
            for (QueryParameter p : selection.getParameters()) {
                parametersEDT.addRecord(new EListRecord<QueryParameter>(p));
            }
        }
    }

    private void compile(final String text) {
        log("compiling");
        selectedQueryParameter.setValue(null);
        try {
            selectedQueryParameter.setValue(GroovyCompiler.eval(text));
        } catch (Exception ex2) {
            log(ex2);
        }
        log("compiled: " + selectedQueryParameter);
        parameterValue.setText(selectedQueryParameter.toString());
    }

    private JPopupMenu getInsertHelperProperties() {
        if (insertClassHelper == null) {
            insertClassHelper = new JPopupMenu();

            insertPropertyHelper = new EList<String>(new EListConfig());

            JScrollPane scroll = new JScrollPane(insertPropertyHelper);
            insertClassHelper.add(scroll);

            insertClassHelper.setPreferredSize(new Dimension(300, 150));
            insertClassHelper.setSize(new Dimension(300, 150));
            insertClassHelper.setMinimumSize(new Dimension(300, 150));

            insertPropertyHelper.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (EventHelper.isMouse1x1(e)) {
                            insertPropertyHelp();
                        }
                    } catch (Exception ex) {
                        logger.error("$MouseAdapter.mouseClicked(MouseEvent)", ex); //$NON-NLS-1$
                    }
                }
            });
        }

        return insertClassHelper;
    }

    private void insertPropertyHelp() throws Exception {
        String prop = insertPropertyHelper.getSelectedRecord().get();
        hql.getDocument().insertString(hql.getCaretPosition(), prop, null);
        insertClassHelper.setVisible(false);
    }

    private JPopupMenu getInsertHelperClasses() {
        if (insertHelper == null) {
            insertHelper = new JPopupMenu();

            EList<String> list = new EList<String>(new EListConfig());
            insertHelperList = font(list, null);

            JScrollPane scroll = new JScrollPane(insertHelperList);
            insertHelper.add(scroll);

            TreeSet<String> options = new TreeSet<String>();

            for (HibernateWebResolver.ClassNode node : getHibernateWebResolver().getClasses()) {
                String clazz = node.getId();
                String option = clazz.substring(clazz.lastIndexOf('.') + 1) + " (" + clazz.substring(0, clazz.lastIndexOf('.')) + ")";
                options.add(option);
            }

            for (String option : options) {
                list.addRecord(new EListRecord<String>(option));
            }

            Dimension size = new Dimension(500, 200);
            insertHelper.setPreferredSize(size);
            insertHelper.setSize(size);
            insertHelper.setMinimumSize(size);

            insertHelperList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (EventHelper.isMouse1x1(e)) {
                            insertHelp();
                        }
                    } catch (Exception ex) {
                        logger.error("$MouseAdapter.mouseClicked(MouseEvent)", ex); //$NON-NLS-1$
                    }
                }
            });
        }

        return insertHelper;
    }

    private void insertHelp() throws Exception {
        String clazz = insertHelperList.getSelectedRecord().get();
        clazz = clazz.substring(0, clazz.indexOf(" ")).trim();
        hql.getDocument().insertString(hql.getCaretPosition(), clazz, null);
        insertHelper.setVisible(false);
    }

    private void helpInsertClass() {
        getInsertHelperClasses().show(hql, (int) hql.getCaret().getMagicCaretPosition().getX(), (int) hql.getCaret().getMagicCaretPosition().getY());
        insertHelperList.grabFocus();
    }

    private void helpInsertProperty(String before) {
        int pos = Math.max(0, Math.max(before.lastIndexOf(getNewline()), before.lastIndexOf(' ')));
        before = before.substring(pos + 1, before.length() - 1);
        String[] parts = before.split("\\Q.\\E");
        Object key = aliases.get(parts[0]);

        List<String> propertyNames = hqlService.getPropertyNames(key, parts);

        JPopupMenu insertHelper2local = getInsertHelperProperties();

        insertPropertyHelper.removeAllRecords();

        for (String prop : new TreeSet<String>(propertyNames)) {
            if (!prop.startsWith("_")) {
                insertPropertyHelper.addRecord(new EListRecord<String>(prop));
            }
        }

        insertHelper2local.show(hql, (int) hql.getCaret().getMagicCaretPosition().getX(), (int) hql.getCaret().getMagicCaretPosition().getY());
        insertPropertyHelper.grabFocus();
    }

    protected String getNewline() {
        return hqlService.getNewline();
    }

    private void helpInsert() {
        String before;
        try {
            before = getHqlText().substring(0, hql.getCaret().getDot());
        } catch (StringIndexOutOfBoundsException ex) {
            before = getHqlText().substring(0, hql.getCaret().getDot() - 1) + " ";
        }
        if (before.toLowerCase().endsWith("from ")) {
            helpInsertClass();
        } else if (before.endsWith(".")) {
            helpInsertProperty(before);
        }
    }

    private String remarkToggle(String hqltext, int selectionStart, int selectionEnd) {
        if (selectionStart != 0) {
            int lastEnter = hqltext.lastIndexOf(getNewline(), selectionStart);

            if (lastEnter == -1) {
                selectionStart = 0;
            } else {
                selectionStart = lastEnter;
            }
        }

        if (selectionEnd != hqltext.length()) {
            int nextEnter = hqltext.indexOf(getNewline(), selectionEnd);

            if (nextEnter == -1) {
                selectionEnd = hqltext.length();
            } else {
                selectionEnd = nextEnter;
            }
        }

        String voor = hqltext.substring(0, selectionStart);
        String geselecteerd = hqltext.substring(selectionStart, selectionEnd);
        String na = hqltext.substring(selectionEnd);

        StringBuilder sb = new StringBuilder();

        if (geselecteerd.contains("--") || geselecteerd.contains("//")) {
            sb.append(voor);
            sb.append(geselecteerd.replaceAll("--", "").replaceAll("//", ""));
            sb.append(na);
        } else {
            if (selectionStart == 0) {
                sb.append("--");
            } else {
                sb.append(voor);
            }

            String[] lijnen = geselecteerd.split(getNewline());

            for (int i = 0; i < (lijnen.length - 1); i++) {
                sb.append(lijnen[i]).append(getNewline()).append("--");
            }

            sb.append(lijnen[lijnen.length - 1]);

            sb.append(na);
        }

        return sb.toString();
    }

    /**
     * start app
     */
    public static void start(@SuppressWarnings("unused") String[] args, HqlServiceClientLoader serviceLoader) {
        try {
            preferences = Preferences.userRoot().node(HqlBuilderFrame.PERSISTENT_ID);
            String lang = preferences.get(PERSISTENT_LOCALE, SystemSettings.getCurrentLocale().getLanguage());
            SystemSettings.setCurrentLocale(new Locale(lang));

            SplashHelper.setup();
            SplashHelper.step();

            // problem in "all in one" setup
            // sending 'wrong' locale to Oracle gives exception
            // Oracle will now give English exceptions
            SystemSettings.setCurrentLocale(Locale.UK);
            HqlServiceClient service = serviceLoader.getHqlServiceClient();
            // now load users locale
            SystemSettings.setCurrentLocale(new Locale(lang));

            SplashHelper.update(service.getConnectionInfo());
            SplashHelper.step();

            if (!PROGRAM_DIR.exists()) {
                PROGRAM_DIR.mkdirs();
            }
            if (!FAVORITES_DIR.exists()) {
                FAVORITES_DIR.mkdirs();
            }

            // zet look en feel gelijk aan default voor OS
            UIUtils.systemLookAndFeel();

            SplashHelper.step();

            {
                // dtd validatie uitschakelen
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                // factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                // unhandled exceptions toch opvangen
                UIUtils.registerDefaultUncaughtExceptionHandler();

                // tooltip cfg: sneller en langer tonen
                UIUtils.setLongerTooltips();
            }

            Eval.me("new Integer(0)"); // warm up Groovy

            SplashHelper.step();

            // maak frame en start start
            final HqlBuilderFrame hqlBuilder = new HqlBuilderFrame();
            hqlBuilder.hqlService = service;

            SplashHelper.step();

            hqlBuilder.startPre();

            SplashHelper.step();

            hqlBuilder.start();

            hqlBuilder.hql.grabFocus();

            // log connectie
            ClientUtils.log(hqlBuilder.hqlService.getConnectionInfo());

            SplashHelper.step();
            SplashHelper.end();

            // wanneer system tray wordt ondersteund, gebruikt deze dan (configureerbaar)
            if (SystemTray.isSupported()) {
                ActionListener listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hqlBuilder.switchVisibility();
                    }
                };
                hqlBuilder.trayIcon = new TrayIcon(hqlBuilder.frame.getIconImage(), hqlBuilder.frame.getTitle());
                PopupMenu popupmenu = new PopupMenu(hqlBuilder.frame.getTitle());
                {
                    MenuItem switchvisibility = new MenuItem(HqlResourceBundle.getMessage("show"));
                    switchvisibility.addActionListener(listener);
                    popupmenu.add(switchvisibility);
                }
                {

                    MenuItem trayswitch = new MenuItem(HqlResourceBundle.getMessage("disable systemtray"));
                    trayswitch.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            boolean newvalue = !hqlBuilder.systrayAction.isSelected();
                            hqlBuilder.systrayAction.setSelected(newvalue);
                            if (newvalue && !hqlBuilder.frame.isVisible()) {
                                hqlBuilder.switchVisibility();
                            }
                        }
                    });
                    popupmenu.add(trayswitch);
                }
                {
                    MenuItem exit = new MenuItem(hqlBuilder.exitAction.getName());
                    exit.addActionListener(hqlBuilder.exitAction);
                    popupmenu.add(exit);
                }
                hqlBuilder.trayIcon.setPopupMenu(popupmenu);
                hqlBuilder.trayIcon.addActionListener(listener);
                hqlBuilder.frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowIconified(WindowEvent e) {
                        hqlBuilder.switchVisibility();
                    }
                });
            }
        } catch (Exception ex) {
            logger.error("start(String[], HqlService)", ex); //$NON-NLS-1$
        }
    }

    // private static void loadModelAtRuntime() {
    // JFrame dummy = new JFrame();
    // dummy.setIconImage(new ImageIcon(HqlBuilderFrame.class.getClassLoader().getResource("bricks-icon.png")).getImage());
    // JFileChooser fc = new JFileChooser(new File("."));
    // fc.setDialogTitle(HqlResourceBundle.getMessage("open model & hibernate configuration"));
    // fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    // fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
    // @Override
    // public boolean accept(File f) {
    // return f.isDirectory() || f.getName().endsWith(".zip") || f.getName().endsWith(".jar");
    // }
    //
    // @Override
    // public String getDescription() {
    // return HqlResourceBundle.getMessage("single model & hibernate config jar / maven assembly zip");
    // }
    // });
    // int returnVal = fc.showOpenDialog(dummy);
    // if (returnVal == JFileChooser.APPROVE_OPTION) {
    // try {
    // File file = fc.getSelectedFile();
    //
    // URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    // Class<?> sysclass = URLClassLoader.class;
    // Method method = sysclass.getDeclaredMethod("addURL", URL.class);
    // method.setAccessible(true);
    //
    // if (file.getName().endsWith("zip")) {
    // File target = new File(getTmpdir(), file.getName());
    // unzip(target, file);
    // String[] list = target.list(new FilenameFilter() {
    // @Override
    // public boolean accept(File dir, String name) {
    // return name.endsWith("jar");
    // }
    // });
    // if (list != null) {
    // for (String element : list) {
    // method.invoke(sysloader, new Object[] { new File(target, element).toURI().toURL() });
    // }
    // }
    // } else {
    // method.invoke(sysloader, new Object[] { file.toURI().toURL() });
    // }
    // } catch (Exception ex) {
    //                logger.error("loadModelAtRuntime()", ex); //$NON-NLS-1$
    // System.exit(0);
    // return;
    // }
    // } else {
    // System.exit(-1);
    // return;
    // }
    // }
    //
    // private static void unzip(File dir, File zipFile) {
    // FileInputStream fis;
    // ZipInputStream zis = null;
    // try {
    // fis = new FileInputStream(zipFile);
    // zis = new ZipInputStream(new BufferedInputStream(fis));
    // BufferedOutputStream dest = null;
    //
    // ZipEntry entry;
    //
    // while ((entry = zis.getNextEntry()) != null) {
    // int count;
    // byte[] data = new byte[1024 * 8];
    // String extractFileLocation = dir + "/" + entry.getName();
    //
    // // maak alle tussenliggende directories aan
    // if (extractFileLocation.lastIndexOf('/') != -1) {
    // File file = new File(extractFileLocation.substring(0, extractFileLocation.lastIndexOf('/')));
    //
    // if (!file.exists()) {
    // file.mkdirs();
    // }
    // }
    //
    // // write the files to the disk
    // FileOutputStream fos = new FileOutputStream(dir + "/" + entry.getName());
    // dest = new BufferedOutputStream(fos, 1024 * 8);
    //
    // try {
    // while ((count = zis.read(data, 0, 1024 * 8)) != -1) {
    // dest.write(data, 0, count);
    // }
    // } finally {
    // dest.close();
    // }
    // }
    //
    // } catch (Exception ex) {
    // throw new RuntimeException(ex);
    // } finally {
    // if (zis != null) {
    // try {
    // zis.close();
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // }
    // }

    private void switchVisibility() {
        if (!SystemTray.isSupported()) {
            return;
        }
        if (!Boolean.TRUE.equals(systrayAction.getValue())) {
            return;
        }
        boolean v = !frame.isVisible();
        frame.setVisible(v);
        if (v) {
            frame.toFront();
            frame.setState(Frame.NORMAL);
        }
        if (!v) {
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException ex) {
                logger.error("switchVisibility()", ex); //$NON-NLS-1$
            }
        } else {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    private synchronized void query() {
        executeQuery(null);
    }

    private void progressbarStop() {
        glass.setMessage(null);
        setGlassVisible(false);
    }

    private void progressbarStart(final String text) {
        glass.setMessage(text);
        setGlassVisible(true);
    }

    private void query(RowProcessor rowProcessor) {
        progressbarStart(HqlResourceBundle.getMessage("quering"));
        executeQuery(rowProcessor);
        progressbarStop();
        JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("done"));
    }

    private synchronized void executeQuery(final RowProcessor rowProcessor) {
        final long start = System.currentTimeMillis();

        if (!startQueryAction.isEnabled()) {
            return;
        }

        preQuery();

        final String hqlGetText = getHqlText();

        final int maxresults = rowProcessor == null ? (Integer) maximumNumberOfResultsAction.getValue() : Integer.MAX_VALUE;

        SwingWorker<ExecutionResult, Void> sw = new SwingWorker<ExecutionResult, Void>() {
            @Override
            protected ExecutionResult doInBackground() throws Exception {
                return doQuery(hqlGetText, maxresults);
            }

            @Override
            protected void done() {
                try {
                    afterQuery(start, get(), rowProcessor);
                } catch (Exception ex) {
                    afterQuery(ex);
                } finally {
                    postQuery();
                }
            }
        };

        sw.execute();
    }

    private void postQuery() {
        startQueryAction.setEnabled(true);
        progressbarStop();
    }

    private void preQuery() {
        startQueryAction.setEnabled(false);
        progressbarStart(HqlResourceBundle.getMessage("quering"));
        sql.setText("");
        resultsInfo.setText("");
        clearResults();
        hql_sql_tabs.setForegroundAt(1, Color.gray);
        try {
            addLast(LAST);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        hql.removeHighlights(syntaxErrorsHighlight);
        hilightSyntax();
    }

    private ExecutionResult doQuery(String hqlGetText, int maxresults) {
        return hqlService.execute(hqlGetText, maxresults, EList.convertRecords(parametersEDT.getRecords()).toArray(new QueryParameter[0]));
    }

    private void afterQuery(Throwable ex) {
        hql_sql_tabs.setForegroundAt(1, Color.RED);
        logger.error("executeQuery(RowProcessor)", ex); //$NON-NLS-1$
        String sql_tmp = sql.getText();
        if (ex instanceof java.util.concurrent.ExecutionException) {
            ex = ex.getCause();
        }
        String exceptionString = ex.toString();
        if (ex instanceof ServiceException) {
            ExecutionResult partialResult = ServiceException.class.cast(ex).getPartialResult();
            if (partialResult != null && partialResult.getSql() != null) {
                sql_tmp = partialResult.getSql();
            }
        }
        if (ex instanceof SqlException) {
            SqlException sqlException = SqlException.class.cast(ex);
            if (sqlException.getSql() != null) {
                sql_tmp = sqlException.getSql();
            }
            exceptionString += getNewline() + sqlException.getState() + " - " + sqlException.getException();
        }
        sql.setText(exceptionString + getNewline() + "-----------------------------" + getNewline() + getNewline() + sql_tmp);
        clearResults();
        if (ex instanceof SyntaxException) {
            SyntaxException se = SyntaxException.class.cast(ex);
            hilightSyntaxException(se.getType(), se.getWrong(), se.getLine(), se.getCol());
        }
    }

    private void afterQuery(long start, ExecutionResult rv, RowProcessor rowProcessor) throws Exception {
        log(rv.getSql());
        if (formatSqlAction.isSelected()) {
            sql.setText(cleanupSql(rv.getSql(), null, null));
        } else {
            sql.setText(rv.getSql());
        }
        log(sql.getText());

        if (formatSqlAction.isSelected()) {
            try {
                sql.setText(hqlService.removeBlanks(hqlService.makeMultiline(cleanupSql(rv.getSql(), rv.getQueryReturnAliases(),
                        rv.getScalarColumnNames()))));
                log(sql.getText());
            } catch (Exception ex) {
                logger.error("executeQuery(RowProcessor)", ex); //$NON-NLS-1$
                log(rv.getSql());
            }
        }

        aliases = rv.getFromAliases();

        List<?> list = rv.getResults();
        ETableHeaders<List<Object>> headers = null;

        List<ETableRecord<List<Object>>> records = new ArrayList<ETableRecord<List<Object>>>();
        if (list != null) {
            for (Object o : list) {
                ETableRecordCollection<Object> record = null;
                List<Object> recordItems;

                if (o instanceof Object[]) {
                    recordItems = new ArrayList<Object>(Arrays.asList((Object[]) o));
                } else if (o instanceof Collection<?>) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> o2 = (Collection<Object>) o;
                    recordItems = new ArrayList<Object>(o2);
                } else {
                    recordItems = new ArrayList<Object>(Collections.singleton(o));
                }

                record = new ETableRecordCollection<Object>(recordItems);

                if (rowProcessor != null) {
                    rowProcessor.process(record.getBean());
                    continue;
                }

                if (headers == null) {
                    headers = new ETableHeaders<List<Object>>();

                    for (int i = 0; i < record.size(); i++) {
                        boolean script = scripts.get(i) != null;
                        Class<?> type;
                        String name;
                        try {
                            name = rv.getQueryReturnTypeNames()[i];
                            type = Class.forName(rv.getQueryReturnTypeNames()[i]);
                            name = type.getSimpleName();
                        } catch (Exception ex) {
                            type = Object.class;
                            name = "";
                        }
                        if ((rv.getQueryReturnAliases() == null) || String.valueOf(i).equals(rv.getQueryReturnAliases()[i])) {
                            try {
                                headers.add("<html>" + (script ? "*" : "") + name + "<br>" + rv.getScalarColumnNames()[i][0] + (script ? "*" : "")
                                        + "<html>", type);
                            } catch (Exception ex) {
                                log(ex);

                                try {
                                    headers.add("<html>" + (script ? "*" : "") + name + "<br>" + rv.getSqlAliases()[i] + (script ? "*" : "")
                                            + "<html>", type);
                                } catch (Exception ex2) {
                                    log(ex2);
                                    headers.add("<html>" + (script ? "*" : "") + name + "<br>" + i + (script ? "*" : "") + "<html>", type);
                                }
                            }
                        } else {
                            headers.add("<html>" + name + "<br>" + rv.getQueryReturnAliases()[i] + "<html>", type);
                        }
                    }

                    resultsEDT.setHeaders(headers);
                }

                records.add(record);
            }
        }
        resultsEDT.addRecords(records);
        hql_sql_tabs.setForegroundAt(1, Color.GREEN);

        resultsUnsafe.setAutoResizeMode(ETable.AUTO_RESIZE_OFF);
        for (int i = 0; i < resultsEDT.getColumnCount(); i++) {
            resultsUnsafe.packColumn(i, 2);
        }
        if (resizeColumnsAction.isSelected()) {
            resultsUnsafe.setAutoResizeMode(ETable.AUTO_RESIZE_ALL_COLUMNS);
        }
        if (rv.getSize() == 0) {
            resultsInfo.setText(HqlResourceBundle.getMessage("No results"));
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.HALF_UP);
            String d = df.format(rv.getDuration() / 1000.0);
            resultsInfo.setText(HqlResourceBundle.getMessage("results in seconds", String.valueOf(rv.getSize()), d));
        }
        System.out.println("duration (ms): " + rv.getDuration());
        System.out.println("overhead-server (ms): " + rv.getOverhead());
        System.out.println("overhead-client (ms): " + (System.currentTimeMillis() - start - rv.getDuration()));
    };

    private void hilightSyntaxException(SyntaxExceptionType syntaxExceptionType, String wrong, int line, int col) {
        hql.removeHighlights(syntaxErrorsHighlight);
        String hqltext = this.hql.getText();
        switch (syntaxExceptionType) {
            case unable_to_resolve_path: {
                try {
                    int indexOf = hqltext.indexOf(wrong);
                    this.hql.addHighlight(indexOf, indexOf + wrong.length(), syntaxErrorsHighlight);
                } catch (Exception ex) {
                    logger.error("hilightSyntaxException(SyntaxExceptionType, String, int, int)", ex); //$NON-NLS-1$
                }
            }
                break;
            case could_not_resolve_property: {
                try {
                    wrong = "." + wrong.split("#")[1];
                    int indexOf = hqltext.indexOf(wrong);
                    while (indexOf != -1) {
                        boolean accept = false;
                        try {
                            char nextChar = hqltext.charAt(indexOf + wrong.length());
                            if (!(('a' <= nextChar && nextChar <= 'z') || ('A' <= nextChar && nextChar <= 'Z'))) {
                                accept = true;
                            }
                        } catch (StringIndexOutOfBoundsException ex) {
                            accept = true;
                        }
                        if (accept) {
                            this.hql.addHighlight(indexOf, indexOf + wrong.length(), syntaxErrorsHighlight);
                        }
                        indexOf = hqltext.indexOf(wrong, indexOf + 1);
                    }
                } catch (Exception ex) {
                    logger.error("hilightSyntaxException(SyntaxExceptionType, String, int, int)", ex); //$NON-NLS-1$
                }
            }
                break;
            case invalid_path: {
                try {
                    int indexOf = hqltext.indexOf(wrong);
                    this.hql.addHighlight(indexOf, indexOf + wrong.length(), syntaxErrorsHighlight);
                } catch (Exception ex) {
                    logger.error("hilightSyntaxException(SyntaxExceptionType, String, int, int)", ex); //$NON-NLS-1$
                }
            }
                break;
            case not_mapped: {
                try {
                    int indexOf = hqltext.indexOf(wrong);
                    this.hql.addHighlight(indexOf, indexOf + wrong.length(), syntaxErrorsHighlight);
                } catch (Exception ex) {
                    logger.error("hilightSyntaxException(SyntaxExceptionType, String, int, int)", ex); //$NON-NLS-1$
                }
            }
                break;
            case unexpected_token: {
                String lines[] = hqltext.split("\\r?\\n");
                int pos = 0;
                for (int i = 0; i < lines.length; i++) {
                    if (i + 1 == line) {
                        for (int j = 0; j < col - 1; j++) {
                            pos++;
                        }
                        break;
                    }
                    pos += lines[i].length() + 1;
                }
                try {
                    this.hql.addHighlight(pos - 1, pos + 1 + 1, syntaxErrorsHighlight);
                } catch (Exception ex) {
                    logger.error("hilightSyntaxException(SyntaxExceptionType, String, int, int)", ex); //$NON-NLS-1$
                }
            }
                break;
        }
    }

    private String cleanupSql(String sqlString, String[] queryReturnAliases, String[][] scalarColumnNames) {
        return hqlService.cleanupSql(sqlString, queryReturnAliases, scalarColumnNames, replacePropertiesAction.isSelected(),
                formatLinesAction.isSelected(), removeJoinsAction.isSelected());
    }

    private String getHqlText() {
        Caret caret = hql.getCaret();
        String hqlstring;
        if (canExecuteSelectionAction.isSelected()) {
            int p1 = caret.getDot();
            int p2 = caret.getMark();
            if (p1 != p2) {
                if (p1 > p2) {
                    int tmp = p2;
                    p2 = p1;
                    p1 = tmp;
                }
                hqlstring = hql.getText().substring(p1, p2);
            } else {
                hqlstring = hql.getText();
            }
        } else {
            hqlstring = hql.getText();
        }
        String lines[] = hqlstring.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            int dash = line.indexOf("--");
            if (dash != -1) {
                sb.append(line.substring(0, dash));
                for (int i = dash; i < line.length(); i++) {
                    sb.append(" ");
                }
            } else {
                sb.append(line);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * start
     */
    public void start() throws IOException {
        reloadColor();

        syntaxHighlight.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0F, new float[] { 1.0F }, 0.F));

        hql.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                hilightBraces(hql.getText());
            }
        });

        sql.setEditable(false);

        resultsUnsafe.setAutoscrolls(true);
        resultsUnsafe.setFillsViewportHeight(true);
        resultsUnsafe.setColumnSelectionAllowed(true);

        propertypanel.add(values, BorderLayout.CENTER);

        Container framepanel = frame.getContentPane();

        JScrollPane jspResults = new JScrollPane(resultsUnsafe, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        resultPanel.add(jspResults, BorderLayout.CENTER);
        resultsUnsafe.addRowHeader(jspResults, 3);

        resultPanel.setBorder(BorderFactory.createTitledBorder(HqlResourceBundle.getMessage("results")));

        parameterspanel.setBorder(BorderFactory.createTitledBorder(HqlResourceBundle.getMessage("parameters")));

        propertypanel.setBorder(BorderFactory.createTitledBorder(HqlResourceBundle.getMessage("properties")));

        hql_sql_tabs.addTab("HQL", new JScrollPane(hql, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
        hql_sql_tabs.addTab("SQL", new JScrollPane(sql, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));

        {
            EList<String> searchresults = new EList<String>(new EListConfig().setSortable(false));
            final EList<String> searchresultsEDTSafe = searchresults.stsi();
            final ECheckBox searchClass = new ECheckBox("class", true);
            final ECheckBox searchField = new ECheckBox("field", true);
            ELabeledTextFieldButtonComponent searchinput = new ELabeledTextFieldButtonComponent() {
                private static final long serialVersionUID = 6519657911421417572L;

                @Override
                public JComponent getParentComponent() {
                    return null;
                }

                @Override
                public void copy(ActionEvent e) {
                    //
                }

                @Override
                protected Icon getIcon() {
                    return new ImageIcon(HqlBuilderFrame.class.getClassLoader().getResource("search.png"));
                }

                @Override
                protected String getAction() {
                    return "search-index";
                }

                @Override
                protected void doAction() {
                    searchresultsEDTSafe.removeAllRecords();
                    try {
                        List<String> results;
                        if (searchClass.isSelected() != searchField.isSelected()) {
                            if (searchClass.isSelected()) {
                                results = hqlService.search(getInput().getText(), "class");
                            } else {
                                results = hqlService.search(getInput().getText(), "field");
                            }
                        } else {
                            results = hqlService.search(getInput().getText(), null);
                        }
                        searchresultsEDTSafe.addRecords(EList.convert(results));
                    } catch (Exception ex) {
                        logger.error("$ELabeledTextFieldButtonComponent.doAction()", ex); //$NON-NLS-1$
                    }
                }
            };
            JPanel searchActions = new JPanel(new BorderLayout());
            searchActions.add(searchinput, BorderLayout.CENTER);

            JPanel searchTypes = new JPanel(new GridLayout(1, 2));
            searchTypes.add(searchClass);
            searchTypes.add(searchField);
            searchActions.add(searchTypes, BorderLayout.EAST);

            JPanel infopanel = new JPanel(new BorderLayout());
            infopanel.add(searchActions, BorderLayout.NORTH);
            infopanel.add(new JScrollPane(searchresults, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),
                    BorderLayout.CENTER);
            hql_sql_tabs.addTab(HqlResourceBundle.getMessage("Lucene search"), infopanel);
        }

        hql_sql_tabs.setForegroundAt(1, Color.gray);

        Dimension bd = new Dimension(24, 24);
        EButton saveButton = new EButton(new EToolBarButtonCustomizer(bd), saveAction);
        saveButton.setText("");
        EButton setNullButton = new EButton(new EToolBarButtonCustomizer(bd), setNullAction);
        setNullButton.setText("");
        EButton toTextButton = new EButton(new EToolBarButtonCustomizer(bd), toTextAction);
        toTextButton.setText("");
        EButton removeButton = new EButton(new EToolBarButtonCustomizer(bd), removeAction);
        removeButton.setText("");
        EButton upButton = new EButton(new EToolBarButtonCustomizer(bd), upAction);
        upButton.setText("");
        EButton addParameterButton = new EButton(new EToolBarButtonCustomizer(bd), addParameterAction);
        addParameterButton.setText("");
        EButton downButton = new EButton(new EToolBarButtonCustomizer(bd), downAction);
        downButton.setText("");
        EButton importParametersFromTextBtn = new EButton(new EToolBarButtonCustomizer(bd), importParametersAction);
        importParametersFromTextBtn.setText("");

        parameterspanel.add(new ELabel(HqlResourceBundle.getMessage("name") + ": "));
        parameterspanel.add(parameterName, "grow");
        parameterspanel.add(saveButton, "");

        parameterspanel.add(new ELabel(HqlResourceBundle.getMessage("value") + ": "));
        parameterspanel.add(parameterBuilder, "grow");
        parameterspanel.add(toTextButton, "");

        parameterspanel.add(new ELabel(HqlResourceBundle.getMessage("compiled") + ": "));
        parameterspanel.add(parameterValue, "grow");
        parameterspanel.add(setNullButton, "");

        parameterspanel.add(new JScrollPane(parametersUnsafe), "spanx 2, spany 4, growx, growy");
        parameterspanel.add(addParameterButton, "bottom, shrinky");
        parameterspanel.add(upButton, "shrinky");
        parameterspanel.add(removeButton, "shrinky");
        parameterspanel.add(downButton, "top, shrinky, wrap");

        parameterspanel.add(new ELabel(HqlResourceBundle.getMessage("import parameters") + ": "), "growx, shrinky");
        parameterspanel.add(importParametersFromTextF, "growx, shrinky");
        parameterspanel.add(importParametersFromTextBtn, "growx, shrinky");

        parameterspanel.add(new ELabel(), "growy, growx, spanx 3"); // filler

        maxResults.setMinimumSize(new Dimension(80, 22));
        maxResults.setPreferredSize(new Dimension(80, 22));
        resultsInfo.setMinimumSize(new Dimension(300, 22));
        resultsInfo.setPreferredSize(new Dimension(300, 22));

        JPanel resultsStatusPanel = new JPanel(new FlowLayout());
        resultsStatusPanel.add(resultsInfo);
        resultsStatusPanel.add(maxResults);
        resultPanel.add(resultsStatusPanel, BorderLayout.SOUTH);

        switch_layout();
        framepanel.add(normalContentPane, BorderLayout.CENTER);

        parameterBuilder.addDocumentKeyListener(new DocumentKeyListener() {
            @Override
            public void update(Type type, DocumentEvent e) {
                // extra help enkel in geval van groovy
                String text = parameterBuilder.getText();

                try {
                    // vervangt datums als string automatisch door localdate (parameters zijn dikwijls localdate)
                    LocalDate localDate = ISODateTimeFormat.date().parseDateTime(text).toLocalDate();
                    text = "new LocalDate(" + localDate.getYear() + "," + localDate.getMonthOfYear() + "," + localDate.getDayOfMonth() + ")";
                } catch (Exception ex2) {
                    //
                }

                try {
                    // zet automatisch l achter nummers (parameters zijn dikwijls id's en die zijn van het type long)
                    Long.parseLong(text);
                    text = text + "l";
                } catch (Exception ex2) {
                    //
                }

                compile(text);
            }
        });
        hql.addDocumentKeyListener(new DocumentKeyListener() {
            @Override
            public void update(Type type, DocumentEvent e) {
                //
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (EventHelper.keyEvent(e, EventModifier.CTR_SHIFT_DOWN, ' ')) {
                    help_insert();
                    return;
                }

                if (EventHelper.keyEvent(e, EventModifier.CTR_SHIFT_DOWN, '/')) {
                    remark_toggle();
                    return;
                }
            }
        });

        createHqlPopupMenu();

        createSqlPopupMenu();

        createResultsPopupMenu();

        addTableSelectionListener(resultsUnsafe, new TableSelectionListener() {
            @Override
            public void rowChanged(int row) {
                //
            }

            @Override
            public void columnChanged(int column) {
                //
            }

            @Override
            public void cellChanged(int row, int column) {
                propertypanel.removeAll();
                Object data = ((row == -1) || (column == -1)) ? null : resultsEDT.getValueAt(row, column);
                if (data == null) {
                    propertypanel.add(ClientUtils.getPropertyFrame(new Object(), editableResultsAction.isSelected()), BorderLayout.CENTER);
                } else {
                    PropertyPanel propertyFrame = ClientUtils.getPropertyFrame(data, editableResultsAction.isSelected());
                    propertyFrame.setHqlService(hqlService);
                    propertypanel.add(font(propertyFrame, null), BorderLayout.CENTER);
                }
                propertypanel.revalidate();
            }
        });
        parametersEDT.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                parameterSelected();
            }
        });

        hql_sql_tabs_panel.add(hql_sql_tabs, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        EToolBarButtonCustomizer etbc = new EToolBarButtonCustomizer();
        {
            JToolBar hqltools = new JToolBar(javax.swing.SwingConstants.VERTICAL);
            hqltools.add(new EButton(etbc, startQueryAction));
            hqltools.add(new EButton(etbc, wizardAction));
            hqltools.add(new EButton(etbc, clearAction));
            hqltools.add(new EButton(etbc, findParametersAction));
            hqltools.add(new EButton(etbc, favoritesAction));
            hqltools.add(new EButton(etbc, addToFavoritesAction));
            hqltools.add(new EButton(etbc, formatAction));
            hqltools.add(new EButton(etbc, namedQueryAction));
            hqltools.add(new EButton(etbc, clipboardExportAction));
            hqltools.add(new EButton(etbc, clipboardImportAction));
            hqltools.add(new EButton(etbc, helpInsertAction));
            hqltools.add(new EButton(etbc, remarkToggleAction));
            hqltools.add(new EButton(etbc, deleteInvertedSelectionAction));
            hql_sql_tabs_panel.add(hqltools, BorderLayout.WEST);
        }
        {
            JToolBar resultstools = new JToolBar(javax.swing.SwingConstants.VERTICAL);
            resultstools.add(new EButton(etbc, hibernatePropertiesAction));
            resultstools.add(new EButton(etbc, objectTreeAction));
            resultstools.add(new EButton(etbc, deleteObjectAction));
            resultstools.add(new EButton(etbc, copyCellAction));
            resultstools.add(new EButton(etbc, executeScriptOnColumnAction));
            resultPanel.add(resultstools, BorderLayout.WEST);
        }

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        {
            JMenu actionsMenu = new JMenu(HqlResourceBundle.getMessage("actions"));
            actionsMenu.add(new JCheckBoxMenuItem(switchLayoutAction));
            actionsMenu.add(new JMenuItem(exitAction));
            actionsMenu.add(new JMenuItem(forceExitAction));
            menuBar.add(actionsMenu);
        }
        {
            JMenu settingsMenu = new JMenu(HqlResourceBundle.getMessage("settings"));
            {
                settingsMenu.add(new JCheckBoxMenuItem(formatSqlAction));
                settingsMenu.add(formatSqlOptionsMenu);
                formatSqlOptionsMenu.add(new JCheckBoxMenuItem(removeJoinsAction));
                formatSqlOptionsMenu.add(new JCheckBoxMenuItem(formatLinesAction));
                formatSqlOptionsMenu.add(new JCheckBoxMenuItem(replacePropertiesAction));
            }
            {
                JMenu addmi = new JMenu(HqlResourceBundle.getMessage("language"));
                ButtonGroup lanGroup = new ButtonGroup();

                List<Locale> locales = new ArrayList<Locale>();

                for (Locale locale : Locale.getAvailableLocales()) {
                    URL bundle = HqlBuilderFrame.class.getClassLoader().getResource("HqlResourceBundle_" + locale + ".properties");
                    System.out.println(locale + ">" + bundle);
                    if (null != bundle) {
                        locales.add(locale);
                    }
                }

                locales.remove(DEFAULT_LOCALE);

                Collections.sort(locales, new Comparator<Locale>() {
                    @Override
                    public int compare(Locale o1, Locale o2) {
                        return new CompareToBuilder().append(o1.getDisplayLanguage(o1), o2.getDisplayLanguage(o2))
                                .append(o1.getDisplayCountry(o1), o2.getDisplayCountry(o2)).toComparison();
                    }
                });

                locales.add(0, DEFAULT_LOCALE);

                for (Locale locale : locales) {
                    final Locale loc = locale;
                    String title = locale.getDisplayLanguage(locale);
                    if (StringUtils.isNotBlank(locale.getCountry())) {
                        title += ", " + locale.getDisplayCountry(locale);
                    }
                    JCheckBoxMenuItem lanMenu = new JCheckBoxMenuItem(title);
                    addmi.add(lanMenu);
                    lanGroup.add(lanMenu);
                    if (locale.equals(DEFAULT_LOCALE) || locale.getLanguage().equals(SystemSettings.getCurrentLocale().getLanguage())) {
                        lanMenu.setSelected(true);
                    }
                    lanMenu.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // SystemSettings.setCurrentLocale(loc);
                            preferences.put(PERSISTENT_LOCALE, loc.toString());
                            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("change visible after restart"), "",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                }

                settingsMenu.add(addmi);
            }
            {
                JMenu addmi = new JMenu(HqlResourceBundle.getMessage("additional settings"));
                addmi.add(new JCheckBoxMenuItem(canExecuteSelectionAction));
                addmi.add(new JCheckBoxMenuItem(hiliAction));
                addmi.add(new JMenuItem(hiliColorAction));
                addmi.add(new JCheckBoxMenuItem(resizeColumnsAction));
                addmi.add(maximumNumberOfResultsAction);
                addmi.add(fontAction);
                addmi.add(new JCheckBoxMenuItem(alwaysOnTopAction));
                addmi.add(new JCheckBoxMenuItem(editableResultsAction));
                addmi.add(new JMenuItem(searchColorAction));
                if (SystemTray.isSupported()) {
                    addmi.add(new JCheckBoxMenuItem(systrayAction));
                }
                settingsMenu.add(addmi);
            }
            {
                settingsMenu.addSeparator();
            }
            {
                JMenuItem resetMi = new JMenuItem(HqlResourceBundle.getMessage("reset settings"));
                resetMi.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fontAction.setWarnRestart(false);

                        maximumNumberOfResultsAction.setValue(100);
                        fontAction.setValue(ClientUtils.getDefaultFont());
                        searchColorAction.setValue(null);
                        hiliColorAction.setValue(null);
                        removeJoinsAction.setSelected(true);
                        formatLinesAction.setSelected(true);
                        replacePropertiesAction.setSelected(true);
                        resizeColumnsAction.setSelected(true);
                        formatSqlAction.setSelected(true);
                        systrayAction.setSelected(true);
                        canExecuteSelectionAction.setSelected(true);
                        hiliAction.setSelected(false);
                        alwaysOnTopAction.setSelected(false);
                        editableResultsAction.setSelected(false);
                        switchLayoutAction.setSelected(true);

                        fontAction.setWarnRestart(true);

                        JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("change visible after restart"), "",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                settingsMenu.add(resetMi);
            }
            menuBar.add(settingsMenu);
        }
        {
            JMenu hqlmenu = new JMenu("HQL");
            hqlmenu.add(new JMenuItem(startQueryAction));
            hqlmenu.add(new JMenuItem(wizardAction));
            hqlmenu.add(new JMenuItem(clearAction));
            hqlmenu.add(new JMenuItem(findParametersAction));
            hqlmenu.add(new JMenuItem(favoritesAction));
            hqlmenu.add(new JMenuItem(addToFavoritesAction));
            hqlmenu.add(new JMenuItem(formatAction));
            hqlmenu.add(new JMenuItem(namedQueryAction));
            hqlmenu.add(new JMenuItem(clipboardExportAction));
            hqlmenu.add(new JMenuItem(clipboardImportAction));
            menuBar.add(hqlmenu);
        }
        {
            JMenu resultsmenu = new JMenu(HqlResourceBundle.getMessage("results"));
            resultsmenu.add(new JMenuItem(hibernatePropertiesAction));
            resultsmenu.add(new JMenuItem(objectTreeAction));
            resultsmenu.add(new JMenuItem(deleteObjectAction));
            resultsmenu.add(new JMenuItem(copyCellAction));
            resultsmenu.add(new JMenuItem(executeScriptOnColumnAction));
            menuBar.add(resultsmenu);
        }
        {
            JMenu helpmenu = new JMenu(HqlResourceBundle.getMessage("help"));
            helpmenu.add(new JMenuItem(helpHibernateAction));
            helpmenu.add(new JMenuItem(helpHqlAction));
            helpmenu.add(new JMenuItem(luceneQuerySyntaxAction));
            helpmenu.add(new JMenuItem(helpAction));
            menuBar.add(helpmenu);
        }

        String version;
        try {
            Properties p = new Properties();
            p.load(getClass().getClassLoader().getResourceAsStream("META-INF/maven/org.tools/hql-builder-client/pom.properties"));
            version = p.getProperty("version").toString();
        } catch (Exception ex) {
            try {
                version = org.w3c.dom.Node.class.cast(
                        ClientUtils.getFromXml(new FileInputStream("pom.xml"), "project", "/default:project/default:version/text()")).getNodeValue();
            } catch (Exception ex2) {
                try {
                    version = org.w3c.dom.Node.class.cast(
                            ClientUtils.getFromXml(new FileInputStream("pom.xml"), "project",
                                    "/default:project/default:parent/default:version/text()")).getNodeValue();
                } catch (Exception ex3) {
                    version = HqlResourceBundle.getMessage("latest");
                }
            }
        }

        frame.setTitle(NAME + " v" + version + " - " + hqlService.getConnectionInfo() + " - " + hqlService.getProject()
                + (hqlService.getServiceUrl() == null ? "" : " - " + hqlService.getServiceUrl()));
        frame.setVisible(true);
        frame.setSize(new Dimension(1024, 768));
        frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        frame.setIconImage(new ImageIcon(HqlBuilderFrame.class.getClassLoader().getResource("bricks-icon.png")).getImage());
        frame.setGlassPane(getGlass(frame));
        frame.setAlwaysOnTop(Boolean.TRUE.equals(alwaysOnTopAction.getValue()));
    }

    protected void startPre() {
        loadFavorites();
    }

    @SuppressWarnings("cast")
    private void layout(Dimension size) {
        if (frame.getSize().height == 0) {
            size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(
                    java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
            size = new Dimension((int) (size.getWidth() - insets.left - insets.right), (int) (size.getHeight() - insets.top - insets.bottom));
        } else {
            size = normalContentPane.getSize();
        }

        int w = (int) size.getWidth();
        int h = (int) size.getHeight();
        // if (menu_largeLayout.getState()) {
        if (switchLayoutAction.isSelected()) {
            // hql_sql_tabs .... parameterspanel .... propertypanel
            // resultPanel
            split0.setDividerLocation((int) ((double) h * 1.0 / 3.0));
            split1.setDividerLocation((int) ((double) w * 1.0 / 3.0));
            split2.setDividerLocation((int) ((double) w * 1.0 / 3.0));
        } else {
            // hql_sql_tabs .... parameterspanel
            // resultPanel ..... propertypanel
            split0.setDividerLocation((int) ((double) h * 1.0 / 3.0));
            split1.setDividerLocation((int) ((double) w * 2.0 / 3.0));
            split2.setDividerLocation((int) ((double) w * 2.0 / 3.0));
        }
    }

    protected void switch_layout() {
        log("change layout");

        normalContentPane.setVisible(false);
        normalContentPane.removeAll();

        // if (menu_largeLayout.getState()) {
        if (switchLayoutAction.isSelected()) {
            // hql_sql_tabs .... parameterspanel .... propertypanel
            // resultPanel
            split0 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

            EComponentPopupMenu.removeAllRegisteredKeystroke(split0);
            EComponentPopupMenu.removeAllRegisteredKeystroke(split1);
            EComponentPopupMenu.removeAllRegisteredKeystroke(split2);

            split0.setLeftComponent(split1);
            split1.setRightComponent(split2);

            split1.setLeftComponent(hql_sql_tabs_panel);
            split2.setLeftComponent(parameterspanel);
            split2.setRightComponent(propertypanel);
            split0.setRightComponent(resultPanel);

            normalContentPane.add(split0, BorderLayout.CENTER);
        } else {
            // hql_sql_tabs .... parameterspanel
            // resultPanel ..... propertypanel
            split0 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

            EComponentPopupMenu.removeAllRegisteredKeystroke(split0);
            EComponentPopupMenu.removeAllRegisteredKeystroke(split1);
            EComponentPopupMenu.removeAllRegisteredKeystroke(split2);

            split0.setLeftComponent(split1);
            split0.setRightComponent(split2);

            split1.setLeftComponent(hql_sql_tabs_panel);
            split1.setRightComponent(parameterspanel);
            split2.setLeftComponent(resultPanel);
            split2.setRightComponent(propertypanel);

            normalContentPane.add(split0, BorderLayout.CENTER);
        }

        layout(null);

        normalContentPane.setVisible(true);
    }

    private void createSqlPopupMenu() {
        // EComponentPopupMenu.installPopupMenu((ReadableComponent) new TextComponentWritableComponent(sql));
    }

    private void createHqlPopupMenu() {
        JPopupMenu hqlpopupmenu = hql.getComponentPopupMenu();
        hqlpopupmenu.addSeparator();
        hqlpopupmenu.add(new JMenuItem(startQueryAction));
        hqlpopupmenu.add(new JMenuItem(wizardAction));
        hqlpopupmenu.add(new JMenuItem(clearAction));
        hqlpopupmenu.add(new JMenuItem(findParametersAction));
        hqlpopupmenu.add(new JMenuItem(favoritesAction));
        hqlpopupmenu.add(new JMenuItem(addToFavoritesAction));
        hqlpopupmenu.add(new JMenuItem(formatAction));
        hqlpopupmenu.add(new JMenuItem(namedQueryAction));
        hqlpopupmenu.add(new JMenuItem(clipboardExportAction));
        hqlpopupmenu.add(new JMenuItem(clipboardImportAction));
        hqlpopupmenu.add(new JMenuItem(helpInsertAction));
        hqlpopupmenu.add(new JMenuItem(remarkToggleAction));
        hqlpopupmenu.add(new JMenuItem(deleteInvertedSelectionAction));
    }

    private void createResultsPopupMenu() {
        JPopupMenu resultspopupmenu = resultsUnsafe.getComponentPopupMenu();
        resultspopupmenu.addSeparator();
        resultspopupmenu.add(new JMenuItem(hibernatePropertiesAction));
        resultspopupmenu.add(new JMenuItem(objectTreeAction));
        resultspopupmenu.add(new JMenuItem(deleteObjectAction));
        resultspopupmenu.add(new JMenuItem(copyCellAction));
        resultspopupmenu.add(new JMenuItem(executeScriptOnColumnAction));
    }

    protected void exit() {
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame, HqlResourceBundle.getMessage("exit_confirmation"), "",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            log("exit(0)");
            System.exit(0);
        }
    }

    protected void force_exit() {
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame, HqlResourceBundle.getMessage("force_exit_confirmation"), "",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            log("exit(-1)");
            System.exit(-1);
        }
    }

    @SuppressWarnings("null")
    protected void export_dataset_to_csv_file() {
        OutputStream fout = null;
        try {
            final JFileChooser fc = new JFileChooser();
            if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(frame)) {
                File dir_shortname = fc.getSelectedFile();
                String absolutePath = dir_shortname.getAbsolutePath();
                if (!absolutePath.endsWith(CSV)) {
                    absolutePath += CSV;
                }
                fout = new FileOutputStream(new File(absolutePath));
                final BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fout));
                recordCount = 0;
                query(new RowProcessor() {
                    @Override
                    public void process(List<Object> lijn) {
                        try {
                            for (Object object : lijn) {
                                if (object instanceof Object[]) {
                                    Object[] array = (Object[]) object;
                                    for (int i = 0; i < array.length; i++) {
                                        br.write("\"");
                                        br.write(array[i].toString().replaceAll(",", ";").replaceAll("\"", "'"));
                                        br.write("\"");

                                        if (i < array.length - 1) {
                                            br.write(",");
                                        }
                                    }
                                } else {
                                    br.write("\"");
                                    br.write(object.toString().replaceAll(",", ";").replaceAll("\"", "'"));
                                    br.write("\"");
                                }
                            }
                            br.write(getNewline());
                            recordCount++;
                            if (recordCount != 0 && recordCount % 100 == 0) {
                                log(recordCount + " records");
                            }
                        } catch (Exception ex) {
                            logger.error("$RowProcessor.process(List<Object>)", ex); //$NON-NLS-1$
                        }
                    }
                });
                log(recordCount + " records");
                br.flush();
                br.close();
            }
        } catch (Exception ex) {
            logger.error("export data", ex); //$NON-NLS-1$
        } finally {
            try {
                fout.close();
            } catch (Exception ex2) {
                //
            }
        }
    }

    protected void export_table_data_to_clipboard() {
        try {
            StringBuilder sb = new StringBuilder();

            for (String name : resultsEDT.getHeadernames()) {
                sb.append(name).append("\t");
            }

            sb.append(getNewline());

            for (ETableRecord<List<Object>> record : resultsEDT.getRecords()) {
                for (int i = 0; i < record.size(); i++) {
                    sb.append(record.get(i)).append("\t");
                }

                sb.append(getNewline());
            }

            clipboard.setContents(new StringSelection(sb.toString()), getClipboardOwner());
        } catch (Exception ex) {
            logger.error("export data", ex); //$NON-NLS-1$
        }
    }

    protected void format() {
        try {
            hql.setText(format(hql.getText()));
            hql.setCaret(0);
        } catch (Exception ex) {
            logger.error("format()", ex); //$NON-NLS-1$
        }
    }

    protected void copy() {
        try {
            int start = hql.getCaret().getDot();
            int end = hql.getCaret().getMark();
            String string = hql.getDocument().getText(Math.min(start, end), Math.max(start, end) - Math.min(start, end));
            clipboard.setContents(new StringSelection(string), getClipboardOwner());
        } catch (Exception ex) {
            logger.error("copy()", ex); //$NON-NLS-1$
        }
    }

    protected void paste() {
        try {
            Transferable contents = clipboard.getContents(this);
            Object transferData = contents.getTransferData(DataFlavor.stringFlavor);
            String string = (String) transferData;
            int start = hql.getCaret().getDot();
            int end = hql.getCaret().getMark();
            string = hql.getDocument().getText(0, Math.min(start, end)) + string
                    + hql.getDocument().getText(Math.max(start, end), hql.getDocument().getLength() - Math.max(start, end));
            hql.setText(string);
        } catch (Exception ex) {
            logger.error("paste()", ex); //$NON-NLS-1$
        }
    }

    protected void import__paste_hql_as_java_from_clipboard() {
        try {
            Transferable contents = clipboard.getContents(this);
            Object transferData = contents.getTransferData(DataFlavor.stringFlavor);
            String string = (String) transferData;
            String replaceAll = string.replaceAll("//", "").replaceAll("\";", "").replaceAll("hql[ ]{0,}\\+[ ]{0,}=[ ]{0,}\"", "")
                    .replaceAll("\\Q\r\n\\E", getNewline());
            StringBuilder sb = new StringBuilder();
            for (String line : replaceAll.split(getNewline())) {
                sb.append(line.trim()).append(getNewline());
            }
            hql.setText(sb.toString());
        } catch (Exception ex) {
            logger.error("import hql", ex); //$NON-NLS-1$
        }
    }

    private String format(String text) {
        return hqlService.makeMultiline(hqlService.removeBlanks(text.replace('\n', ' ').replace('\t', ' ').replace('\r', ' ')));
    }

    private static List<QueryParameter> convertParameterString(String map) {
        map = map.trim();
        if (map.startsWith("[") && map.endsWith("]")) {
            map = map.substring(1, map.length() - 1);
        } else {
            if (map.startsWith("{") && map.endsWith("}")) {
                map = map.substring(1, map.length() - 1);
            } else {
                return new ArrayList<QueryParameter>();
            }
        }
        List<String> parts = new ArrayList<String>();
        String splitted = null;
        for (String part : map.split(",")) {
            part = part.trim();
            if (part.contains("[")) {
                splitted = part;
            } else if (splitted == null) {
                parts.add(part);
            } else {
                splitted = splitted + "," + part;
                if (part.contains("]")) {
                    parts.add(splitted);
                    splitted = null;
                }
            }
        }
        List<QueryParameter> qps = new ArrayList<QueryParameter>();
        for (String el : parts) {
            el = el.trim();
            try {
                System.out.println(el);
                if (el.contains("=")) {
                    String[] p = el.split("=");
                    Object val = GroovyCompiler.eval(p[1]);
                    System.out.println(p[0] + "=" + val.getClass() + "=" + val);
                    QueryParameter qp = new QueryParameter(p[1], p[0], val);
                    qps.add(qp);
                } else {
                    Object val = GroovyCompiler.eval(el);
                    System.out.println(val.getClass() + "=" + val);
                    QueryParameter qp = new QueryParameter(el, null, val);
                    qps.add(qp);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return qps;
    }

    protected void import_parameters() {
        String m = importParametersFromTextF.getText();
        importParametersFromTextF.setText("");

        for (QueryParameter qp : convertParameterString(m)) {
            boolean exists = false;
            if (qp.getName() == null) {
                // just add at bottom in order
            } else {
                for (EListRecord<QueryParameter> rec : parametersEDT.getRecords()) {
                    if (rec.get().getName().equals(qp.getName())) {
                        rec.get().setName(qp.getName());
                        rec.get().setText(qp.getText());
                        rec.get().setValue(qp.getValue());
                        exists = true;
                        break;
                    }
                }
            }
            if (!exists) {
                parametersEDT.addRecord(new EListRecord<QueryParameter>(qp));
            }
        }
    }

    protected void to_text() {
        if (parameterBuilder.getText().startsWith("'") && parameterBuilder.getText().endsWith("'")) {
            return;
        }
        parameterBuilder.setText("'" + parameterBuilder.getText() + "'");
        save();
    }

    private void addTableSelectionListener(final ETable<?> table, final TableSelectionListener listener) {
        final HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put(ROW, -1);
        map.put(COL, -1);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                outputSelection(ROW, map, table, listener);
            }
        });
        table.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                outputSelection(COL, map, table, listener);
            }
        });
    }

    private static void outputSelection(@SuppressWarnings("unused") String type, HashMap<String, Integer> map, ETable<?> table,
            TableSelectionListener listener) {
        int row = map.get(ROW);
        int col = map.get(COL);

        int row_ = ((table.getSelectedRows() == null) || (table.getSelectedRows().length == 0)) ? (-1) : table.getSelectedRows()[0];
        int col_ = ((table.getSelectedColumns() == null) || (table.getSelectedColumns().length == 0)) ? (-1) : table.getSelectedColumns()[0];

        if (table.getColumnSelectionAllowed() && table.getRowSelectionAllowed() && ((row_ != row) || (col_ != col))) {
            listener.cellChanged(row_, col_);
        } else if (table.getColumnSelectionAllowed() && (col_ != col)) {
            listener.columnChanged(col_);
        } else if (table.getRowSelectionAllowed() && (row_ != row)) {
            listener.rowChanged(row_);
        } else {
            //
        }

        map.put(ROW, row_);
        map.put(COL, col_);
    }

    public <T extends JComponent> T font(T comp, Integer size) {
        Font f = getFont();
        if (size != null && f.getSize() != size) {
            f = f.deriveFont(f.getSize() + (float) size);
        }
        comp.setFont(f);
        return comp;
    }

    protected void start_query() {
        System.out.println("start query");
        try {
            query();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("end query");
    }

    protected void pause_query() {
        System.out.println("pause_query");
    }

    protected void export__copy_hql_as_java_to_clipboard() {
        try {
            StringBuilder sb = new StringBuilder("String hql = \"\";" + getNewline());

            for (String line : hql.getText().replaceAll("\r\n", getNewline()).split(getNewline())) {
                if (!line.startsWith("--") && !line.startsWith("//")) {
                    sb.append("hql +=\" ").append(line).append("\";");
                } else {
                    sb.append("// ").append(line);
                }

                sb.append(getNewline());
            }

            clipboard.setContents(new StringSelection(sb.toString()), getClipboardOwner());
        } catch (Exception ex) {
            logger.error("export__copy_hql_as_java_to_clipboard()", ex); //$NON-NLS-1$
        }
    }

    protected void wizard() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new HqlWizard(new HqlWizardListener() {
                    @Override
                    public void query(String query) {
                        hql.setText(query);
                    }
                }, frame, getHibernateWebResolver());
            }
        }).start();
    }

    protected void help() {
        try {
            Desktop.getDesktop().browse(URI.create(ClientUtils.getHelpUrl()));
        } catch (Exception ex) {
            logger.error("help()", ex); //$NON-NLS-1$
        }
    }

    protected void hibernate_documentation() {
        try {
            Desktop.getDesktop().browse(URI.create("http://docs.jboss.org/hibernate/core/3.6/reference/en-US/html/"));
        } catch (Exception ex) {
            logger.error("hibernate_documentation()", ex); //$NON-NLS-1$
        }
    }

    protected void hql_documentation() {
        try {
            Desktop.getDesktop().browse(URI.create("http://docs.jboss.org/hibernate/core/3.6/reference/en-US/html/queryhql.html"));
        } catch (Exception ex) {
            logger.error("hql_documentation()", ex); //$NON-NLS-1$
        }
    }

    protected void copy_selected_cell() {
        int selectedRow = resultsEDT.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no row selected"), "", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedColumn = resultsEDT.getSelectedColumn();
        if (selectedColumn == -1) {
            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no col selected"), "", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object valueAt = resultsEDT.getValueAt(selectedRow, selectedColumn);
        if (valueAt == null) {
            return;
        }
        clipboard.setContents(new StringSelection(valueAt.toString()), getClipboardOwner());
    }

    private ClipboardOwner getClipboardOwner() {
        return new ClipboardOwner() {
            @Override
            public void lostOwnership(Clipboard cb, Transferable contents) {
                //
            }
        };
    }

    protected void execute_script_on_column() {
        int selectedCol = resultsEDT.getSelectedColumn();
        if (selectedCol == -1) {
            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no column selected"), "", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String script = scripts.get(selectedCol);
        script = JOptionPane.showInputDialog(frame, HqlResourceBundle.getMessage("groovy_cell"), script != null ? script : "");
        if (script == null) {
            scripts.remove(selectedCol);
            return;
        }
        boolean error = false;
        scripts.put(selectedCol, script);
        for (int row = 0; row < resultsEDT.getRowCount(); row++) {
            Object x = resultsEDT.getValueAt(row, selectedCol);
            if (x == null) {
                return;
            }
            try {
                x = Eval.x(x, script);
                resultsEDT.setValueAt(x, row, selectedCol);
            } catch (Exception ex) {
                logger.error("execute_script_on_column()", ex); //$NON-NLS-1$
                error = true;
            }
        }
        if (error) {
            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("script_exception"));
        }
        resultsEDT.repaint();
    }

    private ProgressGlassPane getGlass(JFrame parent) {
        if (glass == null) {
            glass = new ProgressGlassPane(parent);

            try {
                glass.setFont(new Font("DejaVu Sans Mono", Font.BOLD, 22));
            } catch (Exception ex) {
                //
            }
        }
        return glass;
    }

    private void setGlassVisible(boolean v) {
        if (glass != null) {
            glass.setVisible(v);
        }
    }

    protected void maximum_number_of_results() {
        Object newValue = JOptionPane.showInputDialog(frame, HqlResourceBundle.getMessage(MAXIMUM_NUMBER_OF_RESULTS),
                String.valueOf(maximumNumberOfResultsAction.getValue()));
        if (newValue != null) {
            maximumNumberOfResultsAction.setValue(Integer.parseInt(String.valueOf(newValue)));
            maxResults.setText(" / " + newValue);
        }
    }

    protected void font() {
        Font font = EFontChooser.showDialog((JComponent) frame.getContentPane(), getFont().getFamily());
        if (font == null) {
            return;
        }
        fontAction.setValue(font);
    }

    public Font getFont() {
        return (Font) fontAction.getValue();
    }

    @SuppressWarnings("unchecked")
    protected <T> T get(Object o, String path, @SuppressWarnings("unused") Class<T> t) {
        return (T) new ObjectWrapper(o).get(path);
    }

    protected void format_sql() {
        formatSqlOptionsMenu.setEnabled(formatSqlAction.isSelected());
    }

    protected void add_to_favorites() {
        try {
            if (!hql_sql_tabs.getForegroundAt(1).equals(Color.GREEN)) {
                if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(frame, HqlResourceBundle.getMessage("no query"),
                        HqlResourceBundle.getMessage("add to favorites"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
                    return;
                }
            }
            String name = JOptionPane.showInputDialog(frame.getContentPane(), HqlResourceBundle.getMessage("add to favorites"),
                    HqlResourceBundle.getMessage("favorite name"));
            addLast(name);
        } catch (Exception ex) {
            log(ex);
        }
    }

    private void addLast(String name) throws IOException {
        List<QueryParameter> qps = new ArrayList<QueryParameter>();
        for (EListRecord<QueryParameter> qp : parametersEDT.getRecords()) {
            qps.add(qp.get());
        }
        String hql_text = hql.getText();
        QueryFavorite favorite = new QueryFavorite(name, hql_text, qps.toArray(new QueryParameter[qps.size()]));
        if (favorites.contains(favorite)) {
            favorites.remove(favorites.indexOf(favorite));
        }
        favorites.add(favorite);
        File favoritesForProject = new File(FAVORITES_DIR, hqlService.getProject());
        File xmlhistory = new File(favoritesForProject, name + FAVORITES_EXT);
        if (!xmlhistory.getParentFile().exists()) {
            xmlhistory.getParentFile().mkdirs();
        }
        FileOutputStream os = new FileOutputStream(xmlhistory);
        XMLEncoder enc = new XMLEncoder(os);
        enc.writeObject(favorite);
        enc.close();
    }

    private void loadFavorites() {
        File favoritesForProject = new File(FAVORITES_DIR, hqlService.getProject());
        if (!favoritesForProject.exists()) {
            favoritesForProject.mkdir();
        }
        File[] xmls = favoritesForProject.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(FAVORITES_EXT);
            }
        });
        if (xmls == null) {
            return;
        }
        for (File xml : xmls) {
            FileInputStream is;
            try {
                is = new FileInputStream(xml);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            XMLDecoder dec = new XMLDecoder(is);
            QueryFavorite favorite = (QueryFavorite) dec.readObject();
            if (favorites.contains(favorite)) {
                favorites.remove(favorites.indexOf(favorite));
            }
            favorites.add(favorite);
            dec.close();
            if (xml.getName().equals(LAST + FAVORITES_EXT)) {
                importFromFavoritesNoQ(favorite);
            }
        }
    }

    protected void always_on_top() {
        boolean alwaysOnTop = Boolean.TRUE.equals(alwaysOnTopAction.getValue());
        frame.setAlwaysOnTop(alwaysOnTop);
    }

    protected void delete_object() {
        try {
            if (!editableResultsAction.isSelected()) {
                JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("results not editable"), "", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int col = resultsEDT.getSelectedColumn();
            if (col == -1) {
                JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no column selected"), "", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = resultsEDT.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no row selected"), "", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object bean = resultsEDT.getModel().getValueAt(row, col);
            hqlService.delete(bean);
            resultsEDT.getModel().setValueAt(null, row, col);
            propertypanel.removeAll();
            propertypanel.revalidate();
            propertypanel.repaint();
        } catch (Exception ex) {
            log(ex);
        }
    }

    protected void help_insert() {
        try {
            log("help insert");
            helpInsert();
            // scheduleQuery(null, false);
        } catch (Exception ex) {
            log(ex);
        }
    }

    protected void remark_toggle() {
        log("remark toggle");

        int selectionStart = hql.getSelectionStart();
        int selectionEnd = hql.getSelectionEnd();
        String hqltext = hql.getText();

        hqltext = remarkToggle(hqltext, selectionStart, selectionEnd);
        hql.setText(hqltext);
        hql.setCaret(Math.min(selectionStart, selectionEnd));
    }

    protected void object_tree() {
        final int col = resultsEDT.getSelectedColumn();
        if (col == -1) {
            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no column selected"), "", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final int row = resultsEDT.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no row selected"), "", JOptionPane.WARNING_MESSAGE);
            return;
        }
        progressbarStart("fetching data");
        Object bean = resultsEDT.getModel().getValueAt(row, col);
        new ObjectTree(this, hqlService, bean, editableResultsAction.isSelected()).setIconImage(frame.getIconImage());
        progressbarStop();
    }

    protected void resize_columns() {
        //
    }

    private Set<String> reservedKeywords;

    private Set<String> getReservedKeywords() {
        if (reservedKeywords == null) {
            reservedKeywords = hqlService.getReservedKeywords();
        }
        return reservedKeywords;
    }

    private void hilightSyntax() {
        this.hql.removeHighlights(syntaxHighlight);

        if (!hiliAction.isSelected()) {
            return;
        }

        LinkedList<int[]> pos = new LinkedList<int[]>();

        for (String kw : getReservedKeywords()) {
            syntaxHi(pos, kw);
        }

        Collections.sort(pos, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return new CompareToBuilder().append(o1[0], o2[0]).toComparison();
            }
        });

        String hqltext = this.hql.getText().toLowerCase();

        Iterator<int[]> iterator = new ArrayList<int[]>(pos).iterator();

        if (iterator.hasNext()) {
            int[] pair1 = iterator.next();
            while (iterator.hasNext()) {
                int[] pair2 = iterator.next();
                if (pair1[1] + 1 == pair2[0] && (hqltext.charAt(pair1[1]) == ' ' || hqltext.charAt(pair1[1]) == '\t')) {
                    pair1[1] = pair2[1];
                    pos.remove(pair2);
                } else {
                    pair1 = pair2;
                }
            }
            for (int[] p : pos) {
                try {
                    this.hql.addHighlight(p[0], p[1], syntaxHighlight);
                } catch (BadLocationException ex) {
                    logger.error("hilightSyntax()", ex); //$NON-NLS-1$
                }
            }
        }
    }

    protected void syntaxHi(List<int[]> pos, String str) {
        str = str.toLowerCase();
        String hqltext = this.hql.getText().toLowerCase();
        int fromIndex = 0;
        int start = hqltext.indexOf(str, fromIndex);
        while (start != -1) {
            int end = start + str.length();
            boolean before = start == 0 || isSeperator(hqltext.charAt(start - 1));
            boolean after = (end >= (hqltext.length() - 1)) || isSeperator(hqltext.charAt(end));
            if (before && after) {
                try {
                    pos.add(new int[] { start, end });
                } catch (Exception ex) {
                    //
                }
            }
            start = hqltext.indexOf(str, end);
        }
    }

    private boolean isSeperator(char c) {
        return Character.isWhitespace(c) || c == '\'' || c == '(' || c == ')' || c == ',' || c == '-' || c == '/';
    }

    protected void highlight_color() {
        Color color = getHighlightColor();
        color = JColorChooser.showDialog(null, HqlResourceBundle.getMessage("Choose HQL highlight color"), color);
        hiliColorAction.setValue(color);

        this.hql.removeHighlights(syntaxErrorsHighlight);
        this.hql.removeHighlights(syntaxHighlight);
        this.hql.removeHighlights(bracesHighlight);

        syntaxHighlight.setColor(color);
        bracesHighlight.setColor(color);
    }

    private Color getHighlightColor() {
        return hiliColorAction.getValue() == null ? new Color(0, 0, 255) : (Color) hiliColorAction.getValue();
    }

    private Color chooseColor() {
        Color color = getSearchColor();
        color = JColorChooser.showDialog(frame, HqlResourceBundle.getMessage("Choose search highlight color"), color);
        return color;
    }

    protected void search_color() {
        Color chooseColor = chooseColor();
        if(chooseColor!=null)
		searchColorAction.setValue(applyColor(chooseColor));
    }

    private Color applyColor(Color color) {
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);
        Color gradientColor = calcGradient(color);
        ETextAreaFillHighlightPainter painter = ETextAreaFillHighlightPainter.class.cast(hql.getHighlightPainter());
        painter.setVerticalGradient(true);
        painter.setColor(color);
        painter.setGradientColor(gradientColor);
        return color;
    }

    private Color calcGradient(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
    }

    private Color getSearchColor() {
        return searchColorAction.getValue() == null ? new Color(245, 225, 145) : (Color) searchColorAction.getValue();
    }

    private void reloadColor() {
        applyColor(getSearchColor());
    }

    protected void Lucene_query_syntax() {
        try {
            Desktop.getDesktop().browse(URI.create("http://lucene.apache.org/core/old_versioned_docs/versions/3_5_0/queryparsersyntax.html"));
        } catch (Exception ex) {
            logger.error("Lucene_query_syntax()", ex); //$NON-NLS-1$
        }
    }

    protected void hibernate_properties() {
        int selectedRow = resultsEDT.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no row selected"), "", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedColumn = resultsEDT.getSelectedColumn();
        if (selectedColumn == -1) {
            JOptionPane.showMessageDialog(frame, HqlResourceBundle.getMessage("no col selected"), "", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object value = resultsEDT.getValueAt(selectedRow, selectedColumn);
        if (value == null) {
            return;
        }
        Class<?> clas = value.getClass();
        String classname = clas.getName();
        List<String> propertyNames = hqlService.getProperties(classname);
        StringBuilder sb = new StringBuilder();
        propertyNames.remove("id");
        propertyNames.remove("version");
        for (int i = 0; i < propertyNames.size() - 1; i++) {
            sb.append(propertyNames.get(i)).append(",").append(getNewline());
        }
        if (propertyNames.size() > 0) {
            sb.append(propertyNames.get(propertyNames.size() - 1));
        }
        clipboard.setContents(new StringSelection(sb.toString()), getClipboardOwner());
    }

    public static int find(String t, int pos) {
        try {
            if (t.charAt(pos) == '(') {
                return zoekHaakje(t, pos, +1);
            } else if (t.charAt(pos) == ')') {
                return zoekHaakje(t, pos, -1);
            } else {
                return -1;
            }
        } catch (StringIndexOutOfBoundsException ex) {
            return -1;
        }
    }

    public static int zoekHaakje(String t, int startpos, int direction) {
        int position = startpos + direction;
        int count = direction;
        while (true) {
            char c = t.charAt(position);
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
            }
            if (count == 0) {
                break;
            }
            position = position + direction;
        }
        return position;
    }

    private void hilightBraces(String hqltext) {
        hql.removeHighlights(bracesHighlight);

        if (!hiliAction.isSelected()) {
            return;
        }

        try {
            int p1 = hql.getCaret().getDot();
            int p2 = hql.getCaret().getMark();
            int min = Math.min(p1, p2);
            int max = Math.min(p1, p2);
            int len = max - min;
            if (len > 1) {
                return;
            }

            int caret = min;
            int match = find(hqltext, caret);
            if (match != -1) {
                try {
                    this.hql.addHighlight(caret, caret + 1, bracesHighlight);
                    this.hql.addHighlight(match, match + 1, bracesHighlight);
                } catch (BadLocationException ex) {
                    logger.error("hilightBraces(String)", ex); //$NON-NLS-1$
                }
            }
        } catch (Exception ex) {
            //
        }
    }

    protected void load_named_query() {
        final Map<String, String> namedQueries = hqlService.getNamedQueries();
        final EList<String> list = new EList<String>(new EListConfig());
        list.addRecords(EList.convert(new TreeSet<String>(namedQueries.keySet())));
        JPanel container = new JPanel(new BorderLayout());
        container.add(new JScrollPane(list), BorderLayout.CENTER);
        container.add(list.getSearchComponent(), BorderLayout.NORTH);
        if (ResultType.OK == CustomizableOptionPane.showCustomDialog(hql, container, HqlResourceBundle.getMessage(LOAD_NAMED_QUERY),
                MessageType.QUESTION, OptionType.OK_CANCEL, null, new ListOptionPaneCustomizer<String>(list))) {
            if (list.getSelectedRecord() != null) {
                clear();
                hql.setText(namedQueries.get(list.getSelectedRecord().get()));
            }
        }
    }

    public void find_parameters() {
        parametersEDT.removeAllRecords();
        for (QueryParameter p : hqlService.findParameters(getHqlText())) {
            parametersEDT.addRecord(new EListRecord<QueryParameter>(p));
        }
    }

    private HibernateWebResolver getHibernateWebResolver() {
        if (hibernateWebResolver == null) {
            hibernateWebResolver = hqlService.getHibernateWebResolver();
        }
        return this.hibernateWebResolver;
    }

    protected void delete_inverted_selection() {
        Caret caret = hql.getCaret();
        int p1 = caret.getDot();
        int p2 = caret.getMark();
        // System.out.println(p1 + "/" + p2);
        if (p1 == p2) {
            return;
        }
        if (p1 > p2) {
            int tmp = p1;
            p1 = p2;
            p2 = tmp;
        }
        hql.setText(hql.getText().substring(p1, p2));
        hql.setCaret(0);
    }

    protected void add_parameter() {
        ingoreParameterListSelectionListener = true;

        parametersEDT.clearSelection();
        clearParameter();

        ingoreParameterListSelectionListener = false;
    }

    protected void remove() {
        ingoreParameterListSelectionListener = true;

        if (parametersEDT.getSelectedRecords().size() == 0) {
            parametersEDT.removeAllRecords();
        } else {
            parametersEDT.removeSelectedRecords();
        }
        clearParameter();

        ingoreParameterListSelectionListener = false;
    }

    protected void save() {
        ingoreParameterListSelectionListener = true;

        String text = parameterBuilder.getText();
        String name = (parameterName.getText().length() > 0) ? parameterName.getText() : null;
        Object value = selectedQueryParameter.getValue();

        boolean contains = false;
        for (EListRecord<QueryParameter> record : parametersEDT.getRecords()) {
            if (record.get().equals(selectedQueryParameter)) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            EListRecord<QueryParameter> record = new EListRecord<QueryParameter>(selectedQueryParameter);
            parametersEDT.addRecord(record);
            parametersEDT.setSelectedRecord(record);
        }

        selectedQueryParameter.setText(text);
        selectedQueryParameter.setName(name);
        selectedQueryParameter.setValue(value);

        ingoreParameterListSelectionListener = false;

        parametersEDT.repaint();
    }

    private void parameterSelected() {
        if (ingoreParameterListSelectionListener) {
            return;
        }

        EListRecord<QueryParameter> selected = parametersEDT.getSelectedRecord();

        if (selected == null) {
            clearParameter();
            return;
        }

        selectedQueryParameter = parametersEDT.getSelectedRecord().get();
        parameterValue.setText(selectedQueryParameter.toString());
        parameterName.setText((selectedQueryParameter.getName() == null) ? "" : selectedQueryParameter.getName());
        parameterBuilder.setText(selectedQueryParameter.getText());
    }

    protected void set_null() {
        EListRecord<QueryParameter> selected = parametersEDT.getSelectedRecord();
        if (selected == null) {
            return;
        }
        parameterBuilder.setText("");
        save();
    }
}
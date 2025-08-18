package com.xaldon.opendma.swingmanager.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.opendma.api.OdmaCommonNames;
import org.opendma.api.OdmaId;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaProperty;
import org.opendma.api.OdmaProperty.PropertyResolutionState;
import org.opendma.api.OdmaQName;
import org.opendma.api.OdmaSearchResult;
import org.opendma.api.OdmaSession;
import org.opendma.api.OdmaType;
import org.opendma.exceptions.OdmaPropertyNotFoundException;

import com.xaldon.opendma.swingmanager.dialogs.ObjectPropertiesDialog;

public class SearchView extends JPanel
{

    private static final long serialVersionUID = -4421432551832213653L;
    
    protected final static int MAX_RESULT_SET = 5000;
    
    protected final static String[] FIXED_COLUMN_NAMES = { "ID", "Class" };

    protected OdmaSession session;
    
    protected OdmaId repoId;
    
    protected JSplitPane splitPane;
    protected JTextArea searchTextArea;
    protected JTable resultsTable;
    protected JButton searchButton;
    protected JComboBox<OdmaQName> languageComboBox;
    protected JPopupMenu tableContextMenu = new JPopupMenu();
    
    protected ArrayList<OdmaObject> resultSet = new ArrayList<OdmaObject>(0);

    protected LinkedList<OdmaQName> extraColumns = null;
    
    protected OdmaObject contextMenuItem;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    public SearchView(OdmaSession session, OdmaId repoId)
    {
        this.session = session;
        this.repoId = repoId;
        setLayout(new BorderLayout());
        initializeComponents();
    }
    
    protected void initializeComponents()
    {
        // Create toolbar
        JToolBar toolbar = createToolbar();
        
        // Create search text area
        searchTextArea = new JTextArea();
        searchTextArea.setRows(5);
        searchTextArea.setLineWrap(true);
        searchTextArea.setWrapStyleWord(true);
        JScrollPane textScrollPane = new JScrollPane(searchTextArea);
        textScrollPane.setBorder(BorderFactory.createTitledBorder("Search Query"));
        
        // Create results table
        resultsTable = new JTable();
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));
        
        // build table context menu
        JMenuItem menuItem = new JMenuItem("Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ObjectPropertiesDialog.showForObject(contextMenuItem);
            }});
        tableContextMenu.add(menuItem);

        // add content menu to view table
        MouseListener popupListener = new MouseAdapter(){
            public void mousePressed(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e)
            {
                if(!e.isPopupTrigger())
                {
                    return;
                }
                int row = resultsTable.rowAtPoint(new Point(e.getX(), e.getY()));
                resultsTable.getSelectionModel().setSelectionInterval(row,row);
                contextMenuItem = resultSet.get(row);
                tableContextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        };
        resultsTable.addMouseListener(popupListener);
        
        // Create split pane
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScrollPane, tableScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
        splitPane.setResizeWeight(0.3);
        
        // Add components to panel
        add(toolbar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    protected JToolBar createToolbar()
    {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        // Search button
        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Language selection combo box
        languageComboBox = new JComboBox<OdmaQName>();
        languageComboBox.setModel(new DefaultComboBoxModel<OdmaQName>(
            new OdmaQName[] {
                    new OdmaQName("cmod", "sql"),
                    new OdmaQName("opendma", "json-simple")
            }
        ));
        languageComboBox.setPreferredSize(new Dimension(200, languageComboBox.getPreferredSize().height));
        languageComboBox.setMaximumSize(languageComboBox.getPreferredSize());
        
        // Add components to toolbar
        toolbar.add(languageComboBox);
        toolbar.addSeparator();
        toolbar.add(searchButton);
        
        return toolbar;
    }
    
    protected void performSearch()
    {
        String query = searchTextArea.getText();
        OdmaQName language = (OdmaQName) languageComboBox.getSelectedItem();
        resultSet.clear();
        extraColumns = null;
        try
        {
            OdmaSearchResult result = session.search(repoId, language, query);
            resultSet.ensureCapacity(Math.min(result.getSize(), MAX_RESULT_SET));
            for(OdmaObject o : result.getObjects())
            {
                o.prepareProperties(new OdmaQName[] {OdmaCommonNames.PROPERTY_ID,OdmaCommonNames.PROPERTY_CLASS}, false);
                o.getOdmaClass().prepareProperties(new OdmaQName[] {OdmaCommonNames.PROPERTY_NAMESPACE,OdmaCommonNames.PROPERTY_NAME}, false);
                resultSet.add(o);
                if(extraColumns == null)
                {
                    extraColumns = getDisplayablePropertyNames(o);
                }
                else
                {
                    extraColumns.retainAll(getDisplayablePropertyNames(o));
                }
                if(resultSet.size() >= MAX_RESULT_SET)
                {
                    break;
                }
            }
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(
                SearchView.this,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        resultsTable.setModel(createTableModel());
    }
    
    protected TableModel createTableModel()
    {
        return new AbstractTableModel(){

            private static final long serialVersionUID = 3785655523283899607L;

            public int getRowCount()
            {
                return resultSet.size();
            }

            public String getColumnName(int col)
            {
                if(col < FIXED_COLUMN_NAMES.length)
                {
                    return FIXED_COLUMN_NAMES[col];
                }
                else
                {
                    return extraColumns.get(col-FIXED_COLUMN_NAMES.length).toString();
                }
            }

            public int getColumnCount()
            {
                return FIXED_COLUMN_NAMES.length + extraColumns.size();
            }

            public Object getValueAt(int rowIndex, int columnIndex)
            {
                OdmaObject obj = resultSet.get(rowIndex);
                if(columnIndex == 0)
                {
                    return obj.getId().toString();
                }
                else if(columnIndex == 1)
                {
                    return obj.getOdmaClass().getQName().toString();
                }
                else
                {
                    OdmaQName propName = extraColumns.get(columnIndex-FIXED_COLUMN_NAMES.length);
                    try
                    {
                        OdmaProperty prop = obj.getProperty(propName);
                        if(prop.getValue() == null)
                        {
                            return "<null>";
                        }
                        if(prop.getType() == OdmaType.DATETIME)
                        {
                            return dateFormat.format((Date)prop.getValue());
                        }
                        else
                        {
                            return prop.getValue().toString();
                        }
                    }
                    catch (OdmaPropertyNotFoundException e)
                    {
                        return "--";
                    }
                }
            }
        };
    }
    
    protected LinkedList<OdmaQName> getDisplayablePropertyNames(OdmaObject obj)
    {
        LinkedList<OdmaQName> result = new LinkedList<OdmaQName>();
        Iterator<OdmaProperty> itProp = obj.availableProperties();
        while(itProp.hasNext())
        {
            OdmaProperty prop = itProp.next();
            if(prop.getResolutionState() == PropertyResolutionState.UNRESOLVED)
            {
                continue;
            }
            if(prop.getName().equals(OdmaCommonNames.PROPERTY_ID) || prop.getName().equals(OdmaCommonNames.PROPERTY_GUID))
            {
                continue;
            }
            switch(prop.getType())
            {
            case STRING:
            case INTEGER:
            case SHORT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN:
            case DATETIME:
            case ID:
            case GUID:
                result.add(prop.getName());
            default:
                break;
            }
        }
        return result;
    }

} 
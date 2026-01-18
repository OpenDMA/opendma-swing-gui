package com.xaldon.opendma.swingmanager.treenodes;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opendma.api.OdmaClass;
import org.opendma.api.OdmaCommonNames;
import org.opendma.api.OdmaPropertyInfo;
import org.opendma.api.OdmaType;

import com.xaldon.opendma.swingmanager.dialogs.ObjectPropertiesDialog;
import com.xaldon.opendma.swingmanager.treemodel.ContextMenuNode;
import com.xaldon.opendma.swingmanager.treemodel.IconNode;
import com.xaldon.opendma.swingmanager.treemodel.ViewNode;

public class ClassNode extends DefaultMutableTreeNode implements IconNode, ContextMenuNode, ViewNode
{
    
    private static final long serialVersionUID = -4056310757313058899L;

    protected OdmaClass odmaClass;
    
    protected boolean childrenCreated = false;
    
    protected static Icon classIcon = new ImageIcon(ClassNode.class.getResource("/icons/class_obj.gif"));
    
    protected static Icon aspectIcon = new ImageIcon(ClassNode.class.getResource("/icons/aspect.png"));
    
    protected JPopupMenu treeContextMenu = new JPopupMenu();
    
    protected JPopupMenu tableContextMenu = new JPopupMenu();
    
    protected JTable tableView;
    
    protected JScrollPane scrollView;
    
    protected OdmaPropertyInfo contextMenuItem;
    
    private boolean columnModelSet = false;
    
    private boolean ignoreColumnChanges = false;
    
    protected static int[] columnWidth;
    
    public ClassNode(OdmaClass cls)
    {
        odmaClass = cls;
        tableView = new JTable();
        tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollView = new JScrollPane(tableView);
        // build tree context menu
        JMenuItem menuItem = new JMenuItem("Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ObjectPropertiesDialog.showForObject(odmaClass);
            }});
        treeContextMenu.add(menuItem);
        // build table context menu
        menuItem = new JMenuItem("Properties");
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
                int row = tableView.rowAtPoint(new Point(e.getX(), e.getY()));
                tableView.getSelectionModel().setSelectionInterval(row,row);
                contextMenuItem = propertyInfos.get(row);
                tableContextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        };
        tableView.addMouseListener(popupListener);
        tableView.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                // No action needed for column addition
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                // No action needed for column removal
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                // No action needed for column movement
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                if(ignoreColumnChanges) {
                    return;
                }
                int columnCount = tableView.getColumnModel().getColumnCount();
                if(columnWidth == null || columnWidth.length != columnCount) {
                    columnWidth = new int[columnCount];
                }
                for (int i = 0; i < columnCount; i++) {
                    int width = tableView.getColumnModel().getColumn(i).getWidth();
                    columnWidth[i] = width;
                }
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
                // No action needed for column selection changes
            }
        });
    }
    
    public Icon getIcon()
    {
        return isAspect() ? aspectIcon : classIcon;
    }
    
    public JPopupMenu getContextMenu()
    {
        return treeContextMenu;
    }
    
    public Component getViewComponent()
    {
        if(!columnModelSet) {
            tableView.setModel(createTableModel());
            columnModelSet = true;
        }
        int columnCount = tableView.getColumnModel().getColumnCount();
        if(columnWidth != null && columnWidth.length == columnCount) {
            ignoreColumnChanges = true;
            for (int i = 0; i < columnCount; i++) {
                tableView.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
            }
            ignoreColumnChanges = false;
        }
        return scrollView;
    }
    
    protected Boolean isAspectCache;
    
    public boolean isAspect()
    {
        if(isAspectCache != null)
        {
            return isAspectCache.booleanValue();
        }
        OdmaClass test = odmaClass;
        boolean isValidClass = false;
        while(test != null)
        {
            if(test.getQName().equals(OdmaCommonNames.CLASS_OBJECT))
            {
                isValidClass = true;
            }
            test = test.getSuperClass();
        }
        isAspectCache = Boolean.valueOf(!isValidClass);
        return isAspectCache.booleanValue();
    }
    
    public boolean isLeaf()
    {
        return false;
    }

    public int getChildCount()
    {
      if(!childrenCreated)
      {
          createChildren();
      }
      return super.getChildCount();
    }
    
    protected void createChildren()
    {
        childrenCreated = true;
        Iterable<OdmaClass> subClasses = odmaClass.getSubClasses();
        Iterator<OdmaClass> itSubClasses = subClasses.iterator();
        while(itSubClasses.hasNext())
        {
            OdmaClass subClass = (OdmaClass)itSubClasses.next();
            add(new ClassNode(subClass));
        }
    }

    public String toString()
    {
        return odmaClass.getQName().toString();
    }
    
    public String[] columnNames = { "Name", "DisplayName", "Type", "Multivalue", "Required", "ReadOnly", "Hidden", "ReferenceClass", "Id" };
    
    protected ArrayList<OdmaPropertyInfo> propertyInfos;
    
    protected TableModel createTableModel()
    {
        Iterable<OdmaPropertyInfo> declaredProperties = odmaClass.getDeclaredProperties();
        propertyInfos = new ArrayList<OdmaPropertyInfo>();
        Iterator<OdmaPropertyInfo> it = declaredProperties.iterator();
        while(it.hasNext())
        {
            propertyInfos.add((OdmaPropertyInfo)it.next());
        }
        final Object data[][] = new Object[propertyInfos.size()][columnNames.length];
        for(int i = 0; i < propertyInfos.size(); i++)
        {
            OdmaPropertyInfo pi = (OdmaPropertyInfo)propertyInfos.get(i);
            data[i][0] = pi.getQName().toString();
            data[i][1] = pi.getDisplayName();
            Integer dataType = pi.getDataType();
            String dataTypeDisplay;
            try
            {
                dataTypeDisplay = OdmaType.fromNumericId(dataType).toString();
            }
            catch(IllegalArgumentException iae)
            {
                dataTypeDisplay = "unknown ("+dataType.toString()+")";
            }
            data[i][2] = dataTypeDisplay;
            data[i][3] = pi.isMultiValue();
            data[i][4] = pi.isRequired();
            data[i][5] = pi.isReadOnly();
            data[i][6] = pi.isHidden();
            OdmaClass referenceClass = pi.getReferenceClass();
            data[i][7] = referenceClass == null ? null : referenceClass.getQName();
            data[i][8] = pi.getId();
        }
        return new AbstractTableModel(){

            private static final long serialVersionUID = 3785655523283899607L;

            public int getRowCount()
            {
                return data.length;
            }

            public String getColumnName(int col)
            {
                return columnNames[col];
            }

            public int getColumnCount()
            {
                return columnNames.length;
            }

            public Object getValueAt(int rowIndex, int columnIndex)
            {
                return data[rowIndex][columnIndex];
            }};
    }

}

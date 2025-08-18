package com.xaldon.opendma.swingmanager.treenodes;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opendma.api.OdmaClass;
import org.opendma.api.OdmaCommonNames;
import org.opendma.api.OdmaPropertyInfo;

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
        tableView.setModel(createTableModel());
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
            String dataTypeDisplay = (String)datatypeValues.get(dataType);
            data[i][2] = dataTypeDisplay != null ? dataTypeDisplay : "Unknown ("+dataType.toString()+")";
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
                // return lst.size();
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
                /*
                OdmaPropertyInfo pi = (OdmaPropertyInfo)lst.get(rowIndex);
                switch(columnIndex)
                {
                case 0:
                    return pi.getQName().toString();
                case 1:
                    return pi.getDisplayName();
                case 2:
                    Integer dataType = pi.getDataType();
                    String dataTypeDisplay = (String)datatypeValues.get(dataType);
                    return dataTypeDisplay != null ? dataTypeDisplay : "Unknown ("+dataType.toString()+")";
                case 3:
                    return pi.getMultiValue();
                case 4:
                    return pi.getRequired();
                case 5:
                    return pi.getReadOnly();
                case 6:
                    return pi.getHidden();
                case 7:
                    OdmaClass referenceClass = pi.getReferenceClass();
                    return referenceClass == null ? null : referenceClass.getQName();
                case 8:
                    return pi.getId();
                default:
                    return "";
                }
                */
            }};
    }

    protected static Map<Integer, String> datatypeValues = new HashMap<Integer, String>();
    
    static
    {
        datatypeValues.put(new Integer(1),"string");
        datatypeValues.put(new Integer(2),"integer");
        datatypeValues.put(new Integer(3),"short");
        datatypeValues.put(new Integer(4),"long");
        datatypeValues.put(new Integer(5),"float");
        datatypeValues.put(new Integer(6),"double");
        datatypeValues.put(new Integer(7),"boolean");
        datatypeValues.put(new Integer(8),"datetime");
        datatypeValues.put(new Integer(9),"blob");
        datatypeValues.put(new Integer(10),"reference");
        datatypeValues.put(new Integer(11),"content");
        datatypeValues.put(new Integer(100),"id");
        datatypeValues.put(new Integer(101),"guid");
    }

}

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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opendma.api.OdmaAssociation;
import org.opendma.api.OdmaDocument;
import org.opendma.api.OdmaFolder;
import org.opendma.api.OdmaObject;
import org.opendma.api.collections.OdmaAssociationEnumeration;
import org.opendma.api.collections.OdmaFolderEnumeration;

import com.xaldon.opendma.swingmanager.TableModelProvider;
import com.xaldon.opendma.swingmanager.dialogs.PropertiesWindow;
import com.xaldon.opendma.swingmanager.treemodel.ContextMenuNode;
import com.xaldon.opendma.swingmanager.treemodel.ViewNode;

public class FolderNode extends DefaultMutableTreeNode implements TableModelProvider, ContextMenuNode, ViewNode
{
    
    private static final long serialVersionUID = -4056310757313058899L;

    protected OdmaFolder odmaFolder;
    
    protected String nameOverwrite;
    
    protected boolean childrenCreated = false;
    
    protected JPopupMenu treeContextMenu = new JPopupMenu();
    
    protected JPopupMenu tableContextMenu = new JPopupMenu();
    
    protected JTable viewComponent = new JTable();
    
    protected OdmaAssociation contextMenuItem;
    
    public FolderNode(OdmaFolder folder)
    {
        this.odmaFolder = folder;
        // build tree context menu
        JMenuItem menuItem = new JMenuItem("Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PropertiesWindow.showForObject(odmaFolder);
            }});
        treeContextMenu.add(menuItem);
        // build table context menu
        menuItem = new JMenuItem("Association Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PropertiesWindow.showForObject(contextMenuItem);
            }});
        tableContextMenu.add(menuItem);
        menuItem = new JMenuItem("Containment Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PropertiesWindow.showForObject(contextMenuItem.getContainment());
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
                int row = viewComponent.rowAtPoint(new Point(e.getX(), e.getY()));
                viewComponent.getSelectionModel().setSelectionInterval(row,row);
                contextMenuItem = associations.get(row);
                tableContextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        };
        viewComponent.addMouseListener(popupListener);
    }
    
    public FolderNode(OdmaFolder folder, String name)
    {
        this(folder);
        this.nameOverwrite = name;
    }
    
    public JPopupMenu getContextMenu()
    {
        return treeContextMenu;
    }
    
    public Component getViewComponent()
    {
        viewComponent.setModel(getTableModel());
        return viewComponent;
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
        System.out.println("Getting sub folders...");
        OdmaFolderEnumeration subFolders = odmaFolder.getSubFolders();
        Iterator itSubFolders = subFolders.iterator();
        while(itSubFolders.hasNext())
        {
            System.out.println("Creating folder node...");
            OdmaFolder subFolder = (OdmaFolder)itSubFolders.next();
            add(new FolderNode(subFolder));
        }
        System.out.println("Done.");
    }

    public String toString()
    {
        return nameOverwrite != null ? nameOverwrite : odmaFolder.getTitle();
    }
    
    public String[] columnNames = { "Name", "Class", "FiledOn", "FiledBy", "Id", "Title", "Version", "MimeType", "CheckedOut" };
    
    protected ArrayList<OdmaAssociation> associations;

    public TableModel getTableModel()
    {
        OdmaAssociationEnumeration assocs = odmaFolder.getAssociations();
        associations = new ArrayList<OdmaAssociation>();
        Iterator it = assocs.iterator();
        while(it.hasNext())
        {
            associations.add((OdmaAssociation)it.next());
        }
        final Object data[][] = new Object[associations.size()][columnNames.length];
        for(int i = 0; i < associations.size(); i++)
        {
            OdmaAssociation assoc = associations.get(i);
            OdmaObject containment = assoc.getContainment();
            data[i][0] = assoc.getName();
            data[i][1] = containment.getOdmaClass().getQName();
            data[i][2] = assoc.getCreatedAt();
            data[i][3] = assoc.getCreatedBy();
            data[i][4] = containment.getId();
            if(containment instanceof OdmaDocument)
            {
                OdmaDocument doc = (OdmaDocument)containment;
                data[i][5] = doc.getTitle();
                data[i][6] = doc.getVersion();
                data[i][7] = doc.getCombinedContentType();
                data[i][8] = doc.getCheckedOut();
            }
        }
        return new AbstractTableModel(){

            private static final long serialVersionUID = 3785655523283899607L;

            public int getRowCount()
            {
                //return lst.size();
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
                OdmaAssociation assoc = (OdmaAssociation)lst.get(rowIndex);
                switch(columnIndex)
                {
                case 0:
                    return assoc.getName();
                case 1:
                    return assoc.getContainment().getOdmaClass().getQName();
                case 2:
                    return assoc.getCreatedAt();
                case 3:
                    return assoc.getCreatedBy();
                case 4:
                    return assoc.getContainment().getId();
                default:
                    OdmaObject containment = assoc.getContainment();
                    if(containment instanceof OdmaDocument)
                    {
                        OdmaDocument doc = (OdmaDocument)containment;
                        switch(columnIndex)
                        {
                        case 5:
                            return doc.getTitle();
                        case 6:
                            return doc.getVersion();
                        case 7:
                            return doc.getCombinedMimeType();
                        case 8:
                            return doc.getCheckedOut();
                        default:
                            return "";
                        }
                    }
                    else
                    {
                        return "";
                    }
                }
                */
            }};
    }

}

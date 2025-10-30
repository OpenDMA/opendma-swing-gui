package com.xaldon.opendma.swingmanager.treenodes;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

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

import org.opendma.api.OdmaAssociation;
import org.opendma.api.OdmaAuditStamped;
import org.opendma.api.OdmaContainable;
import org.opendma.api.OdmaDocument;
import org.opendma.api.OdmaFolder;
import org.opendma.api.OdmaId;
import org.opendma.api.OdmaObject;

import com.xaldon.opendma.swingmanager.dialogs.ObjectPropertiesDialog;
import com.xaldon.opendma.swingmanager.treemodel.ContextMenuNode;
import com.xaldon.opendma.swingmanager.treemodel.ViewNode;

public class FolderNode extends DefaultMutableTreeNode implements ContextMenuNode, ViewNode
{
    
    private static final long serialVersionUID = -4056310757313058899L;

    private static final int MAX_LIST_SIZE = 10000;

    protected OdmaFolder odmaFolder;
    
    protected String nameOverwrite;
    
    protected boolean childrenCreated = false;
    
    protected JPopupMenu treeContextMenu = new JPopupMenu();
    
    protected JPopupMenu tableContextMenu = new JPopupMenu();
    
    protected JTable tableView;
    
    protected JScrollPane scrollView;
    
    protected ListEntry contextMenuItem;
    
    private boolean columnModelSet = false;
    
    private boolean ignoreColumnChanges = false;
    
    protected static int[] columnWidth;
    
    public FolderNode(OdmaFolder folder)
    {
        odmaFolder = folder;
        tableView = new JTable();
        tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollView = new JScrollPane(tableView);
        // build tree context menu
        JMenuItem menuItem = new JMenuItem("Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ObjectPropertiesDialog.showForObject(odmaFolder);
            }});
        treeContextMenu.add(menuItem);
        // build table context menu
        menuItem = new JMenuItem("Object Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ObjectPropertiesDialog.showForObject(contextMenuItem.containable);
            }});
        tableContextMenu.add(menuItem);
        menuItem = new JMenuItem("Association Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(contextMenuItem.association != null)
                {
                    ObjectPropertiesDialog.showForObject(contextMenuItem.association);
                }
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
                contextMenuItem = listEntries.get(row);
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
        Iterable<OdmaFolder> subFolders = odmaFolder.getSubFolders();
        Iterator<OdmaFolder> itSubFolders = subFolders.iterator();
        while(itSubFolders.hasNext())
        {
            OdmaFolder subFolder = (OdmaFolder)itSubFolders.next();
            add(new FolderNode(subFolder));
        }
    }

    public String toString()
    {
        return nameOverwrite != null ? nameOverwrite : odmaFolder.getTitle();
    }
    
    public String[] columnNames = { "Name", "Class", "FiledOn", "FiledBy", "Id", "Title", "Version", "MimeType", "CheckedOut" };
    
    private class ListEntry
    {
        OdmaAssociation association;
        OdmaObject containable;
    }
    
    protected ArrayList<ListEntry> listEntries;

    protected TableModel createTableModel()
    {
        listEntries = new ArrayList<ListEntry>();
        HashSet<OdmaId> containeesInList = new HashSet<OdmaId>();
        for(OdmaAssociation assoc : odmaFolder.getAssociations())
        {
            ListEntry entry = new ListEntry();
            entry.association = assoc;
            entry.containable = assoc.getContainable();
            containeesInList.add(entry.containable.getId());
            listEntries.add(entry);
            if(listEntries.size() >= MAX_LIST_SIZE)
            {
                break;
            }
        }
        if(listEntries.size() < MAX_LIST_SIZE)
        {
            for(OdmaContainable cont : odmaFolder.getContainees())
            {
                if(containeesInList.contains(cont.getId()))
                {
                    continue;
                }
                ListEntry entry = new ListEntry();
                entry.association = null;
                entry.containable = cont;
                containeesInList.add(cont.getId());
                listEntries.add(entry);
                if(listEntries.size() >= MAX_LIST_SIZE)
                {
                    break;
                }
            }
        }
        final Object data[][] = new Object[listEntries.size()][columnNames.length];
        for(int i = 0; i < listEntries.size(); i++)
        {
            ListEntry entry = listEntries.get(i);
            String name;
            if(entry.association != null && entry.association.getName() != null && !entry.association.getName().trim().isEmpty())
            {
                name = entry.association.getName();
            }
            else if(entry.containable instanceof OdmaDocument)
            {
                name = ((OdmaDocument)entry.containable).getTitle();
            }
            else
            {
                name = entry.containable.getId().toString();
            }
            Date createdAt;
            String createdBy;
            if(entry.association instanceof OdmaAuditStamped)
            {
                createdAt = ((OdmaAuditStamped)entry.association).getCreatedAt();
                createdBy = ((OdmaAuditStamped)entry.association).getCreatedBy();
            }
            else if(entry.containable instanceof OdmaAuditStamped)
            {
                createdAt = ((OdmaAuditStamped)entry.containable).getCreatedAt();
                createdBy = ((OdmaAuditStamped)entry.containable).getCreatedBy();
            }
            else
            {
                createdAt = null;
                createdBy = null;
            }
            
            data[i][0] = name;
            data[i][1] = entry.containable.getOdmaClass().getQName();
            data[i][2] = createdAt == null ? "" : createdAt;
            data[i][3] = createdBy == null ? "" : createdBy;
            data[i][4] = entry.containable.getId();
            if(entry.containable instanceof OdmaDocument)
            {
                OdmaDocument doc = (OdmaDocument)entry.containable;
                data[i][5] = doc.getTitle();
                data[i][6] = doc.getVersion();
                data[i][7] = doc.getCombinedContentType();
                data[i][8] = doc.isCheckedOut();
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
            }
        };
    }

}

package com.xaldon.opendma.swingmanager.treenodes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opendma.api.OdmaFolder;
import org.opendma.api.OdmaRepository;

import com.xaldon.opendma.swingmanager.TableModelProvider;
import com.xaldon.opendma.swingmanager.dialogs.ObjectPropertiesDialog;
import com.xaldon.opendma.swingmanager.treemodel.ContextMenuNode;
import com.xaldon.opendma.swingmanager.treemodel.IconNode;

public class RepositoryNode extends DefaultMutableTreeNode implements TableModelProvider, IconNode, ContextMenuNode
{
    
    private static final long serialVersionUID = -8753719899842441268L;

    protected OdmaRepository odmaRepository;
    
    protected boolean childrenCreated = false;
    
    protected static Icon repositoryIcon = new ImageIcon("database.png");
    
    protected JPopupMenu contextMenu = new JPopupMenu();
    
    public RepositoryNode(OdmaRepository repository)
    {
        this.odmaRepository = repository;
        // build context menu
        JMenuItem menuItem = new JMenuItem("Properties");
        menuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ObjectPropertiesDialog.showForObject(odmaRepository);
            }});
        contextMenu.add(menuItem);
    }
    
    public Icon getIcon()
    {
        return repositoryIcon;
    }
    
    public JPopupMenu getContextMenu()
    {
        return contextMenu;
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
        add(new ClassNode(odmaRepository.getRootClass()));
        add(new RootAspectsNode(odmaRepository.getRootAspects()));
        OdmaFolder rootFolder = odmaRepository.getRootFolder();
        if(rootFolder != null)
        {
            add(new FolderNode(rootFolder,"Root Folder"));
        }
    }

    public String toString()
    {
        return odmaRepository.getDisplayName();
    }

    public TableModel getTableModel()
    {
        return null;
    }

}

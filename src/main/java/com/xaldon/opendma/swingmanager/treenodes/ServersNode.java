package com.xaldon.opendma.swingmanager.treenodes;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import com.xaldon.opendma.swingmanager.treemodel.IconNode;

public class ServersNode extends DefaultMutableTreeNode implements IconNode
{
    
    private static final long serialVersionUID = 141017338693813409L;

    protected static Icon serversIcon = new ImageIcon(ServersNode.class.getResource("/icons/server_database.png"));
    
    public Icon getIcon()
    {
        return serversIcon;
    }
    
    public String toString()
    {
        return "OpenDMA Repositories";
    }

}

package com.xaldon.opendma.swingmanager.treenodes;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opendma.api.OdmaId;
import org.opendma.api.OdmaSession;

import com.xaldon.opendma.swingmanager.treemodel.IconNode;
import com.xaldon.opendma.swingmanager.treemodel.ViewNode;
import com.xaldon.opendma.swingmanager.views.SearchView;

public class SearchNode extends DefaultMutableTreeNode implements IconNode, ViewNode
{

    private static final long serialVersionUID = 2499149520054813615L;
    
    protected static Icon searchIcon = new ImageIcon(SearchNode.class.getResource("/icons/magnifier.png"));
    
    protected SearchView searchView;
    
    public SearchNode(OdmaSession session, OdmaId repoId)
    {
        this.searchView = new SearchView(session, repoId);
    }
    
    public Icon getIcon()
    {
        return searchIcon;
    }
    
    public Component getViewComponent()
    {
        return searchView;
    }
    
    public boolean isLeaf()
    {
        return true;
    }

    public String toString()
    {
        return "Search";
    }

} 
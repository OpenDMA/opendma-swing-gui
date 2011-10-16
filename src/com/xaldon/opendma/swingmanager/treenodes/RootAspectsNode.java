package com.xaldon.opendma.swingmanager.treenodes;

import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.opendma.api.OdmaClass;
import org.opendma.api.collections.OdmaClassEnumeration;

import com.xaldon.opendma.swingmanager.treemodel.IconNode;

public class RootAspectsNode extends DefaultMutableTreeNode implements IconNode
{
    
    private static final long serialVersionUID = -4056310757313058899L;

    protected OdmaClassEnumeration rootAspects;
    
    protected boolean childrenCreated = false;
    
    protected static Icon aspectIcon = new ImageIcon("aspect.png");
    
    public RootAspectsNode(OdmaClassEnumeration aspects)
    {
        this.rootAspects = aspects;
    }
    
    public Icon getIcon()
    {
        return aspectIcon;
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
        Iterator itSubClasses = rootAspects.iterator();
        while(itSubClasses.hasNext())
        {
            OdmaClass subClass = (OdmaClass)itSubClasses.next();
            add(new ClassNode(subClass));
        }
    }

    public String toString()
    {
        return "Root Aspects";
    }

}

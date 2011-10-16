package com.xaldon.opendma.swingmanager;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.xaldon.opendma.swingmanager.treemodel.IconNode;

public class IconNodeRenderer extends DefaultTreeCellRenderer
{

    private static final long serialVersionUID = -5909986218046218404L;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {

          super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
          if(value instanceof IconNode)
          {
              setIcon(((IconNode)value).getIcon());
          }
          return this;
        }
    
    
}

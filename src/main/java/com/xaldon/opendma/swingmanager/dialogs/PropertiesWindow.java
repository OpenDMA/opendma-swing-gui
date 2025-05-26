package com.xaldon.opendma.swingmanager.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaProperty;
import org.opendma.api.OdmaPropertyInfo;
import org.opendma.exceptions.OdmaPropertyNotFoundException;

import com.xaldon.opendma.swingmanager.SpringUtilities;

public class PropertiesWindow extends JFrame
{
    
    private static final long serialVersionUID = -5209414514832674021L;

    protected OdmaObject odmaObject;

    public String[] columnNames = { "Property", "Value" };
    
    public ArrayList<OdmaProperty> allProperties = new ArrayList<OdmaProperty>();
    
    public static void showForObject(OdmaObject obj)
    {
        new PropertiesWindow(obj);
    }

    private JPanel contentPane;

    private PropertiesWindow(OdmaObject obj)
    {
        super("Properties: "+obj.getId());
        // create content pane
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        // create properties table
        odmaObject = obj;
        Iterable<OdmaPropertyInfo> allPropertyInfos = odmaObject.getOdmaClass().getProperties();
        Iterator<?> it = allPropertyInfos.iterator();
        while(it.hasNext())
        {
            OdmaPropertyInfo pi = (OdmaPropertyInfo)it.next();
            try
            {
                allProperties.add(odmaObject.getProperty(pi.getQName()));
            }
            catch(OdmaPropertyNotFoundException onfe)
            {
                System.out.println("Object is missing declared property.");
            }
        }
        TableModel tm = new AbstractTableModel(){

            private static final long serialVersionUID = 3785655523283899607L;

            public int getRowCount()
            {
                return allProperties.size();
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
                OdmaProperty p = allProperties.get(rowIndex);
                if(columnIndex == 0)
                {
                    return p.getName();
                }
                else
                {
                    return p.getValue();
                }
            }};
        JTable propertiesTable = new JTable(tm);
        // create the info panel
        JPanel infoPanel = new JPanel(new SpringLayout());
        JLabel idLabel = new JLabel("ID :", JLabel.TRAILING);
        JLabel idDisplay = new JLabel(obj.getId().toString(), JLabel.TRAILING);
        JLabel classLabel = new JLabel("Class :", JLabel.TRAILING);
        JLabel classDisplay = new JLabel(obj.getOdmaClass().getQName().toString(), JLabel.TRAILING);
        infoPanel.add(idLabel);
        idLabel.setLabelFor(idDisplay);
        infoPanel.add(idDisplay);
        infoPanel.add(classLabel);
        classLabel.setLabelFor(classDisplay);
        infoPanel.add(classDisplay);
        SpringUtilities.makeCompactGrid(infoPanel,
                2, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
        contentPane.add(infoPanel, BorderLayout.NORTH);
        // create tabed layout
        JTabbedPane tabbedPane = new JTabbedPane();
        JScrollPane tableScrollPane = new JScrollPane(propertiesTable);
        tabbedPane.addTab("Properties", null, tableScrollPane);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        // add buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Apply"));
        buttonPanel.add(new JButton("Close"));
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        setSize(300, 400);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
        setVisible(true);
    }

}

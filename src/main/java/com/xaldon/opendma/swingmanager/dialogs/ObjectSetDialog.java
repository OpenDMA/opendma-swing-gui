package com.xaldon.opendma.swingmanager.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.opendma.api.OdmaId;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaQName;

import com.xaldon.opendma.swingmanager.SpringUtilities;

public class ObjectSetDialog extends JFrame
{
    
    private static final long serialVersionUID = -5209414514832674021L;

    public String[] columnNames = { "Object" };
    
    public ArrayList<OdmaObject> allObjects = new ArrayList<OdmaObject>();
    
    public static void showForSet(OdmaQName propertyName, OdmaId objId, Iterable<OdmaObject> set)
    {
        new ObjectSetDialog(propertyName, objId, set);
    }

    private JPanel contentPane;

    @SuppressWarnings("serial")
    private ObjectSetDialog(OdmaQName propertyName, OdmaId objId, Iterable<OdmaObject> set)
    {
        super("Object set");
        // create content pane
        contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        // create objects table
        int count = 0;
        for(OdmaObject obj : set) {
            if(count++ > 1000) {
                break;
            }
            allObjects.add(obj);
        }
        TableModel tm = new AbstractTableModel(){

            private static final long serialVersionUID = 3785655523283899607L;

            public int getRowCount()
            {
                return allObjects.size();
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
                return allObjects.get(rowIndex);
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
        };
        JTable propertiesTable = new JTable(tm) {

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                return new ObjectCellEditor();
            }
            
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return new DefaultTableCellRenderer() {
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        return  super.getTableCellRendererComponent(table, value==null?"<null>":"[Object] "+((OdmaObject)value).getId(), isSelected, hasFocus, row, column);
                    };
                };
            }

        };
        // create the info panel
        JPanel infoPanel = new JPanel(new SpringLayout());
        JLabel propLabel = new JLabel("Property :", JLabel.TRAILING);
        JLabel propDisplay = new JLabel(propertyName.toString(), JLabel.TRAILING);
        JLabel objIdLabel = new JLabel("Object :", JLabel.TRAILING);
        JLabel objIdDisplay = new JLabel(objId.toString(), JLabel.TRAILING);
        infoPanel.add(propLabel);
        propLabel.setLabelFor(propDisplay);
        infoPanel.add(propDisplay);
        infoPanel.add(objIdLabel);
        objIdLabel.setLabelFor(objIdDisplay);
        infoPanel.add(objIdDisplay);
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
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        setSize(500, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
        setVisible(true);
    }

}

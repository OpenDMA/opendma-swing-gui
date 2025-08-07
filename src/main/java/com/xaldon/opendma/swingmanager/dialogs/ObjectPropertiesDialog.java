package com.xaldon.opendma.swingmanager.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.opendma.api.OdmaGuid;
import org.opendma.api.OdmaId;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaProperty;
import org.opendma.api.OdmaPropertyInfo;
import org.opendma.api.OdmaType;
import org.opendma.exceptions.OdmaPropertyNotFoundException;

import com.xaldon.opendma.swingmanager.SpringUtilities;

public class ObjectPropertiesDialog extends JFrame
{
    
    private static final long serialVersionUID = -5209414514832674021L;

    protected OdmaObject odmaObject;

    public String[] columnNames = { "Property", "Value" };
    
    public ArrayList<OdmaProperty> allProperties = new ArrayList<OdmaProperty>();
    
    public static void showForObject(OdmaObject obj)
    {
        new ObjectPropertiesDialog(obj);
    }

    private JPanel contentPane;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @SuppressWarnings("serial")
    private ObjectPropertiesDialog(OdmaObject obj)
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
                    Object value = p.getValue();
                    if(p.getType() == OdmaType.REFERENCE) {
                        return value;
                    }
                    if(value == null)
                    {
                        return "<null>";
                    }
                    if(p.isMultiValue()) {
                        return value.toString();
                    }
                    switch(p.getType()) {
                    case STRING:
                    case INTEGER:
                    case SHORT:
                    case LONG:
                    case FLOAT:
                    case DOUBLE:
                        return value.toString();
                    case BOOLEAN:
                        return ((Boolean)value).booleanValue() ? "TRUE" : "FALSE";
                    case DATETIME:
                        return dateFormat.format((Date)value);
                    case BLOB:
                        byte[] bytes = (byte[])value;
                        String result = bytes.length + " bytes.";
                        for(int i = 0; i < Math.max(bytes.length, 10); i++) {
                            result = result + " 0x" + Integer.toHexString(bytes[i]);
                        }
                        if(bytes.length > 10) {
                            result = result + " ...";
                        }
                        return result;
                    case REFERENCE:
                        return "[OBJECT]";
                    case CONTENT:
                        return "[CONTENT]";
                    case ID:
                        return ((OdmaId)value).toString();
                    case GUID:
                        return ((OdmaGuid)value).toString();
                    default:
                        return "[unknown type]";
                    }
                }
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if(columnIndex == 0) {
                    return false;
                }
                OdmaProperty p = allProperties.get(rowIndex);
                return p.getType() == OdmaType.REFERENCE;
            }
        };
        JTable propertiesTable = new JTable(tm) {

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                OdmaProperty p = allProperties.get(row);
                if(p.getType() == OdmaType.REFERENCE) {
                    if(p.isMultiValue()) {
                        return new ObjectSetCellEditor(p.getName(), odmaObject.getId());
                    } else {
                        return new ObjectCellEditor();
                    }
                } else {
                    return super.getCellEditor(row, column);
                }
            }
            
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if(column == 0) {
                    return super.getCellRenderer(row, column);
                }
                OdmaProperty p = allProperties.get(row);
                if(p.getType() == OdmaType.REFERENCE) {
                    boolean multiValue = p.isMultiValue();
                    return new DefaultTableCellRenderer() {
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            return  super.getTableCellRendererComponent(table, multiValue?"{object set}":(value==null?"<null>":"[Object] "+((OdmaObject)value).getId()), isSelected, hasFocus, row, column);
                        };
                    };
                } else {
                    return super.getCellRenderer(row, column);
                }
            }

        };
        propertiesTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(javax.swing.UIManager.getColor("Panel.background"));
                } else {
                    c.setBackground(table.getSelectionBackground());
                }
                return c;
            }
        });
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

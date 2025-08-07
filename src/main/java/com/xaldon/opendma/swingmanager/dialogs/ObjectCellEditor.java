package com.xaldon.opendma.swingmanager.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.opendma.api.OdmaObject;

class ObjectCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final long serialVersionUID = 1595892959171661928L;
    private JPanel panel;
    private JLabel label;
    private JButton button;
    private OdmaObject currentObject;

    public ObjectCellEditor() {
        panel = new JPanel(new BorderLayout());
        label = new JLabel();
        button = new JButton("...");
        button.setFocusable(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(e -> {
            if (currentObject != null) {
                ObjectPropertiesDialog.showForObject(currentObject);
            }
        });
        panel.add(label, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof OdmaObject) {
            currentObject = (OdmaObject) value;
            label.setText("[Object] " + currentObject.getId());
            button.setEnabled(true);
        } else {
            label.setText("<null>");
            button.setEnabled(false);
        }
        panel.setBackground(table.getSelectionBackground());
        label.setForeground(table.getSelectionForeground());
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return currentObject;
    }
}

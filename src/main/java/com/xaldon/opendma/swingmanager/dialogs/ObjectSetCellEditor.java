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

import org.opendma.api.OdmaId;
import org.opendma.api.OdmaObject;
import org.opendma.api.OdmaQName;

class ObjectSetCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final long serialVersionUID = 1595892959171661928L;
    private JPanel panel;
    private JLabel label;
    private JButton button;
    private Iterable<OdmaObject> currentSet;

    public ObjectSetCellEditor(OdmaQName propName, OdmaId parentObjId) {
        panel = new JPanel(new BorderLayout());
        label = new JLabel();
        button = new JButton("...");
        button.setFocusable(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(e -> {
            if (currentSet != null) {
                ObjectSetDialog.showForSet(propName, parentObjId, currentSet);
            }
        });
        panel.add(label, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if(value instanceof Iterable) {
            currentSet = (Iterable<OdmaObject>)value;
        }
        label.setText("{object set}");
        button.setEnabled(true);
        panel.setBackground(table.getSelectionBackground());
        label.setForeground(table.getSelectionForeground());
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return currentSet;
    }
}

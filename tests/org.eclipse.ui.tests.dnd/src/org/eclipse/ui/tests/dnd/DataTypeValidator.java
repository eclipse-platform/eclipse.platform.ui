package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public class DataTypeValidator {
	
    public boolean isValidDrop(Widget target, Object data) {
        if (target == null || data == null) {
            System.out.println("Invalid drop: target or data is null");
            return false;
        }

        if (target instanceof List) {
            boolean valid = data instanceof String;
            if (!valid) System.out.println("Invalid drop: List can only accept String data");
            return valid;
        } else if (target instanceof Tree) {
            boolean valid = data instanceof String || data instanceof TreeItem;
            if (!valid) System.out.println("Invalid drop: Tree can only accept String or TreeItem data");
            return valid;
        } else if (target instanceof Table) {
            boolean valid = data instanceof TableItem;
            if (!valid) System.out.println("Invalid drop: Table can only accept TableItem data");
            return valid;
        }

        System.out.println("Invalid drop: Unsupported widget type " + target.getClass().getSimpleName());
        return false;
    }
}

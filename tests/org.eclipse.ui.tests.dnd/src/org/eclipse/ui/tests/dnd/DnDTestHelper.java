package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.Event;

public class DnDTestHelper {

    public void selectItems(Widget widget, Object... items) {
        if (items == null || items.length == 0) return;

        if (widget instanceof Tree tree) {
            for (Object item : items) {
                if (item instanceof org.eclipse.swt.widgets.TreeItem) {
                    tree.setSelection((org.eclipse.swt.widgets.TreeItem) item);
                } else {
                    System.out.println("Invalid item type for Tree: " + item);
                }
            }
        } else if (widget instanceof Table table) {
            for (Object item : items) {
                if (item instanceof TableItem) {
                    table.setSelection((TableItem) item);
                } else {
                    System.out.println("Invalid item type for Table: " + item);
                }
            }
        } else if (widget instanceof List list) {
            for (Object item : items) {
                if (item instanceof String) {
                    int index = list.indexOf((String) item);
                    if (index >= 0) {
                        list.select(index);
                    } else {
                        System.out.println("Item not found in List: " + item);
                    }
                } else {
                    System.out.println("Invalid item type for List: " + item);
                }
            }
        }
    }

    public void simulateDrag(Widget source, Widget target, int dragType, Object... items) {
        if (source == null || target == null || items == null || items.length == 0) {
            System.out.println("Invalid drag parameters. Aborting simulation.");
            return;
        }

        Display.getDefault().syncExec(() -> {
            System.out.println("=== Starting drag from " + source + " to " + target + " ===");

            // --- Drag Detect ---
            Event dragDetect = new Event();
            dragDetect.widget = source;
            source.notifyListeners(SWT.DragDetect, dragDetect);

            // --- Drag Set Data ---
            Event setData = new Event();
            setData.widget = source;
            setData.data = items;
            source.notifyListeners(DND.DragSetData, setData);

            // --- Drag Finished ---
            Event dragEnd = new Event();
            dragEnd.widget = source;
            dragEnd.detail = dragType;
            source.notifyListeners(DND.DragEnd, dragEnd);

            // --- Drop ---
            Event dropEvent = new Event();
            dropEvent.widget = target;
            dropEvent.data = items;
            target.notifyListeners(DND.Drop, dropEvent);

            System.out.println("=== Drag simulation finished to " + target + " ===");
        });
    }

    public void runEventLoop() {
        Display display = Display.getDefault();
        while (display.readAndDispatch()) {
        }
    }
}

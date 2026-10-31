package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.widgets.Widget;
import java.util.Arrays;

public class DnDEventFactory {

    public DragSourceEvent createDragStartEvent(Widget source, Object... items) {
        if (source == null) {
            throw new IllegalArgumentException("Source widget cannot be null");
        }

        DragSourceEvent event = new DragSourceEvent(null);
        event.widget = source;
        event.data = items != null ? items : new Object[0];
        event.detail = 0; 

        System.out.println("DragStartEvent created for source: " + source 
                           + " with items: " + Arrays.toString((Object[]) event.data));

        return event;
    }
    
    public DragSourceEvent createDragSetDataEvent(Widget source, Object data, int operation) {
        if (source == null) throw new IllegalArgumentException("Source widget cannot be null");
        DragSourceEvent event = new DragSourceEvent(null);
        event.widget = source;
        event.data = data;
        event.detail = operation;

        System.out.println("DragSetDataEvent created for source: " + source 
                           + ", data: " + data + ", operation: " + operation);
        return event;
    }

    public DragSourceEvent createDragFinishedEvent(Widget source, int operation) {
        if (source == null) throw new IllegalArgumentException("Source widget cannot be null");
        DragSourceEvent event = new DragSourceEvent(null);
        event.widget = source;
        event.detail = operation;

        System.out.println("DragFinishedEvent created for source: " + source 
                           + ", operation: " + operation);
        return event;
    }
}

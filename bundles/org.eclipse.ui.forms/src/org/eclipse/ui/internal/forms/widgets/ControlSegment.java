package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public class ControlSegment extends ObjectSegment implements IFocusSelectable {
	private boolean fill;
	
	public ControlSegment() {
	}
	
	public void setFill(boolean fill) {
		this.fill = fill;
	}
	
	public Control getControl(Hashtable resourceTable) {
		Object obj = resourceTable.get(getObjectId());
		if (obj instanceof Control)
			return (Control)obj;
		else
			return null;
	}

	protected Point getObjectSize(Hashtable resourceTable, int wHint) {
		Control control = getControl(resourceTable);
		if (control==null)
			return new Point(0,0);
		Point size = control.computeSize(wHint, SWT.DEFAULT);
		if (wHint!=SWT.DEFAULT && fill)
			size.x = Math.max(size.x, wHint);
		return size;
	}
	
	public void layout(GC gc, int width, Locator loc, Hashtable resourceTable,
			boolean selected) {
		super.layout(gc, width, loc, resourceTable, selected);
		Control control = getControl(resourceTable);
		if (control!=null)
			control.setBounds(getBounds());
	}
	
	public void setFocus(Hashtable resourceTable) {
		Control c = getControl(resourceTable);
		if (c!=null)
			c.setFocus();
	}
	
	public boolean isFocusSelectable(Hashtable resourceTable) {
		Control c = getControl(resourceTable);
		if (c!=null)
			return true;
		return false;
	}
}
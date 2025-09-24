/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ControlSegment extends ObjectSegment implements IFocusSelectable {
	private boolean fill;
	private int width = SWT.DEFAULT;
	private int height = SWT.DEFAULT;

	public ControlSegment() {
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Control getControl(Hashtable<String, Object> resourceTable) {
		Object obj = resourceTable.get(getObjectId());
		if (obj instanceof Control c) {
			if (!c.isDisposed()) {
				return c;
			}
		}
		return null;
	}

	@Override
	protected Point getObjectSize(Hashtable<String, Object> resourceTable, int widthHint) {
		Control control = getControl(resourceTable);
		if (control==null) {
			return new Point(0,0);
		}
		Point size = control.computeSize(widthHint, SWT.DEFAULT);
		if (widthHint!=SWT.DEFAULT && fill) {
			size.x = Math.max(size.x, widthHint);
		}
		if (width != SWT.DEFAULT) {
			size.x = width;
		}
		if (height != SWT.DEFAULT) {
			size.y = height;
		}
		return size;
	}

	@Override
	public void layout(GC gc, int width, Locator loc, Hashtable<String, Object> resourceTable,
			boolean selected) {
		super.layout(gc, width, loc, resourceTable, selected);
		Control control = getControl(resourceTable);
		if (control!=null) {
			control.setBounds(getBounds());
		}
	}

	@Override
	public boolean setFocus(Hashtable<String, Object> resourceTable, boolean next) {
		Control c = getControl(resourceTable);
		if (c!=null) {
			return setFocus(c, next);
		}
		return false;
	}

	private boolean setFocus(Control c, boolean direction) {
		if (c instanceof Composite comp) {
			Control [] tabList = comp.getTabList();
			if (direction) {
				for (Control element : tabList) {
					if (setFocus(element, direction)) {
						return true;
					}
				}
			}
			else {
				for (int i=tabList.length-1; i>=0; i--) {
					if (setFocus(tabList[i], direction)) {
						return true;
					}
				}
			}
			if (!(c instanceof Canvas)) {
				return false;
			}
		}
		return c.setFocus();
	}

	@Override
	public boolean isFocusSelectable(Hashtable<String, Object> resourceTable) {
		Control c = getControl(resourceTable);
		if (c!=null) {
			return true;
		}
		return false;
	}
}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.day.internal;

import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

class DayEditorLayout extends Layout {
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		return new Point(wHint, hHint);
	}
	
	protected void layout(Composite composite, boolean flushCache) {
		Control[] children = composite.getChildren();
		Point parentSize = composite.getSize();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof CompositeTable) {
				children[i].setBounds(0, 0, parentSize.x, parentSize.y);
			}
		}
	}
}


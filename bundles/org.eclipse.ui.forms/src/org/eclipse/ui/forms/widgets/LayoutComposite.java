/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
/**
 * The class overrides default method for computing size in Composite by
 * accepting size returned from layout managers as-is. The default code accepts
 * width or height hint assuming it is correct. However, it is possible that
 * the computation using the provided width hint results in a real size that is
 * larger. This can result in wrapped text widgets being clipped, asking to
 * render in bounds narrower than the longest word.
 */
/* package */class LayoutComposite extends Composite {
	public LayoutComposite(Composite parent, int style) {
		super(parent, style);
		setMenu(parent.getMenu());
	}
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Layout layout = getLayout();
		if (layout instanceof TableWrapLayout)
			return ((TableWrapLayout) layout).computeSize(this, wHint, hHint,
					changed);
		if (layout instanceof ColumnLayout)
			return ((ColumnLayout) layout).computeSize(this, wHint, hHint,
					changed);
		return super.computeSize(wHint, hHint, changed);
	}
}

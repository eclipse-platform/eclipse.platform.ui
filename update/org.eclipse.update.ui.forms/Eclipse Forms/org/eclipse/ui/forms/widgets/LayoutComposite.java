/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.widgets;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LayoutComposite extends Composite {
	public LayoutComposite(Composite parent, int style) {
		super(parent, style);
	}
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Layout layout = getLayout();
		if (layout instanceof TableWrapLayout)
			return ((TableWrapLayout) layout).computeSize(this, wHint,
					hHint, changed);
		if (layout instanceof ColumnLayout)
			return ((ColumnLayout) layout).computeSize(this, wHint, hHint,
					changed);
		return super.computeSize(wHint, hHint, changed);
	}
}
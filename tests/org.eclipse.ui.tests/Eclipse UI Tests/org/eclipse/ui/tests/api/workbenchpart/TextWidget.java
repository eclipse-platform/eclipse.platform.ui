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

package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.menus.AbstractWorkbenchWidget;

/**
 * Basic widget wrapping an SWT Text Control.
 * 
 * @since 3.3
 *
 */
public class TextWidget extends AbstractWorkbenchWidget {
	Text tw;

	/**
	 * Provide access to the control to add listeners, format...
	 * 
	 * @return The control after it has been initialized to
	 * the default settings.
	 */
	public Control getControl() {
		return tw;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.AbstractWorkbenchWidget#fill(org.eclipse.swt.widgets.Composite)
	 */
	public void fill(Composite parent) {
		tw = new Text(parent, SWT.BORDER);
		
		// set the initial bounds.
		setInitialBounds();
		tw.setText("Test String");
	}
	
	/**
	 * Set the initial size and location of the conrol within its
	 * enclosing Composite.
	 */
	private void setInitialBounds() {
		// Magic code wrning; the size and location offsets
		// were tuned to the defaults on Widnows XP
		Point prefSize = getPreferredSize();
		prefSize.y -= 1;
		tw.setSize(prefSize);
		tw.setLocation(0,2);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.AbstractWorkbenchWidget#getPreferredSize()
	 */
	public Point getPreferredSize() {
		return tw.computeSize(100, SWT.DEFAULT, true);
	}
}

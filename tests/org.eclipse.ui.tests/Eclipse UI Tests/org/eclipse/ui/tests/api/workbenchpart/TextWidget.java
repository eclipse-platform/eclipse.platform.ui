/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * Basic widget wrapping an SWT Text Control.
 * 
 * @since 3.3
 *
 */
public class TextWidget extends WorkbenchWindowControlContribution {
	public TextWidget() {
		
	}
	
	/**
	 * @param id
	 */
	protected TextWidget(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createControl(Composite parent) {
		Composite textHolder = new Composite(parent, SWT.NONE);
		textHolder.setLayout(new Layout() {
			protected Point computeSize(Composite composite, int wHint,
					int hHint, boolean flushCache) {
				Text tw = (Text) composite.getChildren()[0];
				Point twSize = tw.computeSize(wHint, hHint, flushCache);
				
				// Forst it to be at least 100 pixels
				if (twSize.x < 200)
					twSize.x = 200;
				
				return twSize;
			}

			protected void layout(Composite composite, boolean flushCache) {
				Text tw = (Text) composite.getChildren()[0];
				Point twSize = tw.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				Rectangle bb = composite.getBounds();
				int yOffset = ((bb.height-twSize.y) / 2) + 1;
				if (yOffset < 0) yOffset = 0;
				
				// Set the tw's size to the composite's width and the default height (centered)
				tw.setBounds(0, yOffset, bb.width, twSize.y);
			}
		});
		
		Text tw = new Text(textHolder, SWT.BORDER);
		tw.setText("Test Text Eric was here...XXXXXX");
		
		textHolder.setSize(181, 22);
		return textHolder;
	}
}

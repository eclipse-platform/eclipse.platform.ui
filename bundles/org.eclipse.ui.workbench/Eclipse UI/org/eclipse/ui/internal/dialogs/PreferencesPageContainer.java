/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * The PreferencesPageContainer is the container object for 
 * the preference pages in a node.
 */
public class PreferencesPageContainer {
	
	private Composite control;

	/**
	 * Create a new instance of the receiver.
	 */
	public PreferencesPageContainer() {
		super();
	}
	
	private class PreferencesLayout extends Layout{
		/* (non-Javadoc)
		 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
		 */
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			return new Point (100,100);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
		 */
		protected void layout(Composite composite, boolean flushCache) {
			

		}
	}


	/**
	 * Create the contents area of the composite.
	 * @param composite
	 * @param style
	 */
	public void createContents(Composite composite, int style) {
		control = new Composite(composite, style);
		control.setLayout(new PreferencesLayout());
		
	}

	/**
	 * Return the top level control
	 * @return Control
	 */
	public Control getControl() {
		return control;
	}

}

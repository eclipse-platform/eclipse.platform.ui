/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.AbstractWorkbenchTrimWidget;


/**
 * Readme example Trim contribution widget. This is contributed to
 * the trim throught the use of the <code>org.eclipse.ui.menus</code>
 * extension point.
 * 
 * @since 3.2
 *
 */
public class ReadmeTrimWidget extends AbstractWorkbenchTrimWidget {

	/**
	 * Cache the current trim so we can 'dispose' it on demand
	 */
	private Composite comp = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.menus.AbstractTrimWidget#dispose()
	 * 
	 * Dispose the current trim widget (if any)
	 */
	public void dispose() {
		if (comp != null && !comp.isDisposed())
			comp.dispose();
		comp = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.menus.AbstractTrimWidget#fill(org.eclipse.swt.widgets.Composite, int, int)
	 */
	public void fill(Composite parent, int oldSide, int newSide) {
		// Create a composite to place the label in 
		comp = new Composite(parent, SWT.NONE);
		
		// Give some room around the control
		FillLayout layout = new FillLayout();
		layout.marginHeight = 4;
		layout.marginWidth  = 2;
		comp.setLayout(layout);
		
		// Create a label for the trim. 
		Label dsCtrl = new Label (comp, SWT.BORDER | SWT.CENTER);
		dsCtrl.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		dsCtrl.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		// Check the side and set up the string accordingly. At this point is
		// we you should implement a 'vertical' layout to use when docked on
		// the LEFT or the RIGHT side but the code to do rotations is overly
		// complex for this example.
		String sideStr = ""; //$NON-NLS-1$
		if (newSide == SWT.LEFT) sideStr = "Left"; //$NON-NLS-1$
		if (newSide == SWT.RIGHT) sideStr = "Right"; //$NON-NLS-1$
		if (newSide == SWT.TOP) sideStr = "Top"; //$NON-NLS-1$
		if (newSide == SWT.BOTTOM) sideStr = "Bottom"; //$NON-NLS-1$
		
		dsCtrl.setText("  Read Me Trim (" + sideStr + ")  "); //$NON-NLS-1$ //$NON-NLS-2$
	}

}

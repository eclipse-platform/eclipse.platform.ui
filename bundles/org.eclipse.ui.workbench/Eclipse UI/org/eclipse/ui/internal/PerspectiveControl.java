/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;

/**
 * The PerspectiveControl is the control used to display the currently selected
 * perspective and other available ones.
 */
public class PerspectiveControl {

	IDEWorkbenchWindow window;
	CLabel label;

	/**
	 * Create a new instance of the receiver with the supplied window.
	 * 
	 * @param controlWindow
	 */
	public PerspectiveControl(IDEWorkbenchWindow controlWindow) {
		super();
		window = controlWindow;

		window.addPerspectiveListener(new IPerspectiveListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
			 *      org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
			 */
			public void perspectiveChanged(
				IWorkbenchPage page,
				IPerspectiveDescriptor perspective,
				String changeId) {
				updateLabel(perspective);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage,
			 *      org.eclipse.ui.IPerspectiveDescriptor)
			 */
			public void perspectiveActivated(
				IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
				updateLabel(perspective);
			}
		});
	}

	/**
	 * Create the control for the receiver.
	 * 
	 * @param parent
	 */
	void createControl(Composite parent) {
		label = new CLabel(parent, SWT.RIGHT);
	}

	/**
	 * Update the label for the given perspective.
	 * 
	 * @param current
	 *            The current descriptor.
	 */
	void updateLabel(IPerspectiveDescriptor current) {
		label.setText(current.getLabel());
		
		ImageData data = current.getImageDescriptor().getImageData();
		data = data.scaledTo(32,32);
		label.setImage(new Image(label.getDisplay(),data));
	}

	/**
	 * Return the control for the receiver.
	 * 
	 * @return Control
	 */
	Control getControl() {
		return label;
	}

}

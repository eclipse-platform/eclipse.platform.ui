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
package org.eclipse.ui.internal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
/**
 * The ToolbarFloatingWindow is a window that opens next to an owning widget
 * and locates itself relative to it.
 */
class ToolbarFloatingWindow extends AssociatedWindow {
	Composite childControl;
	final int borderSize = 1;
	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent
	 *            the parent shell.
	 * @param associatedControl
	 *            the associated control.
	 */
	ToolbarFloatingWindow(Shell parent, Control associatedControl) {
		super(parent, associatedControl, AssociatedWindow.TRACK_OUTER_TOP_RHS);
		setShellStyle(SWT.NO_TRIM | SWT.NO_FOCUS);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getLayout()
	 */
	protected Layout getLayout() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2 * borderSize + 1;
		layout.marginWidth = 2 * borderSize + 1;
		return layout;
	}
	/**
	 * Set the background color of the control to the info background.
	 * 
	 * @param control
	 *            the control.
	 */
	private void setBackground(Control control) {
		control.setBackground(control.getDisplay().getSystemColor(
				SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite root) {
		childControl = new Composite(root, SWT.NONE);
		childControl.setLayout(new GridLayout());
		// prevent escape to close shell
		childControl.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_ESCAPE) {
					event.doit = false;
				}
			}
		});
		return childControl;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.AssociatedWindow#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		setBackground(newShell);
		addRoundBorder(newShell,borderSize);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#initializeBounds()
	 */
	protected void initializeBounds() {
		super.initializeBounds();
		Shell floatingShell = getShell();
		floatingShell.layout();
		moveShell(getShell(), AssociatedWindow.HORIZONTAL_VISIBLE);
	}
	/**
	 * Answer the top level control for the receiver, this will be added to
	 * when filling in the receivers contents
	 * 
	 * @return <code>Composite</control>  the top level parent control
	 */
	public Composite getControl() {
		if (childControl != null)
			return childControl;
		create();
		return childControl;
	}
}

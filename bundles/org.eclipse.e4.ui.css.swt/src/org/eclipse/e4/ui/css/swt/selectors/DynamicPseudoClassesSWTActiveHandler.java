/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.selectors;

import org.eclipse.e4.ui.css.core.dom.selectors.IDynamicPseudoClassesHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Handler for dynamic pseudo class Shell:active for SWT
 * Shell widget.
 */
public class DynamicPseudoClassesSWTActiveHandler extends
		AbstractDynamicPseudoClassesControlHandler {

	public static final IDynamicPseudoClassesHandler INSTANCE = new DynamicPseudoClassesSWTActiveHandler();

	private static String ACTIVE_LISTENER = "org.eclipse.e4.ui.core.css.swt.selectors.ACTIVE_LISTENER";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.swt.selectors.AbstractDynamicPseudoClassesControlHandler#intialize(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.e4.ui.core.css.engine.CSSEngine)
	 */
	protected void intialize(final Control control, final CSSEngine engine) {
		// Create SWT Shell Listener
		ShellListener shellListener = new ShellListener() {

			public void shellActivated(ShellEvent e) {
				try {
					control.setData("activeLost", null);
					engine.applyStyles(control, false, true);
				} catch (Exception ex) {
					engine.handleExceptions(ex);
				}
			}

			public void shellDeactivated(ShellEvent e) {
				try {
					// Set activeLost flag to true
					control.setData("activeLost", Boolean.TRUE);
					engine.applyStyles(control, false, true);
				} catch (Exception ex) {
					engine.handleExceptions(ex);
				} finally {
					// Set activeLost flag to false
					control.setData("activeLost", null);
				}
			}

			public void shellDeiconified(ShellEvent e) {				
			}

			public void shellIconified(ShellEvent e) {
			}

			public void shellClosed(ShellEvent e) {
				dispose(control, engine);	
			}
		};
		
		// Register the active listener into Control Data
		// in order to remove it when dispose method is called.
		Shell shell = (Shell)control;
		shell.setData(ACTIVE_LISTENER, shellListener);
		// Add the active listener to the control
		shell.addShellListener(shellListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.swt.selectors.AbstractDynamicPseudoClassesControlHandler#dispose(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.e4.ui.core.css.engine.CSSEngine)
	 */
	protected void dispose(Control control, CSSEngine engine) {
		// Get the active listener registered into control data
		ShellListener shellListener = (ShellListener) control
				.getData(ACTIVE_LISTENER);
		if (shellListener != null)
			// remove the focus listener to the control
			((Shell)control).removeShellListener(shellListener);
		control.setData(ACTIVE_LISTENER, null);
	}
}

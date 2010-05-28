/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.selectors;

import org.eclipse.e4.ui.css.core.dom.selectors.IDynamicPseudoClassesHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

public class DynamicPseudoClassesSWTButtonSelectedHandler extends
	AbstractDynamicPseudoClassesControlHandler{


	public static final IDynamicPseudoClassesHandler INSTANCE = new DynamicPseudoClassesSWTButtonSelectedHandler();

	protected void intialize(final Control control, final CSSEngine engine) {
		if (control instanceof Button){
			final Button button = (Button)control;
			SelectionListener selectionListener = new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					try {
						engine.applyStyles(control, false, true);
					} catch (Exception ex) {
						engine.handleExceptions(ex);
					}
					
				}
						
				public void widgetSelected(SelectionEvent e) {
					try {
						engine.applyStyles(control, false, true);
					} catch (Exception ex) {
						engine.handleExceptions(ex);
					}
				}
			
			};
			
			// Register the selected listener into Control Data
			// in order to remove it when dispose method is called.
			button.setData(CSSSWTConstants.BUTTON_SELECTED_LISTENER, selectionListener);
			// Add the checked listener to the control
			button.addSelectionListener(selectionListener);
		}
	}
	
	protected void dispose(Control control, CSSEngine engine) {
		// Get the checked listener registered into control data
		Listener listener = (Listener) control
				.getData(CSSSWTConstants.BUTTON_SELECTED_LISTENER);
		if (listener != null)
			// remove the checked listener to the control
			((Button)control).removeListener(SWT.Selection, listener);
		control.setData(CSSSWTConstants.BUTTON_SELECTED_LISTENER, null);
	}



}

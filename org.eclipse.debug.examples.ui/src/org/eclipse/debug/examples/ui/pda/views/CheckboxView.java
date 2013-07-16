/*****************************************************************
 * Copyright (c) 2009, 2013 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 286310)
 *     IBM Corporation - ongoing maintenance and enhancements
 *****************************************************************/
package org.eclipse.debug.examples.ui.pda.views;

import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.swt.SWT;

public class CheckboxView extends VariablesView {
	public static String ID = "CHECKBOX_VIEW_ID"; //$NON-NLS-1$
	
	protected int getViewerStyle() {
		return SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION | SWT.CHECK;
	}
	
	protected String getHelpContextId() {
		return ID;
	}
	
	protected String getPresentationContextId() {	
		return ID;
	}
}

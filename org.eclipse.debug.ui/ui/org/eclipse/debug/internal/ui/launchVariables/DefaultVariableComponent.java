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
package org.eclipse.debug.internal.ui.launchVariables;

import org.eclipse.debug.ui.launchVariables.IVariableComponentContainer;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Default variable component implementation which does not
 * allow variable value editing visually.
 */	
final class DefaultVariableComponent extends AbstractVariableComponent {
	private boolean showError = false;
	private Label message = null;
	
	public DefaultVariableComponent(boolean showError) {
		super();
		this.showError = showError;
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public Control getControl() {
		return message;
	}
			
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public void createContents(Composite parent, String varTag, IVariableComponentContainer page) {
		container= page;
		if (showError) {
			message = new Label(parent, SWT.NONE);
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			message.setLayoutData(data);
			message.setFont(parent.getFont());
			message.setText(LaunchVariableMessages.getString("DefaultVariableComponent.0")); //$NON-NLS-1$
			message.setForeground(JFaceColors.getErrorText(message.getDisplay()));
		}
	}
}
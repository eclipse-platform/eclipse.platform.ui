/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.core.variables.IStringVariable;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Argument selector for system properties.
 * 
 * @since 3.2
 */
public class SystemPropertyArgumentSelector implements IArgumentSelector {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.stringsubstitution.IArgumentSelector#selectArgument(org.eclipse.core.variables.IStringVariable, org.eclipse.swt.widgets.Shell)
	 */
	public String selectArgument(IStringVariable variable, Shell shell) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
		dialog.setElements(System.getProperties().keySet().toArray());
		dialog.setTitle(StringSubstitutionMessages.SystemPropertyArgumentSelector_0);
		dialog.setMessage(StringSubstitutionMessages.SystemPropertyArgumentSelector_1);
		if (dialog.open() == Window.OK) {
			return (String) dialog.getResult()[0];
		}
		return null;
	}

}

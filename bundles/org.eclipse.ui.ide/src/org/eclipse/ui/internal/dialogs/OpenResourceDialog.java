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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Shows a list of resources to the user with a text entry field
 * for a string pattern used to filter the list of resources.
 *
 * @since 2.1
 */
public class OpenResourceDialog extends ResourceListSelectionDialog {

/**
 * Creates a new instance of the class.
 */
public OpenResourceDialog(Shell parentShell, IContainer container, int typesMask) {
	super(parentShell, container, typesMask);
	setTitle(WorkbenchMessages.getString("OpenResourceDialog.title")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(parentShell, IHelpContextIds.OPEN_RESOURCE_DIALOG);
}
}

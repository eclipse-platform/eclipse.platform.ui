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


import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

/**
 * Visual component to edit the resource type variable
 * value for the file location. Variable is limited to a specific
 * <code>IFile</code> resource.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class SpecificFileResourceComponent extends ResourceComponent {

	/**
	 * Creates an instance
	 */
	public SpecificFileResourceComponent() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ResourceComponent.
	 */
	protected void createSelectedResourceOption() {
		// Do not present this option...
	}
	
	/* (non-Javadoc)
	 * Method declared on ResourceComponent.
	 */
	protected void createSpecificResourceOption() {
		Label label = new Label(mainGroup, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		label.setLayoutData(data);
		label.setFont(mainGroup.getFont());
		label.setText(LaunchVariableMessages.getString("SpecificFileResourceComponent.0")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on ResourceComponent.
	 */
	protected void validateResourceListSelection() {
		if (resourceList == null)
			return;
			
		IStructuredSelection sel = (IStructuredSelection) resourceList.getSelection();
		IResource resource = (IResource) sel.getFirstElement();
		if (resource == null || resource.getType() != IResource.FILE) {
			getContainer().setErrorMessage(LaunchVariableMessages.getString("SpecificFileResourceComponent.1")); //$NON-NLS-1$
			setIsValid(false);
		}
	}
}

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
package org.eclipse.debug.ui.variables;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

/**
 * Visual component to edit the resource type variable
 * value for the working directory. Variable is limited to a specific
 * <code>IContainer</code> resource.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class SpecificFolderResourceComponent extends ResourceComponent {

	/**
	 * Creates an instance
	 */
	public SpecificFolderResourceComponent() {
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
	protected void createResourceList() {
		super.createResourceList();
		if (resourceList != null)
			resourceList.addFilter(new FileFilter());
	}

	/* (non-Javadoc)
	 * Method declared on ResourceComponent.
	 */
	protected void createSpecificResourceOption() {
		Label label = new Label(mainGroup, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		label.setLayoutData(data);
		label.setFont(mainGroup.getFont());
		label.setText(LaunchConfigurationsMessages.getString("SpecificFolderResourceComponent.Us&e_specific_resource__1")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on ResourceComponent.
	 */
	protected void validateResourceListSelection() {
		if (resourceList == null)
			return;
			
		IStructuredSelection sel = (IStructuredSelection) resourceList.getSelection();
		IResource resource = (IResource) sel.getFirstElement();
		if (resource == null || resource.getType() == IResource.FILE) {
			getPage().setErrorMessage(LaunchConfigurationsMessages.getString("SpecificFolderResourceComponent.Use_&selected_resource_2")); //$NON-NLS-1$
			setIsValid(false);
		}
	}
	
	
	/**
	 * Filter to remove any IFile resources.
	 */
	private static final class FileFilter extends ViewerFilter {
		/* (non-Javadoc)
		 * Method declared on ViewerFilter.
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			IResource resource = null;
			if (element instanceof IResource) {
				resource = (IResource) element;
			} else {
				if (element instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) element;
					resource = (IResource) adaptable.getAdapter(IResource.class);
				}
			}
			
			if (resource != null)
				return resource.getType() != IResource.FILE;
			else
				return false;
		}
	}
}

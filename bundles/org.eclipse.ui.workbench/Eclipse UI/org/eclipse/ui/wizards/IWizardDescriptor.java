/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPartDescriptor;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * Base interface for all wizards defined via workbench extension points.
 * 
 * @since 3.1
 */
public interface IWizardDescriptor extends IWorkbenchPartDescriptor, IAdaptable {

	/**
	 * Answer the selection for the reciever based on whether the it can handle
	 * the selection. If it can return the selection. If it can handle the
	 * adapted to IResource value of the selection. If it satisfies neither of
	 * these conditions return an empty IStructuredSelection.
	 * 
	 * @return IStructuredSelection
	 * @param selection
	 *            IStructuredSelection
	 */
	IStructuredSelection adaptedSelection(IStructuredSelection selection);

	/**
	 * Return the description.
	 * 
	 * @return the description
	 */
	String getDescription();
	
	/**
	 * Return the tags associated with this wizard.
	 * 
	 * @return the tags associated with this wizard
	 */
	String [] getTags();
	
	/**
	 * Create a wizard.
	 * 
	 * @return the wizard 
	 * @throws CoreException thrown if there is a problem creating the wizard
	 */
	IWorkbenchWizard createWizard() throws CoreException;
	
	/**
	 * Return the description image for this wizard.
	 * 
	 * @return the description image for this wizard or <code>null</code>
	 */
	public ImageDescriptor getDescriptionImage();

	/**
	 * Return the help system href for this wizard.
	 * 
	 * @return the help system href for this wizard or <code>null</code>
	 */
	String getHelpHref();	

	/**
	 * Return the category for this wizard.  
	 * 
	 * @return the category or <code>null</code>
	 */
	IWizardCategory getCategory();
}

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

import java.io.StringReader;
import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Expands a working set type variable into the desired
 * result format.
 * @since 3.0
 */
public class WorkingSetExpander extends DefaultVariableExpander {

	/**
	 * Restores a working set based on the XMLMemento represented within
	 * the varValue.
	 * 
	 * see bug 37143.
	 * @param mementoString The string memento of the working set
	 * @return the restored working set or <code>null</code> if problems occurred restoring the
	 * working set.
	 */
	public static IWorkingSet restoreWorkingSet(String mementoString) {
		StringReader reader= new StringReader(mementoString);
		XMLMemento memento= null;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			DebugUIPlugin.log(e);
			return null;
		}

		String factoryID = memento.getString(IVariableConstants.TAG_FACTORY_ID);

		if (factoryID == null) {
			DebugUIPlugin.logErrorMessage(LaunchConfigurationsMessages.getString("WorkingSetExpander.2")); //$NON-NLS-1$
			return null;
		}
		IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
		if (factory == null) {
			DebugUIPlugin.logErrorMessage(LaunchConfigurationsMessages.getString("WorkingSetExpander.3") + factoryID); //$NON-NLS-1$
			return null;
		}
		IAdaptable adaptable = factory.createElement(memento);
		if (adaptable == null) {
			DebugUIPlugin.logErrorMessage(LaunchConfigurationsMessages.getString("WorkingSetExpander.4") + factoryID); //$NON-NLS-1$
		}
		if ((adaptable instanceof IWorkingSet) == false) {
			DebugUIPlugin.logErrorMessage(LaunchConfigurationsMessages.getString("WorkingSetExpander.5") + factoryID); //$NON-NLS-1$
			return null;
		}
			
		return (IWorkingSet) adaptable;
	}
	/**
	 * Create an instance
	 */
	public WorkingSetExpander() {
		super();
	}

	/**
	 * @see IVariableExpander#getResources(String, String, ExpandVariableContext)
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) throws CoreException {
		if (varValue == null || varValue.length() == 0) {
			throwExpansionException(varTag, LaunchConfigurationsMessages.getString("WorkingSetExpander.No_working_set_specified._1")); //$NON-NLS-1$
			return null;
		}

		IWorkingSet set = restoreWorkingSet(varValue);
		if (set == null) {
			throwExpansionException(varTag, MessageFormat.format(LaunchConfigurationsMessages.getString("WorkingSetExpander.No_working_set"), new String[] {varValue})); //$NON-NLS-1$
			return null;
		}
			
		IAdaptable[] elements = set.getElements();
		IResource[] resources = new IResource[elements.length];
		for (int i = 0; i < elements.length; i++) {
			IAdaptable adaptable = elements[i];
			if (adaptable instanceof IResource) {
				resources[i] = (IResource) adaptable;
			} else {
				resources[i] = (IResource) adaptable.getAdapter(IResource.class);
			}
		}
		
		return resources;
	}
}

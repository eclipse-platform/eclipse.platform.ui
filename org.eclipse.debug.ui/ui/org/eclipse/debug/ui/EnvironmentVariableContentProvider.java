package org.eclipse.debug.ui;
/*******************************************************************************
 * Copyright (c) 2000, 2003 Keith Seitz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Keith Seitz (keiths@redhat.com) - initial implementation
 *     IBM Corporation - integration and code cleanup
 *******************************************************************************/

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class EnvironmentVariableContentProvider
	implements IStructuredContentProvider
{

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement)
	{
		EnvironmentVariable[] elements = new EnvironmentVariable[0];
		ILaunchConfiguration config = (ILaunchConfiguration) inputElement;
		Map m;
		try
		{
			m = config.getAttribute(IDebugUIConstants.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		}
		catch (CoreException e)
		{
			DebugUIPlugin.log(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "Error reading configuration", e));
			return elements;
		}

		if (m != null && !m.isEmpty())
		{
			elements = new EnvironmentVariable[m.size()];
			String[] varNames = new String[m.size()];
			m.keySet().toArray(varNames);
			for (int i = 0; i < m.size(); i++)
			{
				elements[i] = new EnvironmentVariable((String) varNames[i], (String) m.get(varNames[i]));
			}
		}
		
		return elements;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose()
	{
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

}

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
package org.eclipse.team.ui.synchronize.viewers;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.team.internal.ui.TeamUIPlugin;

public interface ISynchronizeModelElement extends IDiffContainer, ITypedElement, ICompareInput {

	public static final String BUSY_PROPERTY = TeamUIPlugin.ID + ".busy"; //$NON-NLS-1$
	public static final String PROPAGATED_CONFLICT_PROPERTY = TeamUIPlugin.ID + ".conflict"; //$NON-NLS-1$
	public static final String PROPAGATED_ERROR_MARKER_PROPERTY = TeamUIPlugin.ID + ".error"; //$NON-NLS-1$
	public static final String PROPAGATED_WARNING_MARKER_PROPERTY = TeamUIPlugin.ID + ".warning"; //$NON-NLS-1$

	public abstract void addPropertyChangeListener(IPropertyChangeListener listener);

	public abstract void removePropertyChangeListener(IPropertyChangeListener listener);

	public void setPropertyToRoot(String propertyName, boolean value);
	
	public void setProperty(String propertyName, boolean value);
	
	/**
	 * Return whether this node has the given property set.
	 * @param propertyName the flag to test
	 * @return <code>true</code> if the property is set
	 */
	public abstract boolean getProperty(String propertyName);

	public abstract ImageDescriptor getImageDescriptor(Object object);

	public abstract IResource getResource();
}
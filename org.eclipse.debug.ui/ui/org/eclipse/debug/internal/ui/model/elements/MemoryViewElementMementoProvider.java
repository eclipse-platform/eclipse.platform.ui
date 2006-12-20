/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;

public class MemoryViewElementMementoProvider extends ElementMementoProvider {
	
	private static final String OBJECT_ID = "OBJECT_ID"; //$NON-NLS-1$

	protected boolean encodeElement(Object element, IMemento memento,
			IPresentationContext context) throws CoreException {
		String id = context.getId();
		if (id.equals(IDebugUIConstants.ID_MEMORY_VIEW))
		{
			if (element instanceof IMemoryBlock || element instanceof IMemoryBlockRetrieval)
			{
				memento.putInteger(OBJECT_ID, element.hashCode());
				return true;
			}
		}
		return false;
	}

	protected boolean isEqual(Object element, IMemento memento,
			IPresentationContext context) throws CoreException {
		String id = context.getId();
		if (id.equals(IDebugUIConstants.ID_MEMORY_VIEW))
		{
			if (element instanceof IMemoryBlock || element instanceof IMemoryBlockRetrieval)
			{
				Integer objectId = memento.getInteger(OBJECT_ID);
				if (objectId != null && objectId.intValue() == element.hashCode())
					return true;
			}
		}
		return false;
	}

}

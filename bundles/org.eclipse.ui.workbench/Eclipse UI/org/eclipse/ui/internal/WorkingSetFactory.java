/************************************************************************
Copyright (c) 2002, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * A WorkingSetFactory is used to recreate a persisted WorkingSet 
 * object.
 *
 * @see IElementFactory
 */
public class WorkingSetFactory implements IElementFactory {

	/**
	 * Implements IElementFactory.
	 * 
	 * @see IElementFactory#createElement(IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		String workingSetName = memento.getString(IWorkbenchConstants.TAG_NAME);
		String workingSetEditPageId = memento.getString(IWorkbenchConstants.TAG_EDIT_PAGE_ID);
		
		if (workingSetName == null)
			return null;

		WorkingSet workingSet = new WorkingSet(workingSetName, memento);
		if (workingSetEditPageId != null) {
			workingSet.setId(workingSetEditPageId);
		}
		else {
			// working sets created with builds 20020418 and 20020419 will not
			// have an edit page id. fix this automatically.
			workingSet.setId("org.eclipse.ui.resourceWorkingSetPage");
		}
		return workingSet;
	}
}
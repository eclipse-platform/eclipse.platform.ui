/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;


/**
 * 2.1 - WORK_IN_PROGRESS do not use.
 */
public class SelectionNavigationLocation extends NavigationLocation {
		
	
	private ISelection fSelection;
	
	
	public SelectionNavigationLocation(ISelection selection) {
		fSelection= selection;
	}
	
	public String toString() {
		return "Selection<" + fSelection + ">";
	}
	
	public boolean differsFromCurrentLocation(IEditorPart part) {
		ISelectionProvider provider= part.getSite().getSelectionProvider();
		return !fSelection.equals(provider.getSelection());
	}
	
	public void dispose() {
	}
	
	public boolean mergeInto(NavigationLocation location) {
		
		if (location == null)
			return false;
			
		if (getClass() != location.getClass())
			return false;
			
		SelectionNavigationLocation selection= (SelectionNavigationLocation) location;
		return fSelection.equals(selection.fSelection);
	}
	
	public void restoreLocation(IEditorPart part) {
		ISelectionProvider provider= part.getSite().getSelectionProvider();
		provider.setSelection(fSelection);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.NavigationLocation#clearState()
	 */
	public void deactivate() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.NavigationLocation#restore(org.eclipse.ui.IEditorPart, org.eclipse.ui.IMemento)
	 */
	public void restoreAndActivate(IEditorPart part, IMemento memento) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.NavigationLocation#save(org.eclipse.ui.IEditorPart, org.eclipse.ui.IMemento)
	 */
	public void saveAndDeactivate(IEditorPart part, IMemento memento) {
	}

}
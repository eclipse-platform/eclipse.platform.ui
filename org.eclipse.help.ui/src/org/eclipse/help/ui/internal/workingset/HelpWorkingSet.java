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
package org.eclipse.help.ui.internal.workingset;


import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.ui.*;

/**
 * A working set for help elements. 
 * NOTE: The only reason we inherit from ui's working set is because there is a
 * cast in the wizard, when getting the page id...
 */
public class HelpWorkingSet {

	private WorkingSet workingSet;
	private IWorkingSet iworkingSet;

	/**
	 * Constructor for HelpWorkingSet.
	 * @param name
	 * @param elements
	 */
	public HelpWorkingSet(String name, IAdaptable[] elements) {
		this(
			HelpSystem.getWorkingSetManager().createWorkingSet(
				name,
				(AdaptableHelpResource[]) elements));
	}

	public HelpWorkingSet(WorkingSet ws) {
		this.workingSet = ws;
		this.iworkingSet =
			PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSet(
				ws.getName(),
				ws.getElements());

		iworkingSet.setId(HelpWorkingSetPage.PAGE_ID);

		//HelpSystem.getWorkingSetManager().addWorkingSet(workingSet);
		//PlatformUI.getWorkbench().getWorkingSetManager().addWorkingSet(iworkingSet);
	}

	public HelpWorkingSet(IWorkingSet iws) {
		this.iworkingSet = iws;
		AdaptableHelpResource[] elements =
			new AdaptableHelpResource[iws.getElements().length];
		System.arraycopy(iws.getElements(), 0, elements, 0, elements.length);
		this.workingSet =
			HelpSystem.getWorkingSetManager().createWorkingSet(
				iws.getName(),
				elements);

		//HelpSystem.getWorkingSetManager().addWorkingSet(workingSet);
		//PlatformUI.getWorkbench().getWorkingSetManager().addWorkingSet(iworkingSet);
	}

	public HelpWorkingSet(WorkingSet ws, IWorkingSet iws) {
		this.workingSet = ws;
		this.iworkingSet = iws;
	}

	public IWorkingSet getIWorkingSet() {
		return iworkingSet;
	}

	public WorkingSet getWorkingSet() {
		return workingSet;
	}

	/**
	 * Tests the receiver and the object for equality
	 *
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, the name is the same.
	 * 	false otherwise
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof HelpWorkingSet) {
			HelpWorkingSet ws = (HelpWorkingSet) object;
			return this.workingSet == ws.workingSet;
		} else
			return false;
	}

	/**
	 * @see org.eclipse.ui.IPersistableElement#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		HelpSystem.getWorkingSetManager().saveState();

		memento.putString("workingSet", workingSet.getName());
		//memento.putString(IWorkbenchConstants.TAG_EDIT_PAGE_ID, editPageId);

		for (int i = 0; i < workingSet.getElements().length; i++) {
			saveState(
				(AdaptableHelpResource) workingSet.getElements()[i],
				memento);
		}
	}

	private void saveState(AdaptableHelpResource element, IMemento memento) {
		IToc toc = (IToc) element.getAdapter(IToc.class);
		ITopic topic = (ITopic) element.getAdapter(ITopic.class);
		if (toc != null)
			memento.putString("toc", toc.getHref());
		else if (topic != null) {
			AdaptableHelpResource parent =
				(AdaptableHelpResource) element.getParent();
			memento.putString("toc", parent.getHref());
			// get the index of this topic
			IAdaptable[] topics = parent.getChildren();
			for (int i = 0; i < topics.length; i++)
				if (topics[i] == this) {
					memento.putString("topic", String.valueOf(i));
					return;
				}
		}
	}

}

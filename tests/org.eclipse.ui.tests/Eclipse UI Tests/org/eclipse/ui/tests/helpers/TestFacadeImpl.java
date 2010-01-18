/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.helpers;

import java.util.ArrayList;

import junit.framework.Assert;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.tests.PerspectiveState;

/**
 * @since 3.5
 * 
 */
public class TestFacadeImpl extends TestFacade {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.helpers.TestFacade#assertActionSetFound(org.eclipse
	 * .ui.IWorkbenchPage, java.lang.String)
	 */
	public void assertActionSetId(IWorkbenchPage page, String id, boolean condition) {
		IActionSetDescriptor[] sets = ((WorkbenchPage) page).getActionSets();
		boolean found = false;
		for (int i = 0; i < sets.length && !found; i++) {
			if (id.equals(sets[i].getId())) {
				found = true;
			}
		}
		Assert.assertEquals("Failed for " + id, condition, found);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.helpers.TestFacade#getActionSetCount(org.eclipse.ui.IWorkbenchPage)
	 */
	public int getActionSetCount(IWorkbenchPage page) {
		return ((WorkbenchPage) page).getActionSets().length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.helpers.TestFacade#addFastView(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IViewReference)
	 */
	public void addFastView(IWorkbenchPage page, IViewReference ref) {
		((WorkbenchPage) page).addFastView(ref);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.helpers.TestFacade#saveState(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IMemento)
	 */
	public IStatus saveState(IWorkbenchPage page, IMemento memento) {
		return ((WorkbenchPage)page).saveState(memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.helpers.TestFacade#getFastViews(org.eclipse.ui.IWorkbenchPage)
	 */
	public IViewReference[] getFastViews(IWorkbenchPage page) {
		return ((WorkbenchPage)page).getFastViews();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.helpers.TestFacade#getPerspectivePartIds(org.eclipse.ui.IWorkbenchPage, java.lang.String)
	 */
	public ArrayList getPerspectivePartIds(IWorkbenchPage page, String folderId) {
		PerspectiveState state = new PerspectiveState(page);
		return state.getPartIds(folderId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.helpers.TestFacade#isFastView(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IViewReference)
	 */
	public boolean isFastView(IWorkbenchPage page, IViewReference ref) {
		return ((WorkbenchPage)page).isFastView(ref);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.helpers.TestFacade#testSetAutomatedResponse(int)
	 */
	public void saveableHelperSetAutomatedResponse(int response) {
		SaveableHelper.testSetAutomatedResponse(response);
	}

}

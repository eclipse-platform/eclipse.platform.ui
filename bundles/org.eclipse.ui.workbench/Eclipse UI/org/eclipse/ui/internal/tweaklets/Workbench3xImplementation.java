/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

/**
 * @since 3.4
 *
 */
public class Workbench3xImplementation extends WorkbenchImplementation {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.tweaklets.WorkbenchImplementation#createWBW(int)
	 */
	public WorkbenchWindow createWorkbenchWindow(int newWindowNumber) {
		return new WorkbenchWindow(newWindowNumber);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.tweaklets.WorkbenchImplementation#createWBPage(org.eclipse.ui.internal.WorkbenchWindow, java.lang.String, org.eclipse.core.runtime.IAdaptable)
	 */
	public WorkbenchPage createWorkbenchPage(WorkbenchWindow workbenchWindow,
			String perspID, IAdaptable input) throws WorkbenchException {
		return new WorkbenchPage(workbenchWindow, perspID, input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.tweaklets.WorkbenchImplementation#createWBPage(org.eclipse.ui.internal.WorkbenchWindow, org.eclipse.core.runtime.IAdaptable)
	 */
	public WorkbenchPage createWorkbenchPage(WorkbenchWindow workbenchWindow,
			IAdaptable finalInput) throws WorkbenchException {
		return new WorkbenchPage(workbenchWindow, finalInput);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.tweaklets.WorkbenchImplementation#createPerspective(org.eclipse.ui.internal.registry.PerspectiveDescriptor, org.eclipse.ui.internal.WorkbenchPage)
	 */
	public Perspective createPerspective(PerspectiveDescriptor desc,
			WorkbenchPage workbenchPage) throws WorkbenchException {
		return new Perspective(desc, workbenchPage);
	}

}

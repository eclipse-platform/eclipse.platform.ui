/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.dnd;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonDropActionDelegate;
import org.eclipse.ui.navigator.ICommonDropAdapter;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *  
 */
public abstract class NavigatorDropActionDelegate implements ICommonDropActionDelegate {

	private CommonViewer commonViewer;

 
	public final void init(CommonViewer aViewer) {
		commonViewer = aViewer;
		doInit();
	}

	/**
	 * Implement any additional initialization. The extensionSite is accessible by
	 * getExtensionSite().
	 */
	protected void doInit() {
	}

	/**
	 * Carry out the DND operation
	 * 
	 * @param operation
	 *            one of DND.DROP_MOVE|DND.DROP_COPY|DND.DROP_LINK
	 * @param location
	 *            one of ViewerDropAdapter.LOCATION_* to indicate where in the tree an item is
	 *            dropped
	 * @param source
	 *            The object being dragged
	 * @param target
	 *            The object being dragged onto
	 * @return
	 */
	public abstract boolean run(ICommonDropAdapter dropAdapter, Object source, Object target);

	protected Display getDisplay() {
		return getShell().getDisplay();
	}

	protected Shell getShell() {
		if (commonViewer != null)
			return commonViewer.getControl().getShell();
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IDropActionDelegate#run(java.lang.Object, java.lang.Object)
	 */
	public boolean run(Object source, Object target) {
		return false;
	}
}

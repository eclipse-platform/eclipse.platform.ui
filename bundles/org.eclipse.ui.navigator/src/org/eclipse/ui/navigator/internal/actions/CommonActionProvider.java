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
package org.eclipse.ui.navigator.internal.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonActionProvider implements ICommonActionProvider {

	/**
	 *   
	 * @see ICommonActionProvider#init(String, IViewPart, INavigatorContentService, StructuredViewer)
	 */
	public void init(String anExtensionId, IViewPart aViewPart, INavigatorContentService aContentService, StructuredViewer aStructuredViewer) {

	}

	/**
	 *   
	 * @see ICommonActionProvider#dispose()
	 */
	public void dispose() {

	}

	/**
	 * @see ICommonActionProvider#setActionContext(ActionContext)
	 */
	public void setActionContext(ActionContext aContext) {

	}

	/**
	 * 
	 * @see ICommonActionProvider#fillContextMenu(IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager aMenu) {
		return false;
	}

	/**
	 * 
	 * @see ICommonActionProvider#fillActionBars(IActionBars)
	 */
	public boolean fillActionBars(IActionBars theActionBars) {
		return false;
	}

	/**
	 * 
	 * @see IMementoAware#restoreState(IMemento)
	 */
	public void restoreState(IMemento aMemento) {

	}

	/**
	 * 
	 * @see IMementoAware#saveState(IMemento)
	 */
	public void saveState(IMemento aMemento) {

	}

}

/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionContext;

/**
 * <p>
 * Provides actions from extensions for menu and {@link org.eclipse.ui.IActionBars}
 * &nbsp;contributions.
 * </p>
 * This interface is used by the <b>org.eclipse.wst.common.navigator.views.actionProvider </b>
 * extension point and also as the <i>actionProvider </a> attribute for the
 * <b>org.eclipse.wst.common.navigator.views.navigatorContent </b> extension point.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public interface ICommonActionProvider extends IMementoAware {

	/**
	 * <p>
	 * Initialize the current ICommonActionProvider with the supplied information.
	 * </p>
	 * @param anExtensionId TODO
	 * @param aViewPart
	 *            The view part that the current ICommonActionProvider will be associated with
	 * @param aContentService
	 *            The content service that the current ICommonActionProvider will be associated with
	 * @param aStructuredViewer TODO
	 */
	public void init(String anExtensionId, IViewPart aViewPart, 
						NavigatorContentService aContentService, StructuredViewer aStructuredViewer);

	/**
	 * <p>
	 * Clean up any long-lived objects or resources. Do not dispose of any of the components
	 * supplied during {@link #init(IViewPart, StructuredViewer, NavigatorContentService)}.
	 * </p>
	 *  
	 */
	public void dispose();

	/**
	 * <p>
	 * The action context will be made available before any invocation of
	 * {@link #fillContextMenu(IMenuManager)}&nbsp; or {@link #fillActionBars(IActionBars)}. The
	 * action context contains the current selection in the viewer. Implementors should use that
	 * selection when computing their additions.
	 * </p>
	 * 
	 * @param aContext
	 *            An action context that contains the current selection in the viewer.
	 */
	public void setActionContext(ActionContext aContext);

	/**
	 * <p>
	 * Contribute menu actions to aMenu which are relevant to the selection supplied from
	 * {@link #setActionContext(ActionContext)}.
	 * </p>
	 * 
	 * @param aMenu
	 *            The menu from the viewer that requires contributions.
	 * @return True if anything was added to the menu
	 */
	public boolean fillContextMenu(IMenuManager aMenu);

	/**
	 * <p>
	 * Contribute the correct global, retargetable actions based on the selection supplied in
	 * {@link #setActionContext(ActionContext)}. Do not call {@link IActionBars#updateActionBars()}
	 * &nbsp; during this method. The action bars will be updated after all extensions have had a
	 * chance to contribute.
	 * </p>
	 * 
	 * @param theActionBars
	 *            The action bars associated with the current viewer
	 * @return True if anything was added to the action bars
	 */
	public boolean fillActionBars(IActionBars theActionBars);


}
/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.core.runtime.IPath;
import org.eclipse.help.ui.internal.views.IHelpPart;
import org.eclipse.help.ui.internal.views.ReusableHelpPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;
import org.eclipse.ui.internal.cheatsheets.state.ICheatSheetStateManager;

/**
 * A help part wrapper that contains a cheat sheet. This is used to display
 * cheat sheets inside the ReusableHelpPart.
 */
public class CheatSheetHelpPart extends AbstractFormPart implements IHelpPart {
	
	public static final String ID = "cheatsheet-page"; //$NON-NLS-1$
	
	private CheatSheetViewer viewer;
	private String id;
	
	/**
	 * Constructs a new part.
	 * 
	 * @param parent the parent Composite that will contain the widgets
	 * @param toolkit the form toolkit to use for creating the widgets
	 * @param tbm the toolbar we will contribute to
	 * @param id the unique id of the cheatsheet to display in the part
	 */
	public CheatSheetHelpPart(Composite parent, FormToolkit toolkit, IToolBarManager tbm, CheatSheetElement content, ICheatSheetStateManager trayManager) {
		id = content.getID();
		viewer = new CheatSheetViewer(true);
		viewer.createPartControl(parent);
		viewer.setContent(content, trayManager);
		contributeToToolBar(tbm);
	}

	/**
	 * Contributes any actions we have to the toolbar.
	 * 
	 * @param tbm the toolbar to contribute to
	 */
	private void contributeToToolBar(IToolBarManager tbm) {
		IPath path = CheatSheetPlugin.ICONS_PATH.append(CheatSheetPlugin.T_ELCL).append("collapseall.gif");//$NON-NLS-1$
		ImageDescriptor collapseImage = CheatSheetPlugin.createImageDescriptor(CheatSheetPlugin.getPlugin().getBundle(), path);
		CheatSheetExpandRestoreAction expandRestoreAction = new CheatSheetExpandRestoreAction(Messages.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP, false, viewer);
		expandRestoreAction.setToolTipText(Messages.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP);
		expandRestoreAction.setImageDescriptor(collapseImage);
		tbm.insertBefore("back", expandRestoreAction); //$NON-NLS-1$
		tbm.insertBefore("back", new Separator()); //$NON-NLS-1$
		viewer.setExpandRestoreAction(expandRestoreAction);
	}
	
	/**
	 * This part doesn't require a context menu.
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return false;
	}
	
	/**
	 * Returns the part's top Control.
	 */
	public Control getControl() {
		return viewer.getControl();
	}
	
	/**
	 * This part doesn't use any global actions.
	 */
	public IAction getGlobalAction(String id) {
		return null;
	}
	
	/**
	 * Returns the part's unique identifier.
	 * 
	 * @param the unique id for the part
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns whether or not this part contains the given Control, which
	 * is in focus.
	 * 
	 * @param control the Control in focus
	 */
	public boolean hasFocusControl(Control control) {
		return viewer.hasFocusControl(control);
	}
	
	/**
	 * Initializes the part.
	 */
	public void init(ReusableHelpPart parent, String id, IMemento memento) {
		this.id = id;
	}

	/**
	 * No filtering required.
	 */
	public void refilter() {
	}
	
	/**
	 * The cheat sheet automatically saves its state; no action required.
	 */
	public void saveState(IMemento memento) {
	}
	
	/**
	 * Sets the visibility of the part.
	 * 
	 * @param whether or not the part should be visible
	 */
	public void setVisible(boolean visible) {
		viewer.getControl().setVisible(visible);
	}
	
	/**
	 * No action needed for this part here.
	 */
	public void stop() {
	}
	
	/**
	 * No action needed for this part here.
	 */
	public void toggleRoleFilter() {
	}
}

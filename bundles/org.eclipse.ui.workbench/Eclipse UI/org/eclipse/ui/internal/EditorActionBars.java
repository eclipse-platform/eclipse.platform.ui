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
package org.eclipse.ui.internal;


import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.actions.RetargetAction;

/**
 * The action bars for an editor.
 */
public class EditorActionBars extends SubActionBars {
	private String type;
	private int refCount;
	private IEditorActionBarContributor editorContributor;
	private IEditorActionBarContributor extensionContributor;
	private CoolItemMultiToolBarManager coolItemToolBarMgr;
	private boolean enabledAllowed = true;

	private class Overrides implements IContributionManagerOverrides {
		public Boolean getEnabled(IContributionItem item) {
			if (((item instanceof ActionContributionItem) && (((ActionContributionItem) item).getAction() instanceof RetargetAction)) || enabledAllowed)
				return null;
			else
				return Boolean.FALSE;
		}
		public Integer getAccelerator(IContributionItem item) {
			return null;
		}
		public String getAcceleratorText(IContributionItem item) {
			return null;
		}
		public String getText(IContributionItem item) {
			return null;
		}
	}

	/**
	 * Constructs the EditorActionBars for an editor.  
	 */
	public EditorActionBars(IActionBars parent, String type) {
		super(parent);
		this.type = type;
	}
	/**
	 * Activate the contributions.
	 */
	public void activate(boolean forceVisibility) {
		setActive(true, forceVisibility);
	}
	/**
	 * Add one ref to the bars.
	 */
	public void addRef() {
		++refCount;
	}
	/* (non-Javadoc)
	 * Method declared on SubActionBars.
	 */
	protected SubMenuManager createSubMenuManager(IMenuManager parent) {
		return new EditorMenuManager(parent);
	}
	/* (non-Javadoc)
	 * Method declared on SubActionBars.
	 */
	protected SubToolBarManager createSubToolBarManager(IToolBarManager parent) {
		// return null, editor actions are managed by CoolItemToolBarManagers
		return null;
	}
	/**
	 * Deactivate the contributions.
	 */
	public void deactivate(boolean forceVisibility) {
		setActive(false, forceVisibility);
	}
	/**
	 * Dispose the contributions.
	 */
	public void dispose() {
		super.dispose();
		if (editorContributor != null)
			editorContributor.dispose();
		if (extensionContributor != null)
			extensionContributor.dispose();
		if (coolItemToolBarMgr != null)
			coolItemToolBarMgr.removeAll();
	}
	/**
	 * Gets the editor contributor
	 */
	public IEditorActionBarContributor getEditorContributor() {
		return editorContributor;
	}
	/**
	 * Gets the extension contributor
	 */
	public IEditorActionBarContributor getExtensionContributor() {
		return extensionContributor;
	}
	/**
	 * Returns the editor type.
	 */
	public String getEditorType() {
		return type;
	}
	/**
	 * Returns the tool bar manager.  If items are added or
	 * removed from the manager be sure to call <code>updateActionBars</code>.
	 * Overridden to support CoolBars.
	 *
	 * @return the tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		IToolBarManager parentMgr = getParent().getToolBarManager();
		if (parentMgr == null) {
			return null;
		}
		if (coolItemToolBarMgr == null) {
			// Create a CoolItem manager for this action bar.  The CoolBarContributionItem(s)
			// will be created when the EditorActionBar is initialized.
			CoolBarManager cBarMgr = ((CoolBarManager) parentMgr);
			coolItemToolBarMgr = new CoolItemMultiToolBarManager(cBarMgr, type, getActive());
			coolItemToolBarMgr.setParentMgr(cBarMgr);
			coolItemToolBarMgr.setOverrides(new Overrides());
		}
		return coolItemToolBarMgr;
	}
	/**
	 * Returns the reference count.
	 */
	public int getRef() {
		return refCount;
	}
	/**
	 * Returns whether the contribution list is visible.
	 * If the visibility is <code>true</code> then each item within the manager 
	 * appears within the parent manager.  Otherwise, the items are not visible.
	 *
	 * @return <code>true</code> if the manager is visible
	 */
	private boolean isVisible() {
		if (coolItemToolBarMgr != null)
			return coolItemToolBarMgr.isVisible();
		return false;
	}
	/**
	 * Sets the target part for the action bars.
	 * For views this is ignored because each view has its own action vector.
	 * For editors this is important because the action vector is shared by editors of the same type.
	 */
	public void partChanged(IWorkbenchPart part) {
		super.partChanged(part);
		if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			if (editorContributor != null)
				editorContributor.setActiveEditor(editor);
			if (extensionContributor != null)
				extensionContributor.setActiveEditor(editor);
		}
	}
	/**
	 * Remove one ref to the bars.
	 */
	public void removeRef() {
		--refCount;
	}
	/**
	 * Activate / Deactivate the contributions.
	 * 
	 * Workaround for flashing when editor contributes
	 * many menu/tool contributions. In this case, the force visibility
	 * flag determines if the contributions should be actually
	 * made visible/hidden or just change the enablement state.
	 */
	private void setActive(boolean set, boolean forceVisibility) {
		basicSetActive(set);
		if (isSubMenuManagerCreated())
			 ((EditorMenuManager) getMenuManager()).setVisible(set, forceVisibility);

		if (isSubStatusLineManagerCreated())
			((SubStatusLineManager)getStatusLineManager()).setVisible(set);

		setVisible(set, forceVisibility);
	}
	/**
	 * Sets the editor contributor
	 */
	public void setEditorContributor(IEditorActionBarContributor c) {
		editorContributor = c;
	}
	/**
	 * Sets the extension contributor
	 */
	public void setExtensionContributor(IEditorActionBarContributor c) {
		extensionContributor = c;
	}
	/**
	 * Sets the visibility of the manager.  If the visibility is <code>true</code>
	 * then each item within the manager appears within the parent manager.
	 * Otherwise, the items are not visible.
	 *
	 * @param visible the new visibility
	 */
	private void setVisible(boolean visible) {
		if (coolItemToolBarMgr != null)
			coolItemToolBarMgr.setVisible(visible);
	}
	/**
	 * Sets the visibility of the manager. If the visibility is <code>true</code>
	 * then each item within the manager appears within the parent manager.
	 * Otherwise, the items are not visible if force visibility is
	 * <code>true</code>, or grayed out if force visibility is <code>false</code>
	 * <p>
	 * This is a workaround for the layout flashing when editors contribute
	 * large amounts of items.</p>
	 *
	 * @param visible the new visibility
	 * @param forceVisibility whether to change the visibility or just the
	 * 		enablement state. This parameter is ignored if visible is 
	 * 		<code>true</code>.
	 */
	private void setVisible(boolean visible, boolean forceVisibility) {
		if (visible) {
			if (forceVisibility) {
				// Make the items visible 
				if (!enabledAllowed)
					setEnabledAllowed(true);
			} else {
				if (enabledAllowed)
					setEnabledAllowed(false);
			}
			if (!isVisible())
				setVisible(true);
		} else {
			if (forceVisibility)
				// Remove the editor tool bar items
				setVisible(false);
			else
				// Disabled the tool bar items.
				setEnabledAllowed(false);
		}
		if (coolItemToolBarMgr != null)
			coolItemToolBarMgr.setVisible(visible, forceVisibility);
	}
	/**
	 * Sets the enablement ability of all the items contributed by the editor.
	 * 
	 * @param enabledAllowed <code>true</code> if the items may enable
	 * @since 2.0
	 */
	private void setEnabledAllowed(boolean enabledAllowed) {
		if (this.enabledAllowed == enabledAllowed)
			return;
		this.enabledAllowed = enabledAllowed;
		if (coolItemToolBarMgr != null) {
			IContributionItem[] items = coolItemToolBarMgr.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				item.update(IContributionManagerOverrides.P_ENABLED);
			}
		}
	}
}

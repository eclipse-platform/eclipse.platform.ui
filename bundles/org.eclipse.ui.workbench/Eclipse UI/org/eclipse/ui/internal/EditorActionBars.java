/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubContributionManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.SubActionBars2;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.expressions.LegacyEditorActionBarExpression;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.provisional.presentations.IActionBarPresentationFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * The action bars for an editor.
 */
public class EditorActionBars extends SubActionBars2 {

	private class Overrides implements IContributionManagerOverrides {

		public Integer getAccelerator(IContributionItem item) {
			return null;
		}

		public String getAcceleratorText(IContributionItem item) {
			return null;
		}

		public Boolean getEnabled(IContributionItem item) {
			if (((item instanceof ActionContributionItem) && (((ActionContributionItem) item)
					.getAction() instanceof RetargetAction))
					|| enabledAllowed) {
				return null;
			} else {
				return Boolean.FALSE;
			}
		}

		public String getText(IContributionItem item) {
			return null;
		}
	}

	/**
	 * A switch controlling verbose debugging options surrounding the disposal
	 * of tool bar contribution items. There have been problems in the past with
	 * reusing disposed items, and leaking memory by failing to drop references
	 * to disposed items.
	 */
	private static final boolean DEBUG_TOOLBAR_DISPOSAL = Policy.DEBUG_TOOLBAR_DISPOSAL;

	private IToolBarManager coolItemToolBarMgr = null;

	private IEditorActionBarContributor editorContributor;

	private boolean enabledAllowed = false;

	private IEditorActionBarContributor extensionContributor;

	private int refCount;

	private IToolBarContributionItem toolBarContributionItem = null;

	private String type;
	
	private IActionBarPresentationFactory actionBarPresentation;


	/**
	 * Constructs the EditorActionBars for an editor.
	 */
	public EditorActionBars(WorkbenchPage page,
			final IServiceLocator serviceLocator, String type) {
		super((IActionBars2)page.getActionBars(), serviceLocator);
		this.type = type;
		this.actionBarPresentation = ((WorkbenchWindow) page
				.getWorkbenchWindow()).getActionBarPresentationFactory();
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

	/*
	 * (non-Javadoc) Method declared on SubActionBars.
	 */
	protected SubMenuManager createSubMenuManager(IMenuManager parent) {
		return new EditorMenuManager(parent);
	}

	/*
	 * (non-Javadoc) Method declared on SubActionBars.
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
		if (editorContributor != null) {
			editorContributor.dispose();
		}
		if (extensionContributor != null) {
			extensionContributor.dispose();
		}

		/*
		 * Dispose of the contribution item, but also make sure that no one else
		 * is holding on to it. In this case, go through the SubCoolBarManager
		 * to its parent (the real CoolBarManager), and replace the reference
		 * with a placeholder.
		 */
		if (toolBarContributionItem != null) {
			// Create a placeholder and place it in the cool bar manager.
			ICoolBarManager coolBarManager = getCoolBarManager();
			if (coolBarManager instanceof SubContributionManager) {
				SubContributionManager subManager = (SubContributionManager) coolBarManager;
				IContributionManager manager = subManager.getParent();
				if (manager instanceof ContributionManager) {
					final IContributionItem replacementItem = new PlaceholderContributionItem(
							toolBarContributionItem);
					boolean succeeded = ((ContributionManager) manager)
							.replaceItem(replacementItem.getId(),
									replacementItem);
					if (!succeeded && DEBUG_TOOLBAR_DISPOSAL) {
						System.out
								.println("FAILURE WHILE DISPOSING EditorActionBars"); //$NON-NLS-1$
						System.out
								.println("Could not replace " + replacementItem.getId() + " in the contribution manager."); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else if (DEBUG_TOOLBAR_DISPOSAL) {
					System.out
							.println("FAILURE WHILE DISPOSING EditorActionBars"); //$NON-NLS-1$
					System.out
							.println("The manager was not a ContributionManager."); //$NON-NLS-1$
					System.out
							.println("It was a " + manager.getClass().getName()); //$NON-NLS-1$
				}
			} else if (DEBUG_TOOLBAR_DISPOSAL) {
				System.out.println("FAILURE WHILE DISPOSING EditorActionBars"); //$NON-NLS-1$
				System.out
						.println("The coolBarManager was not a SubContributionManager"); //$NON-NLS-1$
				System.out
						.println("It was a " + coolBarManager.getClass().getName()); //$NON-NLS-1$
			}

			// Dispose of the replaced item.
			toolBarContributionItem.dispose();
		}
		toolBarContributionItem = null;
		// Remove actions
		if (coolItemToolBarMgr != null) {
			coolItemToolBarMgr.removeAll();
		}
		coolItemToolBarMgr = null;
		editorHandlerExpression = null;
	}

	/**
	 * Gets the editor contributor
	 */
	public IEditorActionBarContributor getEditorContributor() {
		return editorContributor;
	}

	/**
	 * Returns the editor type.
	 */
	public String getEditorType() {
		return type;
	}

	/**
	 * Gets the extension contributor
	 */
	public IEditorActionBarContributor getExtensionContributor() {
		return extensionContributor;
	}

	/**
	 * Returns the reference count.
	 */
	public int getRef() {
		return refCount;
	}

	/**
	 * Returns the tool bar manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>. Overridden to
	 * support CoolBars.
	 * 
	 * @return the tool bar manager
	 */
	public IToolBarManager getToolBarManager() {

		// by pass the sub coolBar and use the real cool bar.
		ICoolBarManager coolBarManager = getCastedParent().getCoolBarManager();
		if (coolBarManager == null) {
			return null;
		}

		// add the editor group if the app did not add it already,
		// otherwise the references to it below will fail
		if (coolBarManager.find(IWorkbenchActionConstants.GROUP_EDITOR) == null) {
			coolBarManager.add(new GroupMarker(
					IWorkbenchActionConstants.GROUP_EDITOR));
		}
		if (toolBarContributionItem == null) {
			IContributionItem foundItem = coolBarManager.find(type);
			if ((foundItem instanceof IToolBarContributionItem)) {
				toolBarContributionItem = (IToolBarContributionItem) foundItem;
				coolItemToolBarMgr = toolBarContributionItem.getToolBarManager();
				if (coolItemToolBarMgr == null) {
					coolItemToolBarMgr = actionBarPresentation.createToolBarManager();
					toolBarContributionItem = actionBarPresentation.createToolBarContributionItem(
							coolItemToolBarMgr, type);
					// Add editor item to group
					coolBarManager.prependToGroup(
							IWorkbenchActionConstants.GROUP_EDITOR,
							toolBarContributionItem);
				}
			} else {
				coolItemToolBarMgr = actionBarPresentation.createToolBarManager();
				if ((coolBarManager instanceof ContributionManager)
						&& (foundItem instanceof PlaceholderContributionItem)) {
					PlaceholderContributionItem placeholder = (PlaceholderContributionItem) foundItem;
					toolBarContributionItem = createToolBarContributionItem(coolItemToolBarMgr, placeholder);
					// Restore from a placeholder
					((ContributionManager) coolBarManager).replaceItem(type,
							toolBarContributionItem);
				} else {
					toolBarContributionItem = actionBarPresentation.createToolBarContributionItem(
							coolItemToolBarMgr, type);
					// Add editor item to group
					coolBarManager.prependToGroup(
							IWorkbenchActionConstants.GROUP_EDITOR,
							toolBarContributionItem);
				}
			}
			((ContributionManager)coolItemToolBarMgr).setOverrides(new Overrides());
			toolBarContributionItem.setVisible(getActive());
			coolItemToolBarMgr.markDirty();
		}

		return coolItemToolBarMgr;
	}
	
    /*
     * Creates a new tool bar contribution item on the given manager -- using
     * the stored data to initialize some of its properties.
     */
    IToolBarContributionItem createToolBarContributionItem(
			final IToolBarManager manager, PlaceholderContributionItem item) {
		IToolBarContributionItem toolBarContributionItem = actionBarPresentation
				.createToolBarContributionItem(manager, item.getId());
		toolBarContributionItem.setCurrentHeight(item.getHeight());
		toolBarContributionItem.setCurrentWidth(item.getWidth());
		toolBarContributionItem.setMinimumItemsToShow(item.getMinimumItemsToShow());
		toolBarContributionItem.setUseChevron(item.getUseChevron());
		return toolBarContributionItem;
	}

	/**
	 * Returns whether the contribution list is visible. If the visibility is
	 * <code>true</code> then each item within the manager appears within the
	 * parent manager. Otherwise, the items are not visible.
	 * 
	 * @return <code>true</code> if the manager is visible
	 */
	private boolean isVisible() {
		if (toolBarContributionItem != null) {
			return toolBarContributionItem.isVisible();
		}
		return false;
	}

	/**
	 * Sets the target part for the action bars. For views this is ignored
	 * because each view has its own action vector. For editors this is
	 * important because the action vector is shared by editors of the same
	 * type.
	 */
	public void partChanged(IWorkbenchPart part) {
		super.partChanged(part);
		if (part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			if (editorContributor != null) {
				editorContributor.setActiveEditor(editor);
			}
			if (extensionContributor != null) {
				extensionContributor.setActiveEditor(editor);
			}
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
	 * Workaround for flashing when editor contributes many menu/tool
	 * contributions. In this case, the force visibility flag determines if the
	 * contributions should be actually made visible/hidden or just change the
	 * enablement state.
	 */
	private void setActive(boolean set, boolean forceVisibility) {
		basicSetActive(set);
		if (isSubMenuManagerCreated()) {
			((EditorMenuManager) getMenuManager()).setVisible(set,
					forceVisibility);
		}

		if (isSubStatusLineManagerCreated()) {
			((SubStatusLineManager) getStatusLineManager()).setVisible(set);
		}

		setVisible(set, forceVisibility);
	}

	/**
	 * Sets the editor contributor
	 */
	public void setEditorContributor(IEditorActionBarContributor c) {
		editorContributor = c;
	}

	/**
	 * Sets the enablement ability of all the items contributed by the editor.
	 * 
	 * @param enabledAllowed
	 *            <code>true</code> if the items may enable
	 * @since 2.0
	 */
	private void setEnabledAllowed(boolean enabledAllowed) {
		if (this.enabledAllowed == enabledAllowed) {
			return;
		}
		this.enabledAllowed = enabledAllowed;
		if (coolItemToolBarMgr != null) {
			IContributionItem[] items = coolItemToolBarMgr.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				item.update(IContributionManagerOverrides.P_ENABLED);
			}
		}
	}

	/**
	 * Sets the extension contributor
	 */
	public void setExtensionContributor(IEditorActionBarContributor c) {
		extensionContributor = c;
	}

	/**
	 * Sets the visibility of the manager. If the visibility is
	 * <code>true</code> then each item within the manager appears within the
	 * parent manager. Otherwise, the items are not visible.
	 * 
	 * @param visible
	 *            the new visibility
	 */
	private void setVisible(boolean visible) {
		if (toolBarContributionItem != null) {
			toolBarContributionItem.setVisible(visible);
			if (toolBarContributionItem.getParent() != null) {
				toolBarContributionItem.getParent().markDirty();
			}
		}
	}

	/**
	 * Sets the visibility of the manager. If the visibility is
	 * <code>true</code> then each item within the manager appears within the
	 * parent manager. Otherwise, the items are not visible if force visibility
	 * is <code>true</code>, or grayed out if force visibility is
	 * <code>false</code>
	 * <p>
	 * This is a workaround for the layout flashing when editors contribute
	 * large amounts of items.
	 * </p>
	 * 
	 * @param visible
	 *            the new visibility
	 * @param forceVisibility
	 *            <code>true</code> to change the visibility or
	 *            <code>false</code> to change just the enablement state. This
	 *            parameter is ignored if visible is <code>true</code>.
	 */
	private void setVisible(boolean visible, boolean forceVisibility) {
		if (visible) {
			setEnabledAllowed(true);
			if (!isVisible()) {
				setVisible(true);
			}
		} else {
			if (forceVisibility) {
				// Remove the editor tool bar items
				setVisible(false);
			} else {
				// Disabled the tool bar items.
				setEnabledAllowed(false);
			}
		}

		ICoolBarManager coolBarManager = getCastedParent().getCoolBarManager();
		if ((coolItemToolBarMgr != null) && (coolBarManager != null)) {
			IContributionItem[] items = coolItemToolBarMgr.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				item.setVisible(visible || !forceVisibility);
				coolItemToolBarMgr.markDirty();
				if (!coolBarManager.isDirty()) {
					coolBarManager.markDirty();
				}
			}
			// Update the manager
			coolItemToolBarMgr.update(false);
			if (toolBarContributionItem != null) {
				toolBarContributionItem.setVisible(visible || !forceVisibility);
			}
		}
	}

	private LegacyEditorActionBarExpression editorHandlerExpression = null;
	
	/**
	 * Returns the expression used for action handler activation.
	 * @return the expression used for action handler activation.
	 */
	public Expression getHandlerExpression() {
		if (editorHandlerExpression == null) {
			editorHandlerExpression = new LegacyEditorActionBarExpression(type);
		}
		return editorHandlerExpression;
	}
}

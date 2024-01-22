/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * This class reads the registry for extensions that plug into 'popupMenus'
 * extension point and deals only with the 'viewerContribution' elements.
 */
public class ViewerActionBuilder extends PluginActionBuilder {

	private ISelectionProvider provider;

	private IWorkbenchPart part;

	/**
	 * Basic contstructor
	 */
	public ViewerActionBuilder() {
	}

	@Override
	protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
		if (part instanceof IViewPart) {
			return new ActionDescriptor(element, ActionDescriptor.T_VIEW, part);
		}
		return new ActionDescriptor(element, ActionDescriptor.T_EDITOR, part);
	}

	@Override
	protected BasicContribution createContribution() {
		return new ViewerContribution(provider);
	}

	/**
	 * Dispose of the action builder
	 */
	public void dispose() {
		if (cache != null) {
			for (Object contribution : cache) {
				((BasicContribution) contribution).dispose();
			}
			cache = null;
		}
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		String tag = element.getName();

		// Found visibility sub-element
		if (currentContribution != null && tag.equals(IWorkbenchRegistryConstants.TAG_VISIBILITY)) {
			((ViewerContribution) currentContribution).setVisibilityTest(element);
			return true;
		}

		return super.readElement(element);
	}

	/**
	 * Reads the contributions for a viewer menu. This method is typically used in
	 * conjunction with <code>contribute</code> to read and then insert actions for
	 * a particular viewer menu.
	 *
	 * @param id   the menu id
	 * @param prov the selection provider for the control containing the menu
	 * @param part the part containing the menu.
	 * @return <code>true</code> if 1 or more items were read.
	 */
	public boolean readViewerContributions(String id, ISelectionProvider prov, IWorkbenchPart part) {
		Assert.isTrue(part instanceof IViewPart || part instanceof IEditorPart);
		provider = prov;
		this.part = part;
		readContributions(id, IWorkbenchRegistryConstants.TAG_VIEWER_CONTRIBUTION,
				IWorkbenchRegistryConstants.PL_POPUP_MENU);
		return (cache != null);
	}

	/**
	 * Helper class to collect the menus and actions defined within a contribution
	 * element.
	 */
	private static class ViewerContribution extends BasicContribution implements ISelectionChangedListener {
		private ISelectionProvider selProvider;

		private ActionExpression visibilityTest;

		/**
		 * Create a new ViewerContribution.
		 *
		 * @param selProvider the selection provider
		 */
		public ViewerContribution(ISelectionProvider selProvider) {
			super();
			this.selProvider = selProvider;
			if (selProvider != null) {
				selProvider.addSelectionChangedListener(this);
			}
		}

		/**
		 * Set the visibility test.
		 *
		 * @param element the element
		 */
		public void setVisibilityTest(IConfigurationElement element) {
			visibilityTest = new ActionExpression(element);
		}

		@Override
		public void contribute(IMenuManager menu, boolean menuAppendIfMissing, IToolBarManager toolbar,
				boolean toolAppendIfMissing) {
			boolean visible = true;

			if (visibilityTest != null) {
				ISelection selection = selProvider.getSelection();
				if (selection instanceof IStructuredSelection) {
					visible = visibilityTest.isEnabledFor((IStructuredSelection) selection);
				} else {
					visible = visibilityTest.isEnabledFor(selection);
				}
			}

			if (visible) {
				super.contribute(menu, menuAppendIfMissing, toolbar, toolAppendIfMissing);
			}
		}

		@Override
		public void dispose() {
			if (selProvider != null) {
				selProvider.removeSelectionChangedListener(this);
			}
			disposeActions();
			super.dispose();
		}

		/**
		 * Rather than hooking up each action as a selection listener, the contribution
		 * itself is added, and propagates the selection changed notification to all
		 * actions. This simplifies cleanup, in addition to potentially reducing the
		 * number of listeners.
		 *
		 * @see ISelectionChangedListener
		 * @since 3.1
		 */
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (actions != null) {
				for (ActionDescriptor action : actions) {
					PluginAction proxy = action.getAction();
					proxy.selectionChanged(event);
				}
			}
		}
	}

}

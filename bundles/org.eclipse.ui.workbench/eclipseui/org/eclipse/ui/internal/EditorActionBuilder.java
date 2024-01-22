/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import java.util.ArrayList;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * This class reads the registry for extensions that plug into 'editorActions'
 * extension point.
 */
public class EditorActionBuilder extends PluginActionBuilder {
	private static final String TAG_CONTRIBUTION_TYPE = "editorContribution"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public EditorActionBuilder() {
	}

	@Override
	protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
		return new ActionDescriptor(element, ActionDescriptor.T_EDITOR);
	}

	@Override
	protected BasicContribution createContribution() {
		return new EditorContribution();
	}

	/**
	 * Reads and apply all external contributions for this editor's ID registered in
	 * 'editorActions' extension point.
	 */
	public IEditorActionBarContributor readActionExtensions(IEditorDescriptor desc) {
		ExternalContributor ext = null;
		readContributions(desc.getId(), TAG_CONTRIBUTION_TYPE, IWorkbenchRegistryConstants.PL_EDITOR_ACTIONS);
		if (cache != null) {
			ext = new ExternalContributor(cache);
			cache = null;
		}
		return ext;
	}

	/**
	 * Helper class to collect the menus and actions defined within a contribution
	 * element.
	 */
	private static class EditorContribution extends BasicContribution {
		@Override
		public void dispose() {
			disposeActions();
			super.dispose();
		}

		public void editorChanged(IEditorPart editor) {
			if (actions != null) {
				for (ActionDescriptor ad : actions) {
					EditorPluginAction action = (EditorPluginAction) ad.getAction();
					action.editorChanged(editor);
				}
			}
		}
	}

	/**
	 * Helper class that will populate the menu and toolbar with the external editor
	 * contributions.
	 */
	public static class ExternalContributor implements IEditorActionBarContributor {
		private ArrayList<Object> cache;

		public ExternalContributor(ArrayList<Object> cache) {
			this.cache = cache;
		}

		@Override
		public void dispose() {
			for (Object contribution : cache) {
				((EditorContribution) contribution).dispose();
			}
		}

		public ActionDescriptor[] getExtendedActions() {
			ArrayList<ActionDescriptor> results = new ArrayList<>();
			for (Object contribution : cache) {
				EditorContribution ec = (EditorContribution) contribution;
				if (ec.actions != null) {
					results.addAll(ec.actions);
				}
			}
			return results.toArray(new ActionDescriptor[results.size()]);
		}

		@Override
		public void init(IActionBars bars, IWorkbenchPage page) {
			for (Object contribution : cache) {
				((EditorContribution) contribution).contribute(bars.getMenuManager(), false, bars.getToolBarManager(),
						true);
			}
		}

		@Override
		public void setActiveEditor(IEditorPart editor) {
			for (Object contribution : cache) {
				((EditorContribution) contribution).editorChanged(editor);
			}
		}
	}
}

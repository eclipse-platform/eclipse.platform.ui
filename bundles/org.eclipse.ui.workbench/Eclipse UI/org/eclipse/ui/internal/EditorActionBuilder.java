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

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * This class reads the registry for extensions that plug into
 * 'editorActions' extension point.
 */
public class EditorActionBuilder extends PluginActionBuilder {
    private static final String TAG_CONTRIBUTION_TYPE = "editorContribution"; //$NON-NLS-1$

    /**
     * The constructor.
     */
    public EditorActionBuilder() {
    }

    /* (non-Javadoc)
     * Method declared on PluginActionBuilder.
     */
    protected ActionDescriptor createActionDescriptor(
            IConfigurationElement element) {
        return new ActionDescriptor(element, ActionDescriptor.T_EDITOR);
    }

    /* (non-Javadoc)
     * Method declared on PluginActionBuilder.
     */
    protected BasicContribution createContribution() {
        return new EditorContribution();
    }

    /**
     * Reads and apply all external contributions for this editor's ID
     * registered in 'editorActions' extension point.
     */
    public IEditorActionBarContributor readActionExtensions(
            IEditorDescriptor desc) {
        ExternalContributor ext = null;
        readContributions(desc.getId(), TAG_CONTRIBUTION_TYPE,
                IWorkbenchRegistryConstants.PL_EDITOR_ACTIONS);
        if (cache != null) {
            ext = new ExternalContributor(cache);
            cache = null;
        }
        return ext;
    }

    /**
     * Helper class to collect the menus and actions defined within a
     * contribution element.
     */
    private static class EditorContribution extends BasicContribution {
        public void dispose() {
			disposeActions();
			super.dispose();
        }

        public void editorChanged(IEditorPart editor) {
            if (actions != null) {
                for (int i = 0; i < actions.size(); i++) {
                    ActionDescriptor ad = (ActionDescriptor) actions.get(i);
                    EditorPluginAction action = (EditorPluginAction) ad
                            .getAction();
                    action.editorChanged(editor);
                }
            }
        }
    }

    /**
     * Helper class that will populate the menu and toobar with the external
     * editor contributions.
     */
    public static class ExternalContributor implements
            IEditorActionBarContributor {
        private ArrayList cache;

        public ExternalContributor(ArrayList cache) {
            this.cache = cache;
        }

        public void dispose() {
            for (int i = 0; i < cache.size(); i++) {
                ((EditorContribution) cache.get(i)).dispose();
            }
        }

        public ActionDescriptor[] getExtendedActions() {
            ArrayList results = new ArrayList();
            for (int i = 0; i < cache.size(); i++) {
                EditorContribution ec = (EditorContribution) cache.get(i);
                if (ec.actions != null) {
					results.addAll(ec.actions);
				}
            }
            return (ActionDescriptor[]) results
                    .toArray(new ActionDescriptor[results.size()]);
        }

        public void init(IActionBars bars, IWorkbenchPage page) {
            for (int i = 0; i < cache.size(); i++) {
                ((EditorContribution) cache.get(i)).contribute(bars
                        .getMenuManager(), false, bars.getToolBarManager(),
                        true);
            }
        }

        public void setActiveEditor(IEditorPart editor) {
            for (int i = 0; i < cache.size(); i++) {
                ((EditorContribution) cache.get(i)).editorChanged(editor);
            }
        }
    }
}

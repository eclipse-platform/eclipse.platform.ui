/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.PinEditorAction;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;

/**
 * This contribution item controls the visibility of the pin editor
 * action based on the current preference value for reusing editors.
 * 
 * @since 3.0
 */
public class PinEditorContributionItem extends ActionContributionItem {
    private IWorkbenchWindow window = null;

    private boolean reuseEditors = false;

    private IPropertyChangeListener prefListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(
					IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
                if (getParent() != null) {
                    IPreferenceStore store = WorkbenchPlugin.getDefault()
                            .getPreferenceStore();
					reuseEditors = store
							.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)
							|| ((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).alwaysShowPinAction();
                    setVisible(reuseEditors);
                    getParent().markDirty();
                    if (window.getShell() != null
                            && !window.getShell().isDisposed()) {
                        // this property change notification could be from a non-ui thread
                        window.getShell().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                getParent().update(false);
                            }
                        });
                    }
                }
            }
        }
    };

    /**
     * @param action
     */
    public PinEditorContributionItem(PinEditorAction action,
            IWorkbenchWindow window) {
        super(action);

        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.window = window;

        IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		reuseEditors = store
				.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)
				|| ((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).alwaysShowPinAction();
        setVisible(reuseEditors);
        WorkbenchPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(prefListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isVisible()
     */
    public boolean isVisible() {
        // @issue the ActionContributionItem implementation of this method ignores the "visible" value set
        return super.isVisible() && reuseEditors;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#dispose()
     */
    public void dispose() {
        super.dispose();
        WorkbenchPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(prefListener);
    }
}

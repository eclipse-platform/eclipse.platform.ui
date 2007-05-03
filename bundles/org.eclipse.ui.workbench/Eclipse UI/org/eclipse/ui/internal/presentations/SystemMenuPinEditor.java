/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;

public class SystemMenuPinEditor extends Action implements ISelfUpdatingAction {

    private EditorPane editorPane;

    public SystemMenuPinEditor(EditorPane pane) {
        setText(WorkbenchMessages.EditorPane_pinEditor);
        setPane(pane);
    }

    public void dispose() {
        editorPane = null;
    }

    public void setPane(EditorPane pane) {
        editorPane = pane;
        update();
    }

    public void run() {
        WorkbenchPartReference ref = (WorkbenchPartReference)editorPane.getPartReference();

        ref.setPinned(!isChecked());
    }

    public void update() {
        if (editorPane == null) {
            setEnabled(false);
            return;
        }

        WorkbenchPartReference ref = (WorkbenchPartReference)editorPane.getPartReference();
        setEnabled(true);
        setChecked(ref.isPinned());
    }

    public boolean shouldBeVisible() {
        if (editorPane == null) {
            return false;
        }

        IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean reuseEditor = store
				.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)
				| ((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).alwaysShowPinAction();
        return reuseEditor;
    }

}

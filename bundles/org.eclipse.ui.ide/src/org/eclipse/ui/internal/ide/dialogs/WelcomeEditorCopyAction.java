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

package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Global copy action for the welcome editor.
 */
public class WelcomeEditorCopyAction extends Action {
    private WelcomeEditor editorPart;

    public WelcomeEditorCopyAction(WelcomeEditor editor) {
        editorPart = editor;
        setText(IDEWorkbenchMessages.getString("WelcomeEditor.copy.text")); //$NON-NLS-1$
    }

    public void run() {
        editorPart.getCurrentText().copy();
    }
}
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        setText(IDEWorkbenchMessages.WelcomeEditor_copy_text);
    }

    public void run() {
        editorPart.getCurrentText().copy();
    }
}

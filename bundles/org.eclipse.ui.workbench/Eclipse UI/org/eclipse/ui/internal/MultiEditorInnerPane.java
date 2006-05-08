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

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.part.MultiEditor;

/**
 * Implements a pane of each editor inside a MultiEditor.
 */
public class MultiEditorInnerPane extends EditorPane {

    EditorPane parentPane;

    /**
     * Constructor for MultiEditorInnerPane.
     */
    public MultiEditorInnerPane(EditorPane pane, IEditorReference ref,
            WorkbenchPage page, EditorStack workbook) {
        super(ref, page, workbook);
        parentPane = pane;
    }

    /**
     * Returns the outer editor.
     */
    public EditorPane getParentPane() {
        return parentPane;
    }

    /**
     * Update the gradient on the inner editor title bar
     */
    private void updateGradient() {
        MultiEditor multiEditor = (MultiEditor) parentPane.getPartReference()
                .getPart(true);
        if (multiEditor != null) {
            IEditorPart part = (IEditorPart) this.getEditorReference().getPart(
                    true);
            if (part != null) {
				multiEditor.updateGradient(part);
			}
        }
    }

    /**
     * Indicate focus in part.
     */
    public void showFocus(boolean inFocus) {
        super.showFocus(inFocus);
        updateGradient();
    }

    /* (non-Javadoc)
     * Method declared on PartPane.
     */
    /* package */void shellDeactivated() {
        super.shellDeactivated();
        updateGradient();
    }

    /* (non-Javadoc)
     * Method declared on PartPane.
     */
    /* package */void shellActivated() {
        super.shellActivated();
        updateGradient();
    }

}

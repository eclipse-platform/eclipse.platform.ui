/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nikolay Botev - bug 240651
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.part.AbstractMultiEditor;
import org.eclipse.ui.part.MultiEditor;

/**
 * Implements a pane of each editor inside a AbstractMultiEditor.
 */
public class MultiEditorInnerPane extends EditorPane {

    EditorPane parentPane;

	/**
	 * true if the parent editor is an instance of MultiEditor, which requires
	 * operation in MultiEditor backwards compatibility mode
	 */
    boolean multiEditorCompatibilityMode;

    /**
     * Constructor for MultiEditorInnerPane.
     */
    public MultiEditorInnerPane(EditorPane pane, IEditorReference ref,
            WorkbenchPage page, EditorStack workbook, boolean multiEditor) {
        super(ref, page, workbook);
        parentPane = pane;
        multiEditorCompatibilityMode = multiEditor;
    }

	AbstractMultiEditor getMultiEditor() {
		return (AbstractMultiEditor) parentPane.getPartReference()
                .getPart(true);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		if (!multiEditorCompatibilityMode) {
			Control control = getControl();
			control.addListener(SWT.Activate, new Listener() {
				public void handleEvent(Event event) {
					if (event.type == SWT.Activate) {
						IEditorPart part = (IEditorPart) MultiEditorInnerPane.this.getEditorReference().getPart(
								true);
						AbstractMultiEditor multiEditor = getMultiEditor();
						multiEditor.activateEditor(part);
						multiEditor.setFocus();
					}
				}
			});
			// Inner editor panes should be visible by default
			control.setVisible(true);
		}
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
        AbstractMultiEditor abstractMultiEditor = getMultiEditor();
        if (abstractMultiEditor != null && multiEditorCompatibilityMode) {
            IEditorPart part = (IEditorPart) this.getEditorReference().getPart(
                    true);
            if (part != null) {
				((MultiEditor) abstractMultiEditor).updateGradient(part);
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

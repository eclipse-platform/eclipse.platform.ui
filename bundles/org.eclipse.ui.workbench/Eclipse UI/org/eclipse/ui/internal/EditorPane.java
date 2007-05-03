/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * An EditorPane is a subclass of PartPane offering extended
 * behavior for workbench editors.
 */
public class EditorPane extends PartPane {
    private EditorStack workbook;

   
    /**
     * Constructs an editor pane for an editor part.
     * @param ref
     * @param page
     * @param workbook
     */
    public EditorPane(IEditorReference ref, WorkbenchPage page,
            EditorStack workbook) {
        super(ref, page);
        this.workbook = workbook;
    }

    /**
     * Editor panes do not need a title bar. The editor
     * title and close icon are part of the tab containing
     * the editor. Tools and menus are added directly into
     * the workbench toolbar and menu bar.
     */
    protected void createTitleBar() {
        // do nothing
    }

    /**
     * @see PartPane#doHide()
     */
    public void doHide() {
        getPage().closeEditor(getEditorReference(), true);
    }

    /**
     * Answer the editor part child.
     * @return {@link IEditorReference}
     */
    public IEditorReference getEditorReference() {
        return (IEditorReference) getPartReference();
    }

    /**
     * Answer the SWT widget style.
     */
    int getStyle() {
        return SWT.NONE;
    }

    /**
     * Answer the editor workbook container
     * @return {@link EditorStack}
     */
    public EditorStack getWorkbook() {
        return workbook;
    }

    /**
     * Notify the workbook page that the part pane has
     * been activated by the user.
     */
    public void requestActivation() {
        // By clearing the active workbook if its not the one
        // associated with the editor, we reduce draw flicker
        if (!workbook.isActiveWorkbook()) {
			workbook.getEditorArea().setActiveWorkbook(null, false);
		}

        super.requestActivation();
    }

    /**
     * Set the editor workbook container
     * @param editorWorkbook
     */
    public void setWorkbook(EditorStack editorWorkbook) {
        workbook = editorWorkbook;
    }

    /* (non-Javadoc)
     * Method declared on PartPane.
     */
    /* package */void shellActivated() {
        //this.workbook.drawGradient();
    }

    /* (non-Javadoc)
     * Method declared on PartPane.
     */
    /* package */void shellDeactivated() {
        //this.workbook.drawGradient();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#setFocus()
     */
    public void setFocus() {
        super.setFocus();

        workbook.becomeActiveWorkbook(true);
    }

    /**
     * Indicate focus in part.
     */
    public void showFocus(boolean inFocus) {
        if (inFocus) {
			this.workbook.becomeActiveWorkbook(true);
		} else {
			this.workbook
                    .setActive(this.workbook.isActiveWorkbook() ? StackPresentation.AS_ACTIVE_NOFOCUS
                            : StackPresentation.AS_INACTIVE);
		}
    }

    /**
     * Add the pin menu item on the editor system menu.
     * @param parent
     */
    protected void addPinEditorItem(Menu parent) {
        IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean reuseEditor = store
				.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)
				|| ((TabBehaviour)Tweaklets.get(TabBehaviour.KEY)).alwaysShowPinAction();
        if (!reuseEditor) {
            return;
        }

        final WorkbenchPartReference ref = (WorkbenchPartReference)getPartReference();

        final MenuItem item = new MenuItem(parent, SWT.CHECK);
        item.setText(WorkbenchMessages.EditorPane_pinEditor);
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                IWorkbenchPart part = getPartReference().getPart(true);
                if (part == null) {
                    // this should never happen
                    item.setSelection(false);
                    item.setEnabled(false);
                } else {
                    ref.setPinned(item.getSelection());
                }
            }
        });
        item.setEnabled(true);
        item.setSelection(ref.isPinned());
    }

    /**
     * Update the title attributes for the pane.
     */
    public void updateTitles() {
        //	  TODO commented during presentation refactor 	workbook.updateEditorTab(getEditorReference());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#testInvariants()
     */
    public void testInvariants() {
        super.testInvariants();

        if (getContainer() != null) {
            Assert.isTrue(getContainer() == workbook);
        }
    }

   
    /**
     * Get the name of editor.
     * @return String
     */
    public String getName() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#getToolBar()
     */
    public Control getToolBar() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#isCloseable()
     */
    public boolean isCloseable() {
        return true;
    }

}

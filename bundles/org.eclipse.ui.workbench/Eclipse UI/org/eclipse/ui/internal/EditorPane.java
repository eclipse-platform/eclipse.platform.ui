/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * An EditorPane is a subclass of PartPane offering extended
 * behavior for workbench editors.
 */
public class EditorPane extends PartPane {
    private EditorStack workbook;

    /**
     * Constructs an editor pane for an editor part.
     */
    public EditorPane(IEditorReference ref, WorkbenchPage page,
            EditorStack workbook) {
        super(ref, page);
        this.workbook = workbook;
    }

    protected IWorkbenchPart createErrorPart(IWorkbenchPart oldPart) {
        class ErrorEditorPart extends EditorPart {
            private Text text;

            public void doSave(IProgressMonitor monitor) {
            }

            public void doSaveAs() {
            }

            public void init(IEditorSite site, IEditorInput input) {
                setSite(site);
                setInput(input);
            }

            public boolean isDirty() {
                return false;
            }

            public boolean isSaveAsAllowed() {
                return false;
            }

            public void createPartControl(Composite parent) {
                text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
                text.setForeground(JFaceColors.getErrorText(text.getDisplay()));
                text.setBackground(text.getDisplay().getSystemColor(
                        SWT.COLOR_WIDGET_BACKGROUND));
                text.setText(WorkbenchMessages
                        .getString("EditorPane.errorMessage")); //$NON-NLS-1$
            }

            public void setFocus() {
                if (text != null)
                    text.setFocus();
            }

            protected void setPartName(String title) {
                super.setPartName(title);
            }

            protected void setTitleToolTip(String text) {
                super.setTitleToolTip(text);
            }
        }
        IEditorPart oldEditorPart = (IEditorPart) oldPart;
        EditorSite oldEditorSite = (EditorSite) oldEditorPart.getEditorSite();
        ErrorEditorPart newPart = new ErrorEditorPart();
        newPart.setPartName(oldPart.getTitle());
        newPart.setTitleToolTip(oldPart.getTitleToolTip());
        oldEditorSite.setPart(newPart);
        newPart.init(oldEditorSite, oldEditorPart.getEditorInput());
        return newPart;
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
     * @see PartPane::doHide
     */
    public void doHide() {
        getPage().closeEditor(getEditorReference(), true);
    }

    /**
     * Answer the editor part child.
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
     */
    public EditorStack getWorkbook() {
        return workbook;
    }

    /**
     * Notify the workbook page that the part pane has
     * been activated by the user.
     */
    protected void requestActivation() {
        // By clearing the active workbook if its not the one
        // associated with the editor, we reduce draw flicker
        if (!workbook.isActiveWorkbook())
            workbook.getEditorArea().setActiveWorkbook(null, false);

        super.requestActivation();
    }

    /**
     * Set the editor workbook container
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
        if (inFocus)
            this.workbook.becomeActiveWorkbook(true);
        else
            this.workbook
                    .setActive(this.workbook.isActiveWorkbook() ? StackPresentation.AS_ACTIVE_NOFOCUS
                            : StackPresentation.AS_INACTIVE);
    }

    /**
     * Add the pin menu item on the editor system menu
     */
    protected void addPinEditorItem(Menu parent) {
        boolean reuseEditor = WorkbenchPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
        if (!reuseEditor) {
            return;
        }

        IWorkbenchPart part = getPartReference().getPart(false);
        if (part == null) {
            return;
        }

        final MenuItem item = new MenuItem(parent, SWT.CHECK);
        item.setText(WorkbenchMessages.getString("EditorPane.pinEditor")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                IWorkbenchPart part = getPartReference().getPart(true);
                if (part == null) {
                    // this should never happen
                    item.setSelection(false);
                    item.setEnabled(false);
                } else {
                    ((EditorSite) part.getSite()).setReuseEditor(!item
                            .getSelection());
                }
            }
        });
        item.setEnabled(true);
        item.setSelection(!((EditorSite) part.getSite()).getReuseEditor());
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartPane#getName()
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

}
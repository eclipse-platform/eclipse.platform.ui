/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *      font should be activated and used by other components.
 *******************************************************************************/

package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.misc.CheckboxTreeAndListGroup;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * A standard file selection dialog which solicits a list of files from the user.
 * The <code>getResult</code> method returns the selected files.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 *	FileSelectionDialog dialog = 
 *		new FileSelectionDialog(getShell(), rootElement, msg);
 *	dialog.setInitialSelections(selectedResources);
 *	dialog.open();
 *	return dialog.getResult();
 * </pre>
 * </p>
 * @deprecated Use org.eclipse.swt.widgets.FileDialog,
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileSelectionDialog extends SelectionDialog {
    // the root file representative to populate the viewer with
    private FileSystemElement root;

    // the visual selection widget group
    CheckboxTreeAndListGroup selectionGroup;

    // expand all items in the tree view on dialog open
    private boolean expandAllOnOpen = false;

    // sizing constants
    private static final int SIZING_SELECTION_WIDGET_WIDTH = 500;

    private static final int SIZING_SELECTION_WIDGET_HEIGHT = 250;

    /**
     * Creates a file selection dialog rooted at the given file system element.
     *
     * @param parentShell the parent shell
     * @param fileSystemElement the root element to populate this dialog with
     * @param message the message to be displayed at the top of this dialog, or
     *    <code>null</code> to display a default message
     */
    public FileSelectionDialog(Shell parentShell,
            FileSystemElement fileSystemElement, String message) {
        super(parentShell);
        setTitle(IDEWorkbenchMessages.FileSelectionDialog_title);
        root = fileSystemElement;
        if (message != null) {
			setMessage(message);
		} else {
			setMessage(IDEWorkbenchMessages.FileSelectionDialog_message);
		}
    }

    /**
     * Add the selection and deselection buttons to the dialog.
     * @param composite org.eclipse.swt.widgets.Composite
     */
    private void addSelectionButtons(Composite composite) {

        Composite buttonComposite = new Composite(composite, SWT.RIGHT);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        buttonComposite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        composite.setData(data);

        Button selectButton = new Button(buttonComposite, SWT.PUSH);
        selectButton.setText(SELECT_ALL_TITLE);
        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectionGroup.setAllSelections(true);
            }
        };
        selectButton.addSelectionListener(listener);

        Button deselectButton = new Button(buttonComposite, SWT.PUSH);
        deselectButton.setText(DESELECT_ALL_TITLE);
        listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectionGroup.setAllSelections(false);

            }
        };
        deselectButton.addSelectionListener(listener);

    }

    /**
     * Visually checks the previously-specified elements in the container (left)
     * portion of this dialog's file selection viewer.
     */
    private void checkInitialSelections() {
        Iterator itemsToCheck = getInitialElementSelections().iterator();

        while (itemsToCheck.hasNext()) {
            FileSystemElement currentElement = (FileSystemElement) itemsToCheck
                    .next();

            if (currentElement.isDirectory()) {
				selectionGroup.initialCheckTreeItem(currentElement);
			} else {
				selectionGroup.initialCheckListItem(currentElement);
			}
        }
    }

    /* (non-Javadoc)
     * Method declared in Window.
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.FILE_SELECTION_DIALOG);
    }

    public void create() {
        super.create();
        initializeDialog();
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        // page group
        Composite composite = (Composite) super.createDialogArea(parent);

        createMessageArea(composite);

        // Create a fake parent of the root to be the dialog input element.
        // Use an empty label so that display of the element's full name
        // doesn't include a confusing label
        FileSystemElement input = new FileSystemElement("", null, true);//$NON-NLS-1$
        input.addChild(root);
        root.setParent(input);

        selectionGroup = new CheckboxTreeAndListGroup(composite, input,
                getFolderProvider(), new WorkbenchLabelProvider(),
                getFileProvider(), new WorkbenchLabelProvider(), SWT.NONE,
                SIZING_SELECTION_WIDGET_WIDTH, // since this page has no other significantly-sized
                SIZING_SELECTION_WIDGET_HEIGHT); // widgets we need to hardcode the combined widget's
        // size, otherwise it will open too small

        ICheckStateListener listener = new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                getOkButton().setEnabled(
                        selectionGroup.getCheckedElementCount() > 0);
            }
        };

        WorkbenchViewerComparator comparator = new WorkbenchViewerComparator();
        selectionGroup.setTreeComparator(comparator);
        selectionGroup.setListComparator(comparator);
        selectionGroup.addCheckStateListener(listener);

        addSelectionButtons(composite);

        return composite;
    }

    /**
     * Returns whether the tree view of the file system element
     * will be fully expanded when the dialog is opened.
     *
     * @return true to expand all on dialog open, false otherwise.
     */
    public boolean getExpandAllOnOpen() {
        return expandAllOnOpen;
    }

    /**
     * Returns a content provider for <code>FileSystemElement</code>s that returns 
     * only files as children.
     */
    private ITreeContentProvider getFileProvider() {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
                if (o instanceof FileSystemElement) {
                    return ((FileSystemElement) o).getFiles().getChildren(o);
                }
                return new Object[0];
            }
        };
    }

    /**
     * Returns a content provider for <code>FileSystemElement</code>s that returns 
     * only folders as children.
     */
    private ITreeContentProvider getFolderProvider() {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
                if (o instanceof FileSystemElement) {
                    return ((FileSystemElement) o).getFolders().getChildren(o);
                }
                return new Object[0];
            }
        };
    }

    /**
     * Initializes this dialog's controls.
     */
    private void initializeDialog() {
        // initialize page	
        if (getInitialElementSelections().isEmpty()) {
			getOkButton().setEnabled(false);
		} else {
			checkInitialSelections();
		}
        selectionGroup.aboutToOpen();
        if (expandAllOnOpen) {
			selectionGroup.expandAll();
		}
    }

    /**
     * The <code>FileSelectionDialog</code> implementation of this
     * <code>Dialog</code> method builds a list of the selected files for later 
     * retrieval by the client and closes this dialog.
     */
    protected void okPressed() {
        Iterator resultEnum = selectionGroup.getAllCheckedListItems();
        ArrayList list = new ArrayList();
        while (resultEnum.hasNext()) {
			list.add(resultEnum.next());
		}
        setResult(list);
        super.okPressed();
    }

    /**
     * Set whether the tree view of the file system element
     * will be fully expanded when the dialog is opened.
     *
     * @param expandAll true to expand all on dialog open, false otherwise.
     */
    public void setExpandAllOnOpen(boolean expandAll) {
        expandAllOnOpen = expandAll;
    }
}

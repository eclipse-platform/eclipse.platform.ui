/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be activated and used by other components.
 *******************************************************************************/

package org.eclipse.ui.dialogs;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.SimpleListContentProvider;

/**
 * Dialog to allow the user to select from a list of marker
 * resolutions.
 * <p>
 * This dialog may be instantiated, it is not intented to 
 * be subclassed.
 * </p>
 * 
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MarkerResolutionSelectionDialog extends SelectionDialog {
    /**
     * List width in characters.
     */
    private final static int LIST_WIDTH = 60;

    /**
     * List height in characters.
     */
    private final static int LIST_HEIGHT = 10;

    /**
     * The marker resolutions.
     */
    private IMarkerResolution[] resolutions;

    /**
     * List to display the resolutions.
     */
    private ListViewer listViewer;

    /**
     * Creates an instance of this dialog to display
     * the given resolutions.
     * <p>
     * There must be at least one resolution.
     * </p>
     * 
     * @param shell the parent shell
     * @param markerResolutions the resolutions to display
     */
    public MarkerResolutionSelectionDialog(Shell shell,
            IMarkerResolution[] markerResolutions) {
        super(shell);
        if (markerResolutions == null || markerResolutions.length == 0) {
            throw new IllegalArgumentException();
        }
        resolutions = markerResolutions;
        setTitle(IDEWorkbenchMessages.MarkerResolutionSelectionDialog_title);
        setMessage(IDEWorkbenchMessages.MarkerResolutionSelectionDialog_messageLabel);
        setInitialSelections(new Object[] { markerResolutions[0] });
    }

    /* (non-Javadoc)
     * Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
                IIDEHelpContextIds.MARKER_RESOLUTION_SELECTION_DIALOG);
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        // Create label
        createMessageArea(composite);
        // Create list viewer	
        listViewer = new ListViewer(composite, SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = convertHeightInCharsToPixels(LIST_HEIGHT);
        data.widthHint = convertWidthInCharsToPixels(LIST_WIDTH);
        listViewer.getList().setLayoutData(data);
        listViewer.getList().setFont(parent.getFont());
        // Set the label provider		
        listViewer.setLabelProvider(new LabelProvider() {
            public String getText(Object element) {
                // Return the resolution's label.
                return element == null ? "" : ((IMarkerResolution) element).getLabel(); //$NON-NLS-1$
            }
        });

        // Set the content provider
        SimpleListContentProvider cp = new SimpleListContentProvider();
        cp.setElements(resolutions);
        listViewer.setContentProvider(cp);
        listViewer.setInput(new Object()); // it is ignored but must be non-null

        // Set the initial selection
        listViewer.setSelection(new StructuredSelection(
                getInitialElementSelections()), true);

        // Add a selection change listener
        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                // Update OK button enablement
                getOkButton().setEnabled(!event.getSelection().isEmpty());
            }
        });

        // Add double-click listener
        listViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        return composite;
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected void okPressed() {
        IStructuredSelection selection = (IStructuredSelection) listViewer
                .getSelection();
        setResult(selection.toList());
        super.okPressed();
    }
}

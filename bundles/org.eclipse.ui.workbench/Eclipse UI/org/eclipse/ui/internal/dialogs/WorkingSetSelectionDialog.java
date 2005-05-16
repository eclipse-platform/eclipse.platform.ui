/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 * 		Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font
 *   	should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * A working set selection dialog displays a list of working
 * sets available in the workbench.
 * 
 * @see IWorkingSetSelectionDialog
 * @since 2.0
 */
public class WorkingSetSelectionDialog extends SelectionDialog implements
        IWorkingSetSelectionDialog {
    private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

    private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;

    private static class WorkingSetLabelProvider extends LabelProvider {
        private Map icons;

        /**
         * Create a new instance of the receiver.
         */
        public WorkingSetLabelProvider() {
            icons = new Hashtable();
        }

        public void dispose() {
            Iterator iterator = icons.values().iterator();

            while (iterator.hasNext()) {
                Image icon = (Image) iterator.next();
                icon.dispose();
            }
            super.dispose();
        }

        public Image getImage(Object object) {
            Assert.isTrue(object instanceof IWorkingSet);
            IWorkingSet workingSet = (IWorkingSet) object;
            ImageDescriptor imageDescriptor = workingSet.getImage();

            if (imageDescriptor == null)
                return null;

            Image icon = (Image) icons.get(imageDescriptor);
            if (icon == null) {
                icon = imageDescriptor.createImage();
                icons.put(imageDescriptor, icon);
            }
            return icon;
        }

        public String getText(Object object) {
            Assert.isTrue(object instanceof IWorkingSet);
            IWorkingSet workingSet = (IWorkingSet) object;
            return workingSet.getName();
        }
    }
    
    class WorkingSetFilter extends ViewerFilter {
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof IWorkingSet) {
                String id = ((IWorkingSet) element).getId();
                if (id != null) {
                    return workingSetIds.contains(id);
                }
            }
            return true;
        }
    }

    private ILabelProvider labelProvider;

    private IStructuredContentProvider contentProvider;

    private TableViewer listViewer;

    private Button newButton;

    private Button detailsButton;

    private Button removeButton;

    private IWorkingSet[] result;

    private boolean multiSelect;

    private List addedWorkingSets;

    private List removedWorkingSets;

    private Map editedWorkingSets;

    private List removedMRUWorkingSets;
    
    private Set workingSetIds;

    /**
     * Creates a working set selection dialog.
     *
     * @param parentShell the parent shell
     * @param multi true=more than one working set can be chosen 
     * 	in the dialog. false=only one working set can be chosen. Multiple
     * 	working sets can still be selected and removed from the list but
     * 	the dialog can only be closed when a single working set is selected.
     * @param workingSetIds a list of working set ids which are valid workings sets
     *  to be selected, created, removed or edited, or <code>null</code> if all currently
     *  available working set types are valid 
     */
    public WorkingSetSelectionDialog(Shell parentShell, boolean multi, String[] workingSetIds) {
        super(parentShell);
        contentProvider = new ArrayContentProvider();
        labelProvider = new WorkingSetLabelProvider();
        multiSelect = multi;
        if (multiSelect) {
            setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title_multiSelect); 
            setMessage(WorkbenchMessages.WorkingSetSelectionDialog_message_multiSelect);
        } else {
            setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title); 
            setMessage(WorkbenchMessages.WorkingSetSelectionDialog_message);
        }
        if (workingSetIds != null) {
            this.workingSetIds = new HashSet();
            for (int i = 0; i < workingSetIds.length; i++) {
                this.workingSetIds.add(workingSetIds[i]);
            }
        }
    }

    /**
     * Adds the modify buttons to the dialog.
     * 
     * @param composite Composite to add the buttons to
     */
    private void addModifyButtons(Composite composite) {
        Composite buttonComposite = new Composite(composite, SWT.RIGHT);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        buttonComposite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
                | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        composite.setData(data);

        int id = IDialogConstants.CLIENT_ID + 1;
        newButton = createButton(buttonComposite, id++, WorkbenchMessages.WorkingSetSelectionDialog_newButton_label, false); 
        newButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                createWorkingSet();
            }
        });

        detailsButton = createButton(
                buttonComposite,
                id++,
                WorkbenchMessages.WorkingSetSelectionDialog_detailsButton_label, false);
        detailsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editSelectedWorkingSet();
            }
        });

        removeButton = createButton(
                buttonComposite,
                id++,
                WorkbenchMessages.WorkingSetSelectionDialog_removeButton_label, false);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                removeSelectedWorkingSets();
            }
        });
    }

    /**
     * Overrides method from Dialog.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    protected void cancelPressed() {
        restoreAddedWorkingSets();
        restoreChangedWorkingSets();
        restoreRemovedWorkingSets();
        super.cancelPressed();
    }

    /** 
     * Overrides method from Window.
     * 
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.WORKING_SET_SELECTION_DIALOG);
    }

    /**
     * Overrides method from Dialog.
     * Create the dialog widgets.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        createMessageArea(composite);
        listViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
        listViewer.getTable().setLayoutData(data);
        listViewer.getTable().setFont(parent.getFont());

        listViewer.setLabelProvider(labelProvider);
        listViewer.setContentProvider(contentProvider);
        listViewer.setSorter(new WorkbenchViewerSorter());
        if (workingSetIds != null) {
            listViewer.addFilter(new WorkingSetFilter());
        }
        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handleSelectionChanged();
            }
        });
        listViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        addModifyButtons(composite);
        listViewer.setInput(Arrays.asList(WorkbenchPlugin.getDefault()
                .getWorkingSetManager().getWorkingSets()));

        return composite;
    }

    /**
     * Overrides method from Dialog.
     * Sets the initial selection, if any.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        List selections = getInitialElementSelections();
        if (!selections.isEmpty()) {
            listViewer.setSelection(new StructuredSelection(selections), true);
        }
        updateButtonAvailability();
        //don't allow ok dismissal until a change has been made. 
        //Fixes bug 22735.
        getOkButton().setEnabled(false);
        return control;
    }

    /**
	 * Opens a working set wizard for creating a new working set.
	 */
	void createWorkingSet() {
	    IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
        String ids[] = null;
        if (workingSetIds != null) {
            ids = (String[]) workingSetIds.toArray(new String[workingSetIds.size()]);
        }
	    IWorkingSetNewWizard wizard = manager.createWorkingSetNewWizard(ids);
	    // the wizard can never be null since we have at least a resource working set
	    // creation page
	    WizardDialog dialog = new WizardDialog(getShell(), wizard);
	
	    dialog.create();
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.WORKING_SET_NEW_WIZARD);
	    if (dialog.open() == Window.OK) {
	        IWorkingSet workingSet = wizard.getSelection();
	
	        listViewer.add(workingSet);
	        listViewer.setSelection(new StructuredSelection(workingSet), true);
	        manager.addWorkingSet(workingSet);
	        addedWorkingSets.add(workingSet);
	    }
	}

    /**
     * Opens a working set wizard for editing the currently selected 
     * working set.
     * 
     * @see org.eclipse.ui.dialogs.IWorkingSetPage
     */
    void editSelectedWorkingSet() {
        IWorkingSetManager manager = WorkbenchPlugin.getDefault()
                .getWorkingSetManager();
        IWorkingSet editWorkingSet = (IWorkingSet) getSelectedWorkingSets()
                .get(0);
        IWorkingSetEditWizard wizard = manager
                .createWorkingSetEditWizard(editWorkingSet);
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        IWorkingSet originalWorkingSet = (IWorkingSet) editedWorkingSets
                .get(editWorkingSet);
        boolean firstEdit = originalWorkingSet == null;

        // save the original working set values for restoration when selection 
        // dialog is cancelled.
        if (firstEdit) {
            originalWorkingSet = new WorkingSet(editWorkingSet.getName(),
                    editWorkingSet.getElements());
        } else {
            editedWorkingSets.remove(editWorkingSet);
        }
        dialog.create();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.WORKING_SET_EDIT_WIZARD);
        if (dialog.open() == Window.OK) {
            editWorkingSet = wizard.getSelection();
            listViewer.update(editWorkingSet, null);
            // make sure ok button is enabled when the selected working set 
            // is edited. Fixes bug 33386.
            updateButtonAvailability();
        }
        editedWorkingSets.put(editWorkingSet, originalWorkingSet);
    }

    /**
     * Implements IWorkingSetSelectionDialog.
     *
     * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#getSelection()
     */
    public IWorkingSet[] getSelection() {
        return result;
    }

    /**
     * Returns the selected working sets.
     * 
     * @return the selected working sets
     */
    private List getSelectedWorkingSets() {
        ISelection selection = listViewer.getSelection();
        if (selection instanceof IStructuredSelection)
            return ((IStructuredSelection) selection).toList();
        return null;
    }

    /**
     * Called when the selection has changed.
     */
    void handleSelectionChanged() {
        updateButtonAvailability();
    }

    /**
     * Sets the selected working sets as the dialog result.
     * Overrides method from Dialog
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        List newResult = getSelectedWorkingSets();

        result = (IWorkingSet[]) newResult.toArray(new IWorkingSet[newResult
                .size()]);
        setResult(newResult);
        super.okPressed();
    }

    /**
     * Overrides method in Dialog
     * 
     * @see org.eclipse.jface.dialogs.Dialog#open()
     */
    public int open() {
        addedWorkingSets = new ArrayList();
        removedWorkingSets = new ArrayList();
        editedWorkingSets = new HashMap();
        removedMRUWorkingSets = new ArrayList();
        return super.open();
    }

    /**
     * Removes the selected working sets from the workbench.
     */
    void removeSelectedWorkingSets() {
        ISelection selection = listViewer.getSelection();

        if (selection instanceof IStructuredSelection) {
            IWorkingSetManager manager = WorkbenchPlugin.getDefault()
                    .getWorkingSetManager();
            Iterator iter = ((IStructuredSelection) selection).iterator();
            while (iter.hasNext()) {
                IWorkingSet workingSet = (IWorkingSet) iter.next();
                if (addedWorkingSets.contains(workingSet)) {
                    addedWorkingSets.remove(workingSet);
                } else {
                    IWorkingSet[] recentWorkingSets = manager
                            .getRecentWorkingSets();
                    for (int i = 0; i < recentWorkingSets.length; i++) {
                        if (workingSet.equals(recentWorkingSets[i])) {
                            removedMRUWorkingSets.add(workingSet);
                            break;
                        }
                    }
                    removedWorkingSets.add(workingSet);
                }
                manager.removeWorkingSet(workingSet);
            }
            listViewer.remove(((IStructuredSelection) selection).toArray());
        }
    }

    /**
     * Removes newly created working sets from the working set manager.
     */
    private void restoreAddedWorkingSets() {
        IWorkingSetManager manager = WorkbenchPlugin.getDefault()
                .getWorkingSetManager();
        Iterator iterator = addedWorkingSets.iterator();

        while (iterator.hasNext()) {
            manager.removeWorkingSet(((IWorkingSet) iterator.next()));
        }
    }

    /**
     * Rolls back changes to working sets.
     */
    private void restoreChangedWorkingSets() {
        Iterator iterator = editedWorkingSets.keySet().iterator();

        while (iterator.hasNext()) {
            IWorkingSet editedWorkingSet = (IWorkingSet) iterator.next();
            IWorkingSet originalWorkingSet = (IWorkingSet) editedWorkingSets
                    .get(editedWorkingSet);

            if (editedWorkingSet.getName().equals(originalWorkingSet.getName()) == false) {
                editedWorkingSet.setName(originalWorkingSet.getName());
            }
            if (editedWorkingSet.getElements().equals(
                    originalWorkingSet.getElements()) == false) {
                editedWorkingSet.setElements(originalWorkingSet.getElements());
            }
        }
    }

    /**
     * Adds back removed working sets to the working set manager.
     */
    private void restoreRemovedWorkingSets() {
        IWorkingSetManager manager = WorkbenchPlugin.getDefault()
                .getWorkingSetManager();
        Iterator iterator = removedWorkingSets.iterator();

        while (iterator.hasNext()) {
            manager.addWorkingSet(((IWorkingSet) iterator.next()));
        }
        iterator = removedMRUWorkingSets.iterator();
        while (iterator.hasNext()) {
            manager.addRecentWorkingSet(((IWorkingSet) iterator.next()));
        }
    }

    /**
     * Implements IWorkingSetSelectionDialog.
     *
     * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#setSelection(IWorkingSet[])
     */
    public void setSelection(IWorkingSet[] workingSets) {
        result = workingSets;
        setInitialSelections(workingSets);
    }

    /**
     * Updates the modify buttons' enabled state based on the 
     * current seleciton.
     */
    private void updateButtonAvailability() {
        ISelection selection = listViewer.getSelection();
        boolean hasSelection = selection != null && !selection.isEmpty();
        boolean hasSingleSelection = hasSelection;
        WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();

        newButton.setEnabled(registry.hasNewPageWorkingSetDescriptor());
        
        removeButton.setEnabled(hasSelection);
        
        IWorkingSet selectedWorkingSet= null;
        if (hasSelection && selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection= (IStructuredSelection) selection;
			hasSingleSelection = structuredSelection.size() == 1;
			if (hasSingleSelection)
				selectedWorkingSet= (IWorkingSet)structuredSelection.getFirstElement();
        }
        detailsButton.setEnabled(hasSingleSelection && hasEditPage(selectedWorkingSet));
        
        if (multiSelect == false) {
            getOkButton().setEnabled(
                    hasSelection == false || hasSingleSelection);
        } else {
            getOkButton().setEnabled(true);
        }
    }
    
    private boolean hasEditPage(IWorkingSet workingSet) {
        WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();

        WorkingSetDescriptor descriptor= registry.getWorkingSetDescriptor(workingSet.getId());
        return descriptor.getPageClassName() != null;
    }
}

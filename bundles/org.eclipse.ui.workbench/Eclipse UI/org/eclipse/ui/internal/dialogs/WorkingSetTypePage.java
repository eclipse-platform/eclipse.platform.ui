/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *        IBM Corporation - initial API and implementation 
 * 		  Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *        activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;

/**
 * The working set type page is used in the new working set 
 * wizard to select from a list of plugin defined working set 
 * types.
 * 
 * @since 2.0
 */
public class WorkingSetTypePage extends WizardPage {
    private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;

    private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

    private TableViewer typesListViewer;

    private Map icons;

	private WorkingSetDescriptor[] descriptors;

    /**
     * Creates a new instance of the receiver
     */
    public WorkingSetTypePage() {
        this(WorkbenchPlugin.getDefault().getWorkingSetRegistry().getNewPageWorkingSetDescriptors());
    }

    /**
	 * @param descriptors a set of working set descriptors which can be selected on the page
	 */
	public WorkingSetTypePage(WorkingSetDescriptor[] descriptors) {
		super(
                "workingSetTypeSelectionPage", WorkbenchMessages.Select, WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_WORKINGSET_WIZ)); //$NON-NLS-1$
        setDescription(WorkbenchMessages.WorkingSetTypePage_description); 			
        icons = new Hashtable();
        this.descriptors= descriptors;
	}

    /** 
     * Overrides method in WizardPage
     * 
     * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
     */
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    /**
     * Populates the working set types list.
     */
    private void createContent() {
		Table table = (Table) typesListViewer.getControl();

        for (int i = 0; i < descriptors.length; i++) {
            TableItem tableItem = new TableItem(table, SWT.NULL);
            ImageDescriptor imageDescriptor = descriptors[i].getIcon();

            if (imageDescriptor != null) {
                Image icon = (Image) icons.get(imageDescriptor);
                if (icon == null) {
                    icon = imageDescriptor.createImage();
                    icons.put(imageDescriptor, icon);
                }
                tableItem.setImage(icon);
            }
            tableItem.setText(descriptors[i].getName());
            tableItem.setData(descriptors[i]);
        }
    }

    /** 
     * Implements IDialogPage
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        setControl(composite);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
				IWorkbenchHelpContextIds.WORKING_SET_TYPE_PAGE);
        Label typesLabel = new Label(composite, SWT.NONE);
        typesLabel.setText(WorkbenchMessages.WorkingSetTypePage_typesLabel);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        typesLabel.setLayoutData(data);
        typesLabel.setFont(font);

        typesListViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
        typesListViewer.getTable().setLayoutData(data);
        typesListViewer.getTable().setFont(font);
        typesListViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        handleSelectionChanged();
                    }
                });
        typesListViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                handleDoubleClick();
            }
        });
        createContent();
        setPageComplete(false);
    }

    /** 
     * Overrides method in DialogPage
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
     */
    public void dispose() {
        Iterator iterator = icons.values().iterator();

        while (iterator.hasNext()) {
            Image icon = (Image) iterator.next();
            icon.dispose();
        }
        super.dispose();
    }

    /**
     * Returns the page id of the selected working set type.
     * 
     * @return the page id of the selected working set type.
     */
    public String getSelection() {
        ISelection selection = typesListViewer.getSelection();
        boolean hasSelection = selection != null
                && selection.isEmpty() == false;

        if (hasSelection && selection instanceof IStructuredSelection) {
            WorkingSetDescriptor workingSetDescriptor = (WorkingSetDescriptor) ((IStructuredSelection) selection)
                    .getFirstElement();
            return workingSetDescriptor.getId();
        }
        return null;
    }

    /**
     * Called when a working set type is double clicked.
     */
    private void handleDoubleClick() {
        handleSelectionChanged();
        getContainer().showPage(getNextPage());
    }

    /**
     * Called when the selection has changed.
     */
    private void handleSelectionChanged() {
        ISelection selection = typesListViewer.getSelection();
        boolean hasSelection = selection != null
                && selection.isEmpty() == false;

        setPageComplete(hasSelection);
    }
}

/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *        IBM Corporation - initial API and implementation
 * 		  Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *        activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;

/**
 * The working set type page is used in the new working set wizard to select
 * from a list of plugin defined working set types.
 *
 * @since 2.0
 */
public class WorkingSetTypePage extends WizardPage {
	private static final int SIZING_SELECTION_WIDGET_WIDTH = 50;

	private static final int SIZING_SELECTION_WIDGET_HEIGHT = 200;

	private TableViewer typesListViewer;

	private WorkingSetDescriptor[] descriptors;

	/**
	 * Creates a new instance of the receiver
	 */
	public WorkingSetTypePage() {
		this(WorkbenchPlugin.getDefault().getWorkingSetRegistry().getNewPageWorkingSetDescriptors());
	}

	/**
	 * @param descriptors a set of working set descriptors which can be selected on
	 *                    the page
	 */
	public WorkingSetTypePage(WorkingSetDescriptor[] descriptors) {
		super("workingSetTypeSelectionPage", WorkbenchMessages.WorkingSetTypePage_description, //$NON-NLS-1$
				WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_WORKINGSET_WIZ));
		this.descriptors = descriptors;
	}

	/**
	 * Overrides method in WizardPage
	 *
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	/**
	 * Implements IDialogPage
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IWorkbenchHelpContextIds.WORKING_SET_TYPE_PAGE);
		Label typesLabel = new Label(composite, SWT.NONE);
		typesLabel.setText(WorkbenchMessages.WorkingSetTypePage_typesLabel);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		typesLabel.setLayoutData(data);
		typesLabel.setFont(font);

		typesListViewer = new TableViewer(composite, SWT.BORDER | SWT.SINGLE);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		typesListViewer.getTable().setLayoutData(data);
		typesListViewer.getTable().setFont(font);
		typesListViewer.addSelectionChangedListener(event -> handleSelectionChanged());
		typesListViewer.addDoubleClickListener(event -> handleDoubleClick());
		typesListViewer.setContentProvider(ArrayContentProvider.getInstance());
		typesListViewer.setLabelProvider(new LabelProvider() {
			private ResourceManager images = new LocalResourceManager(JFaceResources.getResources());

			@Override
			public String getText(Object element) {
				return ((WorkingSetDescriptor) element).getName();
			}

			@Override
			public void dispose() {
				images.dispose();
				super.dispose();
			}

			@Override
			public Image getImage(Object element) {
				ImageDescriptor imageDescriptor = ((WorkingSetDescriptor) element).getIcon();
				return imageDescriptor == null ? null : images.get(imageDescriptor);
			}
		});
		typesListViewer.setInput(descriptors);
		setPageComplete(false);
	}

	/**
	 * Overrides method in DialogPage
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * Returns the page id of the selected working set type.
	 *
	 * @return the page id of the selected working set type.
	 */
	public String getSelection() {
		WorkingSetDescriptor descriptor = getSelectedWorkingSet();
		if (descriptor != null)
			return descriptor.getId();

		return null;
	}

	/**
	 * Return the selected working set.
	 *
	 * @return the selected working set or <code>null</code>
	 * @since 3.4
	 */
	private WorkingSetDescriptor getSelectedWorkingSet() {
		return (WorkingSetDescriptor) typesListViewer.getStructuredSelection().getFirstElement();
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
		IStructuredSelection selection = typesListViewer.getStructuredSelection();
		boolean hasSelection = selection != null && selection.isEmpty() == false;

		WorkingSetDescriptor descriptor = getSelectedWorkingSet();
		setDescription(descriptor == null ? "" : descriptor.getDescription()); //$NON-NLS-1$

		setPageComplete(hasSelection);
	}
}

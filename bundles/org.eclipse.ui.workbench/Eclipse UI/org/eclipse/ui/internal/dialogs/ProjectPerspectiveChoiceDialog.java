package org.eclipse.ui.internal.dialogs;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation 
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *     activated and used by other components.
*********************************************************************/
import java.text.Collator;
import java.util.*;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

public class ProjectPerspectiveChoiceDialog extends Dialog {
	private static final int MIN_DIALOG_WIDTH = 200;
	private static final int MIN_DIALOG_HEIGHT = 250;
	
	private IWorkbenchWindow window;
	private ArrayList persps;
	private IPerspectiveDescriptor chosenPersp;
	private boolean sameWindow = true;
	
	private TableViewer list;
	
	private Comparator comparator = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object ob1, Object ob2) {
			IPerspectiveDescriptor d1 = (IPerspectiveDescriptor) ob1;
			IPerspectiveDescriptor d2 = (IPerspectiveDescriptor) ob2;
			return collator.compare(d1.getLabel(), d2.getLabel());
		}
	};
	
	/**
	 * Create a ProjectPerspectiveChoiceDialog
	 * @param window the workbench window launching this dialog
	 * @param perspIds the list of ids the user can choose from
	 */
	public ProjectPerspectiveChoiceDialog(IWorkbenchWindow window, String[] perspIds) {
		super(window.getShell());
		this.window = window;

		IWorkbenchPage page = window.getActivePage();
		if (page != null)
			chosenPersp = page.getPerspective();
		
		IPerspectiveRegistry reg = window.getWorkbench().getPerspectiveRegistry();
		persps = new ArrayList(perspIds.length);
		for (int i = 0; i < perspIds.length; i++) {
			IPerspectiveDescriptor desc;
			desc = reg.findPerspectiveWithId(perspIds[i]);
			if (desc != null && desc != chosenPersp)
				persps.add(desc);
		}
		Collections.sort(persps, comparator);
		
		if (chosenPersp != null)
			persps.add(0,chosenPersp);
	}
	
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(WorkbenchMessages.getString("ProjectPerspectiveChoiceDialog.title")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(shell, IHelpContextIds.SHOW_PROJECT_PERSPECTIVE_DIALOG);
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
	
		composite.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = MIN_DIALOG_WIDTH;
		data.heightHint = MIN_DIALOG_HEIGHT;
		composite.setLayoutData(data);
	
		createPerspectiveGroup(composite);
		createOptionGroup(composite);
	
		if (chosenPersp != null)
			list.setSelection(new StructuredSelection(chosenPersp));
		
		return composite;
	}
	
	/**
	 * Creates the perspective choice controls.
	 *
	 * @param parent the parent composite
	 */
	private void createPerspectiveGroup(Composite parent) {
		Font font = parent.getFont();
		// Label for choosing perspective
		Label label = new Label(parent, SWT.NONE);
		label.setFont(font);
		label.setText(WorkbenchMessages.getString("ProjectPerspectiveChoiceDialog.choosePerspective")); //$NON-NLS-1$
		
		// Add perspective list.
		list = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		list.setLabelProvider(new PerspLabelProvider(false));
		list.setContentProvider(new PerspectiveContentProvider());
		list.setInput(persps);
		list.addSelectionChangedListener(new SelectionListener());
		list.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		list.getTable().setFont(font);
	}
	
	/**
	 * Creates the option controls.
	 *
	 * @param parent the parent composite
	 */
	private void createOptionGroup(Composite parent) {
		Font font = parent.getFont();
		// Create the option group
		Group optionGroup = new Group(parent, SWT.LEFT);
		GridLayout layout = new GridLayout();
		optionGroup.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		optionGroup.setLayoutData(data);
		optionGroup.setFont(font);
		optionGroup.setText(WorkbenchMessages.getString("ProjectPerspectiveChoiceDialog.options")); //$NON-NLS-1$

		// Same window option
		Button button = new Button(optionGroup, SWT.RADIO | SWT.LEFT);
		button.setText(WorkbenchMessages.getString("ProjectPerspectiveChoiceDialog.sameWindow")); //$NON-NLS-1$
		data = new GridData();
		button.setLayoutData(data);
		button.setFont(font);
		button.setSelection(sameWindow);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sameWindow = true;
			}
		});		
		
		// New window option
		button = new Button(optionGroup, SWT.RADIO | SWT.LEFT);
		button.setText(WorkbenchMessages.getString("ProjectPerspectiveChoiceDialog.newWindow")); //$NON-NLS-1$
		data = new GridData();
		button.setLayoutData(data);
		button.setFont(font);
		button.setSelection(!sameWindow);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sameWindow = false;
			}
		});		
	}
	
	/**
	 * Shows the choosen perspective in the same or
	 * new window depending on the option checked.
	 * Returns the workbench window the perspective
	 * was shown in.
	 */
	public IWorkbenchWindow showChosenPerspective() {
		if (chosenPersp == null)
			return window;
		
		final IWorkbenchWindow[] results = new IWorkbenchWindow[1];
		final WorkbenchException[] errors = new WorkbenchException[1];
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				if (sameWindow) {
					results[0] = window;
					IWorkbenchPage page = window.getActivePage();
					if (page != null)
						page.setPerspective(chosenPersp);
				} else {
					try {
						results[0] = window.getWorkbench().openWorkbenchWindow(
							chosenPersp.getId(),
							ResourcesPlugin.getWorkspace().getRoot());
					} catch (WorkbenchException e) {
						errors[0] = e;
					}
				}
			}
		});
		
		IWorkbenchWindow result = results[0];
		results[0] = null;
		
		if (errors[0] != null) {
			ErrorDialog.openError(
				window.getShell(),
				WorkbenchMessages.getString("ProjectPerspectiveChoiceDialog.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("ProjectPerspectiveChoiceDialog.errorMessage"), //$NON-NLS-1$
				errors[0].getStatus());
			errors[0] = null;
			return window;
		}
		
		return result;
	}
	
	class PerspectiveContentProvider implements IStructuredContentProvider {
		public PerspectiveContentProvider() {
			super();
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object element) {
			if (element instanceof ArrayList) {
				return ((ArrayList)element).toArray();
			}
			return null;
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public boolean isDeleted(Object element) {
			return false;
		}
	}
	
	class SelectionListener implements ISelectionChangedListener {
		public SelectionListener() {
			super();
		}
		
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			if (selection != null)
				chosenPersp = (IPerspectiveDescriptor)selection.getFirstElement();
		}
	}
}

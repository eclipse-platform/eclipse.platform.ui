package org.eclipse.ui.internal.dialogs;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *    IBM Corporation - initial API and implementation 
 *    Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *       activated and used by other components.
*********************************************************************/
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ICapabilityUninstallWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.Capability;
import org.eclipse.ui.internal.registry.CapabilityRegistry;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * A property page for IProject resources to edit a single
 * capability at a time.
 */
public class ProjectCapabilityEditingPropertyPage extends ProjectCapabilityPropertyPage {
	private static final int SIZING_WIZARD_WIDTH = 500;
	private static final int SIZING_WIZARD_HEIGHT = 500;

	private TableViewer table;
	private Button addButton;
	private Button removeButton;
	private ArrayList disabledCaps = new ArrayList();
	private Capability selectedCap;
	private CapabilityRegistry reg;

	/**
	 * Creates a new ProjectCapabilityEditingPropertyPage.
	 */
	public ProjectCapabilityEditingPropertyPage() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();
		WorkbenchHelp.setHelp(parent, IHelpContextIds.PROJECT_CAPABILITY_PROPERTY_PAGE);
		noDefaultAndApplyButton();
		reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		
		Composite topComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		topComposite.setLayout(layout);
		topComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		String instructions;
		if (reg.hasCapabilities())
			instructions = WorkbenchMessages.getString("ProjectCapabilityPropertyPage.chooseCapabilities"); //$NON-NLS-1$
		else
			instructions = WorkbenchMessages.getString("ProjectCapabilityPropertyPage.noCapabilities"); //$NON-NLS-1$
		Label label = new Label(topComposite, SWT.LEFT);
		label.setFont(font);
		label.setText(instructions);

		Capability[] caps = reg.getProjectDisabledCapabilities(getProject());
		disabledCaps.addAll(Arrays.asList(caps));

		Composite mainComposite = new Composite(topComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite capComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		capComposite.setLayout(layout);
		capComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		label = new Label(capComposite, SWT.LEFT);
		label.setFont(font);
		label.setText(WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.capabilities")); //$NON-NLS-1$
		
		table = new TableViewer(capComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		table.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		table.getTable().setFont(font);
		table.setLabelProvider(new CapabilityLabelProvider());
		table.setContentProvider(getContentProvider());
		table.setInput(getProject());
		
		Composite buttonComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		label = new Label(buttonComposite, SWT.LEFT);
		label.setFont(font);
		label.setText(""); //$NON-NLS-1$
		
		addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText(WorkbenchMessages.getString("ProjectCapabilityEditingPropertyPage.add")); //$NON-NLS-1$
		addButton.setEnabled(true);
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				addCapability();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		addButton.setLayoutData(data);
		addButton.setFont(font);
		
		removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setText(WorkbenchMessages.getString("ProjectCapabilityEditingPropertyPage.remove")); //$NON-NLS-1$
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				removeCapability(selectedCap);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		removeButton.setLayoutData(data);
		removeButton.setFont(font);

		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedCap = null;
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				if (sel != null)
					selectedCap = (Capability)sel.getFirstElement();
				removeButton.setEnabled(selectedCap != null);
			}
		});
		
		return topComposite;
	}
	
	/**
	 * Returns the content provider for the viewers
	 */
	private IContentProvider getContentProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IProject)
					return reg.getProjectCapabilities((IProject)parentElement);
				else
					return null;
			}
		};
	}
	
	/**
	 * Returns whether the category is considered disabled
	 */
	private boolean isDisabledCapability(Capability cap) {
		return disabledCaps.contains(cap);
	}
	
	private void addCapability() {
		ProjectCapabilitySimpleAddWizard wizard;
		wizard = new ProjectCapabilitySimpleAddWizard(PlatformUI.getWorkbench(), StructuredSelection.EMPTY, getProject());
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.UPDATE_CAPABILITY_WIZARD);
		dialog.open();
		
		table.refresh();
	}
	
	private void removeCapability(Capability cap) {
		ArrayList results = new ArrayList();
		results.addAll(Arrays.asList(reg.getProjectCapabilities(getProject())));
		results.remove(cap);
		Capability[] caps = new Capability[results.size()];
		results.toArray(caps);
		
		for (int i = 0; i < caps.length; i++) {
			List prereqs = Arrays.asList(reg.getPrerequisiteIds(caps[i]));
			if (prereqs.contains(cap.getId())) {
				MessageDialog.openWarning(
					getShell(),
					WorkbenchMessages.getString("ProjectCapabilityPropertyPage.errorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format("ProjectCapabilityPropertyPage.capabilityRequired", new Object[] {caps[i].getName()})); //$NON-NLS-1$
				return;
			}
		}
		
		IStatus status = reg.validateCapabilities(caps);
		if (!status.isOK()) {
			ErrorDialog.openError(
				getShell(),
				WorkbenchMessages.getString("ProjectCapabilityPropertyPage.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("ProjectCapabilityPropertyPage.invalidSelection"), //$NON-NLS-1$
				status);
			return;
		}
		
		String[] natureIds = new String[1];
		natureIds[0] = cap.getNatureId();
		
		ICapabilityUninstallWizard wizard = cap.getUninstallWizard();
		if (wizard == null)
			wizard = new RemoveCapabilityWizard();
		if (wizard != null) {
			wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY, getProject(), natureIds);
			wizard.addPages();
		}
		
		if (wizard.getStartingPage() == null) {
			wizard.setContainer(new StubContainer());
			wizard.performFinish();
			wizard.setContainer(null);
		} else {
			wizard = cap.getUninstallWizard();
			if (wizard == null)
				wizard = new RemoveCapabilityWizard();
			if (wizard != null)
				wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY, getProject(), natureIds);
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
			WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.UPDATE_CAPABILITY_WIZARD);
			dialog.open();
		}
		
		table.refresh();
	}
	
	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	public boolean performOk() {
		return true;
	}
	
		
	class CapabilityLabelProvider extends LabelProvider {
		private Map imageTable;
		
		public void dispose() {
			if (imageTable != null) {
				Iterator enum = imageTable.values().iterator();
				while (enum.hasNext())
					((Image) enum.next()).dispose();
				imageTable = null;
			}
		}

		public Image getImage(Object element) {
			ImageDescriptor descriptor = ((Capability) element).getIconDescriptor();
			if (descriptor == null)
				return null;
			
			//obtain the cached image corresponding to the descriptor
			if (imageTable == null) {
				 imageTable = new Hashtable(40);
			}
			Image image = (Image) imageTable.get(descriptor);
			if (image == null) {
				image = descriptor.createImage();
				imageTable.put(descriptor, image);
			}
			return image;
		}

		public String getText(Object element) {
			Capability cap = (Capability) element;
			String text = cap.getName();
			if (isDisabledCapability(cap))
				text = WorkbenchMessages.format("ProjectCapabilitySelectionGroup.disabledLabel", new Object[] {text}); //$NON-NLS-1$
			return text;
		}
	}
	
	class StubContainer implements IWizardContainer {
		public IWizardPage getCurrentPage() {
			return null;
		}
		public Shell getShell() {
			return ProjectCapabilityEditingPropertyPage.this.getShell();
		}
		public void showPage(IWizardPage page) {
		}
		public void updateButtons() {
		}
		public void updateMessage() {
		}
		public void updateTitleBar() {
		}
		public void updateWindowTitle() {
		}
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			new ProgressMonitorDialog(getShell()).run(fork, cancelable, runnable);
		}
	}
}

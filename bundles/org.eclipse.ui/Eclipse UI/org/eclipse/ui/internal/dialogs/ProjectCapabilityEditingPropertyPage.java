package org.eclipse.ui.internal.dialogs;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ICapabilityUninstallWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
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
	private ArrayList rootInput = new ArrayList(0);
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
		WorkbenchHelp.setHelp(parent, IHelpContextIds.PROJECT_CAPABILITY_PROPERTY_PAGE);
		noDefaultAndApplyButton();
		reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		
		Composite topComposite = new Composite(parent, SWT.NONE);
		topComposite.setLayout(new GridLayout());
		topComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		String instructions;
		if (reg.hasCapabilities())
			instructions = WorkbenchMessages.getString("ProjectCapabilityPropertyPage.chooseCapabilities"); //$NON-NLS-1$
		else
			instructions = WorkbenchMessages.getString("ProjectCapabilityPropertyPage.noCapabilities"); //$NON-NLS-1$
		Label label = new Label(topComposite, SWT.LEFT);
		label.setText(instructions);

		Capability[] caps = reg.getProjectCapabilities(getProject());
		rootInput.addAll(Arrays.asList(caps));
		caps = reg.getProjectDisabledCapabilities(getProject());
		disabledCaps.addAll(Arrays.asList(caps));

		Composite mainComposite = new Composite(topComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite capComposite = new Composite(mainComposite, SWT.NONE);
		capComposite.setLayout(new GridLayout());
		capComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		label = new Label(capComposite, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.capabilities")); //$NON-NLS-1$
		
		table = new TableViewer(capComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		table.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLabelProvider(new CapabilityLabelProvider());
		table.setContentProvider(getContentProvider());
		table.setInput(rootInput);
		
		Composite buttonComposite = new Composite(mainComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		label = new Label(buttonComposite, SWT.LEFT);
		label.setText(""); //$NON-NLS-1$
		
		addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText(WorkbenchMessages.getString("ProjectCapabilityEditingPropertyPage.add")); //$NON-NLS-1$
		addButton.setEnabled(false);
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
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
				if (parentElement instanceof ArrayList)
					return ((ArrayList)parentElement).toArray();
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
	
	private void removeCapability(Capability cap) {
		ArrayList results = new ArrayList();
		results.addAll(rootInput);
		results.remove(cap);
		Capability[] caps = new Capability[results.size()];
		results.toArray(caps);
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
		
		rootInput.remove(cap);
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

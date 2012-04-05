/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.*;
import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.operations.*;


public class RevertConfigurationWizardPage extends WizardPage {

	private TableViewer activitiesViewer;
	private TableViewer configViewer;
	public static Color blueBGColor;
	private SashForm sashForm;

	public RevertConfigurationWizardPage() {
		super("RevertConfiguration"); //$NON-NLS-1$
		setTitle(UpdateUIMessages.RevertConfigurationWizardPage_title); 
		setDescription(UpdateUIMessages.RevertConfigurationWizardPage_desc); 
		blueBGColor = new Color(null, 238,238,255);
		
	}

	public void createControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		createConfigurationsSection(sashForm);
		createActivitiesSection(sashForm);
		setControl(sashForm);

		Object element = configViewer.getElementAt(0);
		if (element != null)
			configViewer.setSelection(new StructuredSelection(element));
		Dialog.applyDialogFont(sashForm);
	}

	private void createConfigurationsSection(Composite parent) {
		Composite tableContainer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		tableContainer.setLayout(layout);
		tableContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(tableContainer, SWT.NONE);
		label.setText(UpdateUIMessages.RevertConfigurationWizardPage_label); 

		Table table = new Table(tableContainer, SWT.BORDER | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		configViewer = new TableViewer(table);
		configViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				UpdateLabelProvider provider =
					UpdateUI.getDefault().getLabelProvider();
				return provider.get(UpdateUIImages.DESC_CONFIG_OBJ, 0);
			}
			public String getText(Object element) {
				return Utilities.format(
					((IInstallConfiguration) element).getCreationDate());
			}

		});
		configViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object element) {
				ArrayList result = new ArrayList();
				ILocalSite localSite = (ILocalSite) element;
				IInstallConfiguration current =	localSite.getCurrentConfiguration();
				IInstallConfiguration[] configurations = localSite.getConfigurationHistory();
				for (int i = configurations.length - 1; i >= 0; i--) {
					if (!current.equals(configurations[i])) {
						result.add(configurations[i]);
					}
				}
				return result.toArray();
			}
			
			public void dispose() {
			}

			public void inputChanged(
				Viewer viewer,
				Object oldInput,
				Object newInput) {
			}

		});

		configViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				IStructuredSelection ssel =	(IStructuredSelection) e.getSelection();
				InstallConfiguration currentConfig = (InstallConfiguration)ssel.getFirstElement();
				activitiesViewer.setInput(currentConfig);
				activitiesViewer.refresh();
				TableItem[] items = activitiesViewer.getTable().getItems();
				for (int i =0; i<items.length; i++){
					IActivity activity = (IActivity)items[i].getData();
					// for now, we test exact config match. If needed, we can also compare dates, etc.
					if (((InstallConfiguration)activity.getInstallConfiguration()).equals(currentConfig))
						items[i].setBackground(blueBGColor);
					else
						items[i].setBackground(activitiesViewer.getControl().getBackground());
				}
			}
		});

		try {
			configViewer.setInput(SiteManager.getLocalSite());
		} catch (CoreException e1) {
		}
	}

	private void createActivitiesSection(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);



		Label label = new Label(composite, SWT.NONE);
		label.setText(
			UpdateUIMessages.RevertConfigurationWizardPage_activities); 

		TableLayoutComposite tlComposite= new TableLayoutComposite(composite, SWT.NONE);
		tlComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		activitiesViewer = ActivitiesTableViewer.createViewer(tlComposite, false);

		tlComposite.addColumnData(new ColumnPixelData(16, true, true));
		tlComposite.addColumnData(new ColumnWeightData(2, 150, true));
		tlComposite.addColumnData(new ColumnWeightData(5, 200, true));
		tlComposite.addColumnData(new ColumnWeightData(4, 100, true));

		//activitiesViewer.getTable().setLayout(layout);

		TableItem[] configs = configViewer.getTable().getItems();
		if (configs.length >0)
			activitiesViewer.setInput(configs[0].getData());
		
		composite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				int sashHeight = getSashForm().getSize().y;
				int sashWidth = getSashForm().getSize().x;
				if (composite.getSize().y > (sashHeight*0.85) && composite.getSize().x > (sashWidth*0.5)){
					getSashForm().setOrientation(SWT.HORIZONTAL);
				} else {
					getSashForm().setOrientation(SWT.VERTICAL);
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		blueBGColor.dispose();
	}
	
	public SashForm getSashForm(){
		return sashForm;
	}
	public boolean performFinish() {
		Shell shell = getContainer().getShell();
		boolean result =
			MessageDialog.openQuestion(
				shell,
				shell.getText(),
				UpdateUIMessages.RevertConfigurationWizardPage_question); 
		if (!result)
			return false;

		boolean finish = performRevert();
		if (finish) {
			PlatformUI.getWorkbench().restart();
		}
		return finish;
	}

	public boolean performRevert() {

		IStructuredSelection ssel =
			(IStructuredSelection) configViewer.getSelection();
		final IInstallConfiguration target =
			(IInstallConfiguration) ssel.getFirstElement();

		IStatus status =
			OperationsManager.getValidator().validatePendingRevert(target);
		if (status != null && status.getCode() == IStatus.ERROR) {
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				status);
			return false;
		}

		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
				IOperation revertOperation =
					OperationsManager
						.getOperationFactory()
						.createRevertConfigurationOperation(
						target,
						new UIProblemHandler());
				try {
					revertOperation.execute(monitor, null);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(false, true, operation);
			return true;
		} catch (InvocationTargetException e) {
			Throwable targetException = e.getTargetException();
			if (targetException instanceof InstallAbortedException) {
				return true;
			} else {
				UpdateUI.logException(e);
			}
			return false;
		} catch (InterruptedException e) {
			return false;
		}
	}
}

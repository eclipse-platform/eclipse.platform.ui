/*
 * Created on May 21, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import java.util.*;
import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.api.operations.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.views.*;


public class RevertConfigurationWizardPage extends WizardPage {

	private TableViewer activitiesViewer;
	private TableViewer configViewer;
	private InstallLogParser parser;

	public RevertConfigurationWizardPage() {
		super("RevertConfiguration"); //$NON-NLS-1$
		setTitle(UpdateUI.getString("RevertConfigurationWizardPage.title")); //$NON-NLS-1$
		setDescription(UpdateUI.getString("RevertConfigurationWizardPage.desc")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		SashForm composite = new SashForm(parent, SWT.VERTICAL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createConfigurationsSection(composite);
		createActivitiesSection(composite);
		setControl(composite);

		Object element = configViewer.getElementAt(0);
		if (element != null)
			configViewer.setSelection(new StructuredSelection(element));
		Dialog.applyDialogFont(composite);
	}

	private void createConfigurationsSection(Composite parent) {
		Composite tableContainer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		tableContainer.setLayout(layout);
		tableContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(tableContainer, SWT.NONE);
		label.setText(UpdateUI.getString("RevertConfigurationWizardPage.label")); //$NON-NLS-1$

		Table table = new Table(tableContainer, SWT.BORDER | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
				TableItem[] items = activitiesViewer.getTable().getItems();
				Color color = new Color(null, 238,238,255);
				for (int i =0; i<items.length; i++){
					IActivity activity = (IActivity)items[i].getData();
					// for now, we test exact config match. If needed, we can also compare dates, etc.
					if (activity.getInstallConfiguration() == currentConfig)
						items[i].setBackground(color);//activitiesViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
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
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		composite.setLayoutData(gd);

		Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 1;
		line.setLayoutData(gd);

		Label label = new Label(composite, SWT.NONE);
		label.setText(
			UpdateUI.getString("RevertConfigurationWizardPage.activities")); //$NON-NLS-1$
		activitiesViewer = ActivitiesTableViewer.createViewer(composite);

		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(8, 20, false));
		layout.addColumnData(new ColumnWeightData(50, 160, true));
		layout.addColumnData(new ColumnWeightData(50, 183, true));
		layout.addColumnData(new ColumnWeightData(50, 100, true));

		activitiesViewer.getTable().setLayout(layout);
		parser = new InstallLogParser();
		parser.parseInstallationLog();
//		InstallConfigElement[] ele = parser.getInstallConfigurations();
//		return ele;
		activitiesViewer.setInput(parser);
	}

	public boolean performFinish() {
		Shell shell = getContainer().getShell();
		boolean result =
			MessageDialog.openQuestion(
				shell,
				shell.getText(),
				UpdateUI.getString("RevertConfigurationWizardPage.question")); //$NON-NLS-1$
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

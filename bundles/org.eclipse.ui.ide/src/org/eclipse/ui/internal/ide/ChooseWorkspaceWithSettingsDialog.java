/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.preferences.SettingsTransfer;

/**
 * The ChooseWorkspaceWithSettingsDialog is the dialog used to switch workspaces
 * with an optional settings export.
 * 
 * @since 3.3
 * 
 */
public class ChooseWorkspaceWithSettingsDialog extends ChooseWorkspaceDialog {

	private Collection selectedSettings = new HashSet();

	/**
	 * Open a new instance of the receiver.
	 * 
	 * @param parentShell
	 * @param launchData
	 * @param suppressAskAgain
	 * @param centerOnMonitor
	 */
	public ChooseWorkspaceWithSettingsDialog(Shell parentShell,
			ChooseWorkspaceData launchData, boolean suppressAskAgain,
			boolean centerOnMonitor) {
		super(parentShell, launchData, suppressAskAgain, centerOnMonitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ide.ChooseWorkspaceDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Control top = super.createDialogArea(parent);
		createSettingsControls((Composite) top);
		return top;

	}

	/**
	 * Create the controls for selecting the controls we are going to export.
	 * 
	 * @param workArea
	 */
	private void createSettingsControls(Composite workArea) {
		FormToolkit toolkit = new FormToolkit(workArea.getDisplay());
		final ScrolledForm form = toolkit.createScrolledForm(workArea);
		form.setBackground(workArea.getBackground());
		form.getBody().setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ExpandableComposite expandable = toolkit.createExpandableComposite(form
				.getBody(), ExpandableComposite.TWISTIE);
		expandable
				.setText(IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_SettingsGroupName);
		expandable.setBackground(workArea.getBackground());
		expandable.setLayout(new GridLayout());
		expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		expandable.addExpansionListener(new IExpansionListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanging(ExpansionEvent e) {
				// Nothing to do here

			}
		});

		Composite sectionClient = toolkit.createComposite(expandable);
		sectionClient.setLayout(new GridLayout());
		sectionClient.setBackground(workArea.getBackground());

		createButtons(toolkit, sectionClient);

		expandable.setClient(sectionClient);

	}

	/**
	 * Create the buttons for the settings transfer.
	 * 
	 * @param toolkit
	 * @param sectionClient
	 */
	private void createButtons(FormToolkit toolkit, Composite sectionClient) {

		IConfigurationElement[] settings = SettingsTransfer
				.getSettingsTransfers();

		for (int i = 0; i < settings.length; i++) {
			final IConfigurationElement settingsTransfer = settings[i];
			final Button button = toolkit.createButton(sectionClient,
					settings[i].getAttribute(SettingsTransfer.ATT_NAME),
					SWT.CHECK);
			button.setBackground(sectionClient.getBackground());
			button.addSelectionListener(new SelectionAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					if (button.getSelection())
						selectedSettings.add(settingsTransfer);
					else
						selectedSettings.remove(settingsTransfer);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ide.ChooseWorkspaceDialog#okPressed()
	 */
	protected void okPressed() {
		Iterator settingsIterator = selectedSettings.iterator();
		MultiStatus result = new MultiStatus(
				PlatformUI.PLUGIN_ID,
				IStatus.OK,
				IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_ProblemsTransferTitle,
				null);

		IPath path = new Path(getWorkspaceLocation());

		while (settingsIterator.hasNext()) {
			IConfigurationElement elem = (IConfigurationElement) settingsIterator
					.next();
			result.add(transferSettings(elem, path));
		}
		if (result.getSeverity() != IStatus.OK) {
			ErrorDialog
					.openError(
							getShell(),
							IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_TransferFailedMessage,
							IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_SaveSettingsFailed,
							result);
			return;
		}
		super.okPressed();
	}

	/**
	 * Take the values from element and execute the class for path.
	 * 
	 * @param elem
	 * @param path
	 * @return IStatus the result of the settings transfer.
	 */
	private IStatus transferSettings(final IConfigurationElement element,
			final IPath path) {

		final IStatus[] exceptions = new IStatus[1];

		SafeRunner.run(new ISafeRunnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			public void run() throws Exception {

				try {
					SettingsTransfer transfer = (SettingsTransfer) WorkbenchPlugin
							.createExtension(element,
									SettingsTransfer.ATT_CLASS);
					transfer.transferSettings(path);
				} catch (CoreException exception) {
					exceptions[0] = exception.getStatus();
				}

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
			 */
			public void handleException(Throwable exception) {
				exceptions[0] = StatusUtil
						.newStatus(
								IStatus.ERROR,
								NLS
										.bind(
												IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_ClassCreationFailed,
												element
														.getAttribute(SettingsTransfer.ATT_CLASS)),
								exception);

			}
		});

		if (exceptions[0] != null)
			return exceptions[0];

		return Status.OK_STATUS;

	}
}

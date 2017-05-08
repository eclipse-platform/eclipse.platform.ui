/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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

	private static final String WORKBENCH_SETTINGS = "WORKBENCH_SETTINGS"; //$NON-NLS-1$
	private static final String ENABLED_TRANSFERS = "ENABLED_TRANSFERS"; //$NON-NLS-1$

	/**
	 * The class attribute for a settings transfer.
	 */
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$
	/**
	 * The name attribute for the settings transfer.
	 */
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	/**
	 * The id attribute for the settings transfer.
	 */
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_HELP_CONTEXT = "helpContext"; //$NON-NLS-1$

	private Collection<IConfigurationElement> selectedSettings = new HashSet<>();

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

	@Override
	protected Control createDialogArea(Composite parent) {
		Control top = super.createDialogArea(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IIDEHelpContextIds.SWITCH_WORKSPACE_ACTION);
		createSettingsControls((Composite) top);
		applyDialogFont(parent);
		return top;

	}

	/**
	 * Create the controls for selecting the controls we are going to export.
	 *
	 * @param workArea
	 */
	private void createSettingsControls(Composite workArea) {
		final FormToolkit toolkit = new FormToolkit(workArea.getDisplay());
		workArea.addDisposeListener(e -> toolkit.dispose());
		final ScrolledForm form = toolkit.createScrolledForm(workArea);
		form.getBody().setBackground(workArea.getBackground());
		form.getBody().setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final ExpandableComposite copySettingsExpandable =
				toolkit.createExpandableComposite(form.getBody(), ExpandableComposite.TWISTIE);

		copySettingsExpandable.setText(IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_SettingsGroupName);
		copySettingsExpandable.setBackground(workArea.getBackground());
		copySettingsExpandable.setLayout(new GridLayout());
		copySettingsExpandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		copySettingsExpandable.addExpansionListener(new IExpansionListener() {

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
				Point size = getInitialSize();
				Shell shell = getShell();
				shell.setBounds(getConstrainedShellBounds(
						new Rectangle(shell.getLocation().x, shell.getLocation().y, size.x, size.y)));
			}

			@Override
			public void expansionStateChanging(ExpansionEvent e) {
				// Nothing to do here

			}
		});

		Composite sectionClient = toolkit.createComposite(copySettingsExpandable);
		sectionClient.setBackground(workArea.getBackground());

		if (createButtons(toolkit, sectionClient))
			copySettingsExpandable.setExpanded(true);

		copySettingsExpandable.setClient(sectionClient);

	}

	/**
	 * Create the buttons for the settings transfer.
	 *
	 * @param toolkit
	 * @param sectionClient
	 * @return boolean <code>true</code> if any were selected
	 */
	private boolean createButtons(FormToolkit toolkit, Composite sectionClient) {
		String[] enabledSettings = getEnabledSettings(IDEWorkbenchPlugin
				.getDefault().getDialogSettings()
				.getSection(WORKBENCH_SETTINGS));

		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.marginLeft = 14;
		layout.spacing = 6;
		sectionClient.setLayout(layout);

		for (final IConfigurationElement settingsTransfer : SettingsTransfer.getSettingsTransfers()) {
			final Button button = toolkit.createButton(sectionClient,
					settingsTransfer.getAttribute(ATT_NAME), SWT.CHECK);

			ControlDecoration deco = new ControlDecoration(button, SWT.TOP | SWT.RIGHT);
			Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING)
					.getImage();
			deco.setDescriptionText(IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_copySettingsDecoLabel);
			deco.setImage(image);

			toggleDecoForSettingsImportButtons(button, deco);
			getCombo().addModifyListener(e -> toggleDecoForSettingsImportButtons(button, deco));

			String helpId = settingsTransfer.getAttribute(ATT_HELP_CONTEXT);

			if (helpId != null)
				PlatformUI.getWorkbench().getHelpSystem().setHelp(button, helpId);

			if (enabledSettings != null && enabledSettings.length > 0) {

				String id = settingsTransfer.getAttribute(ATT_ID);
				for (String enabledSetting : enabledSettings) {
					if (enabledSetting.equals(id)) {
						button.setSelection(true);
						selectedSettings.add(settingsTransfer);
						break;
					}
				}
			}

			button.setBackground(sectionClient.getBackground());
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (button.getSelection()) {
						selectedSettings.add(settingsTransfer);
					} else {
						selectedSettings.remove(settingsTransfer);
					}
					toggleDecoForSettingsImportButtons(button, deco);
				}
			});

		}
		return enabledSettings != null && enabledSettings.length > 0;
	}

	private void toggleDecoForSettingsImportButtons(Button button, ControlDecoration deco) {
		if (!button.getSelection()) {
			deco.hide();
			return;
		}

		if (Files.exists(Paths.get(getWorkspaceLocation()), LinkOption.NOFOLLOW_LINKS)) {
			deco.show();
		} else {
			deco.hide();
		}
	}
	/**
	 * Get the settings for the receiver based on the entries in section.
	 *
	 * @param section
	 * @return String[] or <code>null</code>
	 */
	private String[] getEnabledSettings(IDialogSettings section) {

		if (section == null)
			return null;

		return section.getArray(ENABLED_TRANSFERS);

	}

	@Override
	protected void okPressed() {
		Iterator<IConfigurationElement> settingsIterator = selectedSettings.iterator();
		MultiStatus result = new MultiStatus(
				PlatformUI.PLUGIN_ID,
				IStatus.OK,
				IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_ProblemsTransferTitle,
				null);

		IPath path = new Path(getWorkspaceLocation());
		String[] selectionIDs = new String[selectedSettings.size()];
		int index = 0;

		while (settingsIterator.hasNext()) {
			IConfigurationElement elem = settingsIterator
					.next();
			result.add(transferSettings(elem, path));
			selectionIDs[index] = elem.getAttribute(ATT_ID);
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

		saveSettings(selectionIDs);
		super.okPressed();
	}

	/**
	 * Save the ids of the selected elements.
	 *
	 * @param selectionIDs
	 */
	private void saveSettings(String[] selectionIDs) {
		IDialogSettings settings = IDEWorkbenchPlugin.getDefault()
				.getDialogSettings().getSection(WORKBENCH_SETTINGS);

		if (settings == null)
			settings = IDEWorkbenchPlugin.getDefault().getDialogSettings()
					.addNewSection(WORKBENCH_SETTINGS);

		settings.put(ENABLED_TRANSFERS, selectionIDs);

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
			@Override
			public void run() throws Exception {

				try {
					SettingsTransfer transfer = (SettingsTransfer) WorkbenchPlugin
							.createExtension(element, ATT_CLASS);
					transfer.transferSettings(path);
				} catch (CoreException exception) {
					exceptions[0] = exception.getStatus();
				}

			}

			@Override
			public void handleException(Throwable exception) {
				exceptions[0] = StatusUtil
						.newStatus(
								IStatus.ERROR,
								NLS
										.bind(
												IDEWorkbenchMessages.ChooseWorkspaceWithSettingsDialog_ClassCreationFailed,
												element.getAttribute(ATT_CLASS)),
								exception);

			}
		});

		if (exceptions[0] != null)
			return exceptions[0];

		return Status.OK_STATUS;

	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTLOCATION;
	}
}

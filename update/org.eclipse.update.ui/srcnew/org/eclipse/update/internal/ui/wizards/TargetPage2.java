/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;

public class TargetPage2 extends BannerPage2 {
	// NL keys
	private static final String KEY_TITLE = "InstallWizard.TargetPage.title";
	private static final String KEY_DESC = "InstallWizard.TargetPage.desc";
	private static final String KEY_NEW = "InstallWizard.TargetPage.new";
	private static final String KEY_DELETE = "InstallWizard.TargetPage.delete";
	private static final String KEY_REQUIRED_FREE_SPACE =
		"InstallWizard.TargetPage.requiredSpace";
	private static final String KEY_AVAILABLE_FREE_SPACE =
		"InstallWizard.TargetPage.availableSpace";
	private static final String KEY_LOCATION =
		"InstallWizard.TargetPage.location";
	private static final String KEY_LOCATION_MESSAGE =
		"InstallWizard.TargetPage.location.message";
	private static final String KEY_LOCATION_EMPTY =
		"InstallWizard.TargetPage.location.empty";
	private static final String KEY_LOCATION_EXISTS =
		"InstallWizard.TargetPage.location.exists";
	private static final String KEY_LOCATION_ERROR_TITLE =
		"InstallWizard.TargetPage.location.error.title";
	private static final String KEY_LOCATION_ERROR_MESSAGE =
		"InstallWizard.TargetPage.location.error.message";
	private static final String KEY_ERROR_REASON =
		"InstallWizard.TargetPage.location.error.reason";
	private static final String KEY_SIZE = "InstallWizard.TargetPage.size";
	private static final String KEY_SIZE_UNKNOWN =
		"InstallWizard.TargetPage.unknownSize";
	private TableViewer tableViewer;
	private IInstallConfiguration config;
	private ConfigListener configListener;
	private Label requiredSpaceLabel;
	private Label availableSpaceLabel;
	private PendingOperation pendingChange;
	private IConfiguredSite defaultTargetSite;
	private IConfiguredSite affinitySite;
	private Button deleteButton;
	private HashSet added;

	class TableContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object parent) {
			return config.getConfiguredSites();
		}
	}

	class TableLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
		/**
		* @see ITableLabelProvider#getColumnImage(Object, int)
		*/
		public Image getColumnImage(Object obj, int col) {
			if (obj instanceof IConfiguredSite)
				return UpdateUI
					.getDefault()
					.getLabelProvider()
					.getLocalSiteImage(
					(IConfiguredSite) obj);
			return null;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object obj, int col) {
			if (obj instanceof IConfiguredSite && col == 0) {
				IConfiguredSite csite = (IConfiguredSite) obj;
				ISite site = csite.getSite();
				URL url = site.getURL();
				return url.getFile();
			}
			return null;
		}

	}

	class ConfigListener implements IInstallConfigurationChangedListener {
		public void installSiteAdded(IConfiguredSite csite) {
			tableViewer.add(csite);
			if (added==null) added = new HashSet();
			added.add(csite);
			tableViewer.setSelection(new StructuredSelection(csite));
			tableViewer.getControl().setFocus();
		}

		public void installSiteRemoved(IConfiguredSite csite) {
			tableViewer.remove(csite);
			if (added!=null) added.remove(csite);
			selectFirstTarget();
			tableViewer.getControl().setFocus();
		}
	}

	/**
	 * Constructor for ReviewPage2
	 */
	public TargetPage2(
	PendingOperation pendingChange,
		IInstallConfiguration config) {
		super("Target");
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
		this.config = config;
		this.pendingChange = pendingChange;
		UpdateUI.getDefault().getLabelProvider().connect(this);
		configListener = new ConfigListener();
		defaultTargetSite = UpdateManager.getDefaultTargetSite(config, pendingChange, false);
		affinitySite = UpdateManager.getAffinitySite(config, pendingChange.getFeature());
		if (affinitySite == null)
			affinitySite = pendingChange.getDefaultTargetSite();
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		config.removeInstallConfigurationChangedListener(configListener);
		super.dispose();
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);
		createTableViewer(client);
		Composite buttonContainer = new Composite(client, SWT.NULL);
		GridLayout blayout = new GridLayout();
		blayout.marginWidth = blayout.marginHeight = 0;
		buttonContainer.setLayout(blayout);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		Button button = new Button(buttonContainer, SWT.PUSH);
		button.setText(UpdateUI.getString(KEY_NEW));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addTargetLocation();
			}
		});
		button.setEnabled(affinitySite == null);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		button.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(button);

		deleteButton = new Button(buttonContainer, SWT.PUSH);
		deleteButton.setText(UpdateUI.getString(KEY_DELETE));
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					removeSelection();
				}
				catch (CoreException ex) {
					UpdateUI.logException(ex);
				}
			}
		});
		deleteButton.setEnabled(false);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		deleteButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(deleteButton);

		Composite status = new Composite(client, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		status.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		status.setLayout(layout);
		Label label = new Label(status, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_REQUIRED_FREE_SPACE));
		requiredSpaceLabel = new Label(status, SWT.NULL);
		requiredSpaceLabel.setLayoutData(
			new GridData(GridData.FILL_HORIZONTAL));
		label = new Label(status, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_AVAILABLE_FREE_SPACE));
		availableSpaceLabel = new Label(status, SWT.NULL);
		availableSpaceLabel.setLayoutData(
			new GridData(GridData.FILL_HORIZONTAL));

		tableViewer.setInput(UpdateUI.getDefault().getUpdateModel());
		selectFirstTarget();
		WorkbenchHelp.setHelp(client, "org.eclipse.update.ui.TargetPage2");
		return client;
	}
	private void createTableViewer(Composite parent) {
		tableViewer =
			new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		Table table = tableViewer.getTable();
		table.setLayoutData(gd);
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer v, Object parent, Object obj) {
				IConfiguredSite site = (IConfiguredSite) obj;
				return getSiteVisibility(site);
			}
		});
		tableViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				updateDeleteButton((IStructuredSelection) selection);
				boolean empty = selection.isEmpty();
				verifyNotEmpty(empty);
				updateStatus(
					((IStructuredSelection) selection).getFirstElement());
			}
		});

		if (config != null)
			config.addInstallConfigurationChangedListener(configListener);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			tableViewer.getTable().setFocus();
		}
	}

	private void updateDeleteButton(IStructuredSelection selection) {
		boolean hasUserSites = added!=null && !added.isEmpty();
		boolean enable = !selection.isEmpty() && hasUserSites;

		if (hasUserSites) {
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (added.contains(obj) == false) {
					enable = false;
					break;
				}
			}
		}
		deleteButton.setEnabled(enable);
	}

	private boolean getSiteVisibility(IConfiguredSite site) {
		// If affinity site is known, only it should be shown
		if (affinitySite != null) {
			// Must compare referenced sites because
			// configured sites themselves may come from 
			// different configurations
			return site.getSite().equals(affinitySite.getSite());
		}

		// If this is the default target site, let it show
		if (site.equals(defaultTargetSite))
			return true;
		// Not the default. If update, show only private sites.
		// If install, allow product site + private sites.
		if (site.isPrivateSite() && site.isUpdatable())
			return true;
		if (pendingChange.getOldFeature() == null && site.isProductSite())
			return true;
		return false;
	}

	private void verifyNotEmpty(boolean empty) {
		String errorMessage = null;
		if (empty)
			errorMessage = UpdateUI.getString(KEY_LOCATION_EMPTY);
		setErrorMessage(errorMessage);
		setPageComplete(!empty);
	}

	private void selectFirstTarget() {
		IConfiguredSite firstSite = defaultTargetSite;
		if (firstSite == null) {
			IConfiguredSite[] sites = config.getConfiguredSites();
			for (int i = 0; i < sites.length; i++) {
				IConfiguredSite csite = sites[i];
				if (getSiteVisibility(csite)) {
					firstSite = csite;
					break;
				}
			}
		}
		if (firstSite != null) {
			tableViewer.setSelection(new StructuredSelection(firstSite));
		}
	}

	private void addTargetLocation() {
		DirectoryDialog dd = new DirectoryDialog(getContainer().getShell());
		dd.setMessage(UpdateUI.getString(KEY_LOCATION_MESSAGE));
		String path = dd.open();
		if (path != null) {
			File file = new File(path);
			addConfiguredSite(getContainer().getShell(), config, file, false);
		}
	}
	
	private void removeSelection() throws CoreException {
		IStructuredSelection selection =
			(IStructuredSelection) tableViewer.getSelection();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			IConfiguredSite csite = (IConfiguredSite)obj;
			config.removeConfiguredSite(csite);
		}
	}

	public static IConfiguredSite addConfiguredSite(
		Shell shell,
		IInstallConfiguration config,
		File file,
		boolean linked) {
		try {
			IConfiguredSite csite = null;
			if (linked) {
				csite = config.createLinkedConfiguredSite(file);
				config.addConfiguredSite(csite);
			} else {
				if (!ensureUnique(file, config)) {
					String title = UpdateUI.getString(KEY_LOCATION_ERROR_TITLE);
					String message = UpdateUI.getFormattedMessage(
								KEY_LOCATION_EXISTS,
								file.getPath());
					MessageDialog.openError(shell, title, message);
					return null;
				}
				csite = config.createConfiguredSite(file);
				IStatus status = csite.verifyUpdatableStatus();
				if (status.isOK())
					config.addConfiguredSite(csite);
				else {
					String title = UpdateUI.getString(KEY_LOCATION_ERROR_TITLE);
					String message =
						UpdateUI.getFormattedMessage(
							KEY_LOCATION_ERROR_MESSAGE,
							file.getPath());
					String message2 =
						UpdateUI.getFormattedMessage(
							KEY_ERROR_REASON,
							status.getMessage());
					message = message + "\r\n" + message2;
					ErrorDialog.openError(shell, title, message, status);
					return null;
				}
			}
			return csite;
		} catch (CoreException e) {
			UpdateUI.logException(e);
			return null;
		}
	}
	
	private static boolean ensureUnique(File file, IInstallConfiguration config) {
		IConfiguredSite [] sites = config.getConfiguredSites();
		URL fileURL;
		try {
			fileURL = new URL("file:"+file.getPath());
		}
		catch (MalformedURLException e) {
			return true;
		}
		for (int i=0; i<sites.length; i++) {
			IConfiguredSite csite = sites[i];
			URL url = csite.getSite().getURL();
			if (UpdateManagerUtils.sameURL(fileURL, url))
				return false;
		}
		return true;
	}

	private void updateStatus(Object element) {
		if (element == null) {
			requiredSpaceLabel.setText("");
			availableSpaceLabel.setText("");
			return;
		}
		IConfiguredSite site = (IConfiguredSite) element;
		URL url = site.getSite().getURL();
		String fileName = url.getFile();
		File file = new File(fileName);
		long available = LocalSystemInfo.getFreeSpace(file);
		long required =
			site.getSite().getInstallSizeFor(pendingChange.getFeature());
		if (required == -1)
			requiredSpaceLabel.setText(UpdateUI.getString(KEY_SIZE_UNKNOWN));
		else
			requiredSpaceLabel.setText(
				UpdateUI.getFormattedMessage(KEY_SIZE, "" + required));

		if (available == LocalSystemInfo.SIZE_UNKNOWN)
			availableSpaceLabel.setText(UpdateUI.getString(KEY_SIZE_UNKNOWN));
		else
			availableSpaceLabel.setText(
				UpdateUI.getFormattedMessage(KEY_SIZE, "" + available));
	}

	public IConfiguredSite getTargetSite() {
		IStructuredSelection sel =
			(IStructuredSelection) tableViewer.getSelection();
		if (sel.isEmpty())
			return null;
		return (IConfiguredSite) sel.getFirstElement();
	}
}

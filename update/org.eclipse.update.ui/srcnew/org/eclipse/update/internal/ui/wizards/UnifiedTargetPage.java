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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.*;
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

public class UnifiedTargetPage extends UnifiedBannerPage implements IUnifiedDynamicPage {
	// NL keys
	private static final String KEY_TITLE =
		"MultiInstallWizard.TargetPage.title";
	private static final String KEY_DESC = "MultiInstallWizard.TargetPage.desc";
	private static final String KEY_JOBS_LABEL =
		"MultiInstallWizard.TargetPage.jobsLabel";
	private static final String KEY_SITE_LABEL =
		"MultiInstallWizard.TargetPage.siteLabel";
	private static final String KEY_NEW = "MultiInstallWizard.TargetPage.new";
	private static final String KEY_DELETE = "MultiInstallWizard.TargetPage.delete";
	private static final String KEY_REQUIRED_FREE_SPACE =
		"MultiInstallWizard.TargetPage.requiredSpace";
	private static final String KEY_AVAILABLE_FREE_SPACE =
		"MultiInstallWizard.TargetPage.availableSpace";
	private static final String KEY_LOCATION_MESSAGE =
		"MultiInstallWizard.TargetPage.location.message";
	private static final String KEY_LOCATION_EMPTY =
		"MultiInstallWizard.TargetPage.location.empty";
	private static final String KEY_LOCATION_EXISTS =
		"MultiInstallWizard.TargetPage.location.exists";
	private static final String KEY_LOCATION_ERROR_TITLE =
		"MultiInstallWizard.TargetPage.location.error.title";
	private static final String KEY_LOCATION_ERROR_MESSAGE =
		"MultiInstallWizard.TargetPage.location.error.message";
	private static final String KEY_ERROR_REASON =
		"MultiInstallWizard.TargetPage.location.error.reason";
	private static final String KEY_SIZE = "MultiInstallWizard.TargetPage.size";
	private static final String KEY_SIZE_UNKNOWN =
		"MultiInstallWizard.TargetPage.unknownSize";
	private TableViewer jobViewer;
	private TableViewer siteViewer;
	private IInstallConfiguration config;
	private ConfigListener configListener;
	private Label requiredSpaceLabel;
	private Label availableSpaceLabel;
	private PendingOperation[] jobs;
	private Hashtable targetSites;  // keys are PendingOperation
	private Button addButton;
	private Button deleteButton;
	private HashSet added;

	class JobsContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object parent) {
			return jobs;
		}
	}

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
			
		public Image getColumnImage(Object obj, int col) {
			UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();
			if (obj instanceof IConfiguredSite)
				return provider.getLocalSiteImage((IConfiguredSite) obj);
			if (obj instanceof PendingOperation) {
				PendingOperation job = (PendingOperation) obj;
				ImageDescriptor base =
					job.getFeature().isPatch()
						? UpdateUIImages.DESC_EFIX_OBJ
						: UpdateUIImages.DESC_FEATURE_OBJ;
				int flags = 0;
				JobTargetSite jobSite = (JobTargetSite) targetSites.get(job);
				if (jobSite == null || jobSite.targetSite == null)
					flags = UpdateLabelProvider.F_ERROR;
				return provider.get(base, flags);
			}
			return null;
		}

		public String getColumnText(Object obj, int col) {
			if (obj instanceof PendingOperation && col == 0) {
				IFeature feature = ((PendingOperation) obj).getFeature();
				return feature.getLabel()
					+ " "
					+ feature.getVersionedIdentifier().getVersion().toString();
			}
			if (obj instanceof IConfiguredSite && col == 0) {
				ISite site = ((IConfiguredSite) obj).getSite();
				return site.getURL().getFile();

			}
			return null;
		}
	}

	class ConfigListener implements IInstallConfigurationChangedListener {
		public void installSiteAdded(IConfiguredSite csite) {
			siteViewer.add(csite);
			if (added == null)
				added = new HashSet();
			added.add(csite);
			siteViewer.setSelection(new StructuredSelection(csite));
			siteViewer.getControl().setFocus();
		}

		public void installSiteRemoved(IConfiguredSite csite) {
			siteViewer.remove(csite);
			if (added != null)
				added.remove(csite);
			PendingOperation job = (PendingOperation) siteViewer.getInput();
			if (job != null) {
				JobTargetSite jobSite = (JobTargetSite) targetSites.get(job);
				IConfiguredSite defaultSite = computeTargetSite(jobSite);
				if (defaultSite != null)
					siteViewer.setSelection(new StructuredSelection(defaultSite));
			}
			siteViewer.getControl().setFocus();
		}
	}

	/**
	 * Constructor for ReviewPage2
	 */
	public UnifiedTargetPage(IInstallConfiguration config) {
		super("MultiTarget");
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
		this.config = config;
		UpdateUI.getDefault().getLabelProvider().connect(this);
		configListener = new ConfigListener();
		targetSites = new Hashtable();
	}

	public void setJobs(PendingOperation[] jobs) {
		this.jobs = jobs;
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		config.removeInstallConfigurationChangedListener(configListener);
		super.dispose();
	}

	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);

		Label label = new Label(client, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_JOBS_LABEL));
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		createJobViewer(client);

		new Label(client, SWT.NULL);

		label = new Label(client, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_SITE_LABEL));
		gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		createSiteViewer(client);

		Composite buttonContainer = new Composite(client, SWT.NULL);
		GridLayout blayout = new GridLayout();
		blayout.marginWidth = blayout.marginHeight = 0;
		buttonContainer.setLayout(blayout);
		buttonContainer.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		addButton = new Button(buttonContainer, SWT.PUSH);
		addButton.setText(UpdateUI.getString(KEY_NEW));
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addTargetLocation();
			}
		});
		addButton.setEnabled(false);
		addButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		SWTUtil.setButtonDimensionHint(addButton);
		
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
		deleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		SWTUtil.setButtonDimensionHint(deleteButton);		
		
				
		Composite status = new Composite(client, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		status.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		status.setLayout(layout);
		label = new Label(status, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_REQUIRED_FREE_SPACE));
		requiredSpaceLabel = new Label(status, SWT.NULL);
		requiredSpaceLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label = new Label(status, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_AVAILABLE_FREE_SPACE));
		availableSpaceLabel = new Label(status, SWT.NULL);
		availableSpaceLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		WorkbenchHelp.setHelp(client, "org.eclipse.update.ui.MultiTargetPage2");
		
		Dialog.applyDialogFont(parent);
		
		return client;
	}

	private void createJobViewer(Composite parent) {
		jobViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		jobViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		jobViewer.setContentProvider(new JobsContentProvider());
		jobViewer.setLabelProvider(new TableLabelProvider());

		jobViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleJobsSelected((IStructuredSelection) event.getSelection());
			}
		});
	}

	private void handleJobsSelected(IStructuredSelection selection) {
		PendingOperation job = (PendingOperation) selection.getFirstElement();
		siteViewer.setInput(job);
		JobTargetSite jobSite = (JobTargetSite) targetSites.get(job);
		addButton.setEnabled(jobSite.affinitySite == null);
		if (jobSite.targetSite != null) {
			siteViewer.setSelection(new StructuredSelection(jobSite.targetSite));
		}
	}

	private void computeDefaultTargetSites() {
		targetSites.clear();
		for (int i = 0; i < jobs.length; i++) {
			JobTargetSite jobSite = new JobTargetSite();
			jobSite.job = jobs[i];
			jobSite.defaultSite =
				UpdateManager.getDefaultTargetSite(config, jobs[i], false);
			jobSite.affinitySite =
				UpdateManager.getAffinitySite(config, jobs[i].getFeature());
			if (jobSite.affinitySite == null)
				jobSite.affinitySite = jobs[i].getDefaultTargetSite();
			jobSite.targetSite = computeTargetSite(jobSite);
			targetSites.put(jobs[i], jobSite);
		}
	}

	private IConfiguredSite computeTargetSite(JobTargetSite jobSite) {
		IConfiguredSite csite =
			jobSite.affinitySite != null ? jobSite.affinitySite : jobSite.defaultSite;
		return (csite == null) ? getFirstTarget(jobSite) : csite;
	}

	private void createSiteViewer(Composite parent) {
		siteViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		siteViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		siteViewer.setContentProvider(new TableContentProvider());
		siteViewer.setLabelProvider(new TableLabelProvider());
		siteViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer v, Object parent, Object obj) {
				PendingOperation job = (PendingOperation) siteViewer.getInput();
				JobTargetSite jobSite = (JobTargetSite) targetSites.get(job);
				return getSiteVisibility((IConfiguredSite) obj, jobSite);
			}
		});
		siteViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				selectTargetSite(ssel);
				updateDeleteButton(ssel);
			}
		});

		if (config != null)
			config.addInstallConfigurationChangedListener(configListener);
	}
	
	private void updateDeleteButton(IStructuredSelection selection) {
		boolean enable = added != null && !added.isEmpty();
		if (enable) {
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				if (!added.contains(iter.next())) {
					enable = false;
					break;
				}
			}
		}
		deleteButton.setEnabled(enable);
	}

	public void setVisible(boolean visible) {
		if (visible) {
			computeDefaultTargetSites();
			jobViewer.setInput(jobs);
		}
		super.setVisible(visible);
		if (visible) {
			jobViewer.getTable().setFocus();
			if (jobs.length > 0)
				jobViewer.setSelection(new StructuredSelection(jobs[0]));
		}
	}

	private boolean getSiteVisibility(IConfiguredSite site, JobTargetSite jobSite) {
		// If affinity site is known, only it should be shown
		if (jobSite.affinitySite != null) {
			// Must compare referenced sites because
			// configured sites themselves may come from 
			// different configurations
			return site.getSite().equals(jobSite.affinitySite.getSite());
		}

		// If this is the default target site, let it show
		if (site.equals(jobSite.defaultSite))
			return true;
			
		// Not the default. If update, show only private sites.
		// If install, allow product site + private sites.
		if (site.isPrivateSite() && site.isUpdatable())
			return true;
			
		if (jobSite.job.getOldFeature() == null && site.isProductSite())
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

	private IConfiguredSite getFirstTarget(JobTargetSite jobSite) {
		IConfiguredSite firstSite = jobSite.targetSite;
		if (firstSite == null) {
			IConfiguredSite[] sites = config.getConfiguredSites();
			for (int i = 0; i < sites.length; i++) {
				IConfiguredSite csite = sites[i];
				if (getSiteVisibility(csite, jobSite)) {
					firstSite = csite;
					break;
				}
			}
		}
		return firstSite;
	}

	private void selectTargetSite(IStructuredSelection selection) {
		IConfiguredSite site = (IConfiguredSite) selection.getFirstElement();
		PendingOperation job = (PendingOperation) siteViewer.getInput();
		if (job != null) {
			JobTargetSite jobSite = (JobTargetSite) targetSites.get(job);
			jobSite.targetSite = site;
			pageChanged();
		}
		updateStatus(site);
	}

	private void addTargetLocation() {
		DirectoryDialog dd = new DirectoryDialog(getContainer().getShell());
		dd.setMessage(UpdateUI.getString(KEY_LOCATION_MESSAGE));
		String path = dd.open();
		if (path != null) {
			addConfiguredSite(getContainer().getShell(), config, new File(path), false);
		}
	}
	
	private void removeSelection() throws CoreException {
		IStructuredSelection selection = (IStructuredSelection) siteViewer.getSelection();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			config.removeConfiguredSite((IConfiguredSite) iter.next());
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
					String message =
						UpdateUI.getFormattedMessage(KEY_LOCATION_EXISTS, file.getPath());
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
					message += System.getProperty("line.separator") + message2;
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

	private void updateStatus(Object element) {
		if (element == null) {
			requiredSpaceLabel.setText("");
			availableSpaceLabel.setText("");
			return;
		}
		IConfiguredSite site = (IConfiguredSite) element;
		File file = new File(site.getSite().getURL().getFile());
		long available = LocalSystemInfo.getFreeSpace(file);
		long required = computeRequiredSizeFor(site);
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

	private long computeRequiredSizeFor(IConfiguredSite site) {
		long totalSize = 0;
		for (int i = 0; i < jobs.length; i++) {
			JobTargetSite jobSite = (JobTargetSite) targetSites.get(jobs[i]);
			if (site.equals(jobSite.targetSite)) {
				long jobSize = site.getSite().getInstallSizeFor(jobs[i].getFeature());
				if (jobSize == -1)
					return -1;
				totalSize += jobSize;
			}
		}
		return totalSize;
	}

	private void pageChanged() {
		boolean empty = false;
		for (Enumeration enum = targetSites.elements(); enum.hasMoreElements();) {
			JobTargetSite jobSite = (JobTargetSite) enum.nextElement();
			if (jobSite.targetSite == null) {
				empty = true;
				break;
			}
			IFeature feature = jobSite.job.getFeature();
			if (feature.isPatch()) {
				// Patches must go together with the features
				// they are patching.
				JobTargetSite patchedSite = findPatchedFeature(feature);
				if (patchedSite != null
					&& jobSite.targetSite != null
					&& patchedSite.targetSite != null
					&& jobSite.targetSite.equals(patchedSite.targetSite) == false) {
					setErrorMessage(
						"Patch '"
							+ feature.getLabel()
							+ "' must be installed in the same site as '"
							+ patchedSite.job.getFeature().getLabel()
							+ "'");
					setPageComplete(false);
					return;
				}
			}
		}
		verifyNotEmpty(empty);
	}

	private JobTargetSite findPatchedFeature(IFeature patch) {
		for (Enumeration enum = targetSites.elements(); enum.hasMoreElements();) {
			JobTargetSite jobSite = (JobTargetSite) enum.nextElement();
			IFeature target = jobSite.job.getFeature();
			if (!target.equals(patch) && UpdateUI.isPatch(target, patch))
				return jobSite;
		}
		return null;
	}

	public JobTargetSite[] getTargetSites() {
		JobTargetSite[] sites = new JobTargetSite[jobs.length];
		for (int i = 0; i < jobs.length; i++) {
			JobTargetSite jobSite = (JobTargetSite) targetSites.get(jobs[i]);
			sites[i] = jobSite;
		}
		return sites;
	}

	public IConfiguredSite getTargetSite(PendingOperation job) {
		PendingOperation target = null;
		for (int i = 0; jobs != null && i < jobs.length; i++)
			if (job == jobs[i]) {
				target = jobs[i];
				break;
			}
		if (target != null) {
			JobTargetSite jobSite = (JobTargetSite) targetSites.get(target);
			if (jobSite != null)
				return jobSite.targetSite;
		}
		return null;
	}

	private static boolean ensureUnique(File file, IInstallConfiguration config) {
		IConfiguredSite[] sites = config.getConfiguredSites();
		URL fileURL;
		try {
			fileURL = new URL("file:" + file.getPath());
		} catch (MalformedURLException e) {
			return true;
		}
		for (int i = 0; i < sites.length; i++) {
			URL url = sites[i].getSite().getURL();
			if (UpdateManagerUtils.sameURL(fileURL, url))
				return false;
		}
		return true;
	}
}

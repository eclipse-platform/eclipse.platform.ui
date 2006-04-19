/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.IInstallConfigurationChangedListener;
import org.eclipse.update.configuration.LocalSystemInfo;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.ui.UpdateLabelProvider;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIImages;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;
import org.eclipse.update.internal.ui.parts.SWTUtil;
import org.eclipse.update.operations.IInstallFeatureOperation;

public class TargetPage extends BannerPage implements IDynamicPage {
	private TableViewer jobViewer;
	private IInstallConfiguration config;
	private ConfigListener configListener;
	private Label requiredSpaceLabel;
	private Label availableSpaceLabel;
	private IInstallFeatureOperation[] jobs;
    //private IInstallFeatureOperation currentJob;
    private Label installLocation;
    private Button changeLocation;
    static HashSet added;

	class JobsContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object parent) {
			return jobs;
		}
	}

	class JobsLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
			
		public Image getColumnImage(Object obj, int col) {
			UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();

			IInstallFeatureOperation job = (IInstallFeatureOperation) obj;
			ImageDescriptor base =
				job.getFeature().isPatch()
					? UpdateUIImages.DESC_EFIX_OBJ
					: UpdateUIImages.DESC_FEATURE_OBJ;
			int flags = 0;
			if (job.getTargetSite() == null)
				flags = UpdateLabelProvider.F_ERROR;
			return provider.get(base, flags);
		}

		public String getColumnText(Object obj, int col) {
			if (col == 0) {
				IFeature feature = ((IInstallFeatureOperation) obj).getFeature();
				return feature.getLabel()
					+ " " //$NON-NLS-1$
					+ feature.getVersionedIdentifier().getVersion().toString();
			}
			return null;
		}
	}
	
	class ConfigListener implements IInstallConfigurationChangedListener {
		public void installSiteAdded(IConfiguredSite csite) {
			
			// set the site as target for all jobs without a target
			for (int i=0; jobs != null && i<jobs.length; i++)
				if (jobs[i].getTargetSite() == null && getSiteVisibility(csite, jobs[i])) {
					jobs[i].setTargetSite(csite);
				}

			jobViewer.refresh();
		}

		public void installSiteRemoved(IConfiguredSite csite) {
			// remove the target site for all jobs that use it
			// set the site as target for all jobs without a target
			boolean refreshJobs = false;
			for (int i=0; jobs != null && i<jobs.length; i++)
				if (jobs[i].getTargetSite() == csite) {
					jobs[i].setTargetSite(null);
					refreshJobs = true;
				}
				
			pageChanged();
			
			jobViewer.refresh();
			if (refreshJobs) {
				jobViewer.getControl().setFocus();
			}
		}
	}

	/**
	 * Constructor for ReviewPage2
	 */
	public TargetPage(IInstallConfiguration config) {
		super("Target"); //$NON-NLS-1$
		setTitle(UpdateUIMessages.InstallWizard_TargetPage_title); 
		setDescription(UpdateUIMessages.InstallWizard_TargetPage_desc); 
		this.config = config;
		UpdateUI.getDefault().getLabelProvider().connect(this);
		configListener = new ConfigListener();
	}

	public void setJobs(IInstallFeatureOperation[] jobs) {
		this.jobs = jobs;
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
//		config.removeInstallConfigurationChangedListener(configListener);
		super.dispose();
	}

	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);
		client.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(client, SWT.NULL);
		label.setText(UpdateUIMessages.InstallWizard_TargetPage_jobsLabel); 
		createJobViewer(client);

		label = new Label(client, SWT.NULL);
		label.setText(UpdateUIMessages.InstallWizard_TargetPage_location); 
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        label.setLayoutData(gd);

		installLocation = new Label(client, SWT.NULL);
        installLocation.setText("foo"); //$NON-NLS-1$
        gd = new GridData(GridData.FILL_HORIZONTAL);
        installLocation.setLayoutData(gd);
        
        changeLocation = new Button(client, SWT.PUSH);
        changeLocation.setText(UpdateUIMessages.InstallWizard_TargetPage_location_change); 
        changeLocation.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) jobViewer.getSelection();
                if (selection == null)
                    return;
                IInstallFeatureOperation job = (IInstallFeatureOperation) selection.getFirstElement();
                if (job == null) 
                    return;
                
                TargetSiteDialog dialog = new TargetSiteDialog(getShell(), config, job, configListener);
                dialog.create();

                SWTUtil.setDialogSize(dialog, 400, 300);
                
                dialog.getShell().setText(UpdateUIMessages.SitePage_new); 
                dialog.open();
                setTargetLocation(job);
                pageChanged();
                jobViewer.refresh();
                updateStatus(job.getTargetSite());
            }
        });
			
		Composite status = new Composite(client, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		status.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		status.setLayout(layout);
		label = new Label(status, SWT.NULL);
		label.setText(UpdateUIMessages.InstallWizard_TargetPage_requiredSpace); 
		requiredSpaceLabel = new Label(status, SWT.NULL);
		requiredSpaceLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label = new Label(status, SWT.NULL);
		label.setText(UpdateUIMessages.InstallWizard_TargetPage_availableSpace); 
		availableSpaceLabel = new Label(status, SWT.NULL);
		availableSpaceLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        PlatformUI.getWorkbench().getHelpSystem().setHelp(client, "org.eclipse.update.ui.MultiTargetPage2"); //$NON-NLS-1$
		
		Dialog.applyDialogFont(parent);
		
		return client;
	}

	private void createJobViewer(Composite parent) {
		jobViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 3;
		gd.widthHint = 150;
		jobViewer.getTable().setLayoutData(gd);
		jobViewer.setContentProvider(new JobsContentProvider());
		jobViewer.setLabelProvider(new JobsLabelProvider());

		jobViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				IInstallFeatureOperation job = (IInstallFeatureOperation) selection.getFirstElement();
				setTargetLocation(job);
			}
		});
	}

	public void setVisible(boolean visible) {
		if (visible) {
			initializeDefaultTargetSites();
			jobViewer.setInput(jobs);
			if (jobViewer.getSelection().isEmpty() && jobs.length > 0)
				jobViewer.setSelection(new StructuredSelection(jobs[0]));
		}
		
		super.setVisible(visible);
	}

	private void verifyNotEmpty(boolean empty) {
		String errorMessage = null;
		if (empty)
			errorMessage = UpdateUIMessages.InstallWizard_TargetPage_location_empty; 
		setErrorMessage(errorMessage);
		setPageComplete(!empty);
	}


	private void updateStatus(Object element) {
		if (element == null) {
			requiredSpaceLabel.setText(""); //$NON-NLS-1$
			availableSpaceLabel.setText(""); //$NON-NLS-1$
			return;
		}
		IConfiguredSite site = (IConfiguredSite) element;
		File file = new File(site.getSite().getURL().getFile());
		long available = LocalSystemInfo.getFreeSpace(file);
		long required = computeRequiredSizeFor(site);
		//add the download size to space required to do operation since all plugins and nonplugin data will be downloaded first
		required += computeDownloadSizeFor(site); 
		if (required <= 0)
			requiredSpaceLabel.setText(UpdateUIMessages.InstallWizard_TargetPage_unknownSize); 
		else
			requiredSpaceLabel.setText(
				NLS.bind(UpdateUIMessages.InstallWizard_TargetPage_size, "" + required)); //$NON-NLS-1$

		if (available == LocalSystemInfo.SIZE_UNKNOWN)
			availableSpaceLabel.setText(UpdateUIMessages.InstallWizard_TargetPage_unknownSize); 
		else
			availableSpaceLabel.setText(
				NLS.bind(UpdateUIMessages.InstallWizard_TargetPage_size, "" + available)); //$NON-NLS-1$
		//if the available space was retireved from the OS and the required space is greater that the available space, do not let the user continue
		if(available != LocalSystemInfo.SIZE_UNKNOWN && required > available){
			this.setPageComplete(false);
			//TODO: set error message: "...not enough space..."
		}else
			this.setPageComplete(true);
	}

	private long computeRequiredSizeFor(IConfiguredSite site) {
		long totalSize = 0;
		for (int i = 0; i < jobs.length; i++) {
			if (site.equals(jobs[i].getTargetSite())) {
				long jobSize = site.getSite().getInstallSizeFor(jobs[i].getFeature());
				if (jobSize == -1)
					return -1;
				totalSize += jobSize;
			}
		}
		return totalSize;
	}
	
	private long computeDownloadSizeFor(IConfiguredSite site) {
		long totalSize = 0;
		for (int i = 0; i < jobs.length; i++) {
			if (site.equals(jobs[i].getTargetSite())) {
				long jobSize = site.getSite().getDownloadSizeFor(jobs[i].getFeature());
				if (jobSize == -1)
					return -1;
				totalSize += jobSize;
			}
		}
		return totalSize;
	}

	private void pageChanged() {
		boolean empty = false;
		for (int i=0; jobs!=null && i<jobs.length; i++) {
			if (jobs[i].getTargetSite() == null) {
				empty = true;
				break;
			}
		
			IFeature feature = jobs[i].getFeature();
			if (feature.isPatch()) {
				// Patches must go together with the features
				// they are patching.
				
				// Check current jobs
				IInstallFeatureOperation patchedFeatureJob = findPatchedFeature(feature);
				if (patchedFeatureJob != null
					&& patchedFeatureJob.getTargetSite() != null
					&& !jobs[i].getTargetSite().equals(patchedFeatureJob.getTargetSite())) {
					String msg = NLS.bind(UpdateUIMessages.InstallWizard_TargetPage_patchError, (new String[] {
                    feature.getLabel(),
                    patchedFeatureJob.getFeature().getLabel()}));
					setErrorMessage(msg);
					setPageComplete(false);
					return;
				}
				// Check installed features
				IFeature patchedFeature = UpdateUtils.getPatchedFeature(feature);
				if (patchedFeature != null  
					&& !jobs[i].getTargetSite().equals(patchedFeature.getSite().getCurrentConfiguredSite())) {
					String msg = NLS.bind(UpdateUIMessages.InstallWizard_TargetPage_patchError2, (new String[] {
                    feature.getLabel(),
                    patchedFeature.getLabel(),
                    patchedFeature.getSite().getCurrentConfiguredSite().getSite().getURL().getFile()}));
					setErrorMessage(msg);
					setPageComplete(false);
					return;
				}
			}
		}
		verifyNotEmpty(empty);
	}
	
	
	private  boolean getSiteVisibility(IConfiguredSite site, IInstallFeatureOperation job) {
		// Do not allow installing into a non-updateable site
		if (!site.isUpdatable())
			return false;
		
		// If affinity site is known, only it should be shown
		IConfiguredSite affinitySite = UpdateUtils.getAffinitySite(config, job.getFeature());
		if (affinitySite != null) {
			// Must compare referenced sites because
			// configured sites themselves may come from 
			// different configurations
			return site.getSite().equals(affinitySite.getSite());
		}
		
		// Co-locate updates with the old feature
		if (job.getOldFeature() != null) {
			IConfiguredSite oldSite = UpdateUtils.getSiteWithFeature(config, job.getOldFeature().getVersionedIdentifier().getIdentifier());
			return (site == oldSite);
		}

		// Allow installing into any site that is updateable and there is no affinity specified
		return true;
	}
	
	private void initializeDefaultTargetSites() {
		for (int i = 0; i < jobs.length; i++) {
			if (jobs[i].getTargetSite() != null)
				continue;
			
			IConfiguredSite affinitySite =	UpdateUtils.getAffinitySite(config, jobs[i].getFeature());
			if (affinitySite != null) {
				jobs[i].setTargetSite(affinitySite);
				continue;
			}

			IConfiguredSite defaultSite = UpdateUtils.getDefaultTargetSite(config, jobs[i], false);
			if (defaultSite != null) {
				jobs[i].setTargetSite(defaultSite);
				continue;
			}

			jobs[i].setTargetSite(getFirstTargetSite(jobs[i]));

		}
	}
	

	private IConfiguredSite getFirstTargetSite(IInstallFeatureOperation job) {
		IConfiguredSite[] sites = config.getConfiguredSites();
		for (int i = 0; i < sites.length; i++) {
			IConfiguredSite csite = sites[i];
			if (getSiteVisibility(csite, job)) 
				return csite;
		}
		return null;
	}
	
	public IInstallFeatureOperation findPatchedFeature(IFeature patch) {
		for (int i=0; i<jobs.length; i++) {
			IFeature target = jobs[i].getFeature();
			if (!target.equals(patch) && UpdateUtils.isPatch(target, patch))
				return jobs[i];
		}
		return null;
	}
  
    
    void removeAddedSites() {
        if (added != null) {
        	Iterator it = added.iterator(); 
            while (it.hasNext()) {
                 config.removeConfiguredSite((IConfiguredSite) it.next());
                // the config listener no longer removes the site so we need to cleanup the collection at the end
            }
            added.clear();
        }           
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
     */
    public boolean isPageComplete() {
        // check if all jobs have a target site set
        for (int i = 0; jobs != null && i < jobs.length; i++) {
            if (jobs[i].getTargetSite() == null)
                return false;
        }
        return super.isPageComplete();
    }

    /**
     * @param job
     */
    private void setTargetLocation(IInstallFeatureOperation job) {
        if (job != null && job.getTargetSite() != null) {
            installLocation.setText(new File(job.getTargetSite().getSite().getURL().getFile()).toString());
            updateStatus(job.getTargetSite());
        }
    }
}

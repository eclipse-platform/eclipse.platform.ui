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
package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.TeamStatus;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ParticipantPageSaveablePart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.part.PageBook;

/**
 * Page that displays the compare input for sharing
 */
public class SharingWizardSyncPage extends CVSWizardPage implements ISyncInfoSetChangeListener {
	
	private ParticipantPageSaveablePart input;
	private ISynchronizePageConfiguration configuration;
	private SyncInfoSet infos;
	private IProject project;
	
	PageBook pageBook;
	private Control syncPage;
	private Control noChangesPage;
	private Control errorPage;
	
	public SharingWizardSyncPage(String pageName, String title, ImageDescriptor titleImage, String description) {
		super(pageName, title, titleImage, description);
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		setControl(composite);
		
		// set F1 help
		//WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARE_WITH_EXISTING_TAG_SELETION_DIALOG);
		
		pageBook = new PageBook(composite, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		pageBook.setLayoutData(data);
		
		input = createCompareInput();
		input.createPartControl(pageBook);
		syncPage = input.getControl();
		infos = (SyncInfoSet)configuration.getSyncInfoSet();
		infos.addSyncSetChangedListener(this);
		
		noChangesPage = createNoChangesPage(pageBook);
		noChangesPage.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		errorPage = createErrorPage(pageBook);
		errorPage.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		updatePage();
		
		Dialog.applyDialogFont(parent);	
	}
	
	private Control createNoChangesPage(PageBook pageBook) {
		Composite composite = createComposite(pageBook, 1);
		createWrappingLabel(composite, Policy.bind("SharingWizardSyncPage.3", project.getName()), 0); //$NON-NLS-1$
		return composite;
	}
	
	private Control createErrorPage(PageBook pageBook) {
		Composite composite = new Composite(pageBook, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		createWrappingLabel(composite, Policy.bind("SharingWizardSyncPage.4"), 0); //$NON-NLS-1$

		Button showErrors = new Button(composite, SWT.PUSH);
		showErrors.setText(Policy.bind("SharingWizardSyncPage.5")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		showErrors.setLayoutData(data);
		showErrors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showErrors();
			}
		});
		
		return composite;
	}

	/* private */ void showErrors() {
		ITeamStatus[] status = infos.getErrors();
		String title = Policy.bind("Errors Occurred"); //$NON-NLS-1$
		if (status.length == 1) {
			IStatus s = status[0];
			if (s.getException() instanceof CoreException) {
				s = ((CoreException)s.getException()).getStatus();
			}
			ErrorDialog.openError(getShell(), title, null, s);
		} else {
			MultiStatus multi = new MultiStatus(CVSUIPlugin.ID, 0, status, "The following errors occurred.", null); //$NON-NLS-1$
			ErrorDialog.openError(getShell(), title, null, multi);
		}
	}
	
	private ParticipantPageSaveablePart createCompareInput() {	
		WorkspaceSynchronizeParticipant participant = CVSUIPlugin.getPlugin().getCvsWorkspaceSynchronizeParticipant();
		configuration = participant.createPageConfiguration();
		configuration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] {SharingWizardPageActionGroup.ACTION_GROUP});
		configuration.addActionContribution(new SharingWizardPageActionGroup());
		
		IWorkingSetManager manager = TeamUIPlugin.getPlugin().getWorkbench().getWorkingSetManager();
		IWorkingSet newSet = manager.createWorkingSet("sharing wizard", new IAdaptable[] {project});
		configuration.setWorkingSet(newSet);
		
		CompareConfiguration cc = new CompareConfiguration();
		cc.setLeftEditable(false);
		cc.setRightEditable(false);
		ParticipantPageSaveablePart part = new ParticipantPageSaveablePart(getShell(), cc, configuration, participant);
		
		return part;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		input.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#setPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public void setPreviousPage(IWizardPage page) {
		// There's no going back from this page
		super.setPreviousPage(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetReset(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
		updatePage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoChanged(org.eclipse.team.core.synchronize.ISyncInfoSetChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
		updatePage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetErrors(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
		updatePage();
	}

	private void updatePage() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (pageBook.isDisposed()) return;
				if (infos.getErrors().length > 0) {
					pageBook.showPage(errorPage);
				} else if (infos.isEmpty()) {
					pageBook.showPage(noChangesPage);
				} else {
					pageBook.showPage(syncPage);
				}
			}
		});
	}

	public void showError(TeamStatus status) {
		infos.addError(status);
	}
}

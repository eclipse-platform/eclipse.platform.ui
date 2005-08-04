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
package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.TeamStatus;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.ui.PixelConverter;
import org.eclipse.team.internal.ui.SWTUtils;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

/**
 * Page that displays the compare input for sharing
 */
public class SharingWizardSyncPage extends CVSWizardPage implements ISyncInfoSetChangeListener {
	
	// Constant keys used to store last size for this page
	private static final String PAGE_HEIGHT = "SyncPageHeight"; //$NON-NLS-1$
	private static final String PAGE_WIDTH = "SyncPageWidth"; //$NON-NLS-1$
	
	private ParticipantPageSaveablePart input;
	private ISynchronizePageConfiguration configuration;
	private SyncInfoSet infos;
	private IProject project;
	
	PageBook pageBook;
	private Control syncPage;
	private Control noChangesPage;
	private Control errorPage;
	
	private int width;
	private int height;
	private SharingWizardPageActionGroup sharingWizardPageActionGroup;
	private Button fCheckbox;
	
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
		
		final PixelConverter converter= SWTUtils.createDialogPixelConverter(parent);
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_DEFAULT));
		setControl(composite);
		
		
		pageBook = new PageBook(composite, SWT.NONE);
		pageBook.setLayoutData(SWTUtils.createHVFillGridData());
		
		input = createCompareInput();
		input.createPartControl(pageBook);
		syncPage = input.getControl();
		infos = configuration.getSyncInfoSet();
		infos.addSyncSetChangedListener(this);
		
		noChangesPage = createNoChangesPage(pageBook);
		noChangesPage.setLayoutData(SWTUtils.createHVFillGridData());
		
		errorPage = createErrorPage(pageBook);
		errorPage.setLayoutData(SWTUtils.createHVFillGridData());
		
		SWTUtils.createPlaceholder(composite, 1);
		
		fCheckbox= new Button(composite, SWT.CHECK);
		fCheckbox.setLayoutData(SWTUtils.createHFillGridData());
		fCheckbox.setText(CVSUIMessages.SharingWizardSyncPage_12); 
		fCheckbox.setSelection(true);
		
		updatePage();
		
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.SHARING_SYNC_PAGE);
		Dialog.applyDialogFont(parent);	
	}
	
	private Control createNoChangesPage(PageBook pageBook) {
		Composite composite = createComposite(pageBook, 1, false);
		createWrappingLabel(composite, NLS.bind(CVSUIMessages.SharingWizardSyncPage_3, new String[] { project.getName() }), 0); 
		return composite;
	}
	
	private Control createErrorPage(PageBook pageBook) {
		Composite composite = new Composite(pageBook, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		createWrappingLabel(composite, CVSUIMessages.SharingWizardSyncPage_4, 0); 

		Button showErrors = new Button(composite, SWT.PUSH);
		showErrors.setText(CVSUIMessages.SharingWizardSyncPage_5); 
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
		String title = CVSUIMessages.SharingWizardSyncPage_8; 
		if (status.length == 1) {
			IStatus s = status[0];
			if (s.getException() instanceof CoreException) {
				s = ((CoreException)s.getException()).getStatus();
			}
			ErrorDialog.openError(getShell(), title, null, s);
		} else {
			MultiStatus multi = new MultiStatus(CVSUIPlugin.ID, 0, status, CVSUIMessages.SharingWizardSyncPage_9, null); 
			ErrorDialog.openError(getShell(), title, null, multi);
		}
	}
	
	private ParticipantPageSaveablePart createCompareInput() {	
		WorkspaceSynchronizeParticipant participant = new WorkspaceSynchronizeParticipant(new ResourceScope(new IResource[] { project }));
		configuration = participant.createPageConfiguration();
		configuration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] {ISynchronizePageConfiguration.LAYOUT_GROUP, SharingWizardPageActionGroup.ACTION_GROUP});
		sharingWizardPageActionGroup = new SharingWizardPageActionGroup();
		configuration.addActionContribution(sharingWizardPageActionGroup);
		configuration.setRunnableContext(getContainer());
		
		CompareConfiguration cc = new CompareConfiguration();
		cc.setLeftEditable(false);
		cc.setRightEditable(false);
		ParticipantPageSaveablePart part = new ParticipantPageSaveablePart(getShell(), cc, configuration, participant);
		part.setShowContentPanes(false);
		return part;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		if (input != null) {
			input.getParticipant().dispose();
			input.dispose();
		}
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

	public WorkspaceSynchronizeParticipant getParticipant() {
		return (WorkspaceSynchronizeParticipant)configuration.getParticipant();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (syncPage.isVisible()) {
			initializeSize();
			getShell().setSize(Math.max(width, 300), Math.max(height, 300));
			if(input != null) {
				Viewer viewer = input.getPageConfiguration().getPage().getViewer();
				if(viewer instanceof AbstractTreeViewer) {
					((AbstractTreeViewer)viewer).expandToLevel(2);
				}
			}
		}
	}
	
	private void initializeSize() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			try {
				width = settings.getInt(PAGE_WIDTH);
				height = settings.getInt(PAGE_HEIGHT);
			} catch (NumberFormatException e) {
				// Ignore and go on;
			}
		}
		if (width == 0) width = 640;
		if (height == 0) height = 480;
	}

	/**
	 * Save the size of the page so it can be opened with the same size next time
	 */
	public void saveSettings() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			Point size = getShell().getSize();
			settings.put(PAGE_WIDTH, size.x);
			settings.put(PAGE_HEIGHT, size.y);
		}
	}
	
	public boolean commitChanges() {
		return fCheckbox != null ? fCheckbox.getSelection() : false;
	}
	
	/**
	 * @return Returns the project.
	 */
	public IProject getProject() {
		return project;
	}
}

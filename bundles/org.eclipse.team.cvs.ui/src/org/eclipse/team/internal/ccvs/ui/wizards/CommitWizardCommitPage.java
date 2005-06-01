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

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

/**
 * This wizard page shows a preview of the commit operation and allows entering
 * a commit comment.
 */
public class CommitWizardCommitPage extends WizardPage implements IPropertyChangeListener {
    
    private final CommitCommentArea fCommentArea;
    
    private ISynchronizePageConfiguration fConfiguration;
    
    protected final CommitWizard fWizard;
    
	private ParticipantPagePane fPagePane;
    private PageBook bottomChild;
    
    public CommitWizardCommitPage(IResource [] resources, CommitWizard wizard) {
        
        super(CVSUIMessages.CommitWizardCommitPage_0); //$NON-NLS-1$
        setTitle(CVSUIMessages.CommitWizardCommitPage_0); //$NON-NLS-1$
        setDescription(CVSUIMessages.CommitWizardCommitPage_2); //$NON-NLS-1$
        
        fWizard= wizard;
        fCommentArea= new CommitCommentArea();
        fCommentArea.setProposedComment(getProposedComment(resources));
        if (resources.length > 0)
            fCommentArea.setProject(resources[0].getProject());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Dialog.applyDialogFont(parent);
        final PixelConverter converter= new PixelConverter(parent);
        
        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_DEFAULT));
        // set F1 help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.COMMIT_COMMENT_PAGE);
        
        createCommentArea(composite, converter);
        createChangesArea(composite, converter);
        
        //fSashForm.setWeights(weights);
        Dialog.applyDialogFont(parent);
        setControl(composite);
        
        fCommentArea.setFocus();
        
        validatePage(false);
    }
    
    private void createCommentArea(Composite parent, PixelConverter converter) {
        
        fCommentArea.createArea(parent);
        fCommentArea.getComposite().setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, SWT.FILL, SWT.FILL, true, true));
        fCommentArea.addPropertyChangeListener(this);
        
        createPlaceholder(parent);
    }
    
    private void createChangesArea(Composite parent, PixelConverter converter) {
        
        CommitWizardParticipant participant= fWizard.getParticipant();
        int size = participant.getSyncInfoSet().size();
        if (size > getFileDisplayThreshold()) {
            // Create a page book to allow eventual inclusion of changes
            bottomChild = new PageBook(parent, SWT.NONE);
            bottomChild.setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, SWT.FILL, SWT.FILL, true, false));
            // Create composite for showing the reason for not showing the changes and a button to show them
            Composite changeDesc = new Composite(bottomChild, SWT.NONE);
            changeDesc.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_NONE));
            SWTUtils.createLabel(changeDesc, NLS.bind(CVSUIMessages.CommitWizardCommitPage_1, new String[] { Integer.toString(size), Integer.toString(getFileDisplayThreshold()) })); //$NON-NLS-1$
            Button showChanges = new Button(changeDesc, SWT.PUSH);
            showChanges.setText(CVSUIMessages.CommitWizardCommitPage_5); //$NON-NLS-1$
            showChanges.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    showChangesPane();
                }
            });
            showChanges.setLayoutData(new GridData());
            bottomChild.showPage(changeDesc);
        } else {
            final Composite composite= new Composite(parent, SWT.NONE);
            composite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_NONE));
            composite.setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, SWT.FILL, SWT.FILL, true, true));
            
            createPlaceholder(composite);
            
            Control c = createChangesPage(composite, participant);
            c.setLayoutData(SWTUtils.createHVFillGridData());
        }
    }

    protected void showChangesPane() {
        Control c = createChangesPage(bottomChild, fWizard.getParticipant());
        bottomChild.setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, SWT.FILL, SWT.FILL, true, true));
        bottomChild.showPage(c);
        Dialog.applyDialogFont(getControl());
        ((Composite)getControl()).layout();
    }

    private Control createChangesPage(final Composite composite, CommitWizardParticipant participant) {
        fConfiguration= participant.createPageConfiguration();
        fPagePane= new ParticipantPagePane(getShell(), true /* modal */, fConfiguration, participant);
        Control control = fPagePane.createPartControl(composite);
        return control;
    }

	private int getFileDisplayThreshold() {
        return CVSUIPlugin.getPlugin().getPreferenceStore().getInt(ICVSUIConstants.PREF_COMMIT_FILES_DISPLAY_THRESHOLD);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		super.dispose();
		// Disposing of the page pane will dispose of the page and the configuration
		if (fPagePane != null)
			fPagePane.dispose();
	}
	
    private void createPlaceholder(final Composite composite) {
        final Composite placeholder= new Composite(composite, SWT.NONE);
        placeholder.setLayoutData(new GridData(SWT.DEFAULT, convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_SPACING) /3));
    }
    
    public String getComment(Shell shell) {
        return fCommentArea.getCommentWithPrompt(shell);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    public boolean isPageComplete() {
        return super.isPageComplete();
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        expand();
        fCommentArea.setFocus();
    }
    
    protected void expand() {
        if (fConfiguration != null) {
            final Viewer viewer= fConfiguration.getPage().getViewer();
            if (viewer instanceof TreeViewer) {
            	try {
    	        	viewer.getControl().setRedraw(false);
    	            ((TreeViewer)viewer).expandAll();
            	} finally {
            		viewer.getControl().setRedraw(true);
            	}
            }
        }
    }
    
	/*
	 * Expand the sync elements and update the page enablement
	 */
	protected void updateForModelChange() {
        Control control = getControl();
        if (control == null || control.isDisposed()) return;
		expand();
		updateEnablements();
	}
	
	public void updateEnablements() {
        if (fConfiguration != null) {
    		SyncInfoSet set = fConfiguration.getSyncInfoSet();
    		if (set.hasConflicts()) {
    			setErrorMessage(CVSUIMessages.CommitWizardCommitPage_4); //$NON-NLS-1$
    			setPageComplete(false);
    			return;
    		}
    		if (set.isEmpty()) {
    			// No need for a message as it should be obvious that there are no resources to commit
    			setErrorMessage(null);
    			setPageComplete(false);
    			return;
    		}
        }
		setErrorMessage(null);
		setPageComplete(true);
	}

	boolean validatePage(boolean setMessage) {
        if (fCommentArea != null && fCommentArea.getComment(false).length() == 0) {
            final IPreferenceStore store= CVSUIPlugin.getPlugin().getPreferenceStore();
            final String value= store.getString(ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS);
            if (MessageDialogWithToggle.NEVER.equals(value)) {
                setPageComplete(false);
                if (setMessage)
                    setErrorMessage(CVSUIMessages.CommitWizardCommitPage_3); //$NON-NLS-1$
                return false;
            }
        }
        setPageComplete(true);
        setErrorMessage(null);
        return true;
    }
    
    public void setFocus() {
        fCommentArea.setFocus();
        validatePage(true);
    }
    
    protected IWizardContainer getContainer() {
        return super.getContainer();
    }
    
    public SyncInfoSet getInfosToCommit() {

        final SyncInfoSet infos= new SyncInfoSet();
        if (fConfiguration == null) {
            return fWizard.getParticipant().getSyncInfoSet();
        }
        
        final IDiffElement root = (ISynchronizeModelElement)fConfiguration.getProperty(SynchronizePageConfiguration.P_MODEL);
        final IDiffElement [] elements= Utils.getDiffNodes(new IDiffElement [] { root });
        
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] instanceof SyncInfoModelElement) {
                SyncInfo syncInfo = ((SyncInfoModelElement)elements[i]).getSyncInfo();
                int direction = syncInfo.getKind() & SyncInfo.DIRECTION_MASK;
				if (syncInfo.getLocal().getType() == IResource.FILE && (direction == SyncInfo.OUTGOING || direction == SyncInfo.CONFLICTING))
                	infos.add(syncInfo);
            }
        }  
        return infos;//(IResource [])result.toArray(new IResource[result.size()]);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        
        if (event.getProperty().equals(CommitCommentArea.OK_REQUESTED)) {
            final IWizardContainer container= getContainer();
            if (container instanceof WizardDialog) {
                final WizardDialog dialog= (WizardDialog)container;
                if (getWizard().canFinish()) {
                    try {
                        getWizard().performFinish();
                    } finally {
                        dialog.close();
                    }
                }
            }
        }
        if (event.getProperty().equals(CommitCommentArea.COMMENT_MODIFIED)) {
            validatePage(true); 
        }
    }
    
	/*
	 * Get a proposed comment by looking at the active change sets
	 */
    private String getProposedComment(IResource[] resourcesToCommit) {
    	StringBuffer comment = new StringBuffer();
        ChangeSet[] sets = CVSUIPlugin.getPlugin().getChangeSetManager().getSets();
        int numMatchedSets = 0;
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set = sets[i];
            if (containsOne(set, resourcesToCommit)) {
            	if(numMatchedSets > 0) comment.append(System.getProperty("line.separator")); //$NON-NLS-1$
                comment.append(set.getComment());
                numMatchedSets++;
            }
        }
        return comment.toString();
    }
    
    private boolean containsOne(ChangeSet set, IResource[] resourcesToCommit) {
   	 for (int j = 0; j < resourcesToCommit.length; j++) {
           IResource resource = resourcesToCommit[j];
           if (set.contains(resource)) {
               return true;
           }
       }
       return false;
   }
    
}    


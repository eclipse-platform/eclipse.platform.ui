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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.subscribers.ChangeSet;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.*;

/**
 * This wizard page shows a preview of the commit operation and allows entering
 * a commit comment.
 */
public class CommitWizardCommitPage extends WizardPage implements IPropertyChangeListener {
    
    
    /**
     * Saves the size of the wizard dialog.
     */
    private class SettingsSaver {
        
        private static final String PREF_WEIGHT_A = "commit_wizard_page.weight_a"; //$NON-NLS-1$
        private static final String PREF_WEIGHT_B = "Commit_wizard_page.weigth_b"; //$NON-NLS-1$
        
        public void saveWeights(int a, int b) {
            final IDialogSettings settings= getSettings();
            if (settings == null)
                return;
            
            settings.put(PREF_WEIGHT_A, a);
            settings.put(PREF_WEIGHT_B, b);
        }
        
        public int [] loadWeights() {
            final int [] weights= { 1, 1 };
            final IDialogSettings settings= getSettings();
            if (settings == null)
                return weights;
            try {
                weights[0]= settings.getInt(PREF_WEIGHT_A);
                weights[1]= settings.getInt(PREF_WEIGHT_B);
            } catch (NumberFormatException e) {
            }
            return weights;
        }
        
        private IDialogSettings getSettings() {
            final IWizard wizard= getWizard();
            if (wizard == null)
                return null;
            return wizard.getDialogSettings();
        }
    }
    
    private final CommitCommentArea fCommentArea;
    private final SettingsSaver fSettingsSaver;
    
    private ISynchronizePageConfiguration fConfiguration;
    private SashForm fSashForm;
    
    protected final CommitWizard fWizard;
    
	private ParticipantPagePane fPagePane;
    
    public CommitWizardCommitPage(IResource [] resources, CommitWizard wizard) {
        
        super(Policy.bind("CommitWizardCommitPage.0")); //$NON-NLS-1$
        setTitle(Policy.bind("CommitWizardCommitPage.0")); //$NON-NLS-1$
        setDescription(Policy.bind("CommitWizardCommitPage.2")); //$NON-NLS-1$
        
        fSettingsSaver= new SettingsSaver();
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
        
        /**
         * A sash for the comment area and the changes.
         */
        fSashForm= new SashForm(composite, SWT.VERTICAL);
        fSashForm.setLayoutData(SWTUtils.createHVFillGridData());
        
        createCommentArea(fSashForm, converter);
        createChangesArea(fSashForm, converter);
                
        fSashForm.setWeights(fSettingsSaver.loadWeights());
        setControl(composite);
        
        fCommentArea.setFocus();
        validatePage(false);
    }
    
    private void createCommentArea(Composite parent, PixelConverter converter) {
        
        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_NONE));
        
        fCommentArea.createArea(composite);
        fCommentArea.getComposite().setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, convertHeightInCharsToPixels(8), SWT.FILL, SWT.FILL, true, true));
        fCommentArea.addPropertyChangeListener(this);
        
        createPlaceholder(composite);
    }
    
    private void createChangesArea(Composite parent, PixelConverter converter) {

        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_NONE));
        composite.setLayoutData(SWTUtils.createHVFillGridData());
        
        createPlaceholder(composite);
        
        final CommitWizardParticipant participant= fWizard.getParticipant();
        fConfiguration= participant.createPageConfiguration();
        fPagePane= new ParticipantPagePane(getShell(), true /* modal */, fConfiguration, participant);
        Control control = fPagePane.createPartControl(composite);
        control.setLayoutData(SWTUtils.createHVFillGridData());
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
		SyncInfoSet set = fConfiguration.getSyncInfoSet();
		if (set.hasConflicts()) {
			setErrorMessage(Policy.bind("CommitWizardCommitPage.4")); //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		if (set.isEmpty()) {
			// No need for a message as it should be obvious that there are no resources to commit
			setErrorMessage(null);
			setPageComplete(false);
			return;
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
                    setErrorMessage(Policy.bind("CommitWizardCommitPage.3")); //$NON-NLS-1$
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
        if (fConfiguration == null)
            return infos;
        
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


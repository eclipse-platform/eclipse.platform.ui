/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.subscribers.ChangeSet;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.actions.IgnoreAction;
import org.eclipse.team.internal.ccvs.ui.subscriber.*;
import org.eclipse.team.internal.ui.SWTUtils;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.*;

/**
 * This wizard page shows a preview of the commit operation and allows entering
 * a commit comment.
 */
public class CommitWizardCommitPage extends WizardPage implements IPropertyChangeListener {
    
    /**
     * The actions to be displayed in the context menu.
     */
    private class ActionContribution extends SynchronizePageActionGroup {
        
        public void initialize(ISynchronizePageConfiguration configuration) {
            super.initialize(configuration);
            appendToGroup(
                    ISynchronizePageConfiguration.P_CONTEXT_MENU, 
                    ISynchronizePageConfiguration.OBJECT_CONTRIBUTIONS_GROUP,
                    new CVSActionDelegateWrapper(new IgnoreAction(), configuration));
        }
        
        public void modelChanged(final ISynchronizeModelElement root) {
            super.modelChanged(root);
            Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updateForModelChange(root);
				}
			});
        }
    }
    
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
    
    /**
     * An extension of the standard label decorator which configures the keyword substitution 
     * mode according to the settings on the file type wizard page.
     */
    private class Decorator extends CVSParticipantLabelDecorator {
        
        public Decorator(ISynchronizePageConfiguration configuration) {
            super(configuration);
        }
        
        protected CVSDecoration getDecoration(IResource resource) throws CVSException {
            final CVSDecoration decoration= super.getDecoration(resource);
            if (fFileTypePage != null && resource instanceof IFile)
                decoration.setKeywordSubstitution(fFileTypePage.getOption((IFile)resource).getShortDisplayText());
            return decoration;
        }
    }
    
    /**
     * A participant that uses our decorator instead of the standard one.
     */
    private class Participant extends WorkspaceSynchronizeParticipant {
        public Participant(ISynchronizeScope scope) {
            super(scope);
        }
        protected ILabelDecorator getLabelDecorator(ISynchronizePageConfiguration configuration) {
            return new Decorator(configuration);
        }
        
        public ChangeSetCapability getChangeSetCapability() {
            return null; // we don't want that button
        }
        /* (non-Javadoc)
		 * @see org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
		 */
		protected void initializeConfiguration( ISynchronizePageConfiguration configuration) {
			super.initializeConfiguration(configuration);
	        configuration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] {ISynchronizePageConfiguration.LAYOUT_GROUP});
	        configuration.setProperty(ISynchronizePageConfiguration.P_CONTEXT_MENU, ISynchronizePageConfiguration.DEFAULT_CONTEXT_MENU);
	        configuration.addActionContribution(new ActionContribution());
	        
	        // Wrap the container so that we can update the enablements after the runnable
	        // (i.e. the container resets the state to what it was at the beginning of the
	        // run even if the state of the page changed. Remove from View changes the state)
	        configuration.setRunnableContext(new IRunnableContext() {
				public void run(boolean fork, boolean cancelable,
						IRunnableWithProgress runnable)
						throws InvocationTargetException, InterruptedException {
					getContainer().run(fork, cancelable, runnable);
					updateEnablements();
				}
			});
	        configuration.setSupportedModes(ISynchronizePageConfiguration.OUTGOING_MODE);
	        configuration.setMode(ISynchronizePageConfiguration.OUTGOING_MODE);
		}
		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#doesSupportSynchronize()
		 */
		public boolean doesSupportSynchronize() {
			return false;
		}
    }
    
    private final CommitCommentArea fCommentArea;
    private final IResource [] fResources;
    
    private final SettingsSaver fSettingsSaver;
    
    private ISynchronizePageConfiguration fConfiguration;
    private SashForm fSashForm;
    protected final CommitWizardFileTypePage fFileTypePage;
	private ParticipantPagePane fPagePane;
	private Participant fParticipant;
    
    public CommitWizardCommitPage(IResource [] resources, CommitWizardFileTypePage fileTypePage) {
        super(Policy.bind("CommitWizardCommitPage.0")); //$NON-NLS-1$
        setTitle(Policy.bind("CommitWizardCommitPage.0")); //$NON-NLS-1$
        setDescription(Policy.bind("CommitWizardCommitPage.2")); //$NON-NLS-1$
        
        fSettingsSaver= new SettingsSaver();
        fFileTypePage= fileTypePage;
        fCommentArea= new CommitCommentArea();
        fCommentArea.setProposedComment(getProposedComment(resources));
        if (resources.length > 0)
            fCommentArea.setProject(resources[0].getProject());
        fResources= resources;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        
        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        
        /**
         * A sash for the comment area and the changes.
         */
        fSashForm= new SashForm(composite, SWT.VERTICAL);
        fSashForm.setLayoutData(SWTUtils.createHVFillGridData());
        
        createCommentArea(fSashForm);
        createChangesArea(fSashForm);
                
        fSashForm.setWeights(fSettingsSaver.loadWeights());
        setControl(composite);
        
        fCommentArea.setFocus();
        validatePage(false);
    }
    
    private void createCommentArea(Composite parent) {
        
        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtils.createGridLayout(1, 0, 0));
        
        fCommentArea.createArea(composite);
        fCommentArea.getComposite().setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, convertHeightInCharsToPixels(8), SWT.FILL, SWT.FILL, true, true));
        fCommentArea.addPropertyChangeListener(this);
        
        createPlaceholder(composite);
    }
    
    private void createChangesArea(Composite parent) {

        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtils.createGridLayout(1, 0, 0));
        composite.setLayoutData(SWTUtils.createHVFillGridData());
        
        createPlaceholder(composite);
        
        fParticipant = new Participant(new ResourceScope(fResources));
        fConfiguration= fParticipant.createPageConfiguration();
        fPagePane= new ParticipantPagePane(getShell(), true /* modal */, fConfiguration, fParticipant);
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
		if (fParticipant != null)
			fParticipant.dispose();
	}
	
    private void createPlaceholder(final Composite composite) {
        final Composite placeholder= new Composite(composite, SWT.NONE);
        placeholder.setLayoutData(new GridData(SWT.DEFAULT, convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_SPACING) /3));
    }
    
    public String getComment() {
        return fCommentArea.getComment(true);
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
        fCommentArea.setFocus();
        fConfiguration.getPage().getViewer().refresh();
        expand();
    }
    
    protected void expand() {
        final Viewer viewer= fConfiguration.getPage().getViewer();
        if (viewer instanceof TreeViewer) {
            ((TreeViewer)viewer).expandAll();
        }
    }
    
	/*
	 * Expand the sync elements and update the page enablement
	 */
	protected void updateForModelChange(ISynchronizeModelElement root) {
		expand();
		updateEnablements();
	}
	
	private void updateEnablements() {
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


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

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.*;
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
        
        public void modelChanged(ISynchronizeModelElement root) {
            super.modelChanged(root);
            Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					expand();
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
    }
    
    private final CommitCommentArea fCommentArea;
    private final IResource [] fResources;
    
    private final SettingsSaver fSettingsSaver;
    
    private ISynchronizePageConfiguration fConfiguration;
    private SashForm fSashForm;
    protected final CommitWizardFileTypePage fFileTypePage;
    
    /**
     * 
     */
    public CommitWizardCommitPage(IResource [] resources, CommitWizardFileTypePage fileTypePage) {
        super(Policy.bind("CommitWizardCommitPage.0")); //$NON-NLS-1$
        setTitle(Policy.bind("CommitWizardCommitPage.0")); //$NON-NLS-1$
        setDescription(Policy.bind("CommitWizardCommitPage.2")); //$NON-NLS-1$
        
        fSettingsSaver= new SettingsSaver();
        fFileTypePage= fileTypePage;
        fCommentArea= new CommitCommentArea();
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
        
        if (fResources.length > 0)
            fCommentArea.setProject(fResources[0].getProject());
    }
    
    
    private void createChangesArea(Composite parent) {

        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtils.createGridLayout(1, 0, 0));
        composite.setLayoutData(SWTUtils.createHVFillGridData());
        
        createPlaceholder(composite);
        
        final WorkspaceSynchronizeParticipant participant = new Participant(new ResourceScope(fResources));
        fConfiguration= participant.createPageConfiguration();
        fConfiguration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] {ISynchronizePageConfiguration.LAYOUT_GROUP});
        fConfiguration.setProperty(ISynchronizePageConfiguration.P_CONTEXT_MENU, ISynchronizePageConfiguration.DEFAULT_CONTEXT_MENU);
        fConfiguration.addActionContribution(new ActionContribution());
        
        fConfiguration.setRunnableContext(getContainer());
        fConfiguration.setMode(ISynchronizePageConfiguration.OUTGOING_MODE);
        
        final ParticipantPagePane part= new ParticipantPagePane(getShell(), true /* modal */, fConfiguration, participant);
        Control control = part.createPartControl(composite);
        control.setLayoutData(SWTUtils.createHVFillGridData());
    }
    /**
     * @param composite
     */
    private void createPlaceholder(final Composite composite) {
        final Composite placeholder= new Composite(composite, SWT.NONE);
        placeholder.setLayoutData(new GridData(SWT.DEFAULT, convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_SPACING) /3));
    }
    
    /**
     * @return
     */
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
    
    /**
     * 
     */
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
    
    /**
     * 
     */
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
    
}    


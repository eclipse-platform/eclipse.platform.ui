/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.CVSDecoration;
import org.eclipse.team.internal.ccvs.ui.actions.IgnoreAction;
import org.eclipse.team.internal.ccvs.ui.subscriber.CVSActionDelegateWrapper;
import org.eclipse.team.internal.ccvs.ui.subscriber.CVSParticipantLabelDecorator;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * A participant that uses our decorator instead of the standard one.
 */
public class CommitWizardParticipant extends WorkspaceSynchronizeParticipant {
    
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
                    final CommitWizardCommitPage page= fWizard.getCommitPage();
                    if (page != null)
                        page.updateForModelChange();
                }
            });
        }
    }

    /**
     * An extension of the standard label decorator which configures the keyword substitution 
     * mode according to the settings on the file type wizard page.
     */
    private static class Decorator extends CVSParticipantLabelDecorator {
        
        private final CommitWizard fWizard;

        public Decorator(ISynchronizePageConfiguration configuration, CommitWizard wizard) {
            super(configuration);
            fWizard= wizard;
        }
        
        protected CVSDecoration getDecoration(IResource resource) throws CVSException {
            final CVSDecoration decoration= super.getDecoration(resource);
            final CommitWizardFileTypePage page= fWizard.getFileTypePage();
            
            if (page != null && resource instanceof IFile) 
                decoration.setKeywordSubstitution(page.getOption((IFile)resource).getShortDisplayText());
            return decoration;
        }
    }
    
    final CommitWizard fWizard;
    
    public CommitWizardParticipant(ISynchronizeScope scope, CommitWizard wizard) {
        super(scope);
        fWizard= wizard;
    }
    
    protected ILabelDecorator getLabelDecorator(ISynchronizePageConfiguration configuration) {
        return new Decorator(configuration, fWizard);
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
                fWizard.getContainer().run(fork, cancelable, runnable);
                final CommitWizardCommitPage page= fWizard.getCommitPage();
                if (page != null)
                    page.updateEnablements();
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

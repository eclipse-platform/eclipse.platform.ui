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
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IFileContentManager;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.ui.synchronize.ResourceScope;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;

/**
 * A wizard to commit the resources whose synchronization state is given in form
 * of a set of <code>SyncInfo</code>.
 */
public class CommitWizard extends ResizableWizard {
    
    /**
     * An operation to add and commit resources to a CVS repository.
     */
    private static class AddAndCommitOperation extends CVSOperation {
        
        private final IResource[] fAllResources;
        private final String fComment;
        
        private Map fModesForExtensionsForOneTime;
        private Map fModesForNamesForOneTime;
        
        private IResource[] fNewResources;
        
        private AddAndCommitOperation(IWorkbenchPart part, IResource[] allResources, String comment) {
            super(part);
            fNewResources = new IResource [0];
            fModesForExtensionsForOneTime = Collections.EMPTY_MAP;
            fModesForNamesForOneTime= Collections.EMPTY_MAP;
            fAllResources = allResources;
            fComment = comment;
        }
        
        public void setModesForExtensionsForOneTime(Map modes) {
            fModesForExtensionsForOneTime= modes;
        }
        
        public void setModesForNamesForOneTime(Map modes) {
            fModesForNamesForOneTime= modes;
        }
        
        public void setNewResources(IResource [] newResources) {
            this.fNewResources= newResources;
        }
        
        protected void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
            try {
                final AddOperation op= new AddOperation(getPart(), fNewResources);
                op.addModesForExtensions(fModesForExtensionsForOneTime);
                op.addModesForNames(fModesForNamesForOneTime);
                op.run(monitor);
                new CommitOperation(getPart(), fAllResources, new Command.LocalOption[0], fComment).run(monitor);
            } catch (InvocationTargetException e) {
                throw CVSException.wrapException(e);
            }
        }
        
        protected String getJobName() {
            return Policy.bind("CommitWizard.0"); //$NON-NLS-1$
        }
        
        protected String getTaskName() {
            return Policy.bind("CommitWizard.1"); //$NON-NLS-1$
        }
    }
    
    private final IResource[] fResources;
    private final SyncInfoSet fOutOfSyncInfos;
    private final SyncInfoSet fUnaddedInfos;
    private final CommitWizardParticipant fParticipant;
    
    private CommitWizardFileTypePage fFileTypePage;
    private CommitWizardCommitPage fCommitPage;
    
    public CommitWizard(SyncInfoSet infos) throws CVSException {
        this(infos.getResources());
    }
    
    public CommitWizard(final IResource [] resources) throws CVSException {
        
        super("CommitWizard", CVSUIPlugin.getPlugin().getDialogSettings());
        
        setWindowTitle(Policy.bind("CommitWizard.2")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_NEW_LOCATION));
        
        fResources= resources;
        fParticipant= new CommitWizardParticipant(new ResourceScope(fResources), this);
        
        SyncInfoSet infos = getAllOutOfSync(resources);
        fOutOfSyncInfos= new SyncInfoSet(infos.getNodes(new FastSyncInfoFilter.SyncInfoDirectionFilter(new int [] { SyncInfo.OUTGOING, SyncInfo.CONFLICTING })));
        fUnaddedInfos= getUnaddedInfos(fOutOfSyncInfos);
    }

	private SyncInfoSet getAllOutOfSync(final IResource[] resources) throws CVSException {
		final SubscriberSyncInfoCollector syncInfoCollector = fParticipant.getSubscriberSyncInfoCollector();
            try {
				Workbench.getInstance().getProgressService().run(true, true, new IRunnableWithProgress() {
				    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				    	monitor.beginTask("Collecting outgoing changes", IProgressMonitor.UNKNOWN);
				    	syncInfoCollector.waitForCollector(monitor);
				    	monitor.done();
				    }
				});
			} catch (InvocationTargetException e) {
				throw CVSException.wrapException(e);
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			} 
		return fParticipant.getSyncInfoSet();
	}
    
    public boolean hasOutgoingChanges() {
        return fOutOfSyncInfos.size() > 0;
    }
    
    public CommitWizardFileTypePage getFileTypePage() {
        return fFileTypePage;
    }

    public CommitWizardCommitPage getCommitPage() {
        return fCommitPage;
    }

    public CommitWizardParticipant getParticipant() {
        return fParticipant;
    }

    public boolean canFinish() {
        final IWizardPage current= getContainer().getCurrentPage();
        if (current == fFileTypePage && fCommitPage != null)
            return false;
        return super.canFinish();
    }

    public boolean performFinish() {
        
        final String comment= getComment();
        if (comment == null)
            return false;
        
        final SyncInfoSet infos= fCommitPage.getInfosToCommit();
        if (infos.size() == 0)
            return true;
        
        final SyncInfoSet unadded;
        try {
            unadded = getUnaddedInfos(infos);
        } catch (CVSException e1) {
            return false;
        }
        
        final AddAndCommitOperation operation= new AddAndCommitOperation(getPart(), infos.getResources(), comment);
        
        if (fFileTypePage != null) {
            final Map extensionsToSave= new HashMap();
            final Map extensionsNotToSave= new HashMap();
            
            fFileTypePage.getModesForExtensions(extensionsToSave, extensionsNotToSave);
            saveExtensionMappings(extensionsToSave);
            operation.setModesForExtensionsForOneTime(extensionsNotToSave);
            
            final Map namesToSave= new HashMap();
            final Map namesNotToSave= new HashMap();
            
            fFileTypePage.getModesForNames(namesToSave, namesNotToSave);
            saveNameMappings(namesToSave);
            operation.setModesForNamesForOneTime(namesNotToSave);
        }
        
        if (unadded.size() > 0) {
            operation.setNewResources(unadded.getResources());
        }
        
        try {
            operation.run();
        } catch (InvocationTargetException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        
        return super.performFinish();
    }

    public void addPages() {
        
        final Collection names= new ArrayList();
        final Collection extensions= new ArrayList();
        getUnknownNamesAndExtension(fUnaddedInfos, names, extensions);
        
        if (names.size() + extensions.size() > 0) {
            fFileTypePage= new CommitWizardFileTypePage(extensions, names); 
            addPage(fFileTypePage);
        }
        
        fCommitPage= new CommitWizardCommitPage(fResources, this);
        addPage(fCommitPage);
        
        super.addPages();
    }

    public void dispose() {
        fParticipant.dispose();
        super.dispose();
    }

    public static void run(Shell shell, IResource [] resources) throws CVSException {
        try {
			run(shell, new CommitWizard(resources));
		} catch (OperationCanceledException e) {
			// Ignore
		}
    }
    
    public static void run(Shell shell, SyncInfoSet infos) throws CVSException {
        try {
			run(shell, new CommitWizard(infos));
		} catch (OperationCanceledException e) {
			// Ignore
		}
    }

    private IWorkbenchPart getPart() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
    }
    
    private String getComment() {
        final String comment= fCommitPage.getComment();
        if (comment.length() == 0) {
            
            final IPreferenceStore store= CVSUIPlugin.getPlugin().getPreferenceStore();
            final String value= store.getString(ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS);
            
            if (MessageDialogWithToggle.NEVER.equals(value))
                return null;
            
            if (MessageDialogWithToggle.PROMPT.equals(value)) {
                
                final String title= Policy.bind("CommitWizard.3"); //$NON-NLS-1$
                final String message= Policy.bind("CommitWizard.4"); //$NON-NLS-1$
                final String toggleMessage= Policy.bind("CommitWizard.5"); //$NON-NLS-1$
                
                final MessageDialogWithToggle dialog= MessageDialogWithToggle.openYesNoQuestion(getShell(), title, message, toggleMessage, false, store, ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS);
                if (dialog.getReturnCode() == IDialogConstants.NO_ID) {
                    fCommitPage.setFocus();
                    return null;
                }
            }
        }
        return comment;
    }
    
    private static void run(Shell shell, CommitWizard wizard) {
        if (!wizard.hasOutgoingChanges()) {
            MessageDialog.openInformation(shell, Policy.bind("CommitWizard.6"), Policy.bind("CommitWizard.7")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            open(shell, wizard);
        }
    }

    private static void getUnknownNamesAndExtension(SyncInfoSet infos, Collection names, Collection extensions) {
        
        final IFileContentManager manager= Team.getFileContentManager();
        
        for (final Iterator iter = infos.iterator(); iter.hasNext();) {
            
            final SyncInfo info = (SyncInfo)iter.next();
            
            final String extension= info.getLocal().getFileExtension();
            if (extension != null && !manager.isKnownExtension(extension)) {
                extensions.add(extension);
            }
            
            final String name= info.getLocal().getName();
            if (extension == null && name != null && !manager.isKnownFilename(name))
                names.add(name);
        }
    }
    
    private static SyncInfoSet getUnaddedInfos(SyncInfoSet infos) throws CVSException {
        final SyncInfoSet unadded= new SyncInfoSet();        
        for (final Iterator iter = infos.iterator(); iter.hasNext();) {
            final SyncInfo info = (SyncInfo) iter.next();
            final IResource file= info.getLocal();
            if (!((file.getType() & IResource.FILE) == 0 || isAdded(file)))
                unadded.add(info);
        }
        return unadded;
    }
    
    private static void saveExtensionMappings(Map modesToPersist) {
        
        final String [] extensions= new String [modesToPersist.size()];
        final int [] modes= new int[modesToPersist.size()];
        
        int index= 0;
        for (Iterator iter= modesToPersist.keySet().iterator(); iter.hasNext();) {
            extensions[index]= (String) iter.next();
            modes[index]= ((Integer)modesToPersist.get(extensions[index])).intValue();
            ++index;
        }
        Team.getFileContentManager().addExtensionMappings(extensions, modes);
    }
    
    private static void saveNameMappings(Map modesToPersist) {
        
        final String [] names= new String [modesToPersist.size()];
        final int [] modes= new int[modesToPersist.size()];
        
        int index= 0;
        for (Iterator iter= modesToPersist.keySet().iterator(); iter.hasNext();) {
            names[index]= (String) iter.next();
            modes[index]= ((Integer)modesToPersist.get(names[index])).intValue();
            ++index;
        }
        Team.getFileContentManager().addNameMappings(names, modes);
    }
    
    private static boolean isAdded(IResource resource) throws CVSException {
        final ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
        if (cvsResource.isFolder()) {
            return ((ICVSFolder)cvsResource).isCVSFolder();
        }
        return cvsResource.isManaged();
    }
}    


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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IFileContentManager;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.core.mapping.IResourceMappingScopeManager;
import org.eclipse.team.core.mapping.provider.ResourceMappingScopeManager;
import org.eclipse.team.core.subscribers.SubscriberResourceMappingContext;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.ui.synchronize.ResourceScope;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

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
		private IJobChangeListener jobListener;
        
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
                final AddOperation op= new AddOperation(getPart(), RepositoryProviderOperation.asResourceMappers(fNewResources));
                op.addModesForExtensions(fModesForExtensionsForOneTime);
                op.addModesForNames(fModesForNamesForOneTime);
                op.run(monitor);
                new CommitOperation(getPart(), RepositoryProviderOperation.asResourceMappers(fAllResources), new Command.LocalOption[0], fComment).run(monitor);
            } catch (InvocationTargetException e) {
                throw CVSException.wrapException(e);
            }
        }
        
        protected String getJobName() {
            return CVSUIMessages.CommitWizard_0; 
        }
        
        protected String getTaskName() {
            return CVSUIMessages.CommitWizard_1; 
        }

        /*
         * Set the job listener. It will only recieve scheduled and done
         * events as these are what are used by a sync model operation
         * to show busy state in the sync view.
         */
		protected void setJobChangeListener(IJobChangeListener jobListener) {
			this.jobListener = jobListener;
		}
		
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void done(IJobChangeEvent event) {
			super.done(event);
			if (jobListener != null)
				jobListener.done(event);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void scheduled(IJobChangeEvent event) {
			super.scheduled(event);
			if (jobListener != null)
				jobListener.scheduled(event);
		}
    }
    
    private final IResource[] fResources;
    private final SyncInfoSet fOutOfSyncInfos;
    private final SyncInfoSet fUnaddedInfos;
    private final CommitWizardParticipant fParticipant;
    
    private CommitWizardFileTypePage fFileTypePage;
    private CommitWizardCommitPage fCommitPage;
	private IJobChangeListener jobListener;
	private IWorkbenchPart part;
    
    public CommitWizard(SyncInfoSet infos) throws CVSException {
        this(infos.getResources());
    }
    
    public CommitWizard(final IResource [] resources) throws CVSException {
        
        super(CVSUIMessages.CommitWizard_3, CVSUIPlugin.getPlugin().getDialogSettings()); 
        
        setWindowTitle(CVSUIMessages.CommitWizard_2); 
        setDefaultPageImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_NEW_LOCATION));
        
        fResources= resources;
        fParticipant= new CommitWizardParticipant(new ResourceScope(fResources), this);
        
        SyncInfoSet infos = getAllOutOfSync();
        fOutOfSyncInfos= new SyncInfoSet(infos.getNodes(new FastSyncInfoFilter.SyncInfoDirectionFilter(new int [] { SyncInfo.OUTGOING, SyncInfo.CONFLICTING })));
        fUnaddedInfos= getUnaddedInfos(fOutOfSyncInfos);
    }

	public CommitWizard(SyncInfoSet infos, IJobChangeListener jobListener) throws CVSException {
		this(infos);
		this.jobListener = jobListener;
	}

	public CommitWizard(Shell shell, IWorkbenchPart part, IResourceMappingScope scope) throws CVSException {
		// TODO: should use scope to create appropriate model sync
		this(scope.getRoots());
		this.part = part;
	}

	private SyncInfoSet getAllOutOfSync() throws CVSException {
		final SubscriberSyncInfoCollector syncInfoCollector = fParticipant.getSubscriberSyncInfoCollector();
            try {
				PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
				    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				    	monitor.beginTask(CVSUIMessages.CommitWizard_4, IProgressMonitor.UNKNOWN); 
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
        
        final String comment= fCommitPage.getComment(getShell());
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
        if (jobListener != null)
        	operation.setJobChangeListener(jobListener);
        
        if (fFileTypePage != null) {
            final Map extensionsToSave= new HashMap();
            final Map extensionsNotToSave= new HashMap();
            
            fFileTypePage.getModesForExtensions(extensionsToSave, extensionsNotToSave);
            CommitWizardFileTypePage.saveExtensionMappings(extensionsToSave);
            operation.setModesForExtensionsForOneTime(extensionsNotToSave);
            
            final Map namesToSave= new HashMap();
            final Map namesNotToSave= new HashMap();
            
            fFileTypePage.getModesForNames(namesToSave, namesNotToSave);
            CommitWizardFileTypePage.saveNameMappings(namesToSave);
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
        
        final Collection names= new HashSet();
        final Collection extensions= new HashSet();
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
    
    public static void run(Shell shell, SyncInfoSet infos, IJobChangeListener jobListener) throws CVSException {
        try {
			run(shell, new CommitWizard(infos, jobListener));
		} catch (OperationCanceledException e) {
			// Ignore
		}
    }

	public static void run(IWorkbenchPart part, Shell shell, ResourceMapping[] mappings) throws CVSException {
        try {
        	IResourceMappingScope scope = buildScope(part, mappings);
        	CommitWizard commitWizard = new CommitWizard(shell, part, scope);
			run(shell, commitWizard);
		} catch (OperationCanceledException e) {
			// Ignore
		} catch (InvocationTargetException e) {
			throw CVSException.wrapException(e);
		} catch (InterruptedException e) {
			// Ignore
		}
	}
	
    private static IResourceMappingScope buildScope(final IWorkbenchPart part, final ResourceMapping[] mappings) throws InvocationTargetException, InterruptedException {
    	final IResourceMappingScope[] scope = new IResourceMappingScope[] { null };
		PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				try {
					scope[0] = buildScope(part, mappings, monitor);
				} catch (CVSException e) {
					throw new InvocationTargetException(e);
				}
			}
		});
		return scope[0];
	}
    
    public static IResourceMappingScope buildScope(IWorkbenchPart part, ResourceMapping[] mappings, IProgressMonitor monitor) throws InterruptedException, CVSException {
		IResourceMappingScopeManager manager = new ResourceMappingScopeManager(mappings, 
				SubscriberResourceMappingContext.createContext(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber()), 
				true);
		try {
			manager.initialize(monitor);
			return manager.getScope();
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			if (manager != null)
				manager.dispose();
		}
    }

	private IWorkbenchPart getPart() {
    	if (part != null)
    		return part;
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
    }
    
    private static void run(Shell shell, CommitWizard wizard) {
        if (!wizard.hasOutgoingChanges()) {
            MessageDialog.openInformation(shell, CVSUIMessages.CommitWizard_6, CVSUIMessages.CommitWizard_7); // 
        } else {
            open(shell, wizard);
        }
    }

    private static void getUnknownNamesAndExtension(SyncInfoSet infos, Collection names, Collection extensions) {
        
        final IFileContentManager manager= Team.getFileContentManager();
        
        for (final Iterator iter = infos.iterator(); iter.hasNext();) {
            
            final SyncInfo info = (SyncInfo)iter.next();
            
            IResource local = info.getLocal();
            if (local instanceof IFile && manager.getType((IFile)local) == Team.UNKNOWN) {
                final String extension= local.getFileExtension();
                if (extension != null && !manager.isKnownExtension(extension)) {
                    extensions.add(extension);
                }
                
                final String name= local.getName();
                if (extension == null && name != null && !manager.isKnownFilename(name))
                    names.add(name);
            }
        }
    }
    
    private static SyncInfoSet getUnaddedInfos(SyncInfoSet infos) throws CVSException {
        final SyncInfoSet unadded= new SyncInfoSet();        
        for (final Iterator iter = infos.iterator(); iter.hasNext();) {
            final SyncInfo info = (SyncInfo) iter.next();
            final IResource file= info.getLocal();
            if (!isAdded(file))
                unadded.add(info);
        }
        return unadded;
    }
    
    private static boolean isAdded(IResource resource) throws CVSException {
        final ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
        if (cvsResource.isFolder()) {
            return ((ICVSFolder)cvsResource).isCVSFolder();
        }
        return cvsResource.isManaged();
    }

}    


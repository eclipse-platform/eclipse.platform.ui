/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui.operations;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.AnnotateListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.*;

/**
 * An operation to fetch the annotations for a file from the repository and
 * display them in the annotations view.
 */
public class ShowAnnotationOperation extends CVSOperation {
    
    final private ICVSResource fCVSResource;
    final private String fRevision;
    private final boolean binary;

    public ShowAnnotationOperation(IWorkbenchPart part, ICVSResource cvsResource, String revision, boolean binary) {
        super(part);
        fCVSResource= cvsResource;
        fRevision= revision;
        this.binary = binary;

    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
        
        monitor.beginTask(null, 100);
        
        // Get the annotations from the repository.
        final AnnotateListener listener= new AnnotateListener();
        fetchAnnotation(listener, fCVSResource, fRevision, Policy.subMonitorFor(monitor, 80));
        try {
    	    if (hasCharset(fCVSResource, listener.getContents())) {
    	        listener.setContents(getRemoteContents(fCVSResource, Policy.subMonitorFor(monitor, 20)));
    	    }
    	} catch (CoreException e) {
    	    // Log and continue, using the original fetched contents
    	    CVSUIPlugin.log(e);
    	}
    	
    	// Open the view and display it from the UI thread.
    	final Display display= getPart().getSite().getShell().getDisplay();
    	display.asyncExec(new Runnable() {
    	    public void run() { showView(listener); }
    	});
    	monitor.done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
     */
    protected String getTaskName() {
        return Policy.bind("ShowAnnotationOperation.taskName"); //$NON-NLS-1$
    }

	protected boolean hasCharset(ICVSResource cvsResource, InputStream contents) {
		try {
			return TeamPlugin.getCharset(cvsResource.getName(), contents) != null;
		} catch (IOException e) {
			// Assume that the contents do have a charset
			return true;
		}
	}

	/**
	 * Shows the view once the background operation is finished. This must be called
	 * from the UI thread.
	 * 
	 * @param listener The listener with the results.
	 */
    protected void showView(final AnnotateListener listener) {
        final IWorkbench workbench= PlatformUI.getWorkbench();
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        
		final String defaultPerspectiveID= promptForPerspectiveSwitch();

		if (defaultPerspectiveID != null) {
			try {
				workbench.showPerspective(defaultPerspectiveID, window);
			} catch (WorkbenchException e) {
				Utils.handleError(window.getShell(), e, Policy.bind("ShowAnnotationOperation.0"), e.getMessage()); //$NON-NLS-1$
			}
		}
       
        try {
            final AnnotateView view = AnnotateView.openInActivePerspective();
            view.showAnnotations(fCVSResource, listener.getCvsAnnotateBlocks(), listener.getContents());
        } catch (PartInitException e) {
            CVSUIPlugin.log(e);
        } catch (CVSException e) {
            CVSUIPlugin.log(e);
        }
    }
    

    private void fetchAnnotation(AnnotateListener listener, ICVSResource cvsResource, String revision, IProgressMonitor monitor) throws CVSException {
    
        monitor = Policy.monitorFor(monitor);
        monitor.beginTask(null, 100);
        
        final ICVSFolder folder = cvsResource.getParent();
        final FolderSyncInfo info = folder.getFolderSyncInfo();
        final ICVSRepositoryLocation location = KnownRepositories.getInstance().getRepository(info.getRoot());
        
        final Session session = new Session(location, folder, true /*output to console*/);
        session.open(Policy.subMonitorFor(monitor, 10), false /* read-only */);
        try {
            final Command.QuietOption quietness = CVSProviderPlugin.getPlugin().getQuietness();
            try {
                CVSProviderPlugin.getPlugin().setQuietness(Command.VERBOSE);
                List localOptions = new ArrayList();
                if (revision != null) {
                    localOptions.add(Annotate.makeRevisionOption(revision));
                }
                if (binary) {
                    localOptions.add(Annotate.FORCE_BINARY_ANNOTATE);
                }
                final IStatus status = Command.ANNOTATE.execute(session, Command.NO_GLOBAL_OPTIONS, (LocalOption[]) localOptions.toArray(new LocalOption[localOptions.size()]), new ICVSResource[]{cvsResource}, listener, Policy.subMonitorFor(monitor, 90));
                if (status.getCode() == CVSStatus.SERVER_ERROR) {
                    throw new CVSServerException(status);
                }
            } finally {
                CVSProviderPlugin.getPlugin().setQuietness(quietness);
                monitor.done();
            }
        } finally {
            session.close();
        }
    }

    private InputStream getRemoteContents(ICVSResource resource, IProgressMonitor monitor) throws CoreException {
        
    	final ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(resource);
    	if (remote == null) {
    		return new ByteArrayInputStream(new byte[0]);
    	}
    	final IStorage storage = ((IResourceVariant)remote).getStorage(monitor);
    	if (storage == null) {
    		return new ByteArrayInputStream(new byte[0]);
    	}
    	return storage.getContents();
    }
    
    /**
     * @return The ID of the perspective if the perspective needs to be changed,
     * null otherwise.
     */
	private String promptForPerspectiveSwitch() {
		// check whether we should ask the user.
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		final String option = store.getString(ICVSUIConstants.PREF_CHANGE_PERSPECTIVE_ON_SHOW_ANNOTATIONS);
		final String desiredID = store.getString(ICVSUIConstants.PREF_DEFAULT_PERSPECTIVE_FOR_SHOW_ANNOTATIONS);
		
		if (option.equals(MessageDialogWithToggle.ALWAYS))
			return desiredID; // no, always switch
		
		if (option.equals(MessageDialogWithToggle.NEVER))
			return null; // no, never switch
		
		// Check whether the desired perspective is already active.
		final IPerspectiveRegistry registry= PlatformUI.getWorkbench().getPerspectiveRegistry();
		final IPerspectiveDescriptor desired = registry.findPerspectiveWithId(desiredID);
		final IWorkbenchPage page = CVSUIPlugin.getActivePage();
		
		if (page != null) {
			final IPerspectiveDescriptor current = page.getPerspective();
			if (current != null && current.getId().equals(desiredID)) {
				return null; // it is active, so no prompt and no switch
			}
		}
		
		if (desired != null) {
		    
		    // Ask the user whether to switch
			final MessageDialogWithToggle m = MessageDialogWithToggle.openYesNoQuestion(
			        Utils.getShell(null),
			        Policy.bind("ShowAnnotationOperation.1"), //$NON-NLS-1$
			        Policy.bind("ShowAnnotationOperation.2", desired.getLabel()), //$NON-NLS-1$
			        Policy.bind("ShowAnnotationOperation.4"),   //$NON-NLS-1$
			        false /* toggle state */,
			        store,
			        ICVSUIConstants.PREF_CHANGE_PERSPECTIVE_ON_SHOW_ANNOTATIONS);
			
			final int result = m.getReturnCode();
			switch (result) {
			// yes
			case IDialogConstants.YES_ID:
			case IDialogConstants.OK_ID :
			    return desiredID;
			// no
			case IDialogConstants.NO_ID :
			    return null;
			}
		}
		return null;
	}
}

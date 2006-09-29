/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
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
import org.eclipse.team.ui.history.*;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import com.ibm.icu.text.DateFormat;

/**
 * An operation to fetch the annotations for a file from the repository and
 * display them in the annotations view.
 */
public class ShowAnnotationOperation extends CVSOperation {
    
    private final ICVSResource fCVSResource;
    private final String fRevision;
    private final boolean fBinary;

    public ShowAnnotationOperation(IWorkbenchPart part, ICVSResource cvsResource, String revision, boolean binary) {
        super(part);
        fCVSResource= cvsResource;
        fRevision= revision;
        fBinary = binary;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {

    	final boolean useLiveAnnotate= !fBinary && isKnownUseLiveAnnotate();
    	final boolean useView= fBinary || isKnownUseView();
    	
		monitor.beginTask(null, 80 + (useView ? 0 : 20) + (useLiveAnnotate ? 0 : 20));

		// Get the annotations from the repository.
		final AnnotateListener listener= new AnnotateListener();
		fetchAnnotation(listener, fCVSResource, fRevision, Policy.subMonitorFor(monitor, 80));

		// If we're not using live annotate or the file is a remote file,
		// we need to fetch the contents to ensure the encoding is correct
		// (i.e. the contents from the annotate command will only work if the file is ASCII).
		if (!useLiveAnnotate || fCVSResource.getIResource() == null)
			fetchContents(listener, Policy.subMonitorFor(monitor, 20));

		// this is not needed if there is no live annotate
		final RevisionInformation information;
		if (!useView)
			information= createRevisionInformation(listener, Policy.subMonitorFor(monitor, 20));
		else
			information= null;

		// Open the view and display it from the UI thread.
		final Display display= getPart().getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				//If the file being annotated is not binary, check to see if we can use the quick
				//annotate. Until we are able to show quick diff annotate on read only text editors
				//we can't make use of it for binary files/remote files.
				boolean useQuickDiffAnnotate = !fBinary && promptForQuickDiffAnnotate();
				if (useQuickDiffAnnotate){
					try {
						//is there an open editor for the given input? If yes, use live annotate
						final AbstractDecoratedTextEditor editor= getEditor(listener);
						if (editor != null) {
							editor.showRevisionInformation(information, "org.eclipse.quickdiff.providers.CVSReferenceProvider"); //$NON-NLS-1$
							final IWorkbenchPage page= getPart().getSite().getPage();
							showHistoryView(page);
						}
					} catch (PartInitException e) {
						CVSException.wrapException(e);
					}
				} else
					showView(listener);	
			}
		});
		
		monitor.done();
	}
    
    /**
     * Shows the history view, creating it if necessary, but does not give it focus.
     * 
     * @param page the workbench page to operate in
     * @return the history view
     * @throws PartInitException
     */
    private IHistoryView showHistoryView(IWorkbenchPage page) throws PartInitException {
    	final IHistoryView historyView= (IHistoryView)page.showView(IHistoryView.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
    	historyView.showHistoryFor(fCVSResource.getIResource());
    	IHistoryPage historyPage = historyView.getHistoryPage();
    	if (historyPage instanceof CVSHistoryPage){
    		CVSHistoryPage cvsHistoryPage = (CVSHistoryPage) historyPage;
    		cvsHistoryPage.setMode(CVSHistoryPage.REMOTE_MODE);
    		// We need to call link to ensure that the history page gets linked
			// even if the page input did not change
    		cvsHistoryPage.linkWithEditor();
    	}
    	return historyView;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#getTaskName()
     */
    protected String getTaskName() {
        return CVSUIMessages.ShowAnnotationOperation_taskName; 
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
				Utils.handleError(window.getShell(), e, CVSUIMessages.ShowAnnotationOperation_0, e.getMessage()); 
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

	private AbstractDecoratedTextEditor getEditor(AnnotateListener listener) throws PartInitException {
		IResource resource= fCVSResource.getIResource();	
		if (resource instanceof IFile){
			return RevisionAnnotationController.openEditor(getPart().getSite().getPage(), (IFile)resource);
		}
		if (fCVSResource instanceof ICVSRemoteResource) {
			return RevisionAnnotationController.openEditor(getPart().getSite().getPage(), fCVSResource, new RemoteAnnotationStorage((ICVSRemoteFile)fCVSResource, listener.getContents()));
		}
        return null;
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
                if (fBinary) {
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

    private RevisionInformation createRevisionInformation(final AnnotateListener listener, IProgressMonitor monitor) throws CVSException {
	    Map logEntriesByRevision= new HashMap();
		if (fCVSResource instanceof ICVSFile) {
			try {
				ILogEntry[] logEntries= ((ICVSFile) fCVSResource).getLogEntries(monitor);
				for (int i= 0; i < logEntries.length; i++) {
					ILogEntry entry= logEntries[i];
					logEntriesByRevision.put(entry.getRevision(), entry);
				}
			} catch (CVSException e) {
				throw e;
			} catch (TeamException e) {
				// XXX why does getLogEntries throw TeamException?
				throw new CVSException("", e); //$NON-NLS-1$
			}
		}

		final CommitterColors colors= CommitterColors.getDefault();
		RevisionInformation info= new RevisionInformation();
		HashMap sets= new HashMap();
		List annotateBlocks= listener.getCvsAnnotateBlocks();
		for (Iterator blocks= annotateBlocks.iterator(); blocks.hasNext();) {
			final CVSAnnotateBlock block= (CVSAnnotateBlock) blocks.next();
			final String revisionString= block.getRevision();
			Revision revision= (Revision) sets.get(revisionString);
			if (revision == null) {
				final ILogEntry entry= (ILogEntry) logEntriesByRevision.get(revisionString);
				if (entry == null)
					continue;
				
				revision= new Revision() {
					private String fCommitter= null;
					
					public Object getHoverInfo() {
						if (entry != null)
							return "<b>" + entry.getAuthor() + " " + entry.getRevision() + " " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(entry.getDate()) + "</b><p>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							entry.getComment() + "</p>"; //$NON-NLS-1$
						return block.toString().substring(0, block.toString().indexOf(" (")); //$NON-NLS-1$
					}
					
					private String getCommitterId() {
						if (fCommitter == null)
							fCommitter= block.toString().substring(0, block.toString().indexOf(' '));
						return fCommitter;
					}
					
					public String getId() {
						return revisionString;
					}
					
					public Date getDate() {
						return entry.getDate();
					}
					
					public RGB getColor() {
						return colors.getCommitterRGB(getCommitterId());
					}

					public String getAuthor() {
						return getCommitterId();
					}
				};
				sets.put(revisionString, revision);
				info.addRevision(revision);
			}
			revision.addRange(new LineRange(block.getStartLine(), block.getEndLine() - block.getStartLine() + 1));
		}
		
		return info;
	}
    
	private void fetchContents(final AnnotateListener listener, IProgressMonitor monitor) {
		try {
			if (hasCharset(fCVSResource, listener.getContents())) {
				listener.setContents(getRemoteContents(fCVSResource, monitor));
			}
		} catch (CoreException e) {
			// Log and continue, using the original fetched contents
			CVSUIPlugin.log(e);
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
		    
			String message;;
			String desc = desired.getDescription();
			if (desc == null) {
				message = NLS.bind(CVSUIMessages.ShowAnnotationOperation_2, new String[] { desired.getLabel() });
			} else {
				message = NLS.bind(CVSUIMessages.ShowAnnotationOperation_3, new String[] { desired.getLabel(), desc });
			}
		    // Ask the user whether to switch
			final MessageDialogWithToggle m = MessageDialogWithToggle.openYesNoQuestion(
			        Utils.getShell(null),
			        CVSUIMessages.ShowAnnotationOperation_1, 
			        message, 
			        CVSUIMessages.ShowAnnotationOperation_4,   
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
	
	private boolean isKnownUseView() {
		//check whether we should ask the user.
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		final String option = store.getString(ICVSUIConstants.PREF_USE_QUICKDIFFANNOTATE);
		
		return option.equals(MessageDialogWithToggle.NEVER);
	}
	
	private boolean isKnownUseLiveAnnotate() {
		//check whether we should ask the user.
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		final String option = store.getString(ICVSUIConstants.PREF_USE_QUICKDIFFANNOTATE);
		
		return option.equals(MessageDialogWithToggle.ALWAYS);
	}
	
	/**
	 * Returns true if the user wishes to always use the live annotate view, false otherwise.
	 * @return
	 */
	private boolean promptForQuickDiffAnnotate(){
		//check whether we should ask the user.
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		final String option = store.getString(ICVSUIConstants.PREF_USE_QUICKDIFFANNOTATE);
		
		if (option.equals(MessageDialogWithToggle.ALWAYS))
			return true; //use live annotate
		else if (option.equals(MessageDialogWithToggle.NEVER))
			return false; //don't use live annotate
		
		final MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(Utils.getShell(null), CVSUIMessages.ShowAnnotationOperation_QDAnnotateTitle,
				CVSUIMessages.ShowAnnotationOperation_QDAnnotateMessage,CVSUIMessages.ShowAnnotationOperation_4, false, store, ICVSUIConstants.PREF_USE_QUICKDIFFANNOTATE);
		
		final int result = dialog.getReturnCode();
		switch (result) {
			//yes
			case IDialogConstants.YES_ID:
			case IDialogConstants.OK_ID :
			    return true;
		}
		return false;
	}
}

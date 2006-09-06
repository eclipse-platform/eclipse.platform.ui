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
import org.eclipse.jface.text.revisions.*;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
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
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import com.ibm.icu.text.DateFormat;

/**
 * An operation to fetch the annotations for a file from the repository and
 * display them in the annotations view.
 */
public class ShowAnnotationOperation extends CVSOperation {
    
    final private ICVSResource fCVSResource;
    final private String fRevision;
    private final boolean binary;
	private Map fUIRevisionsById;

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

		monitor.beginTask(null, 120);

		// Get the annotations from the repository.
		final AnnotateListener listener= new AnnotateListener();
		fetchAnnotation(listener, fCVSResource, fRevision, Policy.subMonitorFor(monitor, 80));

		// this is not needed if there is a live editor - but we don't know, and the user might have closed the live editor since...
		fetchContents(listener, Policy.subMonitorFor(monitor, 20));

		// this is not needed if there is no live annotate
		final RevisionInformation information= createRevisionInformation(listener, Policy.subMonitorFor(monitor, 20));

		// Open the view and display it from the UI thread.
		final Display display= getPart().getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				boolean useQuickDiffAnnotate = false;
				
				//If the file being annotated is not binary AND is not a RemoteFile, check to see if we can use the quick
				//annotate. Until we are able to show quick diff annotate on read only text editors
				//we can't make use of it for binary files/remote files.
				if (!binary && !(fCVSResource instanceof ICVSRemoteFile))
					useQuickDiffAnnotate = promptForQuickDiffAnnotate();
				
				if (useQuickDiffAnnotate){
					//is there an open editor for the given input? If yes, use live annotate
					final AbstractDecoratedTextEditor editor= getEditor();
					if (editor != null){
						editor.showRevisionInformation(information, "org.eclipse.quickdiff.providers.CVSReferenceProvider"); //$NON-NLS-1$
						final IWorkbenchPage page= getPart().getSite().getPage();
						final GenericHistoryView historyView;
						try {
							historyView= (GenericHistoryView) page.showView(IHistoryView.VIEW_ID);
							historyView.showHistoryFor(fCVSResource.getIResource());
							IHistoryPage historyPage = historyView.getHistoryPage();
							if (historyPage instanceof CVSHistoryPage){
								CVSHistoryPage cvsHistoryPage = (CVSHistoryPage) historyPage;
								cvsHistoryPage.setMode(CVSHistoryPage.REMOTE_MODE);
							}
							IRevisionRulerColumn column= (IRevisionRulerColumn) editor.getAdapter(IRevisionRulerColumn.class);
							final ISelectionProvider provider;
							if (column instanceof IRevisionRulerColumnExtension) {
								IRevisionRulerColumnExtension ext= (IRevisionRulerColumnExtension) column;
								provider= ext.getRevisionSelectionProvider();
							} else {
								provider= null;
							}
							if (provider != null) {
								final ISelectionChangedListener selectionListener= new ISelectionChangedListener() {
									public void selectionChanged(SelectionChangedEvent event) {
										GenericHistoryView historyView= (GenericHistoryView) page.findView(IHistoryView.VIEW_ID);
										if (historyView == null)
											return; // user has manually closed the view after it being originally open -> don't reopen

										ISelection selection= event.getSelection();
										Revision selected= null;
										if (selection instanceof IStructuredSelection)
											selected= (Revision) ((IStructuredSelection) selection).getFirstElement();

										if (selected == null)
											return;

										IHistoryPage historyPage = historyView.getHistoryPage();
										if (historyPage instanceof CVSHistoryPage){
											CVSHistoryPage cvsHistoryPage = (CVSHistoryPage) historyPage;
											IPath fileInHistoryViewPath = cvsHistoryPage.getFilePath();
											IPath annotateFilePath = fCVSResource.getIResource().getFullPath();
											if (fileInHistoryViewPath != null && annotateFilePath != null &&
												fileInHistoryViewPath.equals(annotateFilePath))
												cvsHistoryPage.selectRevision(selected.getId());
										}
									}
								};
								provider.addSelectionChangedListener(selectionListener);
								final ISelectionChangedListener viewSelectionListener= new ISelectionChangedListener() {
									public void selectionChanged(SelectionChangedEvent event) {
										ISelection selection= event.getSelection();
										ISelection uiSelection= StructuredSelection.EMPTY;
										if (selection instanceof IStructuredSelection) {
											Object first= ((IStructuredSelection) selection).getFirstElement();
											if (first instanceof IFileRevision) {
												IFileRevision revision= (IFileRevision) first;
												if (fUIRevisionsById != null) {
													Revision rev= (Revision) fUIRevisionsById.get(revision.getContentIdentifier());
													if (rev != null) {
														uiSelection= new StructuredSelection(rev);
													}
												}
											}
										}
										provider.setSelection(uiSelection);
									}
								};
								final TreeViewer viewer;
								if (historyPage instanceof CVSHistoryPage) {
									viewer= ((CVSHistoryPage) historyPage).getTreeViewer();
									viewer.addSelectionChangedListener(viewSelectionListener);
								} else {
									viewer= null;
								}
								page.addPartListener(new IPartListener() {

									public void partOpened(IWorkbenchPart part) {
									}

									public void partDeactivated(IWorkbenchPart part) {
									}

									public void partClosed(IWorkbenchPart part) {
										if (part == historyView || part == editor) {
											page.removePartListener(this);
											provider.removeSelectionChangedListener(selectionListener);
											if (viewer != null)
												viewer.removeSelectionChangedListener(viewSelectionListener);
										}
									}

									public void partBroughtToTop(IWorkbenchPart part) {
									}

									public void partActivated(IWorkbenchPart part) {
									}

								});
							}
						} catch (PartInitException e) {
							CVSException.wrapException(e);
						}
					}
				} else
					showView(listener);	
			}
		});
		monitor.done();
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

	private AbstractDecoratedTextEditor getEditor() {
        final IWorkbench workbench= PlatformUI.getWorkbench();
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        IEditorReference[] references= window.getActivePage().getEditorReferences();
        IResource resource= fCVSResource.getIResource();
		if (resource == null)
			return null;

		for (int i= 0; i < references.length; i++) {
			IEditorReference reference= references[i];
			try {
				if (resource != null && resource.equals(reference.getEditorInput().getAdapter(IFile.class))) {
					IEditorPart editor= reference.getEditor(false);
					if (editor instanceof AbstractDecoratedTextEditor)
						return (AbstractDecoratedTextEditor) editor;
					else {
						//editor opened is not a text editor - reopen file using the default text editor
						IEditorPart part = getPart().getSite().getPage().openEditor(new FileEditorInput((IFile) resource), IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID, true, IWorkbenchPage.MATCH_NONE);
						if (part != null && part instanceof AbstractDecoratedTextEditor)
							return (AbstractDecoratedTextEditor)part;
					}
				}
			} catch (PartInitException e) {
				// ignore
			}
		}
		
		//no existing editor references found, try to open a new editor for the file	
		if (resource instanceof IFile){
			try {
				IEditorDescriptor descrptr = IDE.getEditorDescriptor((IFile) resource);
				//try to open the associated editor only if its an internal editor
				if (descrptr.isInternal()){
					IEditorPart part = IDE.openEditor(getPart().getSite().getPage(), (IFile) resource);
					if (part instanceof AbstractDecoratedTextEditor)
						return (AbstractDecoratedTextEditor)part;
					
					//editor opened is not a text editor - close it
					getPart().getSite().getPage().closeEditor(part, false);
				}
				//open file in default text editor	
				IEditorPart part = IDE.openEditor(getPart().getSite().getPage(), (IFile) resource, IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
				if (part != null && part instanceof AbstractDecoratedTextEditor)
					return (AbstractDecoratedTextEditor)part;
				
			} catch (PartInitException e) {
			}
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

    private RevisionInformation createRevisionInformation(final AnnotateListener listener, IProgressMonitor monitor) throws CVSException {
	    Map logEntriesByRevision= new HashMap();
		if (fCVSResource instanceof ICVSFile) {
			try {
				ILogEntry[] logEntries= ((ICVSFile) fCVSResource).getLogEntries(Policy.subMonitorFor(monitor, 20));
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
		
		fUIRevisionsById= sets;
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

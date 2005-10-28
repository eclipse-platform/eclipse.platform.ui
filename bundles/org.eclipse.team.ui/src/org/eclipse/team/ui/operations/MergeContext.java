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
package org.eclipse.team.ui.operations;

import java.io.*;
import java.util.*;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamMerger;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.mapping.*;

/**
 * Provides the context for an <code>IResourceMappingMerger</code>.
 * It provides access to the ancestor and remote resource mapping contexts
 * so that resource mapping mergers can attempt head-less auto-merges.
 * The ancestor context is only required for merges while the remote
 * is required for both merge and replace.
 * 
 * TODO: Need to have a story for folder merging (see bug 113898)
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IResourceMappingMerger
 * @since 3.2
 */
public abstract class MergeContext extends SynchronizationContext implements IMergeContext {
	
	private static final String TXT_EXTENTION = "txt"; //$NON-NLS-1$

	/**
     * Create a merge context.
	 * @param type 
     */
    protected MergeContext(IResourceMappingScope input, String type, SyncInfoTree tree) {
    	super(input, type, tree);
    }
    
    /**
     * Method that allows the model merger to signal that the file in question
     * has been completely merged. Model mergers can call this method if they
     * have transfered all changes from a remote file to a local file and wish
     * to signal that the merge is done.This will allow repository providers to
     * update the synchronization state of the file to reflect that the file is
     * up-to-date with the repository.
     * 
     * @param file the file that has been merged
     * @param monitor a progress monitor
     * @return a status indicating the results of the operation
     */
    public abstract IStatus markAsMerged(IFile file, IProgressMonitor monitor);

    /**
	 * Method that can be called by the model merger to attempt a file-system
	 * level merge. This is useful for cases where the model merger does not
	 * need to do any special processing to perform the merge. By default, this
	 * method attempts to use an appropriate <code>IStreamMerger</code> to
	 * merge the files covered by the provided traversals. If a stream merger
	 * cannot be found, the text merger is used. If this behavior is not
	 * desired, sub-classes may override this method.
	 * <p>
	 * This method does a best-effort attempt to merge all the files covered
	 * by the provided traversals. Files that could not be merged will be 
	 * indicated in the returned status. If the status returned has the code
	 * <code>MergeStatus.CONFLICTS</code>, the list of failed files can be 
	 * obtained by calling the <code>MergeStatus#getConflictingFiles()</code>
	 * method.
	 * <p>
	 * Any resource changes triggered by this merge will be reported through the 
	 * resource delta mechanism and the sync-info tree associated with this context.
	 * 
	 * @see SyncInfoSet#addSyncSetChangedListener(ISyncInfoSetChangeListener)
	 * @see org.eclipse.core.resources.IWorkspace#addResourceChangeListener(IResourceChangeListener)
	 * 
	 * @param infos
	 *            the array of sync info to be merged
	 * @param monitor
	 *            a progress monitor
	 * @return a status indicating success or failure. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that the file
	 *         contain non-mergable conflicts and must be merged manually.
     * @throws CoreException if an error occurs
	 */
    public IStatus merge(SyncInfoSet infos, IProgressMonitor monitor) throws CoreException {
		List failedFiles = new ArrayList();
		for (Iterator iter = infos.iterator(); iter.hasNext();) {
			SyncInfo info = (SyncInfo) iter.next();
			IStatus s = merge(info, monitor);
			if (!s.isOK()) {
				if (s.getCode() == IMergeStatus.CONFLICTS) {
					failedFiles.addAll(Arrays.asList(((IMergeStatus)s).getConflictingFiles()));
				} else {
					return s;
				}
			}
		}
		if (failedFiles.isEmpty()) {
			return Status.OK_STATUS;
		} else {
			return new MergeStatus(TeamPlugin.ID, "Could not merge all files", (IFile[]) failedFiles.toArray(new IFile[failedFiles.size()]));
		}
    }
    
    /**
     * Method that can be called by the model merger to attempt a file level
     * merge. This is useful for cases where the model merger does not need to
     * do any special processing to perform the merge. By default, this method
     * attempts to use an appropriate <code>IStreamMerger</code> to perform the
     * merge. If a stream merger cannot be found, the text merger is used. If this behavior
     * is not desired, sub-classes may override this method.
     * 
     * @param file the file to be merged
     * @param monitor a progress monitor
     * @return a status indicating success or failure. A code of
     *         <code>MergeStatus.CONFLICTS</code> indicates that the file contain
     *         non-mergable conflicts and must be merged manually.
     * @see org.eclipse.team.ui.operations.MergeContext#merge(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus merge(SyncInfo info, IProgressMonitor monitor) {
		IResource r = info.getLocal();
		if (r.getType() != IResource.FILE)
			return Status.OK_STATUS;
		IFile file = (IFile)r;
        try {
        	if (info.getComparator().isThreeWay()) {
	        	int direction = SyncInfo.getDirection(info.getKind());
	    		if (direction == SyncInfo.OUTGOING) {
	        		// There's nothing to do so return OK
	        		return Status.OK_STATUS;
	        	}
	    		if (direction == SyncInfo.INCOMING) {
	    			// Just copy the stream since there are no conflicts
	    			return performReplace(info, monitor);
	    		}
				// direction == SyncInfo.CONFLICTING
	    		int type = SyncInfo.getChange(info.getKind());
	    		if (type == SyncInfo.DELETION) {
	    			// Nothing needs to be done although subclasses
	    			markAsMerged(file, monitor);
	    			return Status.OK_STATUS;
	    		}
				// type == SyncInfo.CHANGE
				IResourceVariant base = info.getBase();
	        	IResourceVariant remote = info.getRemote();
				if (base == null || remote == null || !file.exists()) {
					// Nothing we can do so return a conflict status
					// TODO: Should we handle the case where the local and remote have the same contents for a conflicting addition?
					return new MergeStatus(TeamUIPlugin.ID, NLS.bind("Conflicting change could not be merged: {0}", new String[] { file.getFullPath().toString() }), new IFile[] { file });
				}
				// We have a conflict, a local, base and remote so we can do 
				// a three-way merge
	            return performThreeWayMerge(info, monitor);
        	} else {
        		return performReplace(info, monitor);
        	}
        } catch (CoreException e) {
            return new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, NLS.bind("Merge of {0} failed due to an internal error.", new String[] { file.getFullPath().toString() }), e);
        }
    }

    /*
     * Replace the local contents with the remote contents.
     * The local resource must be a file.
     */
    private IStatus performReplace(SyncInfo info, IProgressMonitor monitor) throws CoreException {
    	IFile file = (IFile)info.getLocal();
    	IResourceVariant remote = info.getRemote();
    	if (remote == null && file.exists()) {
    		file.delete(false, true, monitor);
    	} else if (remote != null) {
    		InputStream stream = remote.getStorage(monitor).getContents();
    		stream = new BufferedInputStream(stream);
    		try {
	    		if (file.exists()) {
	    			file.setContents(stream, false, true, monitor);
	    		} else {
	    			file.create(stream, false, monitor);
	    		}
    		} finally {
    			try {
					stream.close();
				} catch (IOException e) {
					// Ignore
				}
    		}
    	}
    	markAsMerged(file, monitor);
		return Status.OK_STATUS;
	}

	/*
     * Perform a three-way merge on the given sync-info.
     * The local resource must be a file and all three
     * resources (local, base, remote) must exist.
     */
	private IStatus performThreeWayMerge(SyncInfo info, IProgressMonitor monitor) throws CoreException {
		IFile file = (IFile)info.getLocal();
		IContentDescription contentDescription = file.getContentDescription();
		IStreamMerger merger = null;
		if (contentDescription != null && contentDescription.getContentType() != null) {
		    merger = CompareUI.createStreamMerger(contentDescription.getContentType());
		} else {
		    String fileExtension = file.getFileExtension();
		    if (fileExtension != null)
		        merger = CompareUI.createStreamMerger(fileExtension);
		}
		// If we couldn't find a registered merger, fallback to text
		// since we know we have one of those registered.
		if (merger == null)
		    merger = CompareUI.createStreamMerger(TXT_EXTENTION);
		if (merger == null)
		    return  new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, NLS.bind("Auto-merge support for {0} is not available.", new String[] { file.getFullPath().toString() }), null);
		return merge(merger, info, monitor);
	}

    /*
     * Perform a three-way merge on the given sync-info using the given
     * stream merger. The local resource must be a file and all three
     * resources (local, base, remote) must exist.
     */
    private IStatus merge(IStreamMerger merger, SyncInfo info, IProgressMonitor monitor) throws CoreException {
        
    	// Get the file involved
    	IFile file = (IFile)info.getLocal();
    	
    	// Define all the input streams here so we can ensure they get closed
        InputStream ancestorStream = null;
        InputStream remoteStream = null;
        InputStream targetStream = null;
        
        try {

        
            // Get the ancestor stream and encoding
        	IResourceVariant base = info.getBase();
            IStorage s = base.getStorage(monitor);
            String ancestorEncoding = null;
            if (s instanceof IEncodedStorage) {
                IEncodedStorage es = (IEncodedStorage) s;
                ancestorEncoding = es.getCharset();
            }
            if (ancestorEncoding == null) {
                ancestorEncoding = file.getCharset();
            }
            ancestorStream = new BufferedInputStream(s.getContents());
            
            // Get the remote stream and encoding
            IResourceVariant remote = info.getRemote();
            s = remote.getStorage(monitor);
            String remoteEncoding = null;
            if (s instanceof IEncodedStorage) {
                IEncodedStorage es = (IEncodedStorage) s;
                remoteEncoding = es.getCharset();
            }
            if (remoteEncoding == null) {
                remoteEncoding = file.getCharset();
            }
            remoteStream = new BufferedInputStream(s.getContents());
            
            // Get the local (target) stream and encoding
            targetStream = file.getContents();
            String targetEncoding = file.getCharset();
            IStatus status;
            OutputStream output = getTempOutputStream(file);
            try {
                status = merger.merge(output, targetEncoding, ancestorStream, ancestorEncoding, targetStream, targetEncoding, remoteStream, remoteEncoding, monitor);
                if (status.isOK()) {
                    file.setContents(getTempInputStream(file, output), false, true, monitor);
                    markAsMerged(file, monitor);
                } else {
                	status = new MergeStatus(status.getPlugin(), status.getMessage(), new IFile[]{file});
                }
            } finally {
                disposeTempOutputStream(file, output);
            }
            return status;
        } finally {
            try {
                if (ancestorStream != null)
                    ancestorStream.close();
            } catch (IOException e) {
                // Ignore
            }
            try {
                if (remoteStream != null)
                    remoteStream.close();
            } catch (IOException e) {
                // Ignore
            }
            try {
                if (targetStream != null)
                    targetStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private InputStream getTempInputStream(IFile file, OutputStream output) throws CoreException {
        if (output instanceof ByteArrayOutputStream) {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) output;
            return new ByteArrayInputStream(baos.toByteArray());
        }
        // We created a temporary file so we need to open an input stream on it
        try {
            // First make sure the output stream is closed
            if (output != null)
                output.close();
        } catch (IOException e) {
            // Ignore
        }
        File tmpFile = getTempFile(file);
        try {
            return new BufferedInputStream(new FileInputStream(tmpFile));
        } catch (FileNotFoundException e) {
            throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, NLS.bind("Could not read from temporary file {0}: {1}", new String[] { tmpFile.getAbsolutePath(), e.getMessage() }), e));
        }
    }

    private void disposeTempOutputStream(IFile file, OutputStream output) {
        if (output instanceof ByteArrayOutputStream)
            return;
        // We created a temporary file so we need to clean it up
        try {
            // First make sure the output stream is closed
            // so that file deletion will not fail because of that.
            if (output != null)
                output.close();
        } catch (IOException e) {
            // Ignore
        }
        File tmpFile = getTempFile(file);
        if (tmpFile.exists())
            tmpFile.delete();
    }

    private OutputStream getTempOutputStream(IFile file) throws CoreException {
        File tmpFile = getTempFile(file);
        if (tmpFile.exists())
            tmpFile.delete();
        try {
            return new BufferedOutputStream(new FileOutputStream(tmpFile));
        } catch (FileNotFoundException e) {
            TeamPlugin.log(IStatus.ERROR, NLS.bind("Could not open temporary file {0} for writing: {1}", new String[] { tmpFile.getAbsolutePath(), e.getMessage() }), e);
            return new ByteArrayOutputStream();
        }
    }

    private File getTempFile(IFile file) {
        return TeamPlugin.getPlugin().getStateLocation().append(".tmp").append(file.getName() + ".tmp").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
    }
}

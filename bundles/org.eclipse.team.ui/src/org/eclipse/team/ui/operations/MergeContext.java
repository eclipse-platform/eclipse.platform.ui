/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.operations;

import java.io.*;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamMerger;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.delta.*;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.core.mapping.MergeStatus;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.delta.DeltaTree;

/**
 * A merge context that performs three-way merges using the {@link IStreamMerger}
 * interface.
 * <p>
 * This class may be subclasses by clients
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.2
 */
public abstract class MergeContext extends org.eclipse.team.core.mapping.MergeContext {

	/**
	 * Create a merge context
	 * @param scope the scope of the merge
	 * @param type the type of merge (TWO-WAY or THREE_WAY)
	 * @param tree the sync info tree
	 * @param deltaTree the delta tree
	 */
	protected MergeContext(IResourceMappingScope scope, String type, SyncInfoTree tree, DeltaTree deltaTree) {
		super(scope, type, tree, deltaTree);
	}

	private static final String TXT_EXTENTION = "txt"; //$NON-NLS-1$
	
	/*
     * Perform a three-way merge on the given sync-info.
     * The local resource must be a file and all three
     * resources (local, base, remote) must exist.
     */
	protected IStatus performThreeWayMerge(IThreeWayDelta delta, IProgressMonitor monitor) throws CoreException {
		IFile file = (IFile)getResource(delta);
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
		return merge(merger, delta, monitor);
	}

    /*
     * Perform a three-way merge on the given sync-info using the given
     * stream merger. The local resource must be a file and all three
     * resources (local, base, remote) must exist.
     */
    private IStatus merge(IStreamMerger merger, IDelta delta, IProgressMonitor monitor) throws CoreException {
        
    	// Get the file involved
    	IFile file = (IFile)getResource(delta);
    	
    	// Define all the input streams here so we can ensure they get closed
        InputStream ancestorStream = null;
        InputStream remoteStream = null;
        InputStream targetStream = null;
        
        try {

        
            // Get the ancestor stream and encoding
        	IResourceVariant base = null;
        	ITwoWayDelta remoteChange = ((IThreeWayDelta)delta).getRemoteChange();
        	if (remoteChange != null)
        		base = (IResourceVariant)remoteChange.getBeforeState();
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
            IResourceVariant remote = null;
            if (remoteChange != null)
            	remote = (IResourceVariant)remoteChange.getAfterState();
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
                    markAsMerged(file, false, monitor);
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

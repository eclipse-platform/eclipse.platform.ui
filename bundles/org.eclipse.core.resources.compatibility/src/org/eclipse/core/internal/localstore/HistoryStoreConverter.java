/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.*;
import org.eclipse.core.internal.resources.ResourceStatus;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;

public class HistoryStoreConverter {
	/**
	 * Converts an existing history store lying on disk to the new history store.
	 * Returns Status.OK_STATUS if nothing is done, an IStatus.INFO status if
	 * the conversion happens successfully or an IStatus.ERROR status if an error
	 * happened during the conversion process.
	 */
	public IStatus convertHistory(Workspace workspace, IPath location, int limit, final HistoryStore2 destination, boolean rename) {
		if (!location.toFile().isDirectory())
			// nothing to be converted
			return Status.OK_STATUS;
		IPath indexFile = location.append(HistoryStore.INDEX_FILE);
		if (!indexFile.toFile().isFile()) {
			IPath newIndexDir = location.append(".buckets"); //$NON-NLS-1$
			if (!newIndexDir.toFile().isDirectory())
				// nothing to be converted		
				return Status.OK_STATUS;
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IStatus.OK, Messages.history_conversionTransitional, null); //$NON-NLS-1$ 
			convertFromTransitionalFormat(status, newIndexDir.toFile(), destination);
			Workspace.clear(newIndexDir.toFile());
			status.add(new ResourceStatus(IStatus.INFO, IStatus.OK, null, Messages.history_conversionSucceeded, null));
			return status;
		}
		// visit all existing entries and add them to the new history store
		long start = System.currentTimeMillis();
		final CoreException[] exception = new CoreException[1];
		final BucketTree tree = destination.getTree();
		final HistoryBucket currentBucket = (HistoryBucket) tree.getCurrent();
		HistoryStore source = new HistoryStore(workspace, location, limit);
		source.accept(Path.ROOT, new IHistoryStoreVisitor() {
			public boolean visit(HistoryStoreEntry state) {
				try {
					tree.loadBucketFor(state.getPath());
				} catch (CoreException e) {
					// failed while loading bucket
					exception[0] = e;
					return false;
				}
				currentBucket.addBlob(state.getPath(), state.getUUID(), state.getLastModified());
				return true;
			}
		}, true);
		try {
			// the last bucket changed will not have been saved
			tree.getCurrent().save();
			// we are done using the old history store instance		
			source.shutdown(null);
		} catch (CoreException e) {
			// failed during save
			exception[0] = e;
		}
		if (Policy.DEBUG_HISTORY)
			Policy.debug("Time to convert local history: " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$ //$NON-NLS-2$
		if (exception[0] != null) {
			// failed while visiting the old data or saving the new data
			String conversionFailed = Messages.history_conversionFailed;
			Status failure = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, new IStatus[] {exception[0].getStatus()}, conversionFailed, null);
			// we failed, so don't do anything else - we might try converting again later
			return failure;
		}
		// everything went fine
		// if requested rename the index file to something else
		// so we don't try converting again in the future
		if (rename)
			indexFile.toFile().renameTo(indexFile.addFileExtension(Long.toString(System.currentTimeMillis())).toFile());
		String conversionOk = Messages.history_conversionSucceeded;
		// leave a note to the user so this does not happen silently
		return new Status(IStatus.INFO, ResourcesPlugin.PI_RESOURCES, IStatus.OK, conversionOk, null);
	}

	/** 
	 * Converts from the format used during the M4 cycle.
	 * TODO remove this before 3.1 release
	 * @param destination 
	 */
	private void convertFromTransitionalFormat(MultiStatus status, java.io.File root, HistoryStore2 destination) {
		File[] subdirs = root.listFiles();
		if (subdirs == null)
			return;
		for (int i = 0; i < subdirs.length; i++)
			if (subdirs[i].isDirectory())
				convertFromTransitionalFormat(status, subdirs[i], destination);
		File bucketFile = new File(root, "bucket.index"); //$NON-NLS-1$
		if (!bucketFile.isFile())
			return;
		final BucketTree tree = destination.getTree();
		final HistoryBucket currentBucket = (HistoryBucket) tree.getCurrent();
		DataInputStream source = null;
		try {
			source = new DataInputStream(new BufferedInputStream(new FileInputStream(bucketFile), 8192));
			// don't do any checking
			source.readByte();
			int entryCount = source.readInt();
			for (int i = 0; i < entryCount; i++) {
				String path = source.readUTF();
				tree.loadBucketFor(new Path(path));
				int numberOfStates = source.readUnsignedShort();
				byte[][] states = new byte[numberOfStates][HistoryBucket.HistoryEntry.DATA_LENGTH];
				for (int j = 0; j < numberOfStates; j++)
					source.read(states[j]);
				HistoryBucket.HistoryEntry entry = new HistoryBucket.HistoryEntry(new Path(path), states);
				for (int j = 0; j < entry.getOccurrences(); j++)
					currentBucket.addBlob(entry.getPath(), entry.getUUID(j), entry.getTimestamp(j));
			}
			tree.getCurrent().save();
		} catch (IOException ioe) {
			String msg = ioe.getLocalizedMessage();
			Throwable exception = Platform.inDebugMode() ? ioe : null;
			status.add(new ResourceStatus(IStatus.WARNING, IResourceStatus.FAILED_READ_METADATA, new Path(bucketFile.getAbsolutePath()), msg, exception));
		} catch (CoreException ce) {
			status.add(ce.getStatus());
		} finally {
			if (source != null)
				try {
					source.close();
				} catch (IOException ioe) {
					// we are just closing a stream opened for read, no data can be lost...
				}
		}
	}
}
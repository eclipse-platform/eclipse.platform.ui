/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.File;
import org.eclipse.core.internal.resources.Workspace;
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
		IPath indexFile = location.append(HistoryStore.INDEX_FILE);
		if (!indexFile.toFile().isFile())
			// nothing to be converted
			return Status.OK_STATUS;
		// visit all existing entries and add them to the new history store
		long start = System.currentTimeMillis();
		final CoreException[] exception = new CoreException[1];
		final BucketIndex currentBucket = destination.createBucketTable();
		HistoryStore source = new HistoryStore(workspace, location, limit);
		source.accept(Path.ROOT, new IHistoryStoreVisitor() {
			public boolean visit(HistoryStoreEntry state) {
				File bucketDir = destination.locationFor(state.getPath().removeLastSegments(1));
				try {
					currentBucket.load(bucketDir);
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
			currentBucket.save();
			// we are done using the old history store instance		
			source.shutdown(null);
		} catch (CoreException e) {
			// failed during save
			exception[0] = e;
		}
		if (Policy.DEBUG_HISTORY)
			Policy.debug("Time to convert local history: " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$ //$NON-NLS-2$
		if (exception[0] != null) {
			// failed while visiting or saving
			String conversionFailed = Policy.bind("history.conversionFailed"); //$NON-NLS-1$
			Status failure = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, new IStatus[] {exception[0].getStatus()}, conversionFailed, null);
			// we failed, so don't do anything else - we might try converting again later
			return failure;
		}
		// everything went fine
		// if requested rename the index file to something else
		// so we don't try converting again in the future
		if (rename)
			indexFile.toFile().renameTo(indexFile.addFileExtension(Long.toString(System.currentTimeMillis())).toFile());
		String conversionOk = Policy.bind("history.conversionSucceeded"); //$NON-NLS-1$
		// leave a note to the user so this does not happen silently
		return new Status(IStatus.INFO, ResourcesPlugin.PI_RESOURCES, IStatus.OK, conversionOk, null);
	}
}
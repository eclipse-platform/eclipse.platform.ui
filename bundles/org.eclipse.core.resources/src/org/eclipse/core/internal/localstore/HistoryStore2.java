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
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.internal.localstore.BucketIndex.Entry;
import org.eclipse.core.internal.localstore.BucketIndex.Visitor;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class HistoryStore2 implements IHistoryStore {
	private static final String INDEX_STORE = ".buckets"; //$NON-NLS-1$
	private final static int SEGMENT_LENGTH = 2;
	private final static long SEGMENT_QUOTA = (long) Math.pow(2, 4 * SEGMENT_LENGTH); // 1 char = 2 ^ 4 = 0x10
	private BlobStore blobStore;
	private Set blobsToRemove = new HashSet();
	private BucketIndex currentBucket;
	private File indexLocation;
	private Workspace workspace;

	public HistoryStore2(Workspace workspace, IPath location, int limit) {
		this.workspace = workspace;
		location.toFile().mkdirs();
		this.blobStore = new BlobStore(location, limit);
		this.indexLocation = location.append(INDEX_STORE).toFile();
		this.currentBucket = createBucketTable();
	}

	/**
	 * From a starting point in the tree, visit all nodes under it. 
	 * @param visitor
	 * @param root
	 * @param depth
	 */
	private void accept(Visitor visitor, IPath root, int depth) throws CoreException {
		// we only do anything for the root if depth == infinite
		if (root.isRoot()) {
			if (depth != IResource.DEPTH_INFINITE)
				// root with depth < infinite... nothing to be done
				return;
			// visit all projects DEPTH_INFINITE
			File[] projects = indexLocation.listFiles();
			if (projects == null || projects.length == 0)
				return;
			for (int i = 0; i < projects.length; i++)
				if (projects[i].isDirectory())
					if (!internalAccept(visitor, root.append(projects[i].getName()), projects[i], IResource.DEPTH_INFINITE))
						break;
			// done
			return;
		}
		// handles the case the starting point is a file path
		if (root.segmentCount() > 1) {
			currentBucket.load(locationFor(root.removeLastSegments(1)));
			if (currentBucket.accept(visitor, root, true) != Visitor.CONTINUE || depth == IResource.DEPTH_ZERO)
				return;
		}
		internalAccept(visitor, root, locationFor(root), depth);
	}

	/**
	 * @see IHistoryStore#addState(IPath, File, long, boolean)
	 */
	public IFileState addState(IPath key, java.io.File localFile, long lastModified, boolean moveContents) {
		if (Policy.DEBUG_HISTORY)
			System.out.println("History: Adding state for key: " + key + ", file: " + localFile + ", timestamp: " + lastModified + ", size: " + localFile.length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (!isValid(localFile))
			return null;
		UniversalUniqueIdentifier uuid = null;
		try {
			uuid = blobStore.addBlob(localFile, moveContents);
			File bucketDir = locationFor(key.removeLastSegments(1));
			currentBucket.load(bucketDir);
			currentBucket.addBlob(key, uuid, lastModified);
			currentBucket.save();
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
		return new FileState(this, key, lastModified, uuid);
	}

	public Set allFiles(IPath root, int depth, IProgressMonitor monitor) {
		final Set allFiles = new HashSet();
		try {
			accept(new Visitor() {
				public int visit(Entry fileEntry) {
					allFiles.add(fileEntry.getPath());
					return CONTINUE;
				}
			}, root, depth);
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
		return allFiles;
	}

	/**
	 * Applies the clean-up policy to an entry.
	 */
	void applyPolicy(Entry fileEntry, int maxStates, long minTimeStamp) {
		for (int i = 0; i < fileEntry.getOccurrences(); i++) {
			if (i < maxStates && fileEntry.getTimestamp(i) >= minTimeStamp)
				continue;
			// "delete" the current uuid						
			blobsToRemove.add(fileEntry.getUUID(i));
			fileEntry.deleteOccurrence(i);
		}
	}

	/**
	 * Applies the clean-up policy to a tree.
	 */
	private void applyPolicy(IPath root, final int maxStates, final long minimumTimestamp) throws CoreException {
		// apply policy to destination as a separate pass, since now we want to visit the destination		
		accept(new Visitor() {
			public int visit(BucketIndex.Entry entry) {
				applyPolicy(entry, maxStates, minimumTimestamp);
				return CONTINUE;
			}
		}, root, IResource.DEPTH_INFINITE);
	}

	public void clean(IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		try {
			IWorkspaceDescription description = workspace.internalGetDescription();
			final long minimumTimestamp = System.currentTimeMillis() - description.getFileStateLongevity();
			final int maxStates = description.getMaxFileStates();
			final int[] entryCount = new int[1];
			accept(new Visitor() {
				public int visit(Entry fileEntry) {
					entryCount[0] += fileEntry.getOccurrences();
					applyPolicy(fileEntry, maxStates, minimumTimestamp);
					return CONTINUE;
				}
			}, Path.ROOT, IResource.DEPTH_INFINITE);
			if (Policy.DEBUG_HISTORY) {
				Policy.debug("Time to apply history store policies: " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$ //$NON-NLS-2$
				Policy.debug("Total number of history store entries: " + entryCount[0]); //$NON-NLS-1$
			}
			start = System.currentTimeMillis();
			// remove unreferenced blobs
			blobStore.deleteBlobs(blobsToRemove);
			if (Policy.DEBUG_HISTORY)
				Policy.debug("Time to remove " + blobsToRemove.size() + " unreferenced blobs: " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$			
			blobsToRemove = new HashSet();
		} catch (Exception e) {
			String message = Policy.bind("history.problemsCleaning"); //$NON-NLS-1$
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	public void copyHistory(IResource sourceResource, IResource destinationResource) {
		// return early if either of the paths are null or if the source and
		// destination are the same.
		if (sourceResource == null || destinationResource == null) {
			String message = Policy.bind("history.copyToNull"); //$NON-NLS-1$
			ResourceStatus status = new ResourceStatus(IResourceStatus.INTERNAL_ERROR, null, message, null);
			ResourcesPlugin.getPlugin().getLog().log(status);
			return;
		}
		if (sourceResource.equals(destinationResource)) {
			String message = Policy.bind("history.copyToSelf"); //$NON-NLS-1$
			ResourceStatus status = new ResourceStatus(IResourceStatus.INTERNAL_ERROR, sourceResource.getFullPath(), message, null);
			ResourcesPlugin.getPlugin().getLog().log(status);
			return;
		}

		final IPath source = sourceResource.getFullPath();
		final IPath destination = destinationResource.getFullPath();
		Assert.isLegal(source.segmentCount() > 0);
		Assert.isLegal(destination.segmentCount() > 0);
		Assert.isLegal(source.segmentCount() > 1 || destination.segmentCount() == 1);

		IWorkspaceDescription description = workspace.internalGetDescription();
		final long minimumTimestamp = System.currentTimeMillis() - description.getFileStateLongevity();
		final int maxStates = description.getMaxFileStates();

		boolean file = sourceResource.getType() == IResource.FILE;

		final IPath baseSourceLocation = Path.fromOSString(locationFor(source.removeLastSegments(file ? 1 : 0)).toString());
		final IPath baseDestinationLocation = Path.fromOSString(locationFor(destination.removeLastSegments(file ? 1 : 0)).toString());

		try {
			// special case: source and origin are the same bucket (renaming a file/copying a file/folder to the same directory)
			//TODO isn't this missing the folder case (should be recursive)? 
			if (baseSourceLocation.equals(baseDestinationLocation)) {
				currentBucket.load(baseSourceLocation.toFile());
				Entry sourceEntry = currentBucket.getEntry(source);
				if (sourceEntry == null)
					return;
				Entry destinationEntry = new Entry(destination, sourceEntry.getData(true));
				currentBucket.addBlobs(destinationEntry);
				currentBucket.save();
				// apply clean-up policy to the destination tree 
				applyPolicy(destinationResource.getFullPath(), maxStates, minimumTimestamp);
				return;
			}
			final BucketIndex sourceBucket = currentBucket;
			final BucketIndex destinationBucket = createBucketTable();

			// copy history by visiting the source tree
			accept(new Visitor() {
				private IPath lastPath;

				private boolean ensureLoaded(IPath newPath) {
					IPath tmpLastPath = lastPath;
					lastPath = newPath;
					if (tmpLastPath != null && tmpLastPath.removeLastSegments(1).equals(newPath.removeLastSegments(1)))
						// still in the same source bucket, nothing to do
						return true;
					// need to load the destination bucket 
					// figure out where we want to copy the states for this path with:
					// destinationBucket = baseDestinationLocation + blob - filename - baseSourceLocation
					IPath sourceDir = Path.fromOSString(sourceBucket.getLocation().toString());
					IPath destinationDir = baseDestinationLocation.append(sourceDir.removeFirstSegments(baseSourceLocation.segmentCount()));
					try {
						destinationBucket.load(destinationDir.toFile());
					} catch (CoreException e) {
						ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
						// abort traversal
						return false;
					}
					return true;
				}

				public int visit(Entry sourceEntry) {
					if (!ensureLoaded(sourceEntry.getPath()))
						return STOP;
					IPath destinationPath = destination.append(sourceEntry.getPath().removeFirstSegments(source.segmentCount()));
					Entry destinationEntry = new Entry(destinationPath, sourceEntry.getData(true));
					destinationBucket.addBlobs(destinationEntry);
					return CONTINUE;
				}
			}, source, IResource.DEPTH_INFINITE);
			// the last bucket visited will not be automatically saved
			destinationBucket.save();
			// apply clean-up policy to the destination tree 
			applyPolicy(destinationResource.getFullPath(), maxStates, minimumTimestamp);
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
	}

	BucketIndex createBucketTable() {
		return new BucketIndex(indexLocation);
	}

	public boolean exists(IFileState target) {
		return blobStore.fileFor(((FileState) target).getUUID()).exists();
	}

	public InputStream getContents(IFileState target) throws CoreException {
		if (!target.exists()) {
			String message = Policy.bind("history.notValid"); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		return blobStore.getBlob(((FileState) target).getUUID());
	}

	public File getFileFor(IFileState state) {
		return blobStore.fileFor(((FileState) state).getUUID());
	}

	public IFileState[] getStates(IPath filePath, IProgressMonitor monitor) {
		File bucketDir = locationFor(filePath.removeLastSegments(1));
		try {
			currentBucket.load(bucketDir);
			BucketIndex.Entry fileEntry = currentBucket.getEntry(filePath);
			if (fileEntry == null || fileEntry.isEmpty())
				return new IFileState[0];
			IFileState[] states = new IFileState[fileEntry.getOccurrences()];
			for (int i = 0; i < states.length; i++)
				states[i] = new FileState(this, fileEntry.getPath(), fileEntry.getTimestamp(i), fileEntry.getUUID(i));
			return states;
		} catch (CoreException ce) {
			ResourcesPlugin.getPlugin().getLog().log(ce.getStatus());
			return new IFileState[0];
		}
	}

	/**
	 * 
	 * @return whether to continue visiting other branches 
	 */
	private boolean internalAccept(Visitor visitor, IPath root, File bucketDir, int depth) throws CoreException {
		currentBucket.load(bucketDir);
		int outcome = currentBucket.accept(visitor, root, depth == IResource.DEPTH_ZERO);
		if (outcome != Visitor.CONTINUE)
			return outcome == Visitor.RETURN;
		// nothing else to be done
		if (depth != IResource.DEPTH_INFINITE)
			return true;
		File[] subDirs = bucketDir.listFiles();
		if (subDirs == null)
			return true;
		for (int i = 0; i < subDirs.length; i++)
			if (subDirs[i].isDirectory())
				if (!internalAccept(visitor, root, subDirs[i], IResource.DEPTH_INFINITE))
					return false;
		return true;
	}

	/**
	 * Return a boolean value indicating whether or not the given file
	 * should be added to the history store based on the current history
	 * store policies.
	 * 
	 * @param localFile the file to check
	 * @return <code>true</code> if this file should be added to the history
	 * 	store and <code>false</code> otherwise
	 */
	private boolean isValid(java.io.File localFile) {
		WorkspaceDescription description = workspace.internalGetDescription();
		boolean result = localFile.length() <= description.getMaxFileStateSize();
		if (Policy.DEBUG_HISTORY && !result)
			System.out.println("History: Ignoring file (too large). File: " + localFile.getAbsolutePath() + //$NON-NLS-1$
					", size: " + localFile.length() + //$NON-NLS-1$
					", max: " + description.getMaxFileStateSize()); //$NON-NLS-1$
		return result;
	}

	/**
	 * Returns the index location corresponding to the given path. 
	 */
	File locationFor(IPath resourcePath) {
		int segmentCount = resourcePath.segmentCount();
		// the root
		if (segmentCount == 0)
			return indexLocation;
		// a project
		if (segmentCount == 1)
			return new File(indexLocation, resourcePath.segment(0));
		// a folder
		IPath location = new Path(resourcePath.segment(0));
		for (int i = 1; i < segmentCount; i++)
			// translate all segments except the first one (project name)
			location = location.append(translateSegment(resourcePath.segment(i)));
		return new File(indexLocation, location.toOSString());
	}

	public void remove(IPath root, IProgressMonitor monitor) {
		try {
			final Set tmpBlobsToRemove = blobsToRemove;
			accept(new Visitor() {
				public int visit(Entry fileEntry) {
					for (int i = 0; i < fileEntry.getOccurrences(); i++)
						// remember we need to delete the files later
						tmpBlobsToRemove.add(fileEntry.getUUID(i));
					fileEntry.delete();
					return CONTINUE;
				}
			}, root, IResource.DEPTH_INFINITE);
		} catch (CoreException ce) {
			ResourcesPlugin.getPlugin().getLog().log(ce.getStatus());
		}
	}

	/**
	 * @see IHistoryStore#removeGarbage()
	 */
	public void removeGarbage() {
		try {
			final Set tmpBlobsToRemove = blobsToRemove;
			accept(new Visitor() {
				public int visit(Entry fileEntry) {
					for (int i = 0; i < fileEntry.getOccurrences(); i++)
						// remember we need to delete the files later
						tmpBlobsToRemove.remove(fileEntry.getUUID(i));
					return CONTINUE;
				}
			}, Path.ROOT, IResource.DEPTH_INFINITE);
			blobStore.deleteBlobs(blobsToRemove);
			blobsToRemove = new HashSet();
		} catch (Exception e) {
			String message = Policy.bind("history.problemsCleaning"); //$NON-NLS-1$
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	public void shutdown(IProgressMonitor monitor) throws CoreException {
		// just in case
		currentBucket.save();
	}

	public void startup(IProgressMonitor monitor) {
		// nothing to be done
	}

	private String translateSegment(String segment) {
		// String.hashCode algorithm is API
		return Long.toHexString(Math.abs(segment.hashCode()) % SEGMENT_QUOTA);
	}
}
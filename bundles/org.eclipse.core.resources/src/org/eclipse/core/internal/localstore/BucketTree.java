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

import java.io.*;
import org.eclipse.core.internal.localstore.Bucket.Visitor;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;

public class BucketTree {

	public static final int DEPTH_INFINITE = Integer.MAX_VALUE;
	public static final int DEPTH_ONE = 1;
	public static final int DEPTH_ZERO = 0;

	private final static int SEGMENT_LENGTH = 2;
	private final static long SEGMENT_QUOTA = (long) Math.pow(2, 4 * SEGMENT_LENGTH); // 1 char = 2 ^ 4 = 0x10	

	private final static String VERSION_FILE = "version"; //$NON-NLS-1$

	private Bucket current;

	private File rootLocation;

	public BucketTree(File rootLocation, Bucket bucket) {
		this.rootLocation = rootLocation;
		this.current = bucket;
	}

	/**
	 * From a starting point in the tree, visit all nodes under it. 
	 * @param visitor
	 * @param root
	 * @param depth
	 */
	public void accept(Bucket.Visitor visitor, IPath root, int depth) throws CoreException {
		internalAccept(visitor, root, locationFor(root), depth, 0);
	}

	public void close() throws CoreException {
		current.save();
		saveVersion();
	}

	public Bucket getCurrent() {
		return current;
	}

	/**
	 * @return whether to continue visiting other branches 
	 */
	private boolean internalAccept(Bucket.Visitor visitor, IPath root, File bucketDir, int depthRequested, int currentDepth) throws CoreException {
		current.load(bucketDir);
		int outcome = current.accept(visitor, root, depthRequested);
		if (outcome != Visitor.CONTINUE)
			return outcome == Visitor.RETURN;
		if (depthRequested == currentDepth)
			return true;
		File[] subDirs = bucketDir.listFiles();
		if (subDirs == null)
			return true;
		for (int i = 0; i < subDirs.length; i++)
			if (subDirs[i].isDirectory())
				if (!internalAccept(visitor, root, subDirs[i], depthRequested, currentDepth + 1))
					return false;
		return true;
	}

	public void loadBucketFor(IPath path) throws CoreException {
		current.load(locationFor(path));
	}

	public File locationFor(IPath resourcePath) {
		int segmentCount = resourcePath.segmentCount();
		// the root
		if (segmentCount == 0)
			return rootLocation;
		// a project
		if (segmentCount == 1)
			return new File(rootLocation, resourcePath.segment(0));
		// a folder or file
		IPath location = new Path(resourcePath.segment(0));
		// the last segment is ignored
		for (int i = 1; i < segmentCount - 1; i++)
			// translate all segments except the first one (project name)
			location = location.append(translateSegment(resourcePath.segment(i)));
		return new File(rootLocation, location.toOSString());
	}

	/**
	 * Writes the version tag to a file on disk.
	 */
	private void saveVersion() throws CoreException {
		if (!this.rootLocation.isDirectory())
			return;
		File versionFile = new File(this.rootLocation, VERSION_FILE);
		FileOutputStream stream = null;
		boolean failed = false;
		try {
			stream = new FileOutputStream(versionFile);
			stream.write(current.getVersion());
		} catch (IOException e) {
			failed = true;
			String message = Policy.bind("resources.writeWorkspaceMeta", versionFile.getAbsolutePath()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, null, message, e);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				if (!failed) {
					String message = Policy.bind("resources.writeWorkspaceMeta", versionFile.getAbsolutePath()); //$NON-NLS-1$
					throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, null, message, e);
				}
			}
		}
	}

	private String translateSegment(String segment) {
		// String.hashCode algorithm is API
		return Long.toHexString(Math.abs(segment.hashCode()) % SEGMENT_QUOTA);
	}
}
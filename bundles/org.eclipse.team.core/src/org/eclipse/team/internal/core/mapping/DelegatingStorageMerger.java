/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.mapping;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IStorageMerger;
import org.eclipse.team.internal.core.*;

/**
 * This storage merger delegates to the appropriate merger or returns a conflict
 * if no merger is available.
 * <p>
 * The target storage is used to look for an appropriate merger. If the target
 * is an {@link IFile}, the content type of the file is used. Otherwise, the
 * {@link IContentTypeManager} is used to find an appropriate content type.If an
 * appropriate merger is not found, a status containing the
 * <code>CONFLICT</code> is returned.
 * 
 */
public class DelegatingStorageMerger implements IStorageMerger {

	private static IStreamMergerDelegate mergerDelegate;
	private static DelegatingStorageMerger instance;
	private final IContentType contentType;
	
	private DelegatingStorageMerger() {
		contentType = null;
	}
	
	public DelegatingStorageMerger(IContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * Set the file merger that is used by the {@link #performThreeWayMerge(IThreeWayDiff, IProgressMonitor) }
	 * method. It is the responsibility of subclasses to provide a merger.
	 * If a merger is not provided, subclasses must override <code>performThreeWayMerge</code>.
	 * @param merger the merger used to merge files
	 */
	public static void setMergerDelegate(IStreamMergerDelegate aMerger) {
		mergerDelegate = aMerger;
	}
	
	public static IStorageMerger getInstance() {
		if (instance == null)
			instance = new DelegatingStorageMerger();
		return instance;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.mapping.IStorageMerger#merge(java.io.OutputStream,
	 *      java.lang.String, org.eclipse.core.resources.IStorage,
	 *      org.eclipse.core.resources.IStorage,
	 *      org.eclipse.core.resources.IStorage,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus merge(OutputStream output, String outputEncoding,
			IStorage ancestor, IStorage target, IStorage other,
			IProgressMonitor monitor) throws CoreException {
		IStorageMerger merger = findMerger(target);
		if (merger == null)
			return new Status(IStatus.WARNING, TeamPlugin.ID, CONFLICT,
					Messages.DelegatingStorageMerger_0, null);
		return merger.merge(output, outputEncoding, ancestor, target, other, monitor);
	}

	private IStorageMerger findMerger(IStorage target) throws CoreException {
		IStorageMerger merger = null;
		if (contentType != null) {
			// A particular merger has been requested
			merger = getMerger(contentType);
			if (merger != null) {
				return merger;
			} else {
				// The requested merger is not available but still tryand find another
				TeamPlugin.log(IStatus.ERROR, NLS.bind("Storage merger for {0} not available", contentType.getId()), null); //$NON-NLS-1$
			}
		}
		CoreException exception = null;
		try {
			IContentType type = getContentType(target);
			if (type != null)
				merger = getMerger(type);
		} catch (CoreException e) {
			exception = e;
		}
		// If an exception occurred trying to find a content type,
		// try using the extension before failing
		if (merger == null) {
			merger = getMerger(target.getName());
			if (merger == null) {
				// If team thinks the file is text, try to get a text merger for the file
				int type = Team.getFileContentManager().getType(target);
				if (type == Team.TEXT) 
					merger = getTextMerger();
				if (merger == null) {
					// As a last resort, look for a stream merger
					merger = findAndWrapStreamMerger(target);
				}
			}
		}
		if (exception != null) {
			if (merger == null) {
				// No merger was found so report the error
				throw exception;
			} else {
				// If an extension based merger was found, log the error
				TeamPlugin.log(exception);
			}
		}
		return merger;
	}

	private IStorageMerger findAndWrapStreamMerger(IStorage target) {
		if (mergerDelegate != null) {
			IStorageMerger merger = mergerDelegate.findMerger(target);
			return merger;
		}
		return null;
	}

	private IStorageMerger getTextMerger() {
		return new DelegatingStorageMerger(Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT));
	}

	private IStorageMerger getMerger(String name) {
		String extension = getExtension(name);
		if (extension != null)
			return StorageMergerRegistry.getInstance().createStreamMerger(extension);
		return null;
	}

	public static String getExtension(String name) {
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return null;
		}
		return name.substring(index + 1);
	}

	private IStorageMerger getMerger(IContentType type) {
		IStorageMerger merger = StorageMergerRegistry.getInstance().createStreamMerger(type);
		return merger;
	}

	/*
	 * Find the content type for the given storage and return null if a content
	 * type cannot be found. Any exceptions that occur when trying to determine
	 * the content type are propogated.
	 */
	public static IContentType getContentType(IStorage target) throws CoreException {
		if (target instanceof IFile) {
			IFile file = (IFile) target;
			IContentDescription contentDescription = file.getContentDescription();
			if (contentDescription != null) {
				IContentType contentType = contentDescription.getContentType();
				return contentType;
			}
		} else {
			IContentTypeManager manager = Platform.getContentTypeManager();
			try {
				IContentType type = manager.findContentTypeFor(target
						.getContents(), target.getName());
				return type;
			} catch (IOException e) {
				String name = target.getName();
				if (target.getFullPath() != null) {
					name = target.getFullPath().toString();
				}
				throw new TeamException(new Status(
					IStatus.ERROR,
					TeamPlugin.ID,
					INTERNAL_ERROR,
					NLS.bind(Messages.DelegatingStorageMerger_1,name), e));
			}
		}
		return null;
	}

}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.io.*;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.*;
import org.eclipse.compare.patch.IHunk;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;

public class HunkTypedElement implements ITypedElement, IEncodedStreamContentAccessor, IAdaptable {

	private final HunkResult fHunkResult;
	private final boolean fIsAfterState;
	private final boolean fFullContext;

	public HunkTypedElement(HunkResult result, boolean isAfterState, boolean fullContext) {
		this.fHunkResult = result;
		this.fIsAfterState = isAfterState;
		this.fFullContext = fullContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getImage()
	 */
	public Image getImage() {
		if (!fHunkResult.isOK()) {
			LocalResourceManager imageCache = PatchCompareEditorInput.getImageCache(fHunkResult.getDiffResult().getConfiguration());
			return getHunkErrorImage(null, imageCache, false);
		} 
		return null;
	}

	public static Image getHunkErrorImage(Image baseImage, LocalResourceManager imageCache, boolean onLeft) {
		ImageDescriptor desc = new DiffImage(baseImage, CompareUIPlugin.getImageDescriptor(ICompareUIConstants.ERROR_OVERLAY), ICompareUIConstants.COMPARE_IMAGE_WIDTH, onLeft);
		Image image = imageCache.createImage(desc);
		return image;
	}

	public boolean isManuallyMerged() {
		return getPatcher().isManuallyMerged(getHunkResult().getHunk());
	}

	private Patcher getPatcher() {
		return Patcher.getPatcher(fHunkResult.getDiffResult().getConfiguration());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getName()
	 */
	public String getName() {
		return fHunkResult.getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getType()
	 */
	public String getType() {
		return fHunkResult.getDiffResult().getDiff().getTargetPath(fHunkResult.getDiffResult().getConfiguration()).getFileExtension();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IStreamContentAccessor#getContents()
	 */
	public InputStream getContents() throws CoreException {
		String contents = fHunkResult.getContents(fIsAfterState, fFullContext);
		return fHunkResult.asInputStream(contents);
	}

	public String getCharset() throws CoreException {
		return fHunkResult.getCharset();
	}

	public HunkResult getHunkResult() {
		return fHunkResult;
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IHunk.class)
			return fHunkResult;
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}

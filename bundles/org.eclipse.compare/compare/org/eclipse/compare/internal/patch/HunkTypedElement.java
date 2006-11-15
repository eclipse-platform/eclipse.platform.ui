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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;

public class HunkTypedElement implements ITypedElement, IEncodedStreamContentAccessor, IHunkDescriptor {

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
			LocalResourceManager imageCache = PatchCompareEditorInput.getImageCache(fHunkResult.getDiffResult().getPatcher());
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
		return getHunkResult().getDiffResult().getPatcher().isManuallyMerged(getHunkResult().getHunk());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getName()
	 */
	public String getName() {
		return fHunkResult.getHunk().getDescription();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getType()
	 */
	public String getType() {
		return fHunkResult.getDiffResult().getPatcher().getPath(fHunkResult.getDiffResult().getDiff()).getFileExtension();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IStreamContentAccessor#getContents()
	 */
	public InputStream getContents() throws CoreException {
		String contents = fHunkResult.getContents(fIsAfterState, fFullContext);
		String charSet = getCharset();
		byte[] bytes = null;
		if (charSet != null) {
			try {
				bytes = contents.getBytes(charSet);
			} catch (UnsupportedEncodingException e) {
				CompareUIPlugin.log(e);
			}
		}
		if (bytes == null) {
			bytes = contents.getBytes();
		}
		return new ByteArrayInputStream(bytes);
	}

	public String getCharset() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public HunkResult getHunkResult() {
		return fHunkResult;
	}

}

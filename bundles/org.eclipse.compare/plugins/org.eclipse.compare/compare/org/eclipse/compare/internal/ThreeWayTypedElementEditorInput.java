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
package org.eclipse.compare.internal;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

public class ThreeWayTypedElementEditorInput extends AbstractTypedElementEditorInput {

	private final ICompareInput compareInput;
	private final char leg;
	
	public ThreeWayTypedElementEditorInput(ICompareInput compareInput, char leg) {
		this.compareInput = compareInput;
		this.leg = leg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		ImageDescriptor desc = super.getImageDescriptor();
		if (desc != null)
			return desc;
		Image image = compareInput.getImage();
		if (image != null)
			return ImageDescriptor.createFromImage(image);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		String name = super.getName();
		if (name != null)
			return name;
		return compareInput.getName();
	}
	
	public ITypedElement getTypedElement() {
		if (leg == MergeViewerContentProvider.LEFT_CONTRIBUTOR)
			return compareInput.getLeft();
		if (leg == MergeViewerContentProvider.RIGHT_CONTRIBUTOR)
			return compareInput.getRight();
		if (leg == MergeViewerContentProvider.ANCESTOR_CONTRIBUTOR)
			return compareInput.getAncestor();
		return null;
	}
	
	public ITypedElement getOtherElement() {
		if (leg == MergeViewerContentProvider.LEFT_CONTRIBUTOR) {
			return compareInput.getRight();
		}
		return compareInput.getLeft();
	}

	public ICompareInput getCompareInput() {
		return compareInput;
	}
	
	public char getLeg() {
		return leg;
	}
	
	public String getEncoding() {
		String encoding = super.getEncoding();
		if (encoding != null)
			return encoding;
		encoding = getStreamEncoding(getOtherElement());
		if (encoding != null)
			return encoding;
		return null;
	}
	
	protected void doSave(IDocument document, IProgressMonitor monitor) throws CoreException {
		ITypedElement typedElement= getTypedElement();
		if (typedElement == null) {
			// If there is no element on target side, copy the element from the other side
			// TODO: This can cause problems as it would change the hash code for this instance
			compareInput.copy(getLeg() == MergeViewerContentProvider.RIGHT_CONTRIBUTOR);
			typedElement= getTypedElement();
		}
		super.doSave(document, monitor);
		// TODO: I would like to removed the following hack if possible
		if (compareInput instanceof ResourceCompareInput.MyDiffNode)
			((ResourceCompareInput.MyDiffNode)compareInput).fireChange();		
	}
	
}

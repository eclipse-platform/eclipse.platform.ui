/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.resources.IFile;

/**
 *
 */
public class ElementStateListener implements IElementStateListener2 {
	
	private IFileBufferListener fListener;
	private IFileBuffer fFile;
	
	public ElementStateListener(IFileBufferListener bufferedFileListener, IFileBuffer file) {
		fListener= bufferedFileListener;
		fFile= file;
	}

	private boolean isAffected(Object element) {
		return element == fFile.getUnderlyingFile();
	}
	
	/*
	 * @see org.eclipse.core.buffer.internal.text.IElementStateListener2#elementDirtyStateChanged(java.lang.Object, boolean)
	 */
	public void elementDirtyStateChanged(Object element, boolean isDirty) {
		if (isAffected(element))
			fListener.dirtyStateChanged(fFile, isDirty);
	}


	/*
	 * @see org.eclipse.core.buffer.internal.text.IElementStateListener2#documentContentAboutToBeReplaced(java.lang.Object)
	 */
	public void documentContentAboutToBeReplaced(Object element) {
		if (isAffected(element))
			fListener.bufferContentAboutToBeReplaced(fFile);
	}

	/*
	 * @see org.eclipse.core.buffer.internal.text.IElementStateListener2#documentContentReplaced(java.lang.Object)
	 */
	public void documentContentReplaced(Object element) {
		if (isAffected(element))
			fListener.bufferContentReplaced(fFile);
	}

	/*
	 * @see org.eclipse.core.buffer.internal.text.IElementStateListener2#elementStateValidationChanged(java.lang.Object, boolean)
	 */
	public void elementStateValidationChanged(Object element, boolean isStateValidated) {
		if (isAffected(element))
			fListener.stateValidationChanged(fFile, isStateValidated);
	}

	/*
	 * @see org.eclipse.core.buffer.internal.text.IElementStateListener2#elementStateChanging(java.lang.Object)
	 */
	public void elementStateChanging(Object element) {
		if (isAffected(element))
			fListener.stateChanging(fFile);
	}

	/*
	 * @see org.eclipse.core.buffer.internal.text.IElementStateListener2#elementStateChangeFailed(java.lang.Object)
	 */
	public void elementStateChangeFailed(Object element) {
		if (isAffected(element))
			fListener.stateChangeFailed(fFile);
	}

	public IFileBufferListener getBufferedListener() {
		return fListener;
	}

	/*
	 * @see org.eclipse.core.buffer.internal.text.IElementStateListener2#elementMoved(java.lang.Object, java.lang.Object)
	 */
	public void elementMoved(Object originalElement, Object movedElement) {
		if (isAffected(originalElement))
			fListener.underlyingFileMoved(fFile, (IFile) movedElement);
	}

	/*
	 * @see org.eclipse.core.buffer.internal.text.IElementStateListener2#elementDeleted(java.lang.Object)
	 */
	public void elementDeleted(Object element) {
		if (isAffected(element))
			fListener.underlyingFileDeleted(fFile);
	}
}

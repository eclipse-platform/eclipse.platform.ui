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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;

/**
 *
 */
public class TextFileBufferManager implements ITextFileBufferManager {
	
	private FileDocumentProvider2 fFileDocumentProvider;
	private Map fManagedFiles= new HashMap();
	
	public TextFileBufferManager()  {
		fFileDocumentProvider= new FileDocumentProvider2(new ExtensionsRegistry());
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFileManager#connect(org.eclipse.core.resources.IFile)
	 */
	public void connect(IFile file) throws CoreException {
		Assert.isNotNull(file);
		IFileBuffer bufferedFile= getTextFileBuffer(file);
		if (bufferedFile == null)  {
			bufferedFile= createBufferedFile(file);
			fManagedFiles.put(file, bufferedFile);
		}
		fFileDocumentProvider.connect(file);
	}

	protected FileBuffer createBufferedFile(IFile file) {
		return isTextFile(file) ? new TextFileBuffer(file, fFileDocumentProvider) : new FileBuffer(file, fFileDocumentProvider);
	}
	
	protected boolean isTextFile(IFile file) {
		return true;
	}
	
	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFileManager#disconnect(org.eclipse.core.resources.IFile)
	 */
	public void disconnect(IFile file) throws CoreException {
		Assert.isNotNull(file);
		fFileDocumentProvider.disconnect(file);
		if (fFileDocumentProvider.getDocument(file) == null)
			fManagedFiles.remove(file);
	}
	
	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFileManager#getBufferedFile(org.eclipse.core.resources.IFile)
	 */
	public ITextFileBuffer getTextFileBuffer(IFile file) {
		return (ITextFileBuffer) fManagedFiles.get(file);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFileManager#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		return fFileDocumentProvider.getDefaultEncoding();
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#createEmptyDocument(org.eclipse.core.resources.IFile)
	 */
	public IDocument createEmptyDocument(IFile file) {
		return fFileDocumentProvider.createEmptyDocument(file);
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IRegion;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * A file context can be used to annotate a </code>RefactoringStatusEntry<code> with
 * detailed information about a problem detected in an <code>IFile</code>.
 * 
 * @since 3.0
 */
public class FileStatusContext extends RefactoringStatusContext {

	private IFile fFile;
	private IRegion fSourceRegion;

	/**
	 * Creates an status entry context for the given file and source region.
	 * 
	 * @param file the file that has caused the problem. Must not be <code>
	 *  null</code>
	 * @param region the source region of the problem inside the given file or
	 *  <code>null</code> if now source region is known
	 */
	public FileStatusContext(IFile file, IRegion region) {
		Assert.isNotNull(file);
		fFile= file;
		fSourceRegion= region;
	}

	/**
	 * Returns the context's file.
	 * 
	 * @return the context's file
	 */
	public IFile getFile() {
		return fFile;
	}
	
	/**
	 * Returns the context's source region
	 * 
	 * @return the context's source region
	 */
	public IRegion getTextRegion() {
		return fSourceRegion;
	}
	
	/* (non-Javadoc)
	 * Method declared on RefactoringStatusContext.
	 */
	public Object getCorrespondingElement() {
		return getFile();
	}	
}


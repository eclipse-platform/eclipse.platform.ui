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
package org.eclipse.team.internal.ui.history;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.StorageTypedElement;

public class FileRevisionTypedElement extends StorageTypedElement {

	IFileRevision fileRevision;
	IFile file;
	
	public FileRevisionTypedElement(IFileRevision fileRevision){
		this.fileRevision = fileRevision;
		this.file = null;
	}
	
	public FileRevisionTypedElement(IFile file){
		this.file = file;
		this.fileRevision = null;
	}
	public String getName() {
		if (file != null)
			return file.getName();
		
		return fileRevision.getName();
	}

	public Image getImage() {
		return null;
	}

	protected IStorage getElementStorage(IProgressMonitor monitor) throws CoreException {
		if (file != null)
			return file;
	
		return fileRevision.getStorage(monitor);
	
	}

	public boolean isEditable() {
		return true;
	}

	public ITypedElement replace(ITypedElement dest, ITypedElement src) {
		return null;
	}

	public String getContentIdentifier() {
		if (file != null)
			return file.getName();
		
		return fileRevision.getContentIdentifier();
	}
	
	public String getTimestamp() {
		long date = 0;
		if (file != null) {
			date = file.getModificationStamp();
		} else {
			date = fileRevision.getTimestamp();
		}
		Date dateFromLong = new Date(date);
		return DateFormat.getDateTimeInstance().format(dateFromLong);
	}
	
	public String getComment() {
		if (file != null)
			return ""; //$NON-NLS-1$
		
		return fileRevision.getComment();
	}
	
	/*
	 * Can return either an IFile or an IFileRevision
	 */
	public Object getFileRevision(){
		if (file != null)
			return file;
		
		return fileRevision;
	}

}

/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import java.util.Date;

import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.ibm.icu.text.DateFormat;

public class FileRevisionEditorInput extends PlatformObject implements IWorkbenchAdapter,IStorageEditorInput {

	private final IFileRevision fileRevision;
	private final IStorage storage;
	
	/**
	 * Creates FileRevisionEditorInput on the given revision.
	 * @param revision the file revision
	 * @param monitor 
	 * @return a file revision editor input
	 * @throws CoreException 
	 */
	public static FileRevisionEditorInput createEditorInputFor(IFileRevision revision, IProgressMonitor monitor) throws CoreException {
		IStorage storage = revision.getStorage(monitor);
		return new FileRevisionEditorInput(revision, storage);
	}
	
	/**
	 * Creates FileRevisionEditorInput on the given revision.
	 * @param revision the file revision
	 * @param storage the contents of the file revision
	 */
	public FileRevisionEditorInput(IFileRevision revision, IStorage storage) {
		this.fileRevision = revision;
		this.storage = storage;
	}
	
	public FileRevisionEditorInput(IFileState state) {
		this.storage = state;
		this.fileRevision = null;
	}

	public IFileRevision getFileRevision() {
		return fileRevision;
	}
	
	public IStorage getStorage() throws CoreException {
		return storage;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		if (fileRevision != null)
			return NLS.bind(TeamUIMessages.nameAndRevision, new String[] { fileRevision.getName(), fileRevision.getContentIdentifier()});
		
		if (storage != null){
			return storage.getName() +  " " + DateFormat.getInstance().format(new Date(((IFileState) storage).getModificationTime())) ; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
		
	}

	public IPersistableElement getPersistable() {
		//can't persist
		return null;
	}

	public String getToolTipText() {
		if (fileRevision != null)
			try {
				return getStorage().getFullPath().toString();
			} catch (CoreException e) {
			}
		
		if (storage != null)
			return storage.getFullPath().toString();
		
		return ""; //$NON-NLS-1$
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		if (adapter == IFileRevision.class)
			return fileRevision;
		return super.getAdapter(adapter);
	}

	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	public String getLabel(Object o) {
		if (fileRevision != null)
			return fileRevision.getName();
		
		if (storage != null){
			return storage.getName();
		}
		return ""; //$NON-NLS-1$
	}

	public Object getParent(Object o) {
		return null;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof FileRevisionEditorInput) {
			FileRevisionEditorInput other = (FileRevisionEditorInput) obj;
			return (other.getFileRevision().equals(getFileRevision()));
		}
		return false;
	}
	
	public int hashCode() {
		return getFileRevision().hashCode();
	}

}

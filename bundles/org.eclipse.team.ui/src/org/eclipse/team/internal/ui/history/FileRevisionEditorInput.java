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

import com.ibm.icu.text.DateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class FileRevisionEditorInput extends PlatformObject implements IWorkbenchAdapter,IStorageEditorInput {

	private IFileRevision file;
	private IStorage storage;
	
	/**
	 * Creates FileRevisionEditorInput on the given file.
	 */
	public FileRevisionEditorInput(IFileRevision file) {
		this.file = file;
		//TODO: need a monitor
		try {
			this.storage = file.getStorage(new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Need to get storage before creating input
			TeamUIPlugin.log(e);
		}
	}
	
	public FileRevisionEditorInput(IFileState state) {
		this.storage = state;
		this.file = null;
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
		if (file != null)
			return NLS.bind(TeamUIMessages.nameAndRevision, new String[] { file.getName(), file.getContentIdentifier()});
		
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
		if (file != null)
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
			return file;
		return super.getAdapter(adapter);
	}

	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel(Object o) {
		if (file != null)
			return file.getName();
		
		if (storage != null){
			return storage.getName();
		}
		return ""; //$NON-NLS-1$
	}

	public Object getParent(Object o) {
		return null;
	}


}

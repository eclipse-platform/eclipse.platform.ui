package org.eclipse.team.internal.ui.filehistory;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.filehistory.IFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class FileRevisionEditorInput implements IWorkbenchAdapter,IStorageEditorInput {

	private IFileRevision file;
	private IStorage storage;
	
	/**
	 * Creates FileRevisionEditorInput on the given file.
	 */
	public FileRevisionEditorInput(IFileRevision file) {
		this.file = file;
		//TODO: need a monitor
		this.storage = file.getStorage(new NullProgressMonitor());
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
		return NLS.bind(TeamUIMessages.nameAndRevision, new String[] { file.getName(), file.getContentIndentifier()}); 
	}

	public IPersistableElement getPersistable() {
		//can't persist
		return null;
	}

	public String getToolTipText() {
		// TODO Auto-generated method stub
		return file.getContentIndentifier();
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		return null;
	}

	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel(Object o) {
		return file.getName();
	}

	public Object getParent(Object o) {
		return null;
	}


}

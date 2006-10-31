package org.eclipse.compare.internal.patch;

import java.io.InputStream;

import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.graphics.Image;

public class PatchedFileWrapper implements ITypedElement, IStreamContentAccessor, IEditableContent, IContentChangeNotifier {

	private PatchedFileNode patchedFile;
	private ListenerList fListener;
	private PatcherDiffNode parentNode;
	
	public PatchedFileWrapper(PatchedFileNode patchedFile) {
		this.patchedFile = patchedFile;
	}

	public Image getImage() {
		return patchedFile.getImage();
	}

	public String getName() {
		return patchedFile.getName();
	}

	public String getType() {
		return patchedFile.getType();
	}

	public InputStream getContents() throws CoreException {
		return patchedFile.getContents();
	}

	public boolean isEditable() {
		return patchedFile.isEditable();
	}

	public ITypedElement replace(ITypedElement dest, ITypedElement src) {
		return patchedFile.replace(dest, src);
	}

	public void setContent(byte[] newContent) {
		patchedFile.setContent(newContent);
		fireNotification();
	}

	/*
	 * Registers a listener for changes of this node.
	 * Has no effect if an identical listener is already registered.
	 * @see org.eclipse.compare.IContentChangeNotifier#addContentChangeListener(org.eclipse.compare.IContentChangeListener)
	 */
	public void addContentChangeListener(IContentChangeListener listener) {
		if (fListener == null)
			fListener = new ListenerList();
		fListener.add(listener);
	}

	/* 
	 * Unregisters a listener for changd to this node.
	 * Has no effect if listener is not registered.
	 * @see org.eclipse.compare.IContentChangeNotifier#removeContentChangeListener(org.eclipse.compare.IContentChangeListener)
	 */
	public void removeContentChangeListener(IContentChangeListener listener) {
		if (fListener != null) {
			fListener.remove(listener);
			if (fListener.isEmpty())
				fListener = null;
		}

	}

	/*
	 * Sends out notification that a change has occurred on the node.
	 */
	protected void fireNotification() {
		if (fListener != null) {
			Object[] listeners = fListener.getListeners();
			for (int i = 0; i < listeners.length; i++)
				((IContentChangeListener) listeners[i]).contentChanged(this);
		}
	}

	public void setParent(PatcherDiffNode parentNode) {
		this.parentNode = parentNode;
	}
	
	public PatcherDiffNode getParent(){
		return parentNode;
	}
	
	public void setPatchedFile(PatchedFileNode patchedFileNode){
		this.patchedFile = patchedFileNode;
	}
	
	public PatchedFileNode getPatchedFilNode(){
		return patchedFile;
	}

}

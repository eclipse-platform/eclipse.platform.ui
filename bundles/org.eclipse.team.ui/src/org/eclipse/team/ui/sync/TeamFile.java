package org.eclipse.team.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.InputStream;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A sync node for a version-controlled file, where the left
 * side is the local file, the right side is the remote file,
 * and the ancestor is the common file.
 */
class TeamFile extends DiffElement implements ICompareInput, ITeamNode {
	private MergeResource mergeResource;
	private Shell shell;
	private ListenerList listeners;
	private TypedBufferedContent localByteContents;
	private TypedBufferedContent commonByteContents;
	private TypedBufferedContent remoteByteContents;
	
	/**
	 * Creates a new file node.
	 */	
	public TeamFile(IDiffContainer parent, MergeResource res, int changeType) {
		super(parent, changeType);
		Assert.isNotNull(res);
		this.mergeResource = res;
	
		commonByteContents = new TypedBufferedContent(this, false) {
			public InputStream createStream() throws CoreException {
				return mergeResource.getBaseRevision();
			}
		};
		remoteByteContents = new TypedBufferedContent(this, false) {
			protected InputStream createStream() throws CoreException {
				return mergeResource.getLatestRevision();
			}
		};
		localByteContents = new TypedBufferedContent(this, true) {
			protected InputStream createStream() throws CoreException {
				return mergeResource.getLocalStream();
			}
			public void setContent(byte[] contents) {
				super.setContent(contents);
				merged();
				fireContentChanged();
			}
			public ITypedElement replace(ITypedElement child, ITypedElement other) {
				System.out.println("TeamFile.replace");
				return null;
			}
		};
	}

	public void addCompareInputChangeListener(ICompareInputChangeListener l) {
		if (listeners == null) {
			listeners = new ListenerList();
		}
		listeners.add(l);
	}
	
	public void copy(boolean leftToRight) {
		if (!leftToRight) {
			// Catchup to all on the server
			localByteContents.setContent(remoteByteContents.getContent());
			merged();
		}
	}
	public boolean equals(Object other) {
		if (other != null && other.getClass() == getClass()) {
			TeamFile file = (TeamFile) other;
			return mergeResource.equals(file.mergeResource);
		}
		return super.equals(other);
	}

	private void fireThreeWayInputChange() {
		if (listeners != null) {
			Object[] listenerArray = listeners.getListeners();
			// Iterate backwards so that the model gets updated last
			// it might want to remove the node completely, which might
			// upset other listeners.
			for (int i = listenerArray.length; --i >= 0;)
				 ((ICompareInputChangeListener) listenerArray[i]).compareInputChanged(this);
		}
	}
	
	/*
	 * @see ICompareInput#getAncestor
	 */
	public ITypedElement getAncestor() {
		if (mergeResource.hasBaseRevision()) {
			return commonByteContents;
		} else {
			// XXX return local byte contents if there is no ancestor
			// will allow the compare view to show 2-way diff instead
			// of a conflict window.
			return localByteContents;
		}
	}
	
	/*
	 * Method declared on ITeamNode.
	 */
	public int getChangeDirection() {
		return getKind() & Differencer.DIRECTION_MASK;
	}
	
	/*
	 * @see ITypedInput#getType
	 */
	public Image getImage() {
		return CompareUI.getImage(getType());
	}
	
	/*
	 * @see ICompareInput#getLeft
	 */
	public ITypedElement getLeft() {
		return localByteContents;
	}
	
	/**
	 * Returns the team resource managed by this object.
	 * Guaranteed to be non-null.
	 */
	public MergeResource getMergeResource() {
		return mergeResource;
	}
	
	/*
	 * @see ITypedInput#getName
	 */
	public String getName() {
		return mergeResource.getName();
	}

	/**
	 * Returns the core resource managed by this object.
	 * Guaranteed to be non-null.
	 */
	public IResource getResource() {
		return mergeResource.getResource();
	}
	
	/*
	 * @see ICompareInput#getRight
	 */
	public ITypedElement getRight() {
		if (mergeResource.hasLatestRevision()) {
			return remoteByteContents;
		} else {
			return null;
		}
	}
	/*
	 * @see ITypedInput#getType
	 */
	public String getType() {
		return mergeResource.getExtension();
	}
	
	/*
	 * @see Object
	 */
	public int hashCode() {
		return mergeResource.hashCode();
	}
	
	/**
	 * The local resource has beed modified (i.e. merged).
	 */
	private void merged() {
		mergeResource.confirmMerge();
		try {
			saveChanges();
			setKind(OUTGOING | (getKind() & Differencer.CHANGE_TYPE_MASK));
			fireThreeWayInputChange();
		} catch (CoreException e) {
			ErrorDialog.openError(WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), Policy.bind("TeamFile.saveChanges", getName()), null, e.getStatus());
		}
	}
	
	public void removeCompareInputChangeListener(ICompareInputChangeListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Saves cached copy to disk and clears dirty flag.
	 */
	private void saveChanges() throws CoreException {
		InputStream stream = localByteContents.getContents();
		IFile file = (IFile) getResource();
		if (stream != null) {
			if (!file.exists()) {
				file.create(stream, false, null);
			} else {
				file.setContents(stream, false, true, null);
			}
		} else {
			file.delete(false, true, null);
		}
	}

	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return "TeamFile(" + mergeResource.getName() + ")";
	}
}

package org.eclipse.team.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.TeamUIPlugin;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * A sync node for a version-controlled file, where the left
 * side is the local file, the right side is the remote file,
 * and the ancestor is the common file.
 */
public class TeamFile extends DiffElement implements ICompareInput, ITeamNode {
	private MergeResource mergeResource;
	private Shell shell;
	private ListenerList listeners;
	private TypedBufferedContent localByteContents;
	private TypedBufferedContent commonByteContents;
	private TypedBufferedContent remoteByteContents;
	
	/**
	 * Creates a new file node.
	 */	
	public TeamFile(IDiffContainer parent, MergeResource res, int changeType, Shell shell) {
		super(parent, changeType);
		Assert.isNotNull(res);
		this.mergeResource = res;
		this.shell = shell;
	
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
		// don't allow editing of outgoing deletion content. To revert from the deletion the
		// user should use the appropriate sync view action.
		boolean outgoingDeletion = getChangeDirection() == IRemoteSyncElement.OUTGOING && getChangeType() ==  IRemoteSyncElement.DELETION;
		localByteContents = new TypedBufferedContent(this, !outgoingDeletion) {
			protected InputStream createStream() throws CoreException {
				return mergeResource.getLocalStream();
			}
			public void setContent(byte[] contents) {
				super.setContent(contents);
				merged();
				fireContentChanged();
			}
			public ITypedElement replace(ITypedElement child, ITypedElement other) {
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
		}
		return null;
	}
	
	/*
	 * Method declared on ITeamNode.
	 */
	public int getChangeDirection() {
		return getKind() & Differencer.DIRECTION_MASK;
	}
	
	/*
	 * @see ITeamNode#getChangeType()
	 */
	public int getChangeType() {
		return getKind() & Differencer.CHANGE_TYPE_MASK;
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
			// persist changes to disk (e.g. there is no buffering in the sync view).
			saveChanges();
			
			// calculate the new sync state based on the type of change that was merged. This
			// logic cannot be in the IRemoteSyncElement because there is no way to update the
			// base before calling getSyncKind() again.
			if(getChangeDirection()==INCOMING) {
				switch(getChangeType()) {
					case Differencer.ADDITION:
					case Differencer.CHANGE:
						setKind(OUTGOING | Differencer.CHANGE);	
						break;
					case Differencer.DELETION:
						setKind(CONFLICTING | Differencer.CHANGE);
				}						
			} else {
				setKind(OUTGOING | (getKind() & Differencer.CHANGE_TYPE_MASK));
			}
			
			// update the UI with the sync state change.
			fireThreeWayInputChange();
		} catch (CoreException e) {
			ErrorDialog.openError(WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), Policy.bind("TeamFile.saveChanges", getName()), null, e.getStatus()); //$NON-NLS-1$
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
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					InputStream stream = localByteContents.getContents();
					IFile file = (IFile) getResource();
					if (stream != null) {
						if (!file.exists()) {
							file.create(stream, false, monitor);
						} else {
							file.setContents(stream, false, true, monitor);
						}
					} else {
						file.delete(false, true, monitor);
					}
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		});
	}
	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return "TeamFile(" + mergeResource.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void run(IRunnableWithProgress runnable) throws CoreException {
		try {
			new ProgressMonitorDialog(shell).run(false, false, runnable);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof CoreException) {
				throw (CoreException)e.getTargetException();
			} else {
				throw new CoreException(new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, Policy.bind("simpleInternal"), e.getTargetException())); //$NON-NLS-1$
			}
		} catch (InterruptedException e) {
			// Ignore
		}
	}
}

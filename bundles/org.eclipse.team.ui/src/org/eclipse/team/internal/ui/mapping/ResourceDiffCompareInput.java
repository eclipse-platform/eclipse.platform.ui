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
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.mapping.ISynchronizationCompareInput;
import org.eclipse.team.ui.mapping.SaveableComparison;

/**
 * A resource-based compare input that gets it's contributors from an {@link IDiff}.
 */
public class ResourceDiffCompareInput implements ISynchronizationCompareInput, IAdaptable, IResourceProvider {

	private IDiff node;
	private final ISynchronizationContext context;
	private FileRevisionTypedElement ancestor;
	private LocalResourceTypedElement left;
	private FileRevisionTypedElement right;
	private int kind;
	private final ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	/**
	 * Create the compare input on the given diff.
	 * @param diff the diff
	 * @param context the synchronization context
	 */
	public ResourceDiffCompareInput(IDiff diff, ISynchronizationContext context) {
		this.node = diff;
		this.context = context;
		this.kind = getCompareKind(diff);
		this.ancestor = getAncestor(diff);
		this.left = getLeftContributor(diff);
		this.right = getRightContributor(diff);
	}
	
	/**
	 * Fire a compare input change event.
	 * TODO: This method is public so that the change can be fired 
	 * by the containing editor input on a save.
	 */
	public void fireChange() {
		if (!listeners.isEmpty()) {
			Object[] allListeners = listeners.getListeners();
			for (int i = 0; i < allListeners.length; i++) {
				final ICompareInputChangeListener listener = (ICompareInputChangeListener)allListeners[i];
				SafeRunner.run(new ISafeRunnable() {
					public void run() throws Exception {
						listener.compareInputChanged(ResourceDiffCompareInput.this);
					}
					public void handleException(Throwable exception) {
						// Logged by the safe runner
					}
				});
			}
		}
	}
	
	public static int getCompareKind(IDiff node) {
		int compareKind = 0;
		if (node != null) {
			switch (node.getKind()) {
			case IDiff.ADD:
				compareKind = Differencer.ADDITION;
				break;
			case IDiff.REMOVE:
				compareKind = Differencer.DELETION;
				break;
			case IDiff.CHANGE:
				compareKind = Differencer.CHANGE;
				break;
			}
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;			
				switch (twd.getDirection()) {
				case IThreeWayDiff.OUTGOING :
					compareKind |= Differencer.RIGHT;
					break;
				case IThreeWayDiff.INCOMING :
					compareKind |= Differencer.LEFT;
					break;
				case IThreeWayDiff.CONFLICTING :
					compareKind |= Differencer.LEFT;
					compareKind |= Differencer.RIGHT;
					break;
				}
			}
		}
		return compareKind;
	}
	
	private static FileRevisionTypedElement getRightContributor(IDiff node) {
		// For a resource diff, use the after state
		if (node instanceof IResourceDiff) {
			IResourceDiff rd = (IResourceDiff) node;
			return asTypedElement(rd.getAfterState(), getLocalEncoding(node));
		}
		if (node instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) node;
			IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
			// If there is a remote change, use the after state
			if (diff != null)
				return getRightContributor(diff);
			// There's no remote change so use the before state of the local
			diff = (IResourceDiff)twd.getLocalChange();
			return asTypedElement(diff.getBeforeState(), getLocalEncoding(node));
			
		}
		return null;
	}

	private static LocalResourceTypedElement getLeftContributor(final IDiff node) {
		// The left contributor is always the local resource
		return new LocalResourceTypedElement(ResourceDiffTree.getResourceFor(node));
	}

	private static FileRevisionTypedElement getAncestor(IDiff node) {
		if (node instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) node;
			IResourceDiff diff = (IResourceDiff)twd.getLocalChange();
			if (diff == null)
				diff = (IResourceDiff)twd.getRemoteChange();
			return asTypedElement(diff.getBeforeState(), getLocalEncoding(node));
			
		}
		return null;
	}

	private static String getLocalEncoding(IDiff node) {
		IResource resource = ResourceDiffTree.getResourceFor(node);
		if (resource instanceof IEncodedStorage) {
			IEncodedStorage es = (IEncodedStorage) resource;
			try {
				return es.getCharset();
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
		}
		return null;
	}

	private static FileRevisionTypedElement asTypedElement(IFileRevision state, String localEncoding) {
		if (state == null)
			return null;
		return new FileRevisionTypedElement(state, localEncoding);
	}

	public static void ensureContentsCached(IDiff diff, IProgressMonitor monitor) throws CoreException {
		if (diff != null) {
			ensureContentsCached(getAncestor(diff), getRightContributor(diff), monitor);
		}
	}
	
	private static void ensureContentsCached(Object ancestor, Object right,
			IProgressMonitor monitor) throws CoreException {
		if (ancestor instanceof FileRevisionTypedElement) {
			FileRevisionTypedElement fste = (FileRevisionTypedElement) ancestor;
			fste.cacheContents(monitor);
		}
		if (right instanceof FileRevisionTypedElement) {
			FileRevisionTypedElement fste = (FileRevisionTypedElement) right;
			fste.cacheContents(monitor);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInput#prepareInput(org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void prepareInput(CompareConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		configuration.setLabelProvider(this, getChangeNotifier().getLabelProvider());
		ensureContentsCached(getAncestor(), getRight(), monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInput#getSaveable()
	 */
	public SaveableComparison getSaveable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IFile.class || adapter == IResource.class) {
			return ResourceDiffTree.getResourceFor(node);
		}
		if (adapter == ResourceMapping.class) {
			IResource resource = ResourceDiffTree.getResourceFor(node);
			return resource.getAdapter(adapter);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInput#getFullPath()
	 */
	public String getFullPath() {
		final IResource resource = ResourceDiffTree.getResourceFor(node);
		if (resource != null)
			return resource.getFullPath().toString();
		return getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInput#isCompareInputFor(java.lang.Object)
	 */
	public boolean isCompareInputFor(Object object) {
		final IResource resource = ResourceDiffTree.getResourceFor(node);
		IResource other = Utils.getResource(object);
		if (resource != null && other != null)
			return resource.equals(other);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IResourceProvider#getResource()
	 */
	public IResource getResource() {
		return ResourceDiffTree.getResourceFor(node);
	}

	/**
	 * Return a compare input change notifier that will detect changes in the synchronization context and
	 * translate them into compare input change events by calling {@link #update()}.
	 * @return a compare input change notifier
	 */
	public ResourceCompareInputChangeNotifier getChangeNotifier() {
		return ResourceCompareInputChangeNotifier.getChangeNotifier(context);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#addCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	public void addCompareInputChangeListener(
			ICompareInputChangeListener listener) {
		if (!containsListener(listener)) {
			listeners.add(listener);
			getChangeNotifier().connect(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#removeCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	public void removeCompareInputChangeListener(
			ICompareInputChangeListener listener) {
		if (containsListener(listener)) {
			listeners.remove(listener);
			getChangeNotifier().disconnect(this);
		}
	}
	
	private boolean containsListener(ICompareInputChangeListener listener) {
		if (listeners.isEmpty())
			return false;
		Object[] allListeners = listeners.getListeners();
		for (int i = 0; i < allListeners.length; i++) {
			Object object = allListeners[i];
			if (object == listener)
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other instanceof ResourceDiffCompareInput) {
			ResourceDiffCompareInput otherInput = (ResourceDiffCompareInput) other;
			return (isEqual(getLeft(), otherInput.getLeft())
					&& isEqual(getRight(), otherInput.getRight())
					&& isEqual(getAncestor(), otherInput.getAncestor()));
		}
		return false;
	}
	
	private boolean isEqual(ITypedElement e1, ITypedElement e2) {
		if (e1 == null) {
			return e2 == null;
		}
		if (e2 == null)
			return false;
		return e1.equals(e2);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#hashCode()
	 */
	public int hashCode() {
		return getResource().hashCode();
	}

	/**
	 * Re-obtain the diff for this compare input and update the kind and 3
	 * contributor appropriately.
	 */
	public void update() {
		IDiff newNode = context.getDiffTree().getDiff(getResource());
		if (newNode == null) {
			// The resource is in-sync. Just leave the ancestor and right the same and set the kind
			kind = Differencer.NO_CHANGE;
			fireChange();
		} else {
			LocalResourceTypedElement left = (LocalResourceTypedElement)getLeft();
			if (!this.node.equals(newNode) || !left.isSynchronized()) {
				this.node = newNode;
				kind = getCompareKind(node);
				left.update();
				FileRevisionTypedElement newRight = getRightContributor(node);
				propogateAuthorIfSameRevision(right, newRight);
				right = newRight;
				FileRevisionTypedElement newAncestor = getAncestor(node);
				propogateAuthorIfSameRevision(ancestor, newAncestor);
				ancestor = newAncestor;
				propogateAuthorIfSameRevision(ancestor, right);
			}
			fireChange();
		}
	}

	private boolean propogateAuthorIfSameRevision(FileRevisionTypedElement oldContributor,
			FileRevisionTypedElement newContributor) {
		if (oldContributor == null || newContributor == null)
			return false;
		String author = oldContributor.getAuthor();
		if (newContributor.getAuthor() == null 
				&& author != null
				&& oldContributor.getContentIdentifier().equals(newContributor.getContentIdentifier())) {
			newContributor.setAuthor(author);
			return true;
		}
		return false;
	}

	/**
	 * Return whether the diff associated with this input has changed.
	 * @return whether the diff associated with this input has changed
	 */
	public boolean needsUpdate() {
		IDiff newNode = context.getDiffTree().getDiff(getResource());
		return newNode == null || !newNode.equals(node);
	}

	public void copy(boolean leftToRight) {
		Assert.isTrue(false, "Copy is not support by this type of compare input"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getAncestor()
	 */
	public ITypedElement getAncestor() {
		return ancestor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getImage()
	 */
	public Image getImage() {
		return left.getImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffElement#getKind()
	 */
	public int getKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getLeft()
	 */
	public ITypedElement getLeft() {
		return left;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getName()
	 */
	public String getName() {
		return left.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getRight()
	 */
	public ITypedElement getRight() {
		return right;
	}

	/**
	 * Set the kind of this compare input
	 * @param kind the new kind
	 */
	public void setKind(int kind) {
		this.kind = kind;
	}

	/**
	 * Return the local content id for this compare input.
	 * @return the local content id for this compare input
	 */
	public String getLocalContentId() {
		return Utils.getLocalContentId(node);
	}

	public boolean updateAuthorInfo(IProgressMonitor monitor) throws CoreException {
		boolean authorFound = false;
		if (ancestor != null && ancestor.getAuthor() == null) {
			ancestor.fetchAuthor(monitor);
			if (right != null && propogateAuthorIfSameRevision(ancestor, right)) {
				return true;
			}
			authorFound = ancestor.getAuthor() != null;
		}
		if (right != null && right.getAuthor() == null) {
			right.fetchAuthor(monitor);
			authorFound |= right.getAuthor() != null;
		}
		return authorFound;
	}

}
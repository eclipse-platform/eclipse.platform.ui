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
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.mapping.*;

/**
 * A resource-based compare input that gets it's contributors from an {@link IDiff}.
 */
public class ResourceDiffCompareInput extends DiffNode implements ISynchronizationCompareInput, IAdaptable, IResourceProvider {

	private final IDiff node;

	/**
	 * Create the compare input on the given diff.
	 * @param diff the diff
	 */
	public ResourceDiffCompareInput(IDiff diff) {
		super(getCompareKind(diff), getAncestor(diff), getLeftContributor(diff), getRightContributor(diff));
		this.node = diff;
	}
	
	/**
	 * We need to make this public so we can fire a change event after
	 * we save 
	 */
	public void fireChange() {
		super.fireChange();
	}
	
	private static int getCompareKind(IDiff node) {
		int kind = 0;
		switch (node.getKind()) {
		case IDiff.CHANGE:
			kind = Differencer.CHANGE;
			break;
		case IDiff.ADD:
			kind = Differencer.ADDITION;
			break;
		case IDiff.REMOVE:
			kind = Differencer.DELETION;
			break;
		}
		if (node instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) node;
			switch (twd.getDirection()) {
			case IThreeWayDiff.INCOMING:
				kind |= Differencer.RIGHT;
				break;
			case IThreeWayDiff.OUTGOING:
				kind |= Differencer.LEFT;
				break;
			case IThreeWayDiff.CONFLICTING:
				kind |= Differencer.CONFLICTING;
				break;
			}
		}
		return kind;
	}
	
	private static ITypedElement getRightContributor(IDiff node) {
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

	private static ITypedElement getLeftContributor(final IDiff node) {
		// The left contributor is always the local resource
		return new LocalResourceTypedElement(ResourceDiffTree.getResourceFor(node));
	}

	private static ITypedElement getAncestor(IDiff node) {
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

	private static ITypedElement asTypedElement(IFileRevision state, String localEncoding) {
		if (state == null)
			return null;
		return new FileRevisionTypedElement(state, localEncoding);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IModelCompareInput#prepareInput(org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void prepareInput(CompareConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		Utils.updateLabels(node, configuration, monitor);
		// We need to cache contents here as well
		Object ancestor = getAncestor();
		if (ancestor instanceof FileRevisionTypedElement) {
			FileRevisionTypedElement fste = (FileRevisionTypedElement) ancestor;
			fste.cacheContents(monitor);
		}
		Object right = getRight();
		if (right instanceof FileRevisionTypedElement) {
			FileRevisionTypedElement fste = (FileRevisionTypedElement) right;
			fste.cacheContents(monitor);
		}
	}
	
	private boolean hasSaveConflict() {
		return ((LocalResourceTypedElement)getLeft()).hasSaveConflict();
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
			if (node instanceof IResourceDiff) {
				IResourceDiff rd = (IResourceDiff) node;
				return (IFile)rd.getResource();
			}
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;
				IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
				// If there is a remote change, use the after state
				if (diff != null)
					return (IFile)diff.getResource();
				// There's no remote change so use the before state of the local
				diff = (IResourceDiff)twd.getLocalChange();
				return (IFile)diff.getResource();
				
			}
		}
		return null;
	}

	public String getFullPath() {
		final IResource resource = ResourceDiffTree.getResourceFor(node);
		if (resource != null)
			return resource.getFullPath().toString();
		return getName();
	}

	public boolean isCompareInputFor(Object object) {
		final IResource resource = ResourceDiffTree.getResourceFor(node);
		IResource other = Utils.getResource(object);
		if (resource != null && other != null)
			return resource.equals(other);
		return false;
	}
	
	public boolean checkUpdateConflict() {
		if(hasSaveConflict()) {
			final MessageDialog dialog = 
				new MessageDialog(TeamUIPlugin.getStandardDisplay().getActiveShell(), 
						TeamUIMessages.SyncInfoCompareInput_0,  
						null, 
						TeamUIMessages.SyncInfoCompareInput_1,  
						MessageDialog.QUESTION,
					new String[] {
						TeamUIMessages.SyncInfoCompareInput_2, 
						IDialogConstants.CANCEL_LABEL}, 
					0);
			
			int retval = dialog.open();
			switch(retval) {
				// save
				case 0: 
					return false;
				// cancel
				case 1:
					return true;
			}
		}
		return false;
	}

	public IResource getResource() {
		return ResourceDiffTree.getResourceFor(node);
	}

	public ICompareInputChangeNotifier getChangeNotifier(ISynchronizationContext context) {
		ResourceCompareInputChangeNotifier notifier = (ResourceCompareInputChangeNotifier)context.getCache().get("org.eclipse.team.ui.ResourceChangeNotifier");
		if (notifier == null) {
			notifier = new ResourceCompareInputChangeNotifier(context);
			context.getCache().put("org.eclipse.team.ui.ResourceChangeNotifier", notifier);
		}
		return notifier;
	}
	
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

	public int hashCode() {
		return getResource().hashCode();
	}
}
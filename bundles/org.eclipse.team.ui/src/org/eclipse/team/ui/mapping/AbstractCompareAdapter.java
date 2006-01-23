/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileState;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ui.mapping.FileStateTypedElement;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;

/**
 * A abstract implementation of {@link ICompareAdapter}. Most of the methods
 * are no-ops except for the {@link #asCompareInput(ISynchronizationContext, Object) }
 * which will convert file objects to an appropriate compare input.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class AbstractCompareAdapter implements ICompareAdapter {

	private static class ResourceDiffCompareInput extends DiffNode {

		public ResourceDiffCompareInput(IDiffNode node) {
			super(getCompareKind(node), getAncestor(node), getLeftContributor(node), getRightContributor(node));
		}
		
		private static int getCompareKind(IDiffNode node) {
			switch (node.getKind()) {
			case IDiffNode.CHANGE:
				return Differencer.CHANGE;
			case IDiffNode.ADD:
				return Differencer.ADDITION;
			case IDiffNode.REMOVE:
				return Differencer.DELETION;
			}
			return 0;
		}
		
		private static ITypedElement getRightContributor(IDiffNode node) {
			// For a resource diff, use the after state
			if (node instanceof IResourceDiff) {
				IResourceDiff rd = (IResourceDiff) node;
				return asTypedElement(rd.getAfterState());
			}
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;
				IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
				// If there is a remote change, use the after state
				if (diff != null)
					return getRightContributor(diff);
				// There's no remote change so use the before state of the local
				diff = (IResourceDiff)twd.getLocalChange();
				return asTypedElement(diff.getBeforeState());
				
			}
			return null;
		}

		private static ITypedElement getLeftContributor(final IDiffNode node) {
			// The left contributor is always the local resource
			final IResource resource = ResourceDiffTree.getResourceFor(node);
			return new LocalResourceTypedElement(resource) {
				public boolean isEditable() {
					if(! resource.exists() && isOutgoingDeletion(node)) {
						return false;
					}
					return super.isEditable();
				}

				private boolean isOutgoingDeletion(IDiffNode node) {
					if (node instanceof IThreeWayDiff) {
						IThreeWayDiff twd = (IThreeWayDiff) node;
						return twd.getKind() == IDiffNode.REMOVE && twd.getDirection() == IThreeWayDiff.OUTGOING;
					}
					return false;
				}
			};
		}

		private static ITypedElement getAncestor(IDiffNode node) {
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;
				IResourceDiff diff = (IResourceDiff)twd.getLocalChange();
				if (diff == null)
					diff = (IResourceDiff)twd.getRemoteChange();
				return asTypedElement(diff.getBeforeState());
				
			}
			return null;
		}

		private static ITypedElement asTypedElement(IFileState state) {
			if (state == null)
				return null;
			return new FileStateTypedElement(state);
		}

		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#prepareContext(org.eclipse.team.ui.mapping.ISynchronizationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void prepareContext(ISynchronizationContext context, IProgressMonitor monitor) throws CoreException {
		// Do nothing by default
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#asCompareInput(org.eclipse.team.ui.mapping.ISynchronizationContext, java.lang.Object)
	 */
	public ICompareInput asCompareInput(ISynchronizationContext context, Object o) {
		if (o instanceof IResource) {
			IResource resource = (IResource) o;
			if (resource.getType() == IResource.FILE) {
				IDiffNode node = context.getDiffTree().getDiff(resource);
				if (node != null)
					return new ResourceDiffCompareInput(node);
			}
		}
		return null;
	}

	/**
	 * @deprecated
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#findContentViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.viewers.Viewer, org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration)
	 */
	public Viewer findContentViewer(Composite parent, Viewer oldViewer,
			ICompareInput input, CompareConfiguration configuration) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#hasCompareInput(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object)
	 */
	public boolean hasCompareInput(ISynchronizationContext context, Object object) {
		return asCompareInput(context, object) != null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#countFor(org.eclipse.team.core.mapping.ISynchronizationContext, int, int)
	 */
	public long countFor(ISynchronizationContext context, int state, int mask) {
		return context.getDiffTree().countFor(state, mask);
	}

}

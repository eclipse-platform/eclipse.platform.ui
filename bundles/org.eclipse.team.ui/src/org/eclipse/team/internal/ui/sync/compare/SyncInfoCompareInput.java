/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.compare;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;

public class SyncInfoCompareInput extends CompareEditorInput {

	private SyncInfo sync;
	private SyncInfoDiffNode node;
	private static Image titleImage;
	
	/* protected */ SyncInfoCompareInput() {
		super(new CompareConfiguration());		
	}
	
	public SyncInfoCompareInput(SyncInfo sync) {
		super(new CompareConfiguration());
		this.sync = sync;
				
		ITypedElement elements[] = SyncInfoDiffNode.getTypedElements(sync);
		this.node = new SyncInfoDiffNode(elements[0] /* base */, elements[1] /* local */, elements[2] /* remote */, sync.getKind());
		initializeContentChangeListeners();
	}

	private void initializeContentChangeListeners() {
			ITypedElement te = node.getLeft();
			if(te instanceof IContentChangeNotifier) {
				((IContentChangeNotifier)te).addContentChangeListener(new IContentChangeListener() {
					public void contentChanged(IContentChangeNotifier source) {
						try {
							saveChanges(new NullProgressMonitor());
						} catch (CoreException e) {
						}
					}
				});
			}
		}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitleImage()
	 */
	public Image getTitleImage() {
		if(titleImage == null) {
			titleImage = TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_SYNC_VIEW).createImage();
			TeamUIPlugin.disposeOnShutdown(titleImage);
		}
		return titleImage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		// update the title now that the remote revision number as been fetched from the server
		setTitle(getTitle());
		updateLabels();
		return node;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {		
		return Policy.bind("SyncInfoCompareInput.title", sync.getSubscriber().getName(),  node.getName()); //$NON-NLS-1$
	}
	
	protected void updateLabels() {
		CompareConfiguration config = getCompareConfiguration();
		IRemoteResource remote = sync.getRemote();
		IRemoteResource base = sync.getBase();
		
		String localContentId = sync.getLocalContentIdentifier();
		if(localContentId != null) {		
			config.setLeftLabel(Policy.bind("SyncInfoCompareInput.localLabelExists", localContentId)); //$NON-NLS-1$
		} else {
			config.setLeftLabel(Policy.bind("SyncInfoCompareInput.localLabel")); //$NON-NLS-1$
		}
		
		if(remote != null) {
			try {
				config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabelExists", remote.getContentIdentifier(), remote.getCreatorDisplayName(), flattenText(remote.getComment()))); //$NON-NLS-1$
			} catch (TeamException e) {
				config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabel")); //$NON-NLS-1$
			}
		} else {
			config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabel")); //$NON-NLS-1$
		}
		
		if(base != null) {
			try {
				config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabelExists", base.getContentIdentifier(), base.getCreatorDisplayName(), flattenText(base.getComment()))); //$NON-NLS-1$
			} catch (TeamException e) {
				config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabel")); //$NON-NLS-1$
			}
		} else {
			config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabel")); //$NON-NLS-1$
		}
	}
	
	/*
	 * Flatten the text in the multiline comment
	 * @param string
	 * @return String
	 */
	private String flattenText(String string) {
		StringBuffer buffer = new StringBuffer(string.length() + 20);
		boolean skipAdjacentLineSeparator = true;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == '\r' || c == '\n') {
				if (!skipAdjacentLineSeparator)
					buffer.append("/"); //$NON-NLS-1$
				skipAdjacentLineSeparator = true;
			} else {
				buffer.append(c);
				skipAdjacentLineSeparator = false;
			}
		}
		return buffer.toString();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_SYNC_MODE_FREE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
	return Policy.bind("SyncInfoCompareInput.tooltip", sync.getSubscriber().getName(),  node.getName()); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if(other == this) return true;
		if(other instanceof SyncInfoCompareInput) {
			return equalDiffNodes(node, (SyncInfoDiffNode)((SyncInfoCompareInput)other).getCompareResult());
		}
		return false;
	}	
	
	private boolean equalDiffNodes(SyncInfoDiffNode node1, SyncInfoDiffNode node2) {
		
		if(node1 == null || node2 == null) {
			return false;
		}
		
		// First, ensure the local resources are equals
		IResource local1 = null;
		if (node1.getLeft() != null)
			local1 = ((LocalResourceTypedElement)node1.getLeft()).getResource();
		IResource local2 = null;
		if (node2.getLeft() != null)
			local2 = ((LocalResourceTypedElement)node2.getLeft()).getResource();
		if (!equalObjects(local1, local2)) return false;
		
		// Next, ensure the remote resources are equal
		IRemoteResource remote1 = null;
		if (node1.getRight() != null)
			remote1 = ((RemoteResourceTypedElement)node1.getRight()).getRemote();
		IRemoteResource remote2 = null;
		if (node2.getRight() != null)
			remote2 = ((RemoteResourceTypedElement)node2.getRight()).getRemote();
		if (!equalObjects(remote1, remote2)) return false;

		// Finally, ensure the base resources are equal
		IRemoteResource base1 = null;
		if (node1.getAncestor() != null)
			base1 = ((RemoteResourceTypedElement)node1.getAncestor()).getRemote();
		IRemoteResource base2 = null;
		if (node2.getAncestor() != null)
			base2 = ((RemoteResourceTypedElement)node2.getAncestor()).getRemote();
		if (!equalObjects(base1, base2)) return false;
		
		return true;
	}
	
	private boolean equalObjects(Object o1, Object o2) {
		if (o1 == null && o2 == null) return true;
		if (o1 == null || o2 == null) return false;
		return o1.equals(o2);
	}
	
	/* (non-Javadoc)
	 * @see CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
		if (node instanceof DiffNode) {
			try {
				commit(pm, (DiffNode) node);
			} finally {
				setDirty(false);	
			}
		}
	}
	
	/*
	 * Recursively walks the diff tree and commits all changes.
	 */
	private static void commit(IProgressMonitor pm, DiffNode node) throws CoreException {
		ITypedElement left= node.getLeft();
		if (left instanceof LocalResourceTypedElement)
			((LocalResourceTypedElement) left).commit(pm);
			
		ITypedElement right= node.getRight();
		if (right instanceof LocalResourceTypedElement)
			((LocalResourceTypedElement) right).commit(pm);
	}
	
	public SyncInfo getSyncInfo() {
		return sync;
	}
}

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
	
	public static SyncInfoCompareInput createInput(SyncInfo sync) {
				
		// Create the local ITypedElement
		ITypedElement localTypedElement = SyncInfoDiffNode.createTypeElement(sync.getLocal(), sync.getKind());
		
		// Create the remote ITypedElement
		ITypedElement remoteTypedElement = null;
		IRemoteResource remoteResource = sync.getRemote();
		if (remoteResource != null) {
			remoteTypedElement = SyncInfoDiffNode.createTypeElement(remoteResource);
		}
		
		// Create the base ITypedElement
		ITypedElement baseTypedElement = null;
		IRemoteResource baseResource = sync.getBase();
		if (baseResource != null) {
			baseTypedElement = SyncInfoDiffNode.createTypeElement(baseResource);
		}
		
		return new SyncInfoCompareInput(sync, new SyncInfoDiffNode(baseTypedElement, localTypedElement, remoteTypedElement, sync.getKind()));
	}

	private SyncInfoCompareInput(SyncInfo sync, SyncInfoDiffNode diffNode) {
		super(new CompareConfiguration());
		this.sync = sync;
		this.node = diffNode;
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
		try {
			node.cacheContents(monitor);
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		}
		return node;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {		
		return Policy.bind("SyncInfoCompareInput.title", sync.getSubscriber().getName(),  node.getName()); //$NON-NLS-1$
	}
	
	protected void updateLabels() {
		final CompareConfiguration config = getCompareConfiguration();
		final IRemoteResource remote = sync.getRemote();
		final IRemoteResource base = sync.getBase();
		
		String localContentId = sync.getLocalContentIdentifier();
		if(localContentId != null) {		
			config.setLeftLabel(Policy.bind("SyncInfoCompareInput.localLabelExists", localContentId)); //$NON-NLS-1$
		} else {
			config.setLeftLabel(Policy.bind("SyncInfoCompareInput.localLabel")); //$NON-NLS-1$
		}
		
		if(remote != null) {
			try {
				config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabelExists", remote.getContentIdentifier())); //$NON-NLS-1$
			} catch (TeamException e) {
				config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabel")); //$NON-NLS-1$
			}
		} else {
			config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabel")); //$NON-NLS-1$
		}
		
		if(base != null) {
			try {
				config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabelExists", base.getContentIdentifier())); //$NON-NLS-1$
			} catch (TeamException e) {
				config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabel")); //$NON-NLS-1$
			}
		} else {
			config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabel")); //$NON-NLS-1$
		}
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
			return getSyncInfo().equals(((SyncInfoCompareInput)other).getSyncInfo());
		}
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
		if (node != null) {
			try {
				commit(pm, node);
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
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
		return Policy.bind("SyncInfoCompareInput.title", sync.getSubscriber().getName(),  node.getName());
	}
	
	protected void updateLabels() {
		CompareConfiguration config = getCompareConfiguration();
		IRemoteResource remote = sync.getRemote();
		IRemoteResource base = sync.getRemote();
		
		config.setLeftLabel(Policy.bind("SyncInfoCompareInput.localLabel"));
		
		if(remote != null) {
			try {
				config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabelExists", remote.getContentIdentifier(), remote.getCreatorDisplayName(), flattenText(remote.getComment())));
			} catch (TeamException e) {
				config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabel"));
			}
		} else {
			config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabel"));
		}
		
		if(base != null) {
			try {
				config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabelExists", base.getContentIdentifier(), base.getCreatorDisplayName(), flattenText(base.getComment())));
			} catch (TeamException e) {
				config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabel"));
			}
		} else {
			config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabel"));
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
	return Policy.bind("SyncInfoCompareInput.tooltip", sync.getSubscriber().getName(),  node.getName());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if(other == this) return true;
		if(other instanceof SyncInfoCompareInput) {
			return node.equals(((SyncInfoCompareInput)other).getCompareResult());
		} else if(other instanceof SyncInfoCompareInputFinder) {
			return true;
		}
		return false;
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
}

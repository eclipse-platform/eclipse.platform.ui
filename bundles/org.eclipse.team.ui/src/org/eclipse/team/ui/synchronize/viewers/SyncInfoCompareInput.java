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
package org.eclipse.team.ui.synchronize.viewers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.ISharedImages;

/**
 * A {@link SyncInfo} editor input used as input to a two-way or three-way 
 * compare viewer. It defines methods for accessing the three sides for the 
 * compare, and a name and image which is used when displaying the three way input
 * in an editor. This input can alternatly be used to show compare results in 
 * a dialog by calling {@link CompareUI#openCompareDialog()}.
 * <p>
 * Supports saving the local resource that is changed in the editor.
 * </p>
 * <p>
 * Use {@link SynchronizeCompareInput} to display more than one <code>SyncInfo</code>
 * in an compare viewer. 
 * </p>
 * @see SyncInfoModelElement
 * @since 3.0
 */
public class SyncInfoCompareInput extends CompareEditorInput {

	private SyncInfoModelElement node;

	public SyncInfoCompareInput(SyncInfo sync) {
		super(new CompareConfiguration());
		Assert.isNotNull(sync);
		this.node = new SyncInfoModelElement(null, sync);
		initializeContentChangeListeners();
	}

	private void initializeContentChangeListeners() {
		ITypedElement te = node.getLeft();
		if (te instanceof IContentChangeNotifier) {
			((IContentChangeNotifier) te).addContentChangeListener(new IContentChangeListener() {
				public void contentChanged(IContentChangeNotifier source) {
					try {
						saveChanges(new NullProgressMonitor());
					} catch (CoreException e) {
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.compare.CompareEditorInput#getTitleImage()
	 */
	public Image getTitleImage() {
		ImageRegistry reg = TeamUIPlugin.getPlugin().getImageRegistry();
		Image image = reg.get(ISharedImages.IMG_SYNC_VIEW);
		if (image == null) {
			image = TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_SYNC_VIEW).createImage();
			reg.put(ISharedImages.IMG_SYNC_VIEW, image);
		}
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		// update the title now that the remote revision number as been fetched
		// from the server
		setTitle(getTitle());
		Utils.updateLabels(node.getSyncInfo(), getCompareConfiguration());
		try {
			node.cacheContents(monitor);
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		}
		return node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		return Policy.bind("SyncInfoCompareInput.title", node.getName()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_SYNC_MODE_FREE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return "";
		//return Policy.bind("SyncInfoCompareInput.tooltip", participant.getName(), node.getName()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other instanceof SyncInfoCompareInput) {
			return getSyncInfo().equals(((SyncInfoCompareInput) other).getSyncInfo());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
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

	private static void commit(IProgressMonitor pm, DiffNode node) throws CoreException {
		ITypedElement left = node.getLeft();
		if (left instanceof LocalResourceTypedElement)
			 ((LocalResourceTypedElement) left).commit(pm);

		ITypedElement right = node.getRight();
		if (right instanceof LocalResourceTypedElement)
			 ((LocalResourceTypedElement) right).commit(pm);
	}

	public SyncInfo getSyncInfo() {
		return node.getSyncInfo();
	}
}
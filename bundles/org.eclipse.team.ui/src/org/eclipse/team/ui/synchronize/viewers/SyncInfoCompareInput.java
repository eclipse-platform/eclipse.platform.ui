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

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

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
public final class SyncInfoCompareInput extends CompareEditorInput implements IResourceChangeListener {

	private MyDiffNode node;
	private String description;
	private IResource resource;
	private IEditorPart editor;

	private static class MyDiffNode extends SyncInfoModelElement {
		public MyDiffNode(IDiffContainer parent, SyncInfo info) {
			super(parent, info);
		}
		public void fireChange() {
			super.fireChange();
		}
	}
	
	/**
	 * Creates a compare editor input based on an existing <code>SyncInfo</code>.
	 * 
	 * @param description a description of the context of this sync info. This
	 * is displayed to the user.
	 * @param sync the <code>SyncInfo</code> used as the base for the compare input.
	 */
	public SyncInfoCompareInput(String description, SyncInfo sync) {
		super(new CompareConfiguration());
		Assert.isNotNull(sync);
		Assert.isNotNull(description);
		this.description = description;
		this.resource = sync.getLocal();
		this.node = new MyDiffNode(null, sync);
		initializeContentChangeListeners();
		initializeResourceChangeListeners();
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
	
	private void initializeResourceChangeListeners() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}	
	
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			IResourceDelta resourceDelta = delta.findMember(resource.getFullPath());
			if (resourceDelta != null) {
				if (editor != null && editor instanceof IReusableEditor) {
					UIJob job = new UIJob("") {
						public IStatus runInUIThread(IProgressMonitor monitor) {
							node.update(node.getSyncInfo());
							return Status.OK_STATUS;
						}
					};
					job.setSystem(true);
					job.schedule();						
				}
			}
		}
	}

	public void setCompareEditor(IEditorPart editor) {
		Assert.isNotNull(editor);
		this.editor = editor;
		editor.getSite().getPage().addPartListener(new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
			}
			public void partBroughtToTop(IWorkbenchPart part) {
			}
			public void partClosed(IWorkbenchPart part) {
				getCompareEditor().getSite().getPage().removePartListener(this);
				dispose();
				SyncInfoCompareInput.this.editor = null;
			}
			public void partDeactivated(IWorkbenchPart part) {
			}
			public void partOpened(IWorkbenchPart part) {
			}
		});
	}
	
	public IEditorPart getCompareEditor() {
		return this.editor;
	}
	
	protected void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	/*
	 * (non-Javadoc)
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
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		return Policy.bind("SyncInfoCompareInput.title", node.getName()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_SYNC_MODE_FREE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return Policy.bind("SyncInfoCompareInput.tooltip", description, node.getResource().getFullPath().toString()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
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
	 * @see CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
		if (node != null) {
			try {
				commit(pm, node);
			} finally {
				node.fireChange();
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
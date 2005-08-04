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
package org.eclipse.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.ui.progress.UIJob;

/**
 * A {@link SyncInfo} editor input used as input to a two-way or three-way 
 * compare viewer. It defines methods for accessing the three sides for the 
 * compare, and a name and image which is used when displaying the three way input
 * in an editor. This input can alternately be used to show compare results in 
 * a dialog by calling {@link CompareUI#openCompareDialog(org.eclipse.compare.CompareEditorInput)}.
 * <p>
 * The editor will not update when the elements in the sync info are changed.
 * </p><p>
 * Supports saving the local resource that is changed in the editor and will be updated
 * when the local resources is changed.
 * </p><p>
 * This class cannot be subclassed by clients.
 * </p>
 * @see SyncInfo
 * @since 3.0
 */
public final class SyncInfoCompareInput extends CompareEditorInput implements IResourceChangeListener {

	private MyDiffNode node;
	private String description;
	private IResource resource;
	private long timestamp;
	private boolean isSaving = false;
    private ISynchronizeParticipant participant;

	/*
	 * This class exists so that we can force the text merge viewers to update by
	 * calling #fireChange when we save the compare input to disk. The side
	 * effect is that the compare viewers will be updated to reflect the new changes
	 * that have been made. Compare doesn't do this by default.
	 */
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
		super(getDefaultCompareConfiguration());
		Assert.isNotNull(sync);
		Assert.isNotNull(description);
		this.description = description;
		this.resource = sync.getLocal();
		timestamp = resource.getLocalTimeStamp();
		this.node = new MyDiffNode(null, sync);
		initializeContentChangeListeners();
	}
	
	/**
	 * Creates a compare editor input based on an existing <code>SyncInfo</code>
	 * from the given participant.
	 * 
	 * @param participant the participant from which the sync info was obtained. The
	 * name of the participant is used as the description which is displayed to the user.
	 * @param sync the <code>SyncInfo</code> used as the base for the compare input.
     * 
     * @since 3.1
     */
    public SyncInfoCompareInput(ISynchronizeParticipant participant, SyncInfo sync) {
        this(participant.getName(), sync);
        this.participant = participant;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents(Composite parent) {
		// Add a dispose listener to the created control so that we can use this
		// to de-register our resource change listener.
		final Control control = super.createContents(parent);
		// See bug 66349
		//ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		timestamp = resource.getLocalTimeStamp();
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		return control;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IFile.class.equals(adapter) && resource.getType() == IResource.FILE) {
			return (IFile)resource;
		}
		return super.getAdapter(adapter);
	}
	
	private static CompareConfiguration getDefaultCompareConfiguration() {
		CompareConfiguration cc = new CompareConfiguration();
		//cc.setProperty(CompareConfiguration.USE_OUTLINE_VIEW, true);
		return cc;
	}

	private void initializeContentChangeListeners() {
		ITypedElement te = node.getLeft();
		if (te instanceof IContentChangeNotifier) {
			((IContentChangeNotifier) te).addContentChangeListener(new IContentChangeListener() {
				public void contentChanged(IContentChangeNotifier source) {
					try {
						if(! isSaving)
							saveChanges(new NullProgressMonitor());
					} catch (CoreException e) {
					}
				}
			});
		}
	}
	
	/**
	 * Note that until the compare editor inputs can be part of the compare editors lifecycle we
	 * can't register as a listener because there is no dispose() method to remove the listener.
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			IResourceDelta resourceDelta = delta.findMember(resource.getFullPath());
			if (resourceDelta != null) {
				UIJob job = new UIJob("") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (!isSaveNeeded()) {
							//updateNode();
						}
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
		}
	}
	
	private void dispose() {
		// See bug 66349
		//ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitleImage()
	 */
	public Image getTitleImage() {
		ImageRegistry reg = TeamUIPlugin.getPlugin().getImageRegistry();
		Image image = reg.get(ITeamUIImages.IMG_SYNC_VIEW);
		if (image == null) {
			image = getImageDescriptor().createImage();
			reg.put(ITeamUIImages.IMG_SYNC_VIEW, image);
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
        monitor.beginTask(TeamUIMessages.SyncInfoCompareInput_3, 100);
        monitor.setTaskName(TeamUIMessages.SyncInfoCompareInput_3);
		try {
			if (participant != null) {
			    participant.prepareCompareInput(node, getCompareConfiguration(), Policy.subMonitorFor(monitor, 100));
			} else {
			    Utils.updateLabels(node.getSyncInfo(), getCompareConfiguration());
				node.cacheContents(Policy.subMonitorFor(monitor, 100));
			}
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		} finally {
            monitor.done();
        }
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_title, new String[] { node.getName() }); 
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_tooltip, new String[] { Utils.shortenText(30, description), node.getResource().getFullPath().toString() }); 
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other instanceof SyncInfoCompareInput) {
			SyncInfo otherSyncInfo = ((SyncInfoCompareInput) other).getSyncInfo();
			SyncInfo thisSyncInfo = getSyncInfo();
			IResource otherResource = otherSyncInfo.getLocal();
			return thisSyncInfo.equals(otherSyncInfo) && timestamp == otherResource.getLocalTimeStamp();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		if (checkUpdateConflict())
			return;
		try {
			isSaving = true;
			super.saveChanges(pm);
			if (node != null) {
				commit(pm, node);
			}
		} finally {
			node.fireChange();
			setDirty(false);
			isSaving = false;
			timestamp = resource.getLocalTimeStamp();
		}
	}

	private boolean checkUpdateConflict() {
		long newTimestamp = resource.getLocalTimeStamp();
		if(newTimestamp != timestamp) {
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

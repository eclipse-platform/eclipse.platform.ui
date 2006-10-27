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
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.LocalResourceSaveableComparison;
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
    private ISynchronizeParticipant participant;
	private final LocalResourceSaveableComparison saveable;

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
		this.node = new MyDiffNode(null, sync);
		this.saveable = new LocalResourceSaveableComparison(node, this) {
			protected void fireInputChange() {
				node.fireChange();
			}
		};
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
     * @see org.eclipse.compare.CompareEditorInput#handleDispose()
     */
    protected void handleDispose() {
    	super.handleDispose();
    	saveable.dispose();
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
			// Consider the inputs equal if the sync info are equal and the 
			// left nodes are equal (i.e they have the same timestamp)
			return thisSyncInfo.equals(otherSyncInfo) 
				&& node.getLeft().equals(((SyncInfoCompareInput) other).node.getLeft());
		}
		return false;
	}
	
	public SyncInfo getSyncInfo() {
		return node.getSyncInfo();
	}
}

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
package org.eclipse.team.ui.synchronize;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.OverlayIcon;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.sets.SubscriberInput;
import org.eclipse.team.internal.ui.synchronize.sets.SyncInfoStatistics;
import org.eclipse.team.internal.ui.synchronize.sets.SyncSet;
import org.eclipse.team.internal.ui.synchronize.views.CompressedFolder;
import org.eclipse.team.internal.ui.synchronize.views.SyncSetContentProvider;
import org.eclipse.team.internal.ui.synchronize.views.SynchronizeViewNode;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Provides basic labels for the subscriber participant synchronize view 
 * page. This class provides a facility for subclasses to define annotations
 * on the labels and icons of adaptable objects by overriding
 * <code>decorateText()</code> and <code>decorateImage</code>.
 * 
 * @see TeamSubscriberParticipantPage#getLabelProvider()
 * @since 3.0
 */
public class TeamSubscriberParticipantLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	//column constants
	private static final int COL_RESOURCE = 0;
	private static final int COL_PARENT = 1;
	
	private Image compressedFolderImage;
	
	// cache for folder images that have been overlayed with conflict icon
	private Map fgImageCache;
	
	// Keep track of the compare and workbench image providers
	// so they can be properly disposed
	CompareConfiguration compareConfig = new CompareConfiguration();
	WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();
	
	public Image getCompressedFolderImage() {
		if (compressedFolderImage == null) {
			compressedFolderImage = TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_COMPRESSED_FOLDER).createImage();
		}
		return compressedFolderImage;
	}

	public TeamSubscriberParticipantLabelProvider() {
	}
	
	protected String decorateText(String input, Object resource) {
		return input;
	}
	
	protected Image decorateImage(Image base, Object resource) {
		return base;
	}
	
	public String getText(Object element) {
		String name;
		IResource resource = SyncSetContentProvider.getResource(element);
		if (element instanceof CompressedFolder) {
			name = resource.getProjectRelativePath().toString();
		} else {
			name = workbenchLabelProvider.getText(resource);		
		}
		if (TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_SYNCINFO_IN_LABEL)) {
			SyncInfo info = SyncSetContentProvider.getSyncInfo(element);
			if (info != null && info.getKind() != SyncInfo.IN_SYNC) {
				String syncKindString = SyncInfo.kindToString(info.getKind());
				name = Policy.bind("TeamSubscriberSyncPage.labelWithSyncKind", name, syncKindString); //$NON-NLS-1$
			}
		}
		return decorateText(name, resource);
	}
	
	/**
	 * An image is decorated by at most 3 different plugins. 
	 * 1. ask the workbench for the default icon for the resource
	 * 2. ask the compare plugin for the sync kind overlay
	 * 3. overlay the conflicting image on folders/projects containing conflicts 
	 */
	public Image getImage(Object element) {
		Image decoratedImage = null;
		IResource resource = SyncSetContentProvider.getResource(element);		
		if (element instanceof CompressedFolder) {
			decoratedImage = compareConfig.getImage(getCompressedFolderImage(), IRemoteSyncElement.IN_SYNC);
		} else {						
			Image image = workbenchLabelProvider.getImage(resource);
			decoratedImage = getCompareImage(image, element);			
		}		
		decoratedImage = propagateConflicts(decoratedImage, element, resource);
		return decorateImage(decoratedImage, element);
	}
	
	private Image getCompareImage(Image base, Object element) {
		int kind = SyncSetContentProvider.getSyncKind(element);
		switch (kind & SyncInfo.DIRECTION_MASK) {
			case SyncInfo.OUTGOING:
				kind = (kind &~ SyncInfo.OUTGOING) | SyncInfo.INCOMING;
				break;
			case IRemoteSyncElement.INCOMING:
				kind = (kind &~ SyncInfo.INCOMING) | SyncInfo.OUTGOING;
				break;
		}	
		return compareConfig.getImage(base, kind);
	}
	
	private Image propagateConflicts(Image base, Object element, IResource resource) {
		if(element instanceof SynchronizeViewNode && resource.getType() != IResource.FILE) {
			// if the folder is already conflicting then don't bother propagating the conflict
			int kind = SyncSetContentProvider.getSyncKind(element);
			if((kind & SyncInfo.DIRECTION_MASK) != SyncInfo.CONFLICTING) {
				SubscriberInput input = ((SynchronizeViewNode)element).getSubscriberInput();
				SyncSet set = new SyncSet();
				SyncInfo[] infos = input.getWorkingSetSyncSet().getOutOfSyncDescendants(resource);
				for (int i = 0; i < infos.length; i++) {
					set.add(infos[i]);
				}
				SyncInfoStatistics stats = set.getStatistics();
				long count = stats.countFor(SyncInfo.CONFLICTING, SyncInfo.DIRECTION_MASK);
				if(count > 0) {
					ImageDescriptor overlay = new OverlayIcon(
	   					base, 
	   					new ImageDescriptor[] { TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_CONFLICT_OVR)}, 
	   					new int[] {OverlayIcon.BOTTOM_LEFT}, 
	   					new Point(base.getBounds().width, base.getBounds().height));
	  
					if(fgImageCache == null) {
	   					fgImageCache = new HashMap(10);
	 				}
	 				Image conflictDecoratedImage = (Image) fgImageCache.get(overlay);
	 				if (conflictDecoratedImage == null) {
	   					conflictDecoratedImage = overlay.createImage();
	   					fgImageCache.put(overlay, conflictDecoratedImage);
				 	}
					return conflictDecoratedImage;
				}
			}
		}
		return base;
	}
	   
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		workbenchLabelProvider.dispose();
		compareConfig.dispose();
		if (compressedFolderImage != null) {
			compressedFolderImage.dispose();
		}
		if(fgImageCache != null) {
			Iterator it = fgImageCache.values().iterator();
			while (it.hasNext()) {
				Image element = (Image) it.next();
				element.dispose();				
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == COL_RESOURCE) {
			return getImage(element);
		} else if (columnIndex == COL_PARENT) {
			IResource resource = SyncSetContentProvider.getResource(element);
			return null;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == COL_RESOURCE) {
			return getText(element);
		} else if (columnIndex == COL_PARENT) {
			IResource resource = SyncSetContentProvider.getResource(element);
			return resource.getParent().getFullPath().toString();
		}
		return null;
	}
}
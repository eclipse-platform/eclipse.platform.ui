package org.eclipse.team.internal.ui.sync.views;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.OverlayIcon;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.sync.sets.SubscriberInput;
import org.eclipse.team.internal.ui.sync.sets.SyncInfoStatistics;
import org.eclipse.team.internal.ui.sync.sets.SyncSet;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The SyncViewerLabelProvider can be used in either a tree or table.
 */
public class SyncViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
	
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

	/**
	 * Returns a sync view label provider that is hooked up to the decorator
	 * mechanism.
	 * 
	 * @return a new <code>DecoratingLabelProvider</code> which wraps a <code>
	 *   new <code>WorkbenchLabelProvider</code>
	 */
	public static ILabelProvider getDecoratingLabelProvider() {
		return new DecoratingLabelProvider(
			new SyncViewerLabelProvider(),
			WorkbenchPlugin
				.getDefault()
				.getWorkbench()
				.getDecoratorManager()
				.getLabelDecorator());
	}
	
	public SyncViewerLabelProvider() {
	}
	
	public String getText(Object element) {
		String name;
		IResource resource = getResource(element);
		if (element instanceof CompressedFolder) {
			name = resource.getProjectRelativePath().toString();
		} else {
			name = workbenchLabelProvider.getText(resource);		
		}			
		return name;
	}
	
	/**
	 * An image is decorated by at most 3 different plugins. 
	 * 1. ask the workbench for the default icon for the resource
	 * 2. ask the compare plugin for the sync kind overlay
	 * 3. overlay the conflicting image on folders/projects containing conflicts 
	 */
	public Image getImage(Object element) {
		Image decoratedImage = null;
		IResource resource = getResource(element);
		
		if (element instanceof CompressedFolder) {
			decoratedImage = compareConfig.getImage(getCompressedFolderImage(), IRemoteSyncElement.IN_SYNC);
		} else {			
			int kind = getSyncKind(element);
			switch (kind & IRemoteSyncElement.DIRECTION_MASK) {
				case IRemoteSyncElement.OUTGOING:
					kind = (kind &~ IRemoteSyncElement.OUTGOING) | IRemoteSyncElement.INCOMING;
					break;
				case IRemoteSyncElement.INCOMING:
					kind = (kind &~ IRemoteSyncElement.INCOMING) | IRemoteSyncElement.OUTGOING;
					break;
			}	
			Image image = workbenchLabelProvider.getImage(resource);
			decoratedImage = compareConfig.getImage(image, kind);
		}
		
		return propagateConflicts(decoratedImage, element, resource);
	}
	
	private Image propagateConflicts(Image base, Object element, IResource resource) {
		if(element instanceof SynchronizeViewNode && resource.getType() != IResource.FILE) {
			// if the folder is already conflicting then don't bother propagating the conflict
			if((getSyncKind(element) & SyncInfo.DIRECTION_MASK) != SyncInfo.CONFLICTING) {
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
			IResource resource = getResource(element);
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
			IResource resource = getResource(element);
			return resource.getParent().getFullPath().toString();
		}
		return null;
	}
	
	private IResource getResource(Object obj) {
		return (IResource)TeamAction.getAdapter(obj, IResource.class);
	}
	
	private SyncInfo getSyncInfo(Object obj) {
		return (SyncInfo)TeamAction.getAdapter(obj, SyncInfo.class);
	}
	
	private int getSyncKind(Object element) {
		SyncInfo info = getSyncInfo(element);
		if (info != null) {
			return info.getKind();
		}
		return 0;
	}
}
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.actions.TeamAction;
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
	
	// Keep track of the compare and workbench image providers
	// so they can be properly disposed
	CompareConfiguration compareConfig = new CompareConfiguration();
	WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();
	
	/**
	 * @return
	 */
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
		if (element instanceof CompressedFolder) {
			IResource resource = getResource(element);
			return resource.getProjectRelativePath().toString();
		}
		IResource resource = getResource(element);
		return workbenchLabelProvider.getText(resource);
	}
	
	public Image getImage(Object element) {
		if (element instanceof CompressedFolder) {
			return compareConfig.getImage(getCompressedFolderImage(), 0);
		}
		IResource resource = getResource(element);
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
		return compareConfig.getImage(image, kind);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		workbenchLabelProvider.dispose();
		compareConfig.dispose();
		if (compressedFolderImage != null)
			compressedFolderImage.dispose();
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
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The SyncViewerLabelProvider can be used in either a tree or table.
 */
public class SyncViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	//column constants
	private static final int COL_RESOURCE = 0;
	private static final int COL_PARENT = 1;
	
	// Keep track of the compare and workbench image providers
	// so they can be properly disposed
	CompareConfiguration compareConfig = new CompareConfiguration();
	WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();
	
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
		IResource resource = SyncSet.getIResource(element);
		return workbenchLabelProvider.getText(resource);
	}
	
	public Image getImage(Object element) {
		IResource resource = SyncSet.getIResource(element);
		int kind= SyncSet.getSyncKind(null /* sync set */, element);
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == COL_RESOURCE) {
			return getImage(element);
		} else if (columnIndex == COL_PARENT) {
			IResource resource = SyncSet.getIResource(element);
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
			IResource resource = SyncSet.getIResource(element);
			return resource.getParent().getFullPath().toString();
		}
		return null;
	}
}
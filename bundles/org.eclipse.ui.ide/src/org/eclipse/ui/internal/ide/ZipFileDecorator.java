package org.eclipse.ui.internal.ide;

import java.net.URI;
import java.util.Optional;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.ZipFileUtil;
import org.eclipse.core.internal.resources.VirtualZipFolder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * @since 3.4
 *
 */
public class ZipFileDecorator implements ILightweightLabelDecorator {

	private static final Optional<ImageDescriptor> ZIP_FILE;

	private static final Optional<ImageDescriptor> ZIP_FILE_WARNING;

	static {
		ZIP_FILE = ResourceLocator.imageDescriptorFromBundle(IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/zipfile_ovr.png"); //$NON-NLS-1$
		ZIP_FILE_WARNING = ResourceLocator.imageDescriptorFromBundle(IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/zipfile_ovr.png"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		// no resources to dispose
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * Adds the linked resource overlay if the given element is a linked resource.
	 *
	 * @param element    element to decorate
	 * @param decoration The decoration we are adding to
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(Object,
	 *      IDecoration)
	 */
	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof VirtualZipFolder folder) {
			URI location = folder.getLocationURI();
			if (ZipFileUtil.isOpenZipFile(location)) {
			IFileInfo fileInfo = null;
			if (location != null) {
				fileInfo = IDEResourceInfoUtils.getFileInfo(location);
			}
			if (fileInfo != null && fileInfo.exists()) {
				ZIP_FILE.ifPresent(decoration::addOverlay);
			} else {
				ZIP_FILE_WARNING.ifPresent(decoration::addOverlay);
			}
		}
	}
	}
}

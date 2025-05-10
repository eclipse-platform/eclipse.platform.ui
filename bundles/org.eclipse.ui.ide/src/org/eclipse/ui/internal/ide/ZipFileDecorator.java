package org.eclipse.ui.internal.ide;

import org.eclipse.core.internal.resources.VirtualZipFolder;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A label decorator that adds a custom icon to virtual ZIP folders.
 * <p>
 * This decorator provides a specific icon for objects of type
 * {@link VirtualZipFolder} to visually differentiate them from other folder
 * types in the UI. The icon is the same as for a ZIP when its closed and
 * therefore a file.
 * </p>
 *
 * @since 3.4
 */
public class ZipFileDecorator extends LabelProvider implements ILabelDecorator {

	private static final String PLUGIN_ID = IDEWorkbenchPlugin.IDE_WORKBENCH; // $NON-NLS-1$
	private static final Image CUSTOM_ZIP_FOLDER_ICON = AbstractUIPlugin
			.imageDescriptorFromPlugin(PLUGIN_ID, "icons/full/obj16/zip_file_open.png").createImage(); //$NON-NLS-1$

	@Override
	public Image getImage(Object element) {
		if (element instanceof VirtualZipFolder) {
			return CUSTOM_ZIP_FOLDER_ICON;
		}
		return super.getImage(element);
	}

	@Override
	public void dispose() {
		CUSTOM_ZIP_FOLDER_ICON.dispose();
		super.dispose();
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		if (element instanceof VirtualZipFolder) {
			return CUSTOM_ZIP_FOLDER_ICON;
		}
		return image;
	}

	@Override
	public String decorateText(String text, Object element) {
		return text;
	}
}

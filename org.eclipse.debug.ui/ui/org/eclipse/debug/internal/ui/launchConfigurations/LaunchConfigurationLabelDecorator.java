package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Label decorator for launch configuration files.  Rather than decorate the default File image,
 * this decorator returns an entirely new image.  For text, it simply truncates the file extension.
 */
public class LaunchConfigurationLabelDecorator implements ILabelDecorator {

	/**
	 * @see ILabelDecorator#decorateImage(Image, Object)
	 */
	public Image decorateImage(Image image, Object element) {
		if (isLaunchConfigFile(element)) {
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_LAUNCH_CONFIGURATION);
		}
		return null;
	}

	/**
	 * @see ILabelDecorator#decorateText(String, Object)
	 */
	public String decorateText(String text, Object element) {
		if (isLaunchConfigFile(element)) {
			String filename = ((IFile)element).getName();			
			return filename.substring(0, filename.lastIndexOf('.' + ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION));
		}
		return null;
	}
	
	/**
	 * Return whether the specified object is an IFile whose file extension matches the well-known
	 * extension for launch configurations.
	 */
	private boolean isLaunchConfigFile(Object obj) {
		if (obj instanceof IFile) {
			IFile file = (IFile) obj;
			String extension = file.getFileExtension();
			if (ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION.equals(extension)) {
				return true;
			}
		}
		return false;		
	}

	/**
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}

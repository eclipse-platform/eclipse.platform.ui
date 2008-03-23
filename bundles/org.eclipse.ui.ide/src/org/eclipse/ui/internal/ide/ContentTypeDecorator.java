package org.eclipse.ui.internal.ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.PlatformUI;

/**
 * Lightweight decorator for more specific file icons.
 * 
 * @since 3.4
 * 
 */
public class ContentTypeDecorator implements ILightweightLabelDecorator {

	public void decorate(Object element, IDecoration decoration) {

		if (element instanceof IFile == false) {
			return;
		}
		IFile file = (IFile) element;
		IContentDescription contentDescription = null;
		try {
			contentDescription = file.getContentDescription();
		} catch (CoreException e) {
			// We already have some kind of icon for this file so it's ok to not
			// find a better icon.
		}
		if (contentDescription != null) {
			IContentType contentType = contentDescription.getContentType();
			if (contentType != null) {
				ImageDescriptor image = PlatformUI.getWorkbench()
						.getEditorRegistry().getImageDescriptor(file.getName(),
								contentType);
				if (image != null) {
					decoration.addOverlay(image);
				}
			}
		}

	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}

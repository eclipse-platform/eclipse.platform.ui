package org.eclipse.ui.views.bookmarkexplorer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import java.net.*;

/**
 * Returns the label text and image for bookmarks in the bookmarks viewer.
 */
/* package */ class BookmarkLabelProvider extends LabelProvider {
	private Image image;
/**
 * BookmarkLabelProvider constructor comment.
 */
public BookmarkLabelProvider(BookmarkNavigator view) {
	ImageDescriptor desc = view.getImageDescriptor("obj16/bkmrk_tsk.gif");
	image = desc.createImage();
}
/* (non-Javadoc)
 * Method declared on LabelProvider.
 */
public void dispose() {
	if (image != null) {
		image.dispose();
		image = null;
	}
}
/* (non-Javadoc)
 * Method declared on LabelProvider.
 */
public Image getImage(Object element) {
	return image;
}
/* (non-Javadoc)
 * Method declared on LabelProvider.
 */
public String getText(Object element) {
	IMarker marker = (IMarker) element;
	IResource resource = marker.getResource();
	IResource container = resource.getParent();
	String msg = marker.getAttribute(IMarker.MESSAGE, "");
	String resourceMsg = "on " + resource.getName();
	if (container != null) {
		resourceMsg += " in " + container.getFullPath().makeRelative();
	}
	return msg + " (" + resourceMsg + ")";
}
}

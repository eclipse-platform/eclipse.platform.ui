package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Returns the label text and image for bookmarks in the bookmarks viewer.
 */
/* package */ class BookmarkLabelProvider extends LabelProvider {
	private Image image;
/**
 * BookmarkLabelProvider constructor comment.
 */
public BookmarkLabelProvider(BookmarkNavigator view) {
	ImageDescriptor desc = view.getImageDescriptor("obj16/bkmrk_tsk.gif");//$NON-NLS-1$
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
	String msg = marker.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
	if (container != null) {
		return BookmarkMessages.format("BookmarkLabel.textHasContainer", new Object[] {msg,resource.getName(),container.getFullPath().makeRelative()});//$NON-NLS-1$
	} else {
		return BookmarkMessages.format("BookmarkLabel.textNoContainer", new Object[] {msg,resource.getName()});//$NON-NLS-1$
	}
}



}

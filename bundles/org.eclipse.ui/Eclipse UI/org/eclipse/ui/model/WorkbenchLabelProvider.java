package org.eclipse.ui.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import java.util.*;

/**
 * Provides basic labels for adaptable objects that have the
 * <code>IWorkbenchAdapter</code> adapter associated with them.  All dispensed
 * images are cached until the label provider is explicitly disposed.
 * This class provides a facility for subclasses to define annotations
 * on the labels and icons of adaptable objects.
 */
public class WorkbenchLabelProvider extends LabelProvider {
	/**
	 * The cache of images that have been dispensed by this provider.
	 * Maps ImageDescriptor->Image.
	 */
	private Map imageTable = new Hashtable(40);
/**
 * Creates a new workbench label provider.
 */
public WorkbenchLabelProvider() {
}
/**
 * Returns an image descriptor that is based on the given descriptor,
 * but decorated with additional information relating to the state
 * of the provided object.
 *
 * Subclasses may reimplement this method to decorate an object's
 * image.
 * @see org.eclipse.jface.resource.CompositeImage
 */
protected ImageDescriptor decorateImage(ImageDescriptor input, Object element) {
	return input;
}
/**
 * Returns a label that is based on the given label,
 * but decorated with additional information relating to the state
 * of the provided object.
 *
 * Subclasses may implement this method to decorate an object's
 * label.
 */
protected String decorateText(String input, Object element) {
	return input;
}
/* (non-Javadoc)
 * Method declared on IBaseLabelProvider
 */
/**
 * Disposes of all allocated images.
 */
public final void dispose() {
	Iterator images = imageTable.values().iterator();
	while (images.hasNext()) {
		((Image)images.next()).dispose();
	}
	imageTable = null;
}
/**
 * Returns the implementation of IWorkbenchAdapter for the given
 * object.  Returns <code>null</code> if the adapter is not defined or the
 * object is not adaptable.
 */
protected final IWorkbenchAdapter getAdapter(Object o) {
	if (!(o instanceof IAdaptable)) {
		return null;
	}
	return (IWorkbenchAdapter)((IAdaptable)o).getAdapter(IWorkbenchAdapter.class);
}
/* (non-Javadoc)
 * Method declared on ILabelProvider
 */
public final Image getImage(Object element) {
	//obtain the base image by querying the element
	IWorkbenchAdapter adapter = getAdapter(element);
	if (adapter == null)
		return null;
	ImageDescriptor descriptor = adapter.getImageDescriptor(element);
	if (descriptor == null)
		return null;

	//add any annotations to the image descriptor
	descriptor = decorateImage(descriptor, element);

	//obtain the cached image corresponding to the descriptor
	Image image = (Image) imageTable.get(descriptor);
	if (image == null) {
		image = descriptor.createImage();
		imageTable.put(descriptor, image);
	}
	return image;
}
/* (non-Javadoc)
 * Method declared on ILabelProvider
 */
public final String getText(Object element) {
	//query the element for its label
	IWorkbenchAdapter adapter = getAdapter(element);
	if (adapter == null)
		return "";
	String label = adapter.getLabel(element);

	//return the decorated label
	return decorateText(label, element);
}
}

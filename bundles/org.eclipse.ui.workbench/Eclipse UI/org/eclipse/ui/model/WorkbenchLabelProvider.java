package org.eclipse.ui.model;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.WorkbenchPlugin;

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
	private Map imageTable;

	/**
	 * Get a WorkbenchLabelProvider that is hooked up to the decorator
	 * mechanism.
	 * @return ILabelProvider
	 */
	public static ILabelProvider getDecoratingWorkbenchLabelProvider() {
		return new DecoratingLabelProvider(
			new WorkbenchLabelProvider(),
			WorkbenchPlugin
				.getDefault()
				.getWorkbench()
				.getDecoratorManager()
				.getLabelDecorator());
	}
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
	protected ImageDescriptor decorateImage(
		ImageDescriptor input,
		Object element) {
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
		if (imageTable != null) {
			for (Iterator i = imageTable.values().iterator(); i.hasNext();) {
				((Image) i.next()).dispose();
			}
			imageTable = null;
		}
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
		return (IWorkbenchAdapter) ((IAdaptable) o).getAdapter(
			IWorkbenchAdapter.class);
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
		if (imageTable == null) {
			imageTable = new Hashtable(40);
		}
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
			return ""; //$NON-NLS-1$
		String label = adapter.getLabel(element);

		//return the decorated label
		return decorateText(label, element);
	}

}

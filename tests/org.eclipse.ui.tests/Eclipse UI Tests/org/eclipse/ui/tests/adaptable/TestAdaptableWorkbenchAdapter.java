/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.adaptable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @version 	1.0
 * @author
 */
public class TestAdaptableWorkbenchAdapter extends LabelProvider implements
        IWorkbenchAdapter {

    private static TestAdaptableWorkbenchAdapter singleton = new TestAdaptableWorkbenchAdapter();

    public static TestAdaptableWorkbenchAdapter getInstance() {
        return singleton;
    }

    public TestAdaptableWorkbenchAdapter() {
    }

    /*
     * @see IWorkbenchAdapter#getChildren(Object)
     */
    public Object[] getChildren(Object o) {
        if (o instanceof AdaptableResourceWrapper)
            return ((AdaptableResourceWrapper) o).getChildren();
        if (o instanceof IResource) {
            AdaptableResourceWrapper wrapper = new AdaptableResourceWrapper(
                    (IResource) o);
            return wrapper.getChildren();
        }
        return new Object[0];
    }

    /*
     * @see IWorkbenchAdapter#getImageDescriptor(Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    /*
     * @see IWorkbenchAdapter#getLabel(Object)
     */
    public String getLabel(Object o) {
        if (o instanceof AdaptableResourceWrapper)
            return ((AdaptableResourceWrapper) o).getLabel();
        else
            return null;
    }

    /*
     * @see IWorkbenchAdapter#getParent(Object)
     */
    public Object getParent(Object o) {
        if (o instanceof AdaptableResourceWrapper)
            return ((AdaptableResourceWrapper) o).getParent();
        else
            return null;
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
    protected ImageDescriptor decorateImage(ImageDescriptor input,
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
        return (IWorkbenchAdapter) ((IAdaptable) o)
                .getAdapter(IWorkbenchAdapter.class);
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

        return descriptor.createImage();
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

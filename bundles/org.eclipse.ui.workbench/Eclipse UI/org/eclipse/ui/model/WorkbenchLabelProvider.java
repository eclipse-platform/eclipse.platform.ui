/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fair Isaac Corporation <Hemant.Singh@Gmail.com> - http://bugs.eclipse.org/326695
 *******************************************************************************/
package org.eclipse.ui.model;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.Util;

/**
 * Provides basic labels for adaptable objects that have the
 * <code>IWorkbenchAdapter</code> adapter associated with them.  All dispensed
 * images are cached until the label provider is explicitly disposed.
 * This class provides a facility for subclasses to define annotations
 * on the labels and icons of adaptable objects.
 */
public class WorkbenchLabelProvider extends LabelProvider implements
        IColorProvider, IFontProvider, IStyledLabelProvider {

    /**
     * Returns a workbench label provider that is hooked up to the decorator
     * mechanism.
     * 
     * @return a new <code>DecoratingLabelProvider</code> which wraps a <code>
     *   new <code>WorkbenchLabelProvider</code>
     */
    public static ILabelProvider getDecoratingWorkbenchLabelProvider() {
        return new DecoratingLabelProvider(new WorkbenchLabelProvider(),
                PlatformUI.getWorkbench().getDecoratorManager()
                        .getLabelDecorator());
    }
    
    /**
     * Listener that tracks changes to the editor registry and does a full update
     * when it changes, since many workbench adapters derive their icon from the file
     * associations in the registry.
     */
    private IPropertyListener editorRegistryListener = new IPropertyListener() {
		public void propertyChanged(Object source, int propId) {
			if (propId == IEditorRegistry.PROP_CONTENTS) {
				fireLabelProviderChanged(new LabelProviderChangedEvent(WorkbenchLabelProvider.this));
			}
		}
	};		
	private ResourceManager resourceManager;

    /**
     * Creates a new workbench label provider.
     */
    public WorkbenchLabelProvider() {
    	PlatformUI.getWorkbench().getEditorRegistry().addPropertyListener(editorRegistryListener);
    }

    /**
     * Returns an image descriptor that is based on the given descriptor,
     * but decorated with additional information relating to the state
     * of the provided object.
     *
     * Subclasses may reimplement this method to decorate an object's
     * image.
     * 
     * @param input The base image to decorate.
     * @param element The element used to look up decorations.
     * @return the resuling ImageDescriptor.
     * @see org.eclipse.jface.resource.CompositeImageDescriptor
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
     * @param input The base text to decorate.
     * @param element The element used to look up decorations.
     * @return the resulting text
     */
    protected String decorateText(String input, Object element) {
        return input;
    }

    /* (non-Javadoc)
     * Method declared on ILabelProvider
     */
    public void dispose() {
    	PlatformUI.getWorkbench().getEditorRegistry().removePropertyListener(editorRegistryListener);
		if (resourceManager != null)
			resourceManager.dispose();
    	resourceManager = null;
    	super.dispose();
    }
    
    /**
     * Returns the implementation of IWorkbenchAdapter for the given
     * object.  
     * @param o the object to look up.
     * @return IWorkbenchAdapter or<code>null</code> if the adapter is not defined or the
     * object is not adaptable. 
     */
    protected final IWorkbenchAdapter getAdapter(Object o) {
        return (IWorkbenchAdapter)Util.getAdapter(o, IWorkbenchAdapter.class);
    }

    /**
     * Returns the implementation of IWorkbenchAdapter2 for the given
     * object.  
     * @param o the object to look up.
     * @return IWorkbenchAdapter2 or<code>null</code> if the adapter is not defined or the
     * object is not adaptable. 
     */
    protected final IWorkbenchAdapter2 getAdapter2(Object o) {
        return (IWorkbenchAdapter2)Util.getAdapter(o, IWorkbenchAdapter2.class);
    }

	/**
	 * Returns the implementation of IWorkbenchAdapter3 for the given object.
	 * 
	 * @param o
	 *            the object to look up.
	 * @return IWorkbenchAdapter3 or<code>null</code> if the adapter is not
	 *         defined or the object is not adaptable.
	 * @since 3.7
	 */
	protected final IWorkbenchAdapter3 getAdapter3(Object o) {
		return (IWorkbenchAdapter3) Util.getAdapter(o, IWorkbenchAdapter3.class);
	}

	/**
	 * Lazy load the resource manager
	 * 
	 * @return The resource manager, create one if necessary
	 */
	private ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources
					.getResources());
		}

		return resourceManager;
	}

    /* (non-Javadoc)
     * Method declared on ILabelProvider
     */
    public final Image getImage(Object element) {
        //obtain the base image by querying the element
        IWorkbenchAdapter adapter = getAdapter(element);
        if (adapter == null) {
            return null;
        }
        ImageDescriptor descriptor = adapter.getImageDescriptor(element);
        if (descriptor == null) {
            return null;
        }

        //add any annotations to the image descriptor
        descriptor = decorateImage(descriptor, element);

		return (Image) getResourceManager().get(descriptor);
    }

	/**
	 * The default implementation of this returns the styled text label for the
	 * given element.
	 * 
	 * @param element
	 *            the element to evaluate the styled string for
	 * 
	 * @return the styled string.
	 * 
	 * @since 3.7
	 */
    public StyledString getStyledText(Object element) {
        IWorkbenchAdapter3 adapter = getAdapter3(element);
		if (adapter == null) {
			// If adapter class doesn't implement IWorkbenchAdapter3 than use
			// StyledString with text of element. Since the output of getText is
			// already decorated, so we don't need to call decorateText again
			// here.
			return new StyledString(getText(element));
		}
		StyledString styledString = adapter.getStyledText(element);
		// Now, re-use any existing decorateText implementation, to decorate
		// this styledString.
		String decorated = decorateText(styledString.getString(), element);
		Styler styler = getDecorationStyle(element);
		return StyledCellLabelProvider.styleDecoratedString(decorated, styler, styledString);
    }

	/**
	 * Sets the {@link org.eclipse.jface.viewers.StyledString.Styler} to be used
	 * for string decorations. By default the
	 * {@link StyledString#DECORATIONS_STYLER decoration style}. Clients can
	 * override.
	 * 
	 * @param element
	 *            the element that has been decorated
	 * 
	 * @return return the decoration style
	 * 
	 * @since 3.7
	 */
	protected Styler getDecorationStyle(Object element) {
		return StyledString.DECORATIONS_STYLER;
	}

    /* (non-Javadoc)
     * Method declared on ILabelProvider
     */
    public final String getText(Object element) {
        //query the element for its label
        IWorkbenchAdapter adapter = getAdapter(element);
        if (adapter == null) {
            return ""; //$NON-NLS-1$
        }
        String label = adapter.getLabel(element);

        //return the decorated label
        return decorateText(label, element);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
        return getColor(element, true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
        return getColor(element, false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
        IWorkbenchAdapter2 adapter = getAdapter2(element);
        if (adapter == null) {
            return null;
        }

        FontData descriptor = adapter.getFont(element);
        if (descriptor == null) {
            return null;
        }

		return (Font) getResourceManager().get(
				FontDescriptor.createFrom(descriptor));
    }

    private Color getColor(Object element, boolean forground) {
        IWorkbenchAdapter2 adapter = getAdapter2(element);
        if (adapter == null) {
            return null;
        }
        RGB descriptor = forground ? adapter.getForeground(element) : adapter
                .getBackground(element);
        if (descriptor == null) {
            return null;
        }

		return (Color) getResourceManager().get(
				ColorDescriptor.createFrom(descriptor));
    }
}

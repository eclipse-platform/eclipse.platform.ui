/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.model;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Provides basic labels for adaptable objects that have the
 * <code>IWorkbenchAdapter</code> adapter associated with them.  All dispensed
 * images are cached until the label provider is explicitly disposed.
 * This class provides a facility for subclasses to define annotations
 * on the labels and icons of adaptable objects.
 */
public class WorkbenchLabelProvider extends LabelProvider implements IColorProvider, IFontProvider {
	/**
	 * The cache of images that have been dispensed by this provider.
	 * Maps ImageDescriptor->Image.
	 * Caches are all static to avoid creating extra system
	 * resources for very common images, font and colors.
	 */
	private static Map imageTable = new Hashtable(40);
	
	/**
	 * The cache of colors that have been dispensed by this provider.
	 * Maps RGB->Color.
	 */
	private static Map colorTable = new Hashtable(7);
	
	/**
	 * The cache of fonts that have been dispensed by this provider.
	 * Maps FontData->Font.
	 */
	private static Map fontTable = new Hashtable(7);;	

	/**
	 * Returns a workbench label provider that is hooked up to the decorator
	 * mechanism.
	 * 
	 * @return a new <code>DecoratingLabelProvider</code> which wraps a <code>
	 *   new <code>WorkbenchLabelProvider</code>
	 */
	public static ILabelProvider getDecoratingWorkbenchLabelProvider() {
		return new DecoratingLabelProvider(
			new WorkbenchLabelProvider(),
			PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
	}
	/**
	 * Creates a new workbench label provider.
	 */
	public WorkbenchLabelProvider() {
	    // no-op
	}

	/**
	 * Returns an image descriptor that is based on the given descriptor,
	 * but decorated with additional information relating to the state
	 * of the provided object.
	 *
	 * Subclasses may reimplement this method to decorate an object's
	 * image.
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor
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
	
	/**
	 * Disposes of all allocated images, colors and fonts
	 * when shutting down the plug-in.
	 */
	public static final void shutdown() {
		if (imageTable != null) {
			for (Iterator i = imageTable.values().iterator(); i.hasNext();) {
				((Image) i.next()).dispose();
			}
			imageTable = null;
		}
		if (colorTable != null) {
			for (Iterator i = colorTable.values().iterator(); i.hasNext();) {
				((Color) i.next()).dispose();
			}
			colorTable = null;		    
		}		
		if (fontTable != null) {
			for (Iterator i = fontTable.values().iterator(); i.hasNext();) {
				((Font) i.next()).dispose();
			}
			fontTable = null;		    
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
	
	/**
	 * Returns the implementation of IWorkbenchAdapter2 for the given
	 * object.  Returns <code>null</code> if the adapter is not defined or the
	 * object is not adaptable.
	 */
	protected final IWorkbenchAdapter2 getAdapter2(Object o) {
		if (!(o instanceof IAdaptable)) {
			return null;
		}
		return (IWorkbenchAdapter2) ((IAdaptable) o).getAdapter(
			IWorkbenchAdapter2.class);
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

		Font font = (Font) fontTable.get(descriptor);
		if (font == null) {
			font = new Font(Display.getCurrent(), descriptor);
			fontTable.put(descriptor, font);
		}
		return font;
    }
    
    private Color getColor(Object element, boolean forground) {
		IWorkbenchAdapter2 adapter = getAdapter2(element);
		if (adapter == null) {
			return null;
		}
		RGB descriptor = 
		    forground ? 
		            adapter.getForeground(element) 
		            : adapter.getBackground(element);
		if (descriptor == null) {
			return null;
		}

		Color color = (Color) colorTable.get(descriptor);
		if (color == null) {
			color = new Color(Display.getCurrent(), descriptor);
			colorTable.put(descriptor, color);
		}
		return color;        
    }    
}

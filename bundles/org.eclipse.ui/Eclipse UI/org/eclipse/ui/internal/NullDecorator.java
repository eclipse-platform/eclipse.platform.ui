package org.eclipse.ui.internal;

/**
 * The purpose of the NullDecorator is to provide a decorator
 * that does nothing and can be used as a placeholder.
 */

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

class NullDecorator implements ILabelDecorator {
	
	private static NullDecorator defaultDecorator;
	
	static NullDecorator getNullDecorator(){
		if(defaultDecorator == null)
			defaultDecorator = new NullDecorator();
		return defaultDecorator;
	}
	
	/**
	 * Return a new instance of the receiver
	 */
	private NullDecorator(){
	}

	/*
	 * @see ILabelDecorator#decorateImage(Image, Object)
	 */
	public Image decorateImage(Image image, Object element) {
		return image;
	}

	/*
	 * @see ILabelDecorator#decorateText(String, Object)
	 */
	public String decorateText(String text, Object element) {
		return text;
	}

	/*
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}


package org.eclipse.ui.tests.navigator;

import org.eclipse.jface.viewers.ILabelProviderListener;
import java.lang.Object;
import java.lang.String;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.ILabelDecorator;

/**
 * @see ILabelDecorator
 */
public class HeavyNullImageDecorator implements ILabelDecorator {
	
	/**
     * Whether we should fail with an exception
     */
	public static boolean fail = false;

	/**
	 *
	 */
	public HeavyNullImageDecorator() {
	}

	/**
	 * @see ILabelDecorator#addListener
	 */
	public void addListener(ILabelProviderListener listener)  {
	}

	/**
	 * @see ILabelDecorator#dispose
	 */
	public void dispose()  {
	}

	/**
	 * @see ILabelDecorator#isLabelProperty
	 */
	public boolean isLabelProperty(Object element, String property)  {
		return false;
	}

	/**
	 * @see ILabelDecorator#removeListener
	 */
	public void removeListener(ILabelProviderListener listener)  {
	}

	/**
	 * @see ILabelDecorator#decorateImage
	 */
	public Image decorateImage(Image image, Object element)  {
		if (fail) {
		    fail = false;
			throw new NullPointerException("Heavy image boom");
		}
		return null;
	}

	/**
	 * @see ILabelDecorator#decorateText
	 */
	public String decorateText(String text, Object element)  {
		return text;
	}
}

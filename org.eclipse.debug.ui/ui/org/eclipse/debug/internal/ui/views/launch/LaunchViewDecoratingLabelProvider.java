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
package org.eclipse.debug.internal.ui.views.launch;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * A label provider which receives notification of labels computed in
 * the background by a LaunchViewLabelDecorator.
 */
public class LaunchViewDecoratingLabelProvider extends DecoratingLabelProvider {
	
	/**
	 * A map of text computed for elements. Items are added to this map
	 * when notification is received that text has been computed for an element
	 * and they are removed the first time the text is returned for an
	 * element.
	 * key: Object the element
	 * value: String the label text
	 */
	private Map computedText= new HashMap();
	
	/**
	 * @see DecoratingLabelProvider#DecoratingLabelProvider(org.eclipse.jface.viewers.ILabelProvider, org.eclipse.jface.viewers.ILabelDecorator)
	 */
	public LaunchViewDecoratingLabelProvider(ILabelProvider provider, LaunchViewLabelDecorator decorator) {
		super(provider, decorator);
		decorator.setLabelProvider(this);
	}
	
	/**
	 * Notifies this label provider that the given text was computed
	 * for the given element. The given text will be returned the next
	 * time its text is requested.
	 * 
	 * @param element the element whose label was computed
	 * @param text the label
	 */
	public void textComputed(Object element, String text) {
		computedText.put(element, text);
	}
	
	/**
	 * Returns the stored text computed by the background decorator
	 * or delegates to the decorating label provider to compute text.
	 * If a stored value exists for the given element, it is cleared and
	 * then returned such that the next call to this method will have to
	 * delegate.
	 * 
	 * @see DecoratingLabelProvider#getText(java.lang.Object) 
	 */
	public String getText(Object element) {
		String text= (String) computedText.remove(element);
		if (text != null) {
			return text;
		}
		return super.getText(element);
	}

}

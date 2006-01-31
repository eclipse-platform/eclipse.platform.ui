/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.logic.properties;

import java.util.Iterator;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.examples.logicdesigner.model.Circuit;
import org.eclipse.gef.examples.logicdesigner.model.LogicElement;
import org.eclipse.gef.examples.logicdesigner.model.LogicSubpart;
import org.eclipse.gef.examples.logicdesigner.model.Wire;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

/**
 * Label provider for the title bar for the tabbed property sheet page.
 * 
 * @author Anthony Hunter
 */
public class LogicElementLabelProvider
	extends LabelProvider {

	private ITypeMapper typeMapper;

	/**
	 * constructor.
	 */
	public LogicElementLabelProvider() {
		super();
		typeMapper = new LogicElementTypeMapper();
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object objects) {
		if (objects == null || objects.equals(StructuredSelection.EMPTY)) {
			return null;
		}
		final boolean multiple[] = {false};
		Object object = getObject(objects, multiple);
		if (object == null) {
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(
				Circuit.class, "icons/comp.gif");//$NON-NLS-1$
			return imageDescriptor.createImage();
		} else {
			if (!(object instanceof EditPart)) {
				return null;
			}
			LogicElement element = (LogicElement) ((EditPart) object)
				.getModel();
			if (element instanceof Wire) {
				ImageDescriptor imageDescriptor = ImageDescriptor
					.createFromFile(Circuit.class, "icons/connection16.gif");//$NON-NLS-1$
				return imageDescriptor.createImage();
			}
			return ((LogicSubpart) element).getIconImage();
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object objects) {
		if (objects == null || objects.equals(StructuredSelection.EMPTY)) {
			return "No items selected";//$NON-NLS-1$
		}
		final boolean multiple[] = {false};
		final Object object = getObject(objects, multiple);
		if (object == null || ((IStructuredSelection) objects).size() > 1) {
			return ((IStructuredSelection) objects).size() + " items selected";//$NON-NLS-1$
		} else {
			String name = typeMapper.mapType(object).getName();
			return name.substring(name.lastIndexOf('.') + 1);
		}
	}

	/**
	 * Determine if a multiple object selection has been passed to the label
	 * provider. If the objects is a IStructuredSelection, see if all the
	 * objects in the selection are the same and if so, we want to provide
	 * labels for the common selected element.
	 * 
	 * @param objects
	 *            a single object or a IStructuredSelection.
	 * @param multiple
	 *            first element in the array is true if there is multiple
	 *            unequal selected elements in a IStructuredSelection.
	 * @return the object to get labels for.
	 */
	private Object getObject(Object objects, boolean multiple[]) {
		Assert.isNotNull(objects);
		Object object = null;
		if (objects instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) objects;
			object = selection.getFirstElement();
			if (selection.size() == 1) {
				// one element selected
				multiple[0] = false;
				return object;
			}
			// multiple elements selected
			multiple[0] = true;
			Class firstClass = typeMapper.mapType(object);
			// determine if all the objects in the selection are the same type
			if (selection.size() > 1) {
				for (Iterator i = selection.iterator(); i.hasNext();) {
					Object next = i.next();
					Class nextClass = typeMapper.mapType(next);
					if (!nextClass.equals(firstClass)) {
						// two elements not equal == multiple selected unequal
						multiple[0] = false;
						object = null;
						break;
					}
				}
			}
		} else {
			multiple[0] = false;
			object = objects;
		}
		return object;
	}

}
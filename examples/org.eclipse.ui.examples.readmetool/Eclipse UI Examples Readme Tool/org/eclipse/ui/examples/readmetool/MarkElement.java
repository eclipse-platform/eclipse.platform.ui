/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import java.util.ArrayList;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * This class represents a marked location in the Readme file text.
 *
 * TIP: By implementing the <code>IWorkbenchAdapter</code> interface, we can
 * easily add objects of this type to viewers and parts in the workbench. When a
 * viewer contains <code>IWorkbenchAdapter</code>, the generic
 * <code>WorkbenchContentProvider</code> and <code>WorkbenchLabelProvider</code>
 * can be used to provide navigation and display for that viewer.
 */
public class MarkElement implements IWorkbenchAdapter, IAdaptable {
	private String headingName;

	private IAdaptable parent;

	private int offset;

	private int numberOfLines;

	private int length;

	private ArrayList<MarkElement> children;

	/**
	 * Creates a new MarkElement and stores parent element and location in the text.
	 *
	 * @param parent  the parent of this element
	 * @param heading text corresponding to the heading
	 * @param offset  the offset into the Readme text
	 * @param length  the length of the element
	 */
	public MarkElement(IAdaptable parent, String heading, int offset, int length) {
		this.parent = parent;
		if (parent instanceof MarkElement markElement) {
			markElement.addChild(this);
		}
		this.headingName = heading;
		this.offset = offset;
		this.length = length;
	}

	/**
	 * Adds a child to this element
	 */
	private void addChild(MarkElement child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return (T) this;
		if (adapter == IPropertySource.class)
			return (T) new MarkElementProperties(this);
		return null;
	}

	@Override
	public Object[] getChildren(Object object) {
		if (children != null) {
			return children.toArray();
		}
		return new Object[0];
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		IWorkbenchAdapter parentElement = Adapters.adapt(parent, IWorkbenchAdapter.class);
		if (parentElement != null) {
			return parentElement.getImageDescriptor(object);
		}
		return null;
	}

	@Override
	public String getLabel(Object o) {
		return headingName;
	}

	/**
	 * Returns the number of characters in this section.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Returns the number of lines in the element.
	 *
	 * @return the number of lines in the element
	 */
	public int getNumberOfLines() {
		return numberOfLines;
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}

	/**
	 * Returns the offset of this section in the file.
	 */
	public int getStart() {
		return offset;
	}

	/**
	 * Sets the number of lines in the element
	 *
	 * @param newNumberOfLines the number of lines in the element
	 */
	public void setNumberOfLines(int newNumberOfLines) {
		numberOfLines = newNumberOfLines;
	}
}

/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 207344
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Everything that appears in LogView is Abstract Entry. It provides composite pattern.
 */
public abstract class AbstractEntry extends PlatformObject implements IWorkbenchAdapter {

	/**
	 * The collection of direct children of this entry
	 */
	private List<AbstractEntry> children = new ArrayList<>();
	protected Object parent;

	/**
	 * Adds the specified child entry to the listing of children.
	 * If the specified child is <code>null</code>, no work is done
	 *
	 * @param child
	 */
	public void addChild(AbstractEntry child) {
		if (child != null) {
			children.add(0, child);
			child.setParent(this);
		}
	}

	@Override
	public Object[] getChildren(Object parent) {
		return children.toArray();
	}

	/**
	 * @return true if this entry has children, false otherwise
	 */
	public boolean hasChildren() {
		return children.size() > 0;
	}

	/**
	 * @return the size of the child array
	 *
	 * TODO rename to getChildCount(), or something more meaningful
	 */
	public int size() {
		return children.size();
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	@Override
	public String getLabel(Object o) {
		return null;
	}

	@Override
	public Object getParent(Object o) {
		return parent;
	}

	/**
	 * Sets the parent of this entry
	 * @param parent
	 */
	public void setParent(AbstractEntry parent) {
		this.parent = parent;
	}

	/**
	 * removes all of the children specified in the given listing
	 *
	 * @param list the list of children to remove
	 */
	public void removeChildren(List<AbstractEntry> list) {
		children.removeAll(list);
	}

	/**
	 * Removes all of the children from this entry
	 */
	public void removeAllChildren() {
		children.clear();
	}

	/**
	 * Writes this entry information into the given {@link PrintWriter}
	 *
	 * @param writer
	 */
	public abstract void write(PrintWriter writer);
}

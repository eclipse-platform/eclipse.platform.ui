/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;

public class TestExtensionTreeData {

	private final Map children = new HashMap();

	private TestExtensionTreeData parent;

	private String name;

	private Properties model;

	private IFile container;

	public TestExtensionTreeData(TestExtensionTreeData aParent, String aName,
			Properties theModel, IFile aFile) {
		parent = aParent;
		name = aName;
		model = theModel;
		container = aFile;
	}

	public TestExtensionTreeData getParent() {
		return parent;
	}

	public TestExtensionTreeData[] getChildren() {
		Set updatedChildren = new HashSet();
		if (model != null) {
			String childrenString = model.getProperty(getName());
			if (childrenString != null) {
				String[] childrenElements = childrenString.split(",");
				for (int i = 0; i < childrenElements.length; i++) {
					if (children.containsKey(childrenElements[i])) {
						updatedChildren.add(children.get(childrenElements[i]));
					} else {
						TestExtensionTreeData newChild = new TestExtensionTreeData(
								this, childrenElements[i], model, container);
						children.put(newChild.getName(), newChild);
						updatedChildren.add(newChild);
					}
				}
			}
		}
		return (TestExtensionTreeData[]) updatedChildren
				.toArray(new TestExtensionTreeData[updatedChildren.size()]);
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TestExtensionTreeData
				&& ((TestExtensionTreeData) obj).getName().equals(name);
	}

	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer(getName()).append(":");

		toString.append("[");
		// update local children to remove any stale kids
		for (Iterator childIterator = children.keySet().iterator(); childIterator
				.hasNext();) {
			String childName = (String) childIterator.next();
			TestExtensionTreeData child = (TestExtensionTreeData) children
					.get(childName);
			toString.append(child.toString());
		}
		toString.append("]");
		return toString.toString();
	}

	/**
	 * @return
	 */
	public IFile getFile() { 
		return container;
	}

}

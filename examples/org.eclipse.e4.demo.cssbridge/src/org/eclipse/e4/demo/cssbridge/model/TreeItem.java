/*******************************************************************************
 * Copyright (c) 2014, 2018 IBM Corporation and others.
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
package org.eclipse.e4.demo.cssbridge.model;

import java.util.ArrayList;
import java.util.List;

public class TreeItem {
	private TreeItem parent;

	private List<TreeItem> children;

	private Object value;

	public TreeItem(TreeItem parent, Object value) {
		this.parent = parent;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public TreeItem getParent() {
		return parent;
	}

	public void addChild(TreeItem child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	public List<TreeItem> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return value != null ? value.toString() : "";
	}
}

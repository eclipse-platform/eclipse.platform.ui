/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.decorators;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The TreeElement is the element displayed in the
 * test tree views.
 */
public class TreeElement extends TestElement {
	int level;

	TreeElement parent;

	Collection<TreeElement> children = new ArrayList<>(10);

	TreeElement(TreeElement parent, int index) {
		if (parent == null) {
			name = "Root";
			level = 0;
		} else {
			level = parent.level + 1;
			name = "Level" + String.valueOf(level) + " - " + String.valueOf(index);
			parent.children.add(this);
		}
	}
}

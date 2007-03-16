/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	Collection children = new ArrayList(10);

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

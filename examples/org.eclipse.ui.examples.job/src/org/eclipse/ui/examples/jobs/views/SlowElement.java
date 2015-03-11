/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs.views;

import java.util.*;

import org.eclipse.core.runtime.PlatformObject;

public class SlowElement extends PlatformObject {
	private String name;
	private SlowElement parent;

	SlowElement(String name) {
		this(null, name, null);
	}

	SlowElement(String name, SlowElement[] children) {
		this(null, name, children);
	}

	SlowElement(SlowElement parent, String name, SlowElement[] children) {
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public SlowElement getParent() {
		return parent;
	}

	public SlowElement[] getChildren() {
		Random r = new Random();
		int random = r.nextInt(15);
		List children = new ArrayList();
		for(int i = 0; i < random; i++) {
			children.add(new SlowElement("child" + i)); //$NON-NLS-1$
		}
		return (SlowElement[]) children.toArray(new SlowElement[children.size()]);
	}
}
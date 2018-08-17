/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
package org.eclipse.ui.examples.jobs.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
		List<SlowElement> children = new ArrayList<>();
		for(int i = 0; i < random; i++) {
			children.add(new SlowElement("child" + i)); //$NON-NLS-1$
		}
		return children.toArray(new SlowElement[children.size()]);
	}
}
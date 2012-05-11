/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

public class TestJobFamily {
	public static final int TYPE_NONE = 0;
	public static final int TYPE_ONE = 1;
	public static final int TYPE_TWO = 2;
	public static final int TYPE_THREE = 3;
	public static final int TYPE_FOUR = 4;
	public static final int TYPE_FIVE = 5;

	private int type = TYPE_NONE;

	public TestJobFamily() {
		this(TYPE_NONE);
	}

	public TestJobFamily(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}

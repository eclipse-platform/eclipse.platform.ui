/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

/**
 * Pair is a basic tuple of two with classic first and second members.
 * Since Java is non-polymorphic, we use Object(s) for the pair elements
 * and cast our brains out.
 */
public class Pair {
	public Object fFirst = null;
	public Object fSecond = null;
	public Pair(Object first, Object second) {
		fFirst = first;
		fSecond = second;
	}
	/*
	 * String accessors
	 */
	public String firstAsString() {
		return (String) fFirst;
	}
	public String secondAsString() {
		return (String) fSecond;
	}
}

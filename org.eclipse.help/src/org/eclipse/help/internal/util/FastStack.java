/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.util;

import java.util.ArrayList;

/**
 * Fast Stack is similar to java.uiti.Stack, but simplified for speed. It uses
 * ArrayList as an underlying collection. The methods in this class are not
 * thread safe.
 */
public class FastStack extends ArrayList {

	private static final long serialVersionUID = 1L;
	
	private int last = -1;

	public FastStack() {
		super();
	}

	public final Object push(Object item) {
		super.add(item);
		last++;
		return item;
	}

	public final Object pop() {
		return super.remove(last--);
	}

	public final Object peek() {
		return super.get(last);
	}

	public final boolean empty() {
		return last < 0;
	}
}

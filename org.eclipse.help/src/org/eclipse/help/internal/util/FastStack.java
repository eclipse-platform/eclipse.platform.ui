/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.util;

import java.util.*;

/**
 * Fast Stack is similar to java.uiti.Stack,
 * but simplified for speed.  It uses ArrayList
 * as an underlying collection.  The methods
 * in this class are not thread safe.
 */
public class FastStack extends ArrayList {
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

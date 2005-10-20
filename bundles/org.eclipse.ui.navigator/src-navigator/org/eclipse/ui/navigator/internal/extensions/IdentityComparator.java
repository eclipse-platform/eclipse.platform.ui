/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ui.navigator.internal.extensions;

import java.util.Comparator;

public class IdentityComparator implements Comparator {

	public static final IdentityComparator INSTANCE = new IdentityComparator();
	
	public int compare(Object lvalue, Object rvalue) {
		return 0;
	}
	
	public boolean equals(Object anObject) {
		return this == anObject; 
	}
}


/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator.internal.extensions;

import java.util.Comparator;

import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentExtension;

/**
 * @since 3.2
 * 
 */
public class ExtensionPriorityComparator implements Comparator {

	public static final ExtensionPriorityComparator INSTANCE = new ExtensionPriorityComparator();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {

		INavigatorContentDescriptor lvalue = null;
		INavigatorContentDescriptor rvalue = null;

		if (o1 instanceof INavigatorContentDescriptor)
			lvalue = (INavigatorContentDescriptor) o1;
		else if (o1 instanceof INavigatorContentExtension)
			lvalue = ((INavigatorContentExtension) o1).getDescriptor();

		if (o2 instanceof INavigatorContentDescriptor)
			rvalue = (INavigatorContentDescriptor) o2;
		else if (o2 instanceof INavigatorContentExtension)
			rvalue = ((INavigatorContentExtension) o2).getDescriptor();

		if (lvalue == null || rvalue == null)
			return -1;

		int c = lvalue.getPriority() - rvalue.getPriority();
		if (c != 0)
			return c;
		return lvalue.getId().compareTo(rvalue.getId());

	}

}

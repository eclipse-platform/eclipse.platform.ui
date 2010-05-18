/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * This is an example of a context function that is contributed declaratively using
 * OSGi declarative services. There are no code references to this class, but if a client
 * attempts to retrieve a "realm" value from a context, they will get the value returned
 * by this function, unless a more specific context has registered a different realm. Declarative
 * services allows the function to be instantiated lazily only when a client first requests it.
 * A more elaborate function could use values defined the provided context to select
 * or customize the concrete service object returned from the function.
 */
public class RealmFunction extends ContextFunction {

	public Object compute(IEclipseContext context) {
		return new LockRealm();
	}

}

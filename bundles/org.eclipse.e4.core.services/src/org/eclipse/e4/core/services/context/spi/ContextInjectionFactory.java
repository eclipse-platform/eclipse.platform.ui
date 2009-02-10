/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.services.context.spi;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.internal.context.ContextInjectionImpl;


final public class ContextInjectionFactory {
	
	private ContextInjectionFactory() {
		// prevents instantiations
	}
	
	static public void inject(Object object, IEclipseContext context) {
		inject(object, context, null, null, null);
	}
	
	static public void inject(Object object, IEclipseContext context, String fieldPrefix, String setMethodPrefix, String removeMethodPrefix) {
		ContextInjectionImpl injector = new ContextInjectionImpl(fieldPrefix, setMethodPrefix, removeMethodPrefix);
		injector.injectInto(object, context);
	}
	
}

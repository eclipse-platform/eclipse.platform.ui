/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.osgi;

import org.eclipse.e4.core.internal.contexts.IEclipseContextDebugger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ContextDebugHelper {

	public static IEclipseContextDebugger getDebugger() {
		Bundle bundle = FrameworkUtil.getBundle(ContextDebugHelper.class);
		BundleContext bundleContext = bundle == null ? null : bundle.getBundleContext();
		ServiceReference<IEclipseContextDebugger> ref = bundleContext == null ? null
				: bundleContext.getServiceReference(IEclipseContextDebugger.class);
		if (ref == null)
			return null;
		IEclipseContextDebugger contextDebugger = bundleContext.getService(ref);
		bundleContext.ungetService(ref); // this is not the proper place but will do in a static environment
		return contextDebugger;
	}
}

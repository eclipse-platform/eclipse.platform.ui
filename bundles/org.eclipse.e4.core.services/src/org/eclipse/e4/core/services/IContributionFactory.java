/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.osgi.framework.Bundle;

public interface IContributionFactory {

	public Object call(Object object, String uriString, String methodName, IEclipseContext context,
			Object defaultValue);

	public Object create(String uriString, IEclipseContext context);

	public Bundle getBundle(String uriString);

}

/*******************************************************************************
 *  Copyright (c) 2008, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.contributions;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.Bundle;

// TBD this became an utility method to create object from a bundle.
// Change it into an utility method somewhere.
public interface IContributionFactory {

	public Object create(String uriString, IEclipseContext context);

	public Bundle getBundle(String uriString);

}

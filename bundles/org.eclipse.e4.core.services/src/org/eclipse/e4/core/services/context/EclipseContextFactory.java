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

package org.eclipse.e4.core.services.context;

import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;
import org.eclipse.e4.core.services.internal.context.EclipseContext;


public final class EclipseContextFactory  {

	// TBD do we need a name?
	static public IEclipseContext create(String name) {
		return new EclipseContext(null, name, null);
	}
	
	static public IEclipseContext create(String name, IEclipseContextStrategy strategy) {
		return new EclipseContext(null, name, strategy);
	}
	
	static public IEclipseContext create(String name, IEclipseContext parent, IEclipseContextStrategy strategy) {
		return new EclipseContext(parent, name, strategy);
	}
}
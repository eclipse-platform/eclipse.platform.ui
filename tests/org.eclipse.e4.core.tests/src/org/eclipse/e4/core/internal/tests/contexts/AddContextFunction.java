/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.internal.tests.contexts;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;


/**
 * A function provided as a declarative service. See OSGI-INF/adder.xml.
 */
public class AddContextFunction extends ContextFunction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.services.context.spi.ContextFunction#compute(org.
	 * eclipse.e4.core.services.context.IEclipseContext, java.lang.Object[])
	 */
	public Object compute(IEclipseContext context, String contextKey) {
		Integer xInt = (Integer) context.get("x");
		Integer yInt = (Integer) context.get("y");
		int sum = xInt == null ? 0 : xInt.intValue();
		sum += yInt == null ? 0 : yInt.intValue();
		return new Integer(sum);
	}
}

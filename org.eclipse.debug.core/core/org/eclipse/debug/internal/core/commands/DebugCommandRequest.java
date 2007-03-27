/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.commands;

import org.eclipse.debug.core.commands.IDebugCommandRequest;

/**
 * @since 3.3
 */
public class DebugCommandRequest extends Request implements IDebugCommandRequest {
	
	private Object[] fElements;
	
	public DebugCommandRequest(Object[] elements) {
		fElements = elements;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.commands.IDebugCommandRequest#getElements()
	 */
	public Object[] getElements() {
		return fElements;
	}

	
}

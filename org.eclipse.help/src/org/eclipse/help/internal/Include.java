/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;

import org.eclipse.help.IInclude;
import org.w3c.dom.Element;

public class Include extends UAElement implements IInclude {

	public static final String NAME = "include"; //$NON-NLS-1$
	public static final String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	
	public Include(IInclude src) {
		super(NAME, src);
		setPath(src.getPath());
	}
	
	public Include(Element src) {
		super(src);
	}

	public String getPath() {
		return getAttribute(ATTRIBUTE_PATH);
	}
	
	public void setPath(String path) {
		setAttribute(ATTRIBUTE_PATH, path);
	}
}

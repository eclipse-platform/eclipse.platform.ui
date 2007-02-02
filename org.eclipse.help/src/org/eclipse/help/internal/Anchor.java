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

import org.eclipse.help.IAnchor;
import org.w3c.dom.Element;

public class Anchor extends UAElement implements IAnchor {

	public static final String NAME = "anchor"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	
	public Anchor(IAnchor src) {
		super(NAME, src);
		setId(src.getId());
	}
	
	public Anchor(Element src) {
		super(src);
	}

	public String getId() {
		return getAttribute(ATTRIBUTE_ID);
	}
	
	public void setId(String id) {
		setAttribute(ATTRIBUTE_ID, id);
	}
}

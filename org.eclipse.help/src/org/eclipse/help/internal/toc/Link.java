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
package org.eclipse.help.internal.toc;

import org.eclipse.help.ILink;
import org.eclipse.help.internal.UAElement;
import org.w3c.dom.Element;

public class Link extends UAElement implements ILink {

	public static final String NAME = "link"; //$NON-NLS-1$
	public static final String ATTRIBUTE_TOC = "toc"; //$NON-NLS-1$
	
	public Link(ILink src) {
		super(NAME, src);
		setToc(src.getToc());
	}
	
	public Link(Element src) {
		super(src);
	}

	public String getToc() {
		return getAttribute(ATTRIBUTE_TOC);
	}
	
	public void setToc(String toc) {
		setAttribute(ATTRIBUTE_TOC, toc);
	}
}

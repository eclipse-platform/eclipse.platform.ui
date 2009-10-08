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
package org.eclipse.help.internal.index;

import org.eclipse.help.IIndexSubpath;
import org.eclipse.help.internal.UAElement;
import org.w3c.dom.Element;

public class IndexSubpath extends UAElement implements IIndexSubpath {
	
	public static final String NAME = "subpath"; //$NON-NLS-1$
	public static final String ATTRIBUTE_KEYWORD = "keyword"; //$NON-NLS-1$
	
	public IndexSubpath(IIndexSubpath src) {
		super(NAME, src);
		setKeyword(src.getKeyword());
		appendChildren(src.getChildren());
	}
	
	public IndexSubpath(Element src) {
		super(src);
	}

	public String getKeyword() {
		return getAttribute(ATTRIBUTE_KEYWORD);
	}
	
	public void setKeyword(String keyword) {
		setAttribute(ATTRIBUTE_KEYWORD, keyword);
	}

}

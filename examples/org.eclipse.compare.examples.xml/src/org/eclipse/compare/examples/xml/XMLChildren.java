/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.examples.xml;

import java.util.HashMap;

import org.eclipse.jface.text.IDocument;

/** XMLNode that has children elements */
public class XMLChildren extends XMLNode {
	
	public int children;	// counts the number of children
	public HashMap childElements;	// maps the name of XML child elements to their # of occurence
	
	public XMLChildren(String XMLType, String id, String value, String signature, IDocument doc, int start, int length) {
		super(XMLType, id, value, signature, doc, start, length);
		children= 0;
		childElements = new HashMap();
	}
}


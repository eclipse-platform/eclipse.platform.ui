/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor.outline;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXParseException;

/**
 * OutlinePreparingHandlerNew.java
 */
public class OutlinePreparingHandlerNew extends OutlinePreparingHandler {

	public OutlinePreparingHandlerNew(File mainFileContainer) throws ParserConfigurationException {
		super(mainFileContainer);
	}
	
	protected void generateErrorElementHierarchy(SAXParseException exception) {
		if (getRootElement() == null) {
			setRootElement(generateErrorNode(exception));
		} else {
			super.generateErrorElementHierarchy(exception);
		}
	}
}

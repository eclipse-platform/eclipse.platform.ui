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

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;

/**
 * PlantyContentOutlinePageNew.java
 */
public class PlantyContentOutlinePageNew extends PlantyContentOutlinePage {

	public PlantyContentOutlinePageNew() {
		super();
	}

	public synchronized void reconcile() {
		final XmlElement contentOutline= getInput() == null ? new XmlElement("") : getContentOutline(getInput());
		
		if (!getControl().isDisposed()) {
			getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					update(contentOutline);
				}
			});
		}
	}

	public synchronized void setInput(Object input) {
		super.setInput(input);
	}

	public synchronized void dispose() {
		super.dispose();
	}

	protected String getContentAsString(Object input) {
		return getReaderContentAsString(new BufferedReader(new StringReader(((IDocument) input).get())));
	}

	protected IPath getLocation(Object input) {
		return null;
	}

	protected OutlinePreparingHandler createOutlinePreparingHandler(File tempParentFile) throws ParserConfigurationException {
		return new OutlinePreparingHandlerNew(tempParentFile);
	}

}

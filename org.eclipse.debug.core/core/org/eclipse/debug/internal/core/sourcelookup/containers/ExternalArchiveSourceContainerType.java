/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.*;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * External archive source container type.
 * 
 * @since 3.0
 */
public class ExternalArchiveSourceContainerType extends AbstractSourceContainerTypeDelegate {

	/**
	 * Unique identifier for the folder source container type
	 * (value <code>org.eclipse.debug.core.containerType.archive</code>).
	 */
	public static final String TYPE_ID = DebugPlugin.getUniqueIdentifier() + ".containerType.externalArchive";	 //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = SourceLookupUtils.parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if ("archive".equals(element.getNodeName())) { //$NON-NLS-1$
				String string = element.getAttribute("path"); //$NON-NLS-1$
				if (string == null || string.length() == 0) {
					abort(SourceLookupMessages.getString("ExternalArchiveSourceContainerType.10"), null); //$NON-NLS-1$
				}
				String detect = element.getAttribute("detectRoot"); //$NON-NLS-1$
				boolean auto = "true".equals(detect); //$NON-NLS-1$
				return new ExternalArchiveSourceContainer(string, auto);
			} else {
				abort(SourceLookupMessages.getString("ExternalArchiveSourceContainerType.11"), null); //$NON-NLS-1$
			}
		}
		abort(SourceLookupMessages.getString("ExternalArchiveSourceContainerType.12"), null); //$NON-NLS-1$
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		ExternalArchiveSourceContainer archive = (ExternalArchiveSourceContainer) container;
		Document document = SourceLookupUtils.newDocument();
		Element element = document.createElement("archive"); //$NON-NLS-1$
		element.setAttribute("path", archive.getName()); //$NON-NLS-1$
		String detectRoot = "false"; //$NON-NLS-1$
		if (archive.isDetectRoot()) {
			detectRoot = "true"; //$NON-NLS-1$
		}
		element.setAttribute("detectRoot", detectRoot);  //$NON-NLS-1$
		document.appendChild(element);
		return SourceLookupUtils.serializeDocument(document);
	}
}

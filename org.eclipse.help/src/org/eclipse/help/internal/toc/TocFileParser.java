/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.Node;
import org.eclipse.help.TocContribution;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TocFileParser extends DefaultHandler {

	private NodeReader reader;

	/*
	 * Parses the given toc XML file into model objects (a TocContribution).
	 */
    public TocContribution parse(TocFile tocFile) throws IOException, SAXException, ParserConfigurationException {
    	if (reader == null) {
    		reader = new NodeReader();
    		reader.setIgnoreWhitespaceNodes(true);
    	}
		Node node = reader.read(tocFile.getInputStream());
		TocContribution contribution = new TocContribution();
		contribution.setCategoryId(tocFile.getCategory());
		contribution.setContributorId(tocFile.getPluginId());
		contribution.setExtraDocuments(DocumentFinder.collectExtraDocuments(tocFile));
		contribution.setId(HrefUtil.normalizeHref(tocFile.getPluginId(), tocFile.getFile()));
		contribution.setLocale(tocFile.getLocale());
		contribution.setPrimary(tocFile.isPrimary());
		contribution.setToc(node);
    	return contribution;
    }
}

/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.internal.dynamic.DocumentReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TocFileParser extends DefaultHandler {

	private DocumentReader reader;

	public TocContribution parse(TocFile tocFile) throws IOException, SAXException, ParserConfigurationException {
		if (reader == null) {
			reader = new DocumentReader();
		}
		InputStream in = tocFile.getInputStream();
    	if (in != null) {
			try {
				Toc toc = (Toc) reader.read(in);
				TocContribution contribution = new TocContribution();
				contribution.setCategoryId(tocFile.getCategory());
				contribution.setContributorId(tocFile.getPluginId());
				contribution.setExtraDocuments(DocumentFinder.collectExtraDocuments(tocFile));
				contribution.setId(HrefUtil.normalizeHref(tocFile.getPluginId(), tocFile.getFile()));
				contribution.setLocale(tocFile.getLocale());
				contribution.setToc(toc);
				contribution.setPrimary(tocFile.isPrimary());
				return contribution;
			} finally {
				in.close();
			}
    	}
    	else {
    		throw new FileNotFoundException();
    	}
    }
}

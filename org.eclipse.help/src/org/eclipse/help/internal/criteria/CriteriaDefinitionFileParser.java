/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.criteria;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.internal.dynamic.DocumentReader;
import org.xml.sax.SAXException;

public class CriteriaDefinitionFileParser {

	private DocumentReader reader;
	
    public CriteriaDefinitionContribution parse(CriteriaDefinitionFile criteriaDefinitionFile) throws IOException, SAXException, ParserConfigurationException {
		if (reader == null) {
			reader = new CriteriaDefinitionDocumentReader();
		}
		InputStream in = criteriaDefinitionFile.getInputStream();
		if (in != null) {
			CriteriaDefinition criteria = (CriteriaDefinition)reader.read(in);
			CriteriaDefinitionContribution contrib = new CriteriaDefinitionContribution();
	    	contrib.setId('/' + criteriaDefinitionFile.getPluginId() + '/' + criteriaDefinitionFile.getFile());
			contrib.setCriteriaDefinition(criteria);
			contrib.setLocale(criteriaDefinitionFile.getLocale());
			return contrib;
		}
    	else {
    		throw new FileNotFoundException();
    	}
    }
}

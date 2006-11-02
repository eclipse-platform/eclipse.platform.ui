/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base.remote;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.Node;
import org.eclipse.help.TocContribution;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.xml.sax.SAXException;

public class RemoteTocParser {

	private NodeReader reader;
	
	public TocContribution[] parse(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		if (reader == null) {
			reader = new NodeReader();
		}
		Node root = reader.read(in);
		Node[] children = root.getChildren();
		TocContribution[] contributions = new TocContribution[children.length];
		for (int i=0;i<children.length;++i) {
			Node node = children[i];
			Node[] contribChildren = node.getChildren();
			String[] extraDocuments = new String[contribChildren.length - 1];
			for (int j=0;j<extraDocuments.length;++j) {
				extraDocuments[j] = contribChildren[j + 1].getAttribute("href"); //$NON-NLS-1$
			}
			TocContribution contribution = new TocContribution();
			contribution.setCategoryId(node.getAttribute("categoryId")); //$NON-NLS-1$
			contribution.setContributorId(node.getAttribute("contributorId")); //$NON-NLS-1$
			contribution.setExtraDocuments(extraDocuments);
			contribution.setId(node.getAttribute("id")); //$NON-NLS-1$
			contribution.setLocale(node.getAttribute("locale")); //$NON-NLS-1$
			contribution.setPrimary("true".equals(node.getAttribute("isPrimary")));  //$NON-NLS-1$//$NON-NLS-2$
			contribution.setToc(contribChildren[0]);
			contributions[i] = contribution;
		}		
		return contributions;
	}
}

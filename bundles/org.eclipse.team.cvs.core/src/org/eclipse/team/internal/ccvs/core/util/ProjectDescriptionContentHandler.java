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
package org.eclipse.team.internal.ccvs.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class ProjectDescriptionContentHandler implements ContentHandler {

	IProjectDescription desc;
	boolean inProjectDescription = false;

	boolean inComment = false;
	StringBuffer buffer = new StringBuffer();

	boolean inBuilder = false;
	List builders = new ArrayList();
	ICommand currentBuilder = null;
	Map args = new HashMap();

	List natures = new ArrayList();

	List references = new ArrayList();

	Stack tagStack = new Stack();

	ProjectDescriptionContentHandler(IProjectDescription desc) {
		this.desc = desc;
	}
	/**
	 * @see ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] chars, int startIndex, int length)
		throws SAXException {
		buffer.append(chars, startIndex, length);
	}
	/**
	 * @see ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
	}
	/**
	 * @see ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {
		if (localName.equals("project-description") && inProjectDescription) { //$NON-NLS-1$
			inProjectDescription = false;
			desc.setBuildSpec((ICommand[]) builders.toArray(new ICommand[builders.size()]));
			desc.setNatureIds((String[]) natures.toArray(new String[natures.size()]));
			desc.setReferencedProjects(
				(IProject[]) references.toArray(new IProject[references.size()]));
		} else if (localName.equals("comment") && inProjectDescription && inComment) { //$NON-NLS-1$
			inComment = false;
			desc.setComment(buffer.toString());
		} else if (localName.equals("builder") && inProjectDescription && inBuilder) { //$NON-NLS-1$
			inBuilder = false;
			currentBuilder.setArguments(args);
			if (currentBuilder.getBuilderName() != null)
				builders.add(currentBuilder);
		}
		if (!localName.equals(tagStack.peek())) {
			throw new RuntimeException(Policy.bind("ProjectDescriptionContentHandler.xml")); //$NON-NLS-1$
		}
		tagStack.pop();
	}
	/**
	 * @see ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String arg0) throws SAXException {
	}
	/**
	 * @see ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
		throws SAXException {
	}
	/**
	 * @see ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(String arg0, String arg1)
		throws SAXException {
	}
	/**
	 * @see ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator arg0) {
	}
	/**
	 * @see ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String e) throws SAXException {
	}
	/**
	 * @see ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
	}
	/**
	 * @see ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(
		String namespaceURI,
		String localName,
		String qName,
		Attributes atts)
		throws SAXException {
		if (localName.equals("project-description") && !inProjectDescription) { //$NON-NLS-1$
			inProjectDescription = true;
		} else if (localName.equals("comment") && inProjectDescription && !inComment) { //$NON-NLS-1$
			inComment = true;
		} else if (localName.equals("builder") && inProjectDescription && !inBuilder) { //$NON-NLS-1$
			String builderName = atts.getValue("name"); //$NON-NLS-1$
			if (builderName != null) {
				inBuilder = true;
				currentBuilder = desc.newCommand();
				currentBuilder.setBuilderName(builderName);
				args = new HashMap(11);
			}
		} else if (localName.equals("arg") && inProjectDescription && inBuilder) { //$NON-NLS-1$
			String argName = atts.getValue("name"); //$NON-NLS-1$
			String argValue = atts.getValue("value"); //$NON-NLS-1$
			if (argName != null && argValue != null)
				args.put(argName, argValue);
		} else if (localName.equals("nature") && inProjectDescription && !inBuilder) { //$NON-NLS-1$
			String natureId = atts.getValue("id"); //$NON-NLS-1$
			if (natureId != null)
				natures.add(natureId);
		} else if (
			localName.equals("reference") && inProjectDescription && !inBuilder) { //$NON-NLS-1$
			String projectName = atts.getValue("project-name"); //$NON-NLS-1$
			if (projectName != null)
				references.add(
					ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
		}
		// empty buffer
		buffer = new StringBuffer();
		tagStack.push(localName);
	}
	/**
	 * @see ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	public void startPrefixMapping(String arg0, String arg1) throws SAXException {
	}
}

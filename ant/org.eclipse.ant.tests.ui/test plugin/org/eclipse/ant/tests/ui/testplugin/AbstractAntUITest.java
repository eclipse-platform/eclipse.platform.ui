/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.testplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.ant.ui.internal.editor.outline.AntModel;
import org.eclipse.ant.ui.internal.editor.outline.XMLCore;
import org.eclipse.ant.ui.internal.editor.support.TestLocationProvider;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractAntUITest extends TestCase {

	public static IProject project;
	private IDocument currentDocument;

	public AbstractAntUITest(String name) {
		super(name);
	}
		
	protected File getBuildFile(String buildFileName) {
		IFile file = getProject().getFolder("buildfiles").getFile(buildFileName);
		assertTrue("Could not find build file named: " + buildFileName, file.exists());
		return file.getLocation().toFile();
	}
	
	/**
	 * Returns the 'AntUITests' project.
	 * 
	 * @return the test project
	 */
	protected IProject getProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(ProjectHelper.PROJECT_NAME);
	}
	
	protected IDocument getDocument(String fileName) {
		File file = getBuildFile(fileName);
		InputStream in;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		String initialContent= getStreamContentAsString(in);
		return new Document(initialContent);
	}

	protected String getStreamContentAsString(InputStream inputStream) {
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(inputStream, ResourcesPlugin.getEncoding());
		} catch (UnsupportedEncodingException e) {
			AntUIPlugin.log(e);
			return ""; //$NON-NLS-1$
		}
		BufferedReader tempBufferedReader = new BufferedReader(reader);

		return getReaderContentAsString(tempBufferedReader);
	}
	
	protected String getReaderContentAsString(BufferedReader tempBufferedReader) {
		StringBuffer tempResult = new StringBuffer();
		try {
			String tempLine= tempBufferedReader.readLine();

			while(tempLine != null) {
				if(tempResult.length() != 0) {
					tempResult.append("\n"); //$NON-NLS-1$
				}
				tempResult.append(tempLine);
				tempLine = tempBufferedReader.readLine();
			}
		} catch (IOException e) {
			AntUIPlugin.log(e);
			return null;
		}

		return tempResult.toString();
	}
		
	protected AntModel getAntModel(String fileName) {
		currentDocument= getDocument(fileName);
		AntModel model= new AntModel(XMLCore.getDefault(), currentDocument, null, new TestLocationProvider(fileName));
		model.reconcile();
		return model;
	}
	
	/**
	 * @return
	 */
	public IDocument getCurrentDocument() {
		return currentDocument;
	}

	/**
	 * @param currentDocument
	 */
	public void setCurrentDocument(IDocument currentDocument) {
		this.currentDocument = currentDocument;
	}

	/**
	 * Returns the content of the specified file as <code>String</code>.
	 */
	protected String getFileContentAsString(File aFile) throws FileNotFoundException {
	    InputStream stream = new FileInputStream(aFile);
	    InputStreamReader reader = new InputStreamReader(stream);
	    BufferedReader bufferedReader = new BufferedReader(reader);
	
	    String result = "";
	    try {
	        String line= bufferedReader.readLine();
	    
	        while(line != null) {
	            result += "\n";
	            result += line;
	            line = bufferedReader.readLine();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
		return result;
	}
	
	protected SAXParser getSAXParser() throws SAXException {
		SAXParser parser = parser = new SAXParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", false); //$NON-NLS-1$
		return parser;
	}
	
	protected void parse(InputStream stream, SAXParser parser, DefaultHandler handler, File editedFile) {
		InputSource inputSource= new InputSource(stream);
		if (editedFile != null) {
			//needed for resolving relative external entities
			inputSource.setSystemId(editedFile.getAbsolutePath());
		}

		parser.setContentHandler(handler);
		parser.setDTDHandler(handler);
		parser.setEntityResolver(handler);
		parser.setErrorHandler(handler);
		try {
			parser.parse(inputSource);
		} catch (SAXException e) {
		} catch (IOException e) {
		}
	}
}

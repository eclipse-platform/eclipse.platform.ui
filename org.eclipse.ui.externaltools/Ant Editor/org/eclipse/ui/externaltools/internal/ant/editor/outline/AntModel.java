/**********************************************************************
Copyright (c) 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor.outline;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * AntModel.java
 */
public class AntModel {

	private XMLCore fCore;
	private IDocument fDocument;
	private IProblemRequestor fProblemRequestor;
	private ILocationProvider fLocationProvider;

	private XmlElement[] fRootElements= new XmlElement[0];
	private Object fRootElementLock= new Object();

	private final Object fDirtyLock= new Object();
	private boolean fIsDirty= true;
	private IDocumentListener fListener;

	public AntModel(XMLCore core, IDocument document, IProblemRequestor problemRequestor, ILocationProvider locationProvider) {
		fCore= core;
		fDocument= document;
		fProblemRequestor= problemRequestor;
		fLocationProvider= locationProvider;
	}

	public void install() {
		fListener= new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
				synchronized (fDirtyLock) {
					fIsDirty= true;
				}
			}
			public void documentChanged(DocumentEvent event) {}
		};
		fDocument.addDocumentListener(fListener);
		
		reconcile();
	}
	
	public void dispose() {		

		if (fDocument != null) {
			fDocument.removeDocumentListener(fListener);
		}

		synchronized (this) {
			fDocument= null;
			fCore= null;
		}
	}
	
	public void reconcile() {
		
		synchronized (fDirtyLock) {
			if (!fIsDirty) {
				return;
			}
			fIsDirty= false;
		}

		synchronized (this) {
			if (fCore == null) {
				// disposed
				return;
			}
			
			XmlElement[] rootElements;
			if (fDocument == null) {
				rootElements= new XmlElement[0];
			} else {
				XmlElement rootElement= parseDocument(fDocument);
				if (rootElement == null) {
					rootElements= new XmlElement[0];
				} else {
					rootElements= new XmlElement[] { rootElement };
				}
			} 

			setRootElements(rootElements);
	
			fCore.notifyDocumentModelListeners(new DocumentModelChangeEvent(this));
		}
	}

	private void setRootElements(XmlElement[] rootElements) {
		synchronized (fRootElementLock) {
			fRootElements= rootElements;
		}
	}

	public XmlElement[] getRootElements() {
		synchronized (fRootElementLock) {
			return fRootElements;
		}
	}

	/**
	 * Gets the content outline for a given input element.
	 * Returns the root XmlElement, or null if the
	 * outline could not be generated.
	 */
	private XmlElement parseDocument(IDocument input) {
		/*
		 * What happens here:
		 * The file is parsed by the SAX Parser.
		 * The Parser then creates the DOM Tree of the file.
		 * The root element is returned here.
		 */
         
		String tempWholeDocumentString= input.get();
         
		// Create the parser
		SAXParser tempParser;
		try {
			SAXParserFactory tempSAXParserFactory = SAXParserFactory.newInstance();
			tempSAXParserFactory.setNamespaceAware(false);
			tempParser = tempSAXParserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		} catch (SAXException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		} catch (FactoryConfigurationError e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		}

		// Create the handler
		OutlinePreparingHandler tempHandler = null;
		IPath location= fLocationProvider.getLocation();
		try {
			File tempParentFile = null;
			if(location != null) {
				tempParentFile = location.toFile().getParentFile();
			}
			tempHandler = new OutlinePreparingHandler(tempParentFile);
			tempHandler.setProblemRequestor(fProblemRequestor);
			tempHandler.setDocument(input);
            
		} catch (ParserConfigurationException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		}
        
		// Parse!
		try {
			tempHandler.begin();
			InputSource tempInputSource = new InputSource(new StringReader(tempWholeDocumentString));
			if (location != null) {
				//needed for resolving relative external entities
				tempInputSource.setSystemId(location.toOSString());
			}
			tempParser.setProperty("http://xml.org/sax/properties/lexical-handler", tempHandler); //$NON-NLS-1$
			tempParser.parse(tempInputSource, tempHandler);
		} catch(SAXParseException e) {
			tempHandler.fixEndLocations(e);
		} catch (SAXException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		} catch (IOException e) {
			tempHandler.fixEndLocations();
			XmlElement tempRootElement = tempHandler.getRootElement();
			if (tempRootElement != null) {
				return generateExceptionOutline(e, tempRootElement);
			} else {
				ExternalToolsPlugin.getDefault().log(e);
				return null;		
			}
		} finally {
			tempHandler.end();			
		}
        
		XmlElement tempRootElement = tempHandler.getRootElement();
		return tempRootElement;
	}

	private XmlElement generateExceptionOutline(Exception e, XmlElement rootElement) {
		rootElement.setIsErrorNode(true);		
	
		XmlElement errorNode= new XmlElement(e.getMessage());
		errorNode.setIsErrorNode(true);
		rootElement.addChildNode(errorNode);
		return rootElement;
	}
}

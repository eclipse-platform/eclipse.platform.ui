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

package org.eclipse.ant.ui.internal.editor.outline;

import java.io.IOException;
import java.io.StringReader;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ant.ui.internal.editor.xml.XmlElement;
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
			tempParser = new SAXParser();
			tempParser.setFeature("http://xml.org/sax/features/namespaces", false); //$NON-NLS-1$
		} catch (SAXException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		}

		// Create the handler
		OutlinePreparingHandler tempHandler = null;

		tempHandler = new OutlinePreparingHandler(fLocationProvider);
		tempHandler.setProblemRequestor(fProblemRequestor);
		tempHandler.setDocument(input);
		
		tempParser.setContentHandler(tempHandler);
		tempParser.setDTDHandler(tempHandler);
		tempParser.setEntityResolver(tempHandler);
		tempParser.setErrorHandler(tempHandler);
//		tempParser.setLocale(...);
        
		// Parse!
		try {
			tempHandler.begin();
			InputSource tempInputSource = new InputSource(new StringReader(tempWholeDocumentString));
			IPath location= fLocationProvider.getLocation();
			if (location != null) {
				//needed for resolving relative external entities
				tempInputSource.setSystemId(location.toOSString());
			}
			tempParser.setProperty("http://xml.org/sax/properties/lexical-handler", tempHandler); //$NON-NLS-1$
			tempParser.parse(tempInputSource);
		} catch(SAXParseException e) {
			tempHandler.fixEndLocations(e);
		} catch (SAXException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		} catch (IOException e) {
			XmlElement tempElement= tempHandler.getLastOpenElement();
			tempHandler.fixEndLocations();
			generateExceptionOutline(tempElement);
		} finally {
			tempHandler.end();			
		}
        
		XmlElement tempRootElement = tempHandler.getRootElement();
		return tempRootElement;
	}

	private void generateExceptionOutline(XmlElement openElement) {
		while (openElement != null) {
			openElement.setIsErrorNode(true);
			openElement= openElement.getParentNode();
		}
	}
	
	/**
	 * @return ILocationProvider
	 */
	public ILocationProvider getLocationProvider() {
		return fLocationProvider;
	}
}

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

package org.eclipse.ant.internal.ui.editor.outline;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.ant.internal.ui.editor.xml.XmlElement;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
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
	 * Returns the root XmlElement, or <code>null</code> if the
	 * outline could not be generated.
	 */
	private XmlElement parseDocument(IDocument input) {
		/*
		 * What happens here:
		 * The file is parsed by the SAX Parser.
		 * The Parser then creates the DOM Tree of the file.
		 * The root element is returned here.
		 */
         
		// Create the parser
		SAXParser parser= getSAXParser();
		if (parser == null) {
			return null;
		}
		
		// Create the handler
		OutlinePreparingHandler handler = new OutlinePreparingHandler(fLocationProvider);
		handler.setProblemRequestor(fProblemRequestor);
		handler.setDocument(input);

		// Parse!
		try {
			handler.begin();
			String wholeDocString= input.get();
			InputSource inputSource = new InputSource(new StringReader(wholeDocString));
			IPath location= fLocationProvider.getLocation();
			if (location != null) {
				//needed for resolving relative external entities
				inputSource.setSystemId(location.toOSString());
			}
			parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler); //$NON-NLS-1$
			parser.parse(inputSource, handler);
		} catch(SAXParseException e) {
			handler.fixEndLocations(e);
		} catch (SAXException e) {
			AntUIPlugin.log(e);
			return null;
		} catch (IOException e) {
			handler.fixEndLocations();
			generateExceptionOutline(handler.getLastOpenElement());
		} finally {
			handler.end();			
		}
        
		return handler.getRootElement();
	}
	
	private SAXParser getSAXParser() {
		SAXParser parser = null;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {
			AntUIPlugin.log(e);
		} catch (SAXException e) {
			AntUIPlugin.log(e);
		}
		return parser;
	}

	private void generateExceptionOutline(XmlElement openElement) {
		while (openElement != null) {
			openElement.setIsErrorNode(true);
			openElement= openElement.getParentNode();
		}
	}
	
	public ILocationProvider getLocationProvider() {
		return fLocationProvider;
	}
}
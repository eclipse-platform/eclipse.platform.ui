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

import org.eclipse.jface.text.Region;
import org.eclipse.ant.ui.internal.editor.xml.XmlElement;
import org.xml.sax.SAXParseException;


public class XEErrorHandler implements IProblemRequestor {
	
	protected class XMLProblem extends Region implements IProblem {
		private String fCode, fMessage;
		private int fSeverity;
		
		public XMLProblem(String code, String message, int severity, int offset, int length) {
			super(offset, length);
			fCode= code;
			fMessage= message;
			fSeverity= severity;
		}
		
		public String getCode() {
			return fCode;
		}
		
		public String getMessage() {
			return fMessage;
		}
		
		public boolean isError() {
			return fSeverity == SEVERTITY_ERROR || fSeverity == SEVERTITY_FATAL_ERROR;
		}
		
		public boolean isWarning() {
			return fSeverity == SEVERTITY_WARNING;
		}
		
	}
	
	private static final int SEVERTITY_WARNING= 0;
	private static final int SEVERTITY_ERROR= 1;
	private static final int SEVERTITY_FATAL_ERROR= 2;

	private IProblemRequestor fProblemRequestor;	
	
	/**
	 * Constructor XEErrorHandler.
	 * @param resourceProvider
	 */
	public XEErrorHandler(IProblemRequestor problemRequestor) {
		fProblemRequestor= problemRequestor;
	}

	public void acceptProblem(IProblem problem) {
		if (fProblemRequestor != null) {
			fProblemRequestor.acceptProblem(problem);
		}
	}

	public void beginReporting() {
		if (fProblemRequestor != null) {
			fProblemRequestor.beginReporting();
		}
	}
	
	public void endReporting() {
		if (fProblemRequestor != null) {
			fProblemRequestor.endReporting();
		}
	}

	protected IProblem createProblem(SAXParseException exception, XmlElement element, int severity) {
		return new XMLProblem(exception.toString(), exception.getMessage(), severity, element.getOffset(), element.getLength());
	}

	protected void notifyProblemRequestor(SAXParseException exception, XmlElement element, int severity) {
		IProblem problem= createProblem(exception, element, severity);
		acceptProblem(problem);
	}

	public void warning(SAXParseException exception, XmlElement element) {
		notifyProblemRequestor(exception, element, SEVERTITY_WARNING);
	}
	
	public void error(SAXParseException exception, XmlElement element) {
		notifyProblemRequestor(exception, element, SEVERTITY_ERROR);
	}

	public void fatalError(SAXParseException exception, XmlElement element) {
		notifyProblemRequestor(exception, element, SEVERTITY_FATAL_ERROR);
	}
}

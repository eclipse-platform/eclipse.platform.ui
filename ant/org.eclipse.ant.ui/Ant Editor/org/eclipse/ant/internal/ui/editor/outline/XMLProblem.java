/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.outline;

import org.eclipse.jface.text.Region;

class XMLProblem extends Region implements IProblem {
	
	public static final int SEVERTITY_WARNING= 0;
	public static final int SEVERTITY_ERROR= 1;
	public static final int SEVERTITY_FATAL_ERROR= 2;
	
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
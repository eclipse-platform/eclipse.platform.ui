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

public class XMLProblem extends Region implements IProblem {
	
	public static final int NO_PROBLEM= -1;
	public static final int SEVERITY_WARNING= 0;
	public static final int SEVERITY_ERROR= 1;
	public static final int SEVERITY_FATAL_ERROR= 2;
	
	private String fMessage;
	private int fSeverity;
	private int fAdjustedLength= -1;
	private int fLineNumber= -1;
	
	public XMLProblem(String message, int severity, int offset, int length, int lineNumber) {
		super(offset, length);
		fMessage= message;
		fSeverity= severity;
		fLineNumber= lineNumber;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.outline.IProblem#getMessage()
	 */
	public String getMessage() {
		return fMessage;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.outline.IProblem#isError()
	 */
	public boolean isError() {
		return fSeverity == SEVERITY_ERROR || fSeverity == SEVERITY_FATAL_ERROR;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.outline.IProblem#isWarning()
	 */
	public boolean isWarning() {
		return fSeverity == SEVERITY_WARNING;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IRegion#getLength()
	 */
	public int getLength() {
		if (fAdjustedLength != -1) {
			return fAdjustedLength;
		}
		return super.getLength();
	}
	
	/**
	 * Sets the length for this problem.
	 */
	public void setLength(int adjustedLength) {
		fAdjustedLength= adjustedLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.outline.IProblem#getLineNumber()
	 */
	public int getLineNumber() {
		return fLineNumber;
	}
}
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
package org.eclipse.jface.text.contentassist;



import org.eclipse.jface.text.ITextViewer;


/**
 * A default implementation of the <code>IContextInfomationValidator</code> interface.
 * This implementation determines whether the information is valid by asking the content 
 * assist processor for all  context information objects for the current position. If the 
 * currently displayed information is in the result set, the context information is 
 * considered valid.
 */
public final class ContextInformationValidator implements IContextInformationValidator {
	
	private IContentAssistProcessor fProcessor;
	private IContextInformation fContextInformation;
	private ITextViewer fViewer;

	/**
	 * Creates a new context information validator which is ready to be installed on
	 * a particular context information.
	 */
	public ContextInformationValidator(IContentAssistProcessor processor) {
		fProcessor= processor;
	}

	/*
	 * @see IContextInformationValidator#install
	 */
	public void install(IContextInformation contextInformation, ITextViewer viewer, int position) {
		fContextInformation= contextInformation;
		fViewer= viewer;
	}

	/*
	 * @see IContentAssistTipCloser#isContextInformationValid
	 */
	public boolean isContextInformationValid(int position) {
		IContextInformation[] infos= fProcessor.computeContextInformation(fViewer, position);
		if (infos != null && infos.length > 0) {
			for (int i= 0; i < infos.length; i++)
				if (fContextInformation.equals(infos[i]))
					return true;
		}
		return false;
	}
}

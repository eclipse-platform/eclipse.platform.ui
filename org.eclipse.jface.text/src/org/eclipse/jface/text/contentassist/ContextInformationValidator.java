/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/** The content assist processor. */
	private IContentAssistProcessor fProcessor;
	/** The context information to be validated. */
	private IContextInformation fContextInformation;
	/** The associated text viewer. */
	private ITextViewer fViewer;

	/**
	 * Creates a new context information validator which is ready to be installed on
	 * a particular context information.
	 *
	 * @param processor the processor to be used for validation
	 */
	public ContextInformationValidator(IContentAssistProcessor processor) {
		fProcessor= processor;
	}

	@Override
	public void install(IContextInformation contextInformation, ITextViewer viewer, int offset) {
		fContextInformation= contextInformation;
		fViewer= viewer;
	}

	@Override
	public boolean isContextInformationValid(int offset) {
		IContextInformation[] infos= fProcessor.computeContextInformation(fViewer, offset);
		if (infos != null && infos.length > 0) {
			for (IContextInformation info : infos)
				if (fContextInformation.equals(info))
					return true;
		}
		return false;
	}
}

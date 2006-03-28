/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;


/**
 * A default implementation of the {@link SubjectControlContextInformationValidator} interface.
 * This implementation determines whether the information is valid by asking the content
 * assist processor for all  context information objects for the current position. If the
 * currently displayed information is in the result set, the context information is
 * considered valid.
 *
 * @since 3.0
 * @deprecated As of 3.2, replaced by Platform UI's field assist support
 */
public final class SubjectControlContextInformationValidator implements ISubjectControlContextInformationValidator {

	/** The content assist processor. */
	private IContentAssistProcessor fProcessor;
	/** The context information to be validated. */
	private IContextInformation fContextInformation;
	/** The content assist subject control. */
	private IContentAssistSubjectControl fContentAssistSubjectControl;

	/**
	 * Creates a new context information validator which is ready to be installed on
	 * a particular context information.
	 *
	 * @param processor the processor to be used for validation
	 */
	public SubjectControlContextInformationValidator(IContentAssistProcessor processor) {
		fProcessor= processor;
	}

	/*
	 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
	 */
	public void install(IContextInformation contextInformation, ITextViewer viewer, int offset) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see ISubjectControlContextInformationValidator#install(IContextInformation, IContentAssistSubjectControl, int)
	 */
	public void install(IContextInformation contextInformation, IContentAssistSubjectControl contentAssistSubjectControl, int offset) {
		fContextInformation= contextInformation;
		fContentAssistSubjectControl= contentAssistSubjectControl;
	}

	/*
	 * @see IContentAssistTipCloser#isContextInformationValid(int)
	 */
	public boolean isContextInformationValid(int offset) {
		if (fContentAssistSubjectControl != null && fProcessor instanceof ISubjectControlContentAssistProcessor) {
			IContextInformation[] infos= ((ISubjectControlContentAssistProcessor)fProcessor).computeContextInformation(fContentAssistSubjectControl, offset);
			if (infos != null && infos.length > 0) {
				for (int i= 0; i < infos.length; i++) {
					if (fContextInformation.equals(infos[i]))
						return true;
				}
			}
		}
		return false;
	}
}

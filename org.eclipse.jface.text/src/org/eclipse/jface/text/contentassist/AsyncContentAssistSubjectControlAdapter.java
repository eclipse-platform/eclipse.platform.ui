/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - [251156] async content assist
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.ITextViewer;

class AsyncContentAssistSubjectControlAdapter extends ContentAssistSubjectControlAdapter {

	public AsyncContentAssistSubjectControlAdapter(ITextViewer viewer) {
		super(viewer);
	}
	
	@Override
	CompletionProposalPopup createCompletionProposalPopup(ContentAssistant contentAssistant, AdditionalInfoController controller) {
		if (fContentAssistSubjectControl != null)
			return new AsyncCompletionProposalPopup(contentAssistant, fContentAssistSubjectControl, controller);
		return new AsyncCompletionProposalPopup(contentAssistant, fViewer, controller);
	}

}

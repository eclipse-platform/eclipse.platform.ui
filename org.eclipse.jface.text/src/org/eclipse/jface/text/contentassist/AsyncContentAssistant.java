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

/**
 * A content assistant allowing multiple {@link IContentAssistProcessor}s and invoking their methods
 * asynchronously whenever possible.
 * @since 3.12
 */
public class AsyncContentAssistant extends ContentAssistant {

	@Override
	public void addContentAssistProcessor(IContentAssistProcessor processor, String contentType) {
		super.addContentAssistProcessor(processor, contentType);
	}

	@Override
	public void install(ITextViewer textViewer) {
		fViewer= textViewer;
		fContentAssistSubjectControlAdapter= new AsyncContentAssistSubjectControlAdapter(fViewer);
		install();
	}
}

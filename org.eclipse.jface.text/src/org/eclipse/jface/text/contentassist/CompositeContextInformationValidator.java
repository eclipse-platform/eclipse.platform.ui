/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import java.util.Arrays;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;

class CompositeContextInformationValidator implements IContextInformationValidator, IContextInformationPresenter {

	private final IContextInformationValidator[] children;

	public CompositeContextInformationValidator(IContextInformationValidator[] validators) {
		this.children= validators;
	}

	@Override
	public void install(IContextInformation info, ITextViewer viewer, int offset) {
		Arrays.stream(children).forEach(child -> child.install(info, viewer, offset));
	}

	@Override
	public boolean isContextInformationValid(int offset) {
		return Arrays.stream(children).anyMatch(child -> child.isContextInformationValid(offset));
	}

	@Override
	public boolean updatePresentation(int offset, TextPresentation presentation) {
		if (children.length == 1 && children[0] instanceof IContextInformationPresenter) {
			return ((IContextInformationPresenter) children[0]).updatePresentation(offset, presentation);
		}
		return false;
	}

}

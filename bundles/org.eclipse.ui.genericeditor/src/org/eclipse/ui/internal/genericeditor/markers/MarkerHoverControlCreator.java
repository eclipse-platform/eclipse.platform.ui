/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.markers;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlCreatorExtension;
import org.eclipse.swt.widgets.Shell;

public class MarkerHoverControlCreator implements IInformationControlCreator, IInformationControlCreatorExtension {

	private final boolean showAffordanceString;

	public MarkerHoverControlCreator() {
		this(true);
	}

	public MarkerHoverControlCreator(boolean showAffordanceString) {
		this.showAffordanceString = showAffordanceString;
	}

	@Override
	public boolean canReuse(IInformationControl control) {
		return control instanceof MarkerInformationControl;
	}

	@Override
	public boolean canReplace(IInformationControlCreator creator) {
		return creator instanceof MarkerHoverControlCreator;
	}

	@Override
	public IInformationControl createInformationControl(Shell parent) {
		return new MarkerInformationControl(parent, showAffordanceString);
	}

}

/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	@Override
	public boolean canReuse(IInformationControl control) {
		return false;
	}

	@Override
	public boolean canReplace(IInformationControlCreator creator) {
		return false;
	}

	@Override
	public IInformationControl createInformationControl(Shell parent) {
		return new MarkerInformationControl(parent,this);
	}

}

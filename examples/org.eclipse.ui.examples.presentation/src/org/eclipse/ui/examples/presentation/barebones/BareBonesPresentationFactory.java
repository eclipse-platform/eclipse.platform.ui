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
package org.eclipse.ui.examples.presentation.barebones;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Presentation factory for the bare-bones presentation
 */
public class BareBonesPresentationFactory extends AbstractPresentationFactory {

	public StackPresentation createEditorPresentation(Composite parent,
			IStackPresentationSite site) {
		return new BareBonesPartPresentation(parent, site);
	}

	public StackPresentation createViewPresentation(Composite parent,
			IStackPresentationSite site) {
		return new BareBonesPartPresentation(parent, site);
	}
	
	public StackPresentation createStandaloneViewPresentation(Composite parent,
			IStackPresentationSite site, boolean showTitle) {
		return new BareBonesPartPresentation(parent, site);
	}
}

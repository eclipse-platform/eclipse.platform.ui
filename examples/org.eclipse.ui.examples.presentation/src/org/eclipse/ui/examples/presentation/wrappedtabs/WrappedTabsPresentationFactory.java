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
package org.eclipse.ui.examples.presentation.wrappedtabs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * @since 3.0
 */
public class WrappedTabsPresentationFactory extends AbstractPresentationFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createEditorPresentation(org.eclipse.swt.widgets.Composite, org.eclipse.ui.presentations.IStackPresentationSite)
	 */
	public StackPresentation createEditorPresentation(Composite parent,
			IStackPresentationSite site) {
		return new WrappedTabsPartPresentation(parent, site, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createViewPresentation(org.eclipse.swt.widgets.Composite, org.eclipse.ui.presentations.IStackPresentationSite)
	 */
	public StackPresentation createViewPresentation(Composite parent,
			IStackPresentationSite site) {
		return new WrappedTabsPartPresentation(parent, site, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createStandaloneViewPresentation(org.eclipse.swt.widgets.Composite, org.eclipse.ui.presentations.IStackPresentationSite, boolean)
	 */
	public StackPresentation createStandaloneViewPresentation(Composite parent,
			IStackPresentationSite site, boolean showTitle) {
		return new WrappedTabsPartPresentation(parent, site, false);
	}

}

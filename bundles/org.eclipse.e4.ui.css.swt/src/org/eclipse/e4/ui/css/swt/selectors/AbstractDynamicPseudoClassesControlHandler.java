/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.selectors;

import org.eclipse.e4.ui.css.core.dom.selectors.IDynamicPseudoClassesHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract SWT class to manage dynamic pseudo classes handler like (...:focus,
 * ...:hover) with SWT Control.
 */
public abstract class AbstractDynamicPseudoClassesControlHandler implements
		IDynamicPseudoClassesHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.dom.selectors.IDynamicPseudoClassesHandler#intialize(java.lang.Object,
	 *      org.eclipse.e4.ui.core.css.engine.CSSEngine)
	 */
	public void intialize(final Object element, final CSSEngine engine) {
		final Control control = SWTElementHelpers.getControl(element);
		if (control == null)
			return;
		intialize(control, engine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.dom.selectors.IDynamicPseudoClassesHandler#dispose(java.lang.Object,
	 *      org.eclipse.e4.ui.core.css.engine.CSSEngine)
	 */
	public void dispose(Object element, CSSEngine engine) {
		Control control = SWTElementHelpers.getControl(element);
		if (control == null)
			return;
		dispose(control, engine);
	}

	/**
	 * Initialize the SWT <code>control</code>. In this method you can add
	 * SWT Listener to the control.
	 * 
	 * @param control
	 * @param engine
	 */
	protected abstract void intialize(Control control, CSSEngine engine);

	/**
	 * Dispose the SWT <code>control</code>. In this method you can remove
	 * SWT Listener to the control.
	 * 
	 * @param control
	 * @param engine
	 */
	protected abstract void dispose(Control control, CSSEngine engine);

}

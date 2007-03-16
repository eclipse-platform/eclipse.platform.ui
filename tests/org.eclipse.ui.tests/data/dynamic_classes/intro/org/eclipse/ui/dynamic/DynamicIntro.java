/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dynamic;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.IntroPart;

/**
 * @since 3.1
 */
public class DynamicIntro extends IntroPart {

	/**
	 * 
	 */
	public DynamicIntro() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#setFocus()
	 */
	public void setFocus() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#standbyStateChanged(boolean)
	 */
	public void standbyStateChanged(boolean standby) {
	}
}

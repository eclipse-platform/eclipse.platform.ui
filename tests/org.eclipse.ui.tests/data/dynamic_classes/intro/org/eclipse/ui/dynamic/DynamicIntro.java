/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public void createPartControl(Composite parent) {
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void standbyStateChanged(boolean standby) {
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/package org.eclipse.ui.views.markers.internal;


/**
 * AbstractField is the abstract superclass for fields.
 * @since 3.2
 *
 */
public abstract class AbstractField implements IField {

	boolean visible = true;

	@Override
	public boolean isShowing() {
		return visible;
	}

	/**
	 * Set whether or not the receiver is showing.
	 * @param showing
	 */
	@Override
	public void setShowing(boolean showing){
		visible = showing;

	}
}

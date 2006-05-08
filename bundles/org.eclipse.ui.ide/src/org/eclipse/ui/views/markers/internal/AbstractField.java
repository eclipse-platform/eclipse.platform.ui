/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#isShowing()
	 */
	public boolean isShowing() {
		return visible;
	}
	
	/**
	 * Set whether or not the receiver is showing.
	 * @param showing
	 */
	public void setShowing(boolean showing){
		visible = showing;
		
	}
}

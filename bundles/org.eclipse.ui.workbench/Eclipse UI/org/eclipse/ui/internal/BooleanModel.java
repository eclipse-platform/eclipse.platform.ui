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
package org.eclipse.ui.internal;

/**
 * @since 3.0
 */
public class BooleanModel extends Model {
	public BooleanModel(boolean initialState) {
		super(new Boolean(initialState));
	}
	
	public void set(boolean newValue) {
		set(newValue, null);
	}
	
	public void set(boolean newValue, IChangeListener toOmit) {
		super.setState(new Boolean(newValue), toOmit);
	}
	
	public boolean get() {
		return ((Boolean)getState()).booleanValue();
	}
}

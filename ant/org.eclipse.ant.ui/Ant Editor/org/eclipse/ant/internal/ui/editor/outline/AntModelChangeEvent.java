/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.outline;

public class AntModelChangeEvent {
	
	private AntModel fModel;
	private boolean fPreferenceChange= false;
	
	AntModelChangeEvent(AntModel model) {
		fModel= model;
	}
	
	AntModelChangeEvent(AntModel model, boolean preferenceChange) {
		fModel= model;
		fPreferenceChange= preferenceChange;
	}
	
	public AntModel getModel() {
		return fModel;
	}
	
	/**
	 * Returns whether the Ant model has changed as a result of a preference change.
	 * @return whether the model has changed from a preference change.
	 */
	public boolean isPreferenceChange() {
		return fPreferenceChange;
	}
}
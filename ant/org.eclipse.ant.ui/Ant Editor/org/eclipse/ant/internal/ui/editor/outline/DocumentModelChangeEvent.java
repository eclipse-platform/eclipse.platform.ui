/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.outline;

/**
 * DocumentModelChangeEvent.java
 */
public class DocumentModelChangeEvent {
	
	private AntModel fModel;
	
	DocumentModelChangeEvent(AntModel model) {
		fModel= model;
	}
	
	public AntModel getModel() {
		return fModel;
	}
	
}

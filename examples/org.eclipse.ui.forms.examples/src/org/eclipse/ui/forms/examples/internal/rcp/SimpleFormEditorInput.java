/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.examples.internal.rcp;
import org.eclipse.ui.forms.examples.internal.FormEditorInput;

public class SimpleFormEditorInput extends FormEditorInput {
	private SimpleModel model;
	
	public SimpleFormEditorInput(String name) {
		super(name);
		model = new SimpleModel();
	}
	
	public SimpleModel getModel() {
		return model;
	}
}

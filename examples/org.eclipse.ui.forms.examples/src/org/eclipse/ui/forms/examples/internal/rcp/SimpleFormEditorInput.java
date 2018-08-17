/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

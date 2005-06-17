/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;



import org.eclipse.swt.widgets.Composite;


/**
 * @deprecated no longer supported
 */
public final class PropagatingFontFieldEditor extends WorkbenchChainedTextFontFieldEditor {

	/**
	 * Creates a new propagating font field editor with the given parameters.
	 *
	 * @param name the name
	 * @param labelText the label
	 * @param parent the parent control
	 * @deprecated no longer supported
	 */
	public PropagatingFontFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}
}

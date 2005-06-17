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
package org.eclipse.ui.internal.editors.text;

/**
 * A file buffer operation action that changes the line delimiters to a MacOS9
 * line delimiter.
 *
 * @since 3.1
 */
public class ConvertLineDelimitersToMacOS9 extends ConvertLineDelimitersAction {

	public ConvertLineDelimitersToMacOS9(){
		super("\r", TextEditorMessages.ConvertLineDelimitersToMacOS9_label); //$NON-NLS-1$
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.ui.internal.editors.text;


/**
 * A file buffer operation action that changes the line delimiters to a Windows
 * line delimiter.
 *
 * @since 3.1
 */
public class ConvertLineDelimitersToWindows extends ConvertLineDelimitersAction {

	public ConvertLineDelimitersToWindows(){
		super("\r\n", TextEditorMessages.ConvertLineDelimitersToWindows_label); //$NON-NLS-1$
	}
}

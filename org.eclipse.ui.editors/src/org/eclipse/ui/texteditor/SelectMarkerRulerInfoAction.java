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
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.jface.text.source.IVerticalRulerInfo;


/**
 * @deprecated As of 2.1, replaced by {@link org.eclipse.ui.texteditor.SelectMarkerRulerAction}
 * @since 2.0
 */
public class SelectMarkerRulerInfoAction extends SelectMarkerRulerAction {

	/**
	 * @deprecated As of 2.1 replaced by {@link org.eclipse.ui.texteditor.SelectMarkerRulerAction#SelectMarkerRulerAction(ResourceBundle, String, ITextEditor, IVerticalRulerInfo)}
	 */
	public SelectMarkerRulerInfoAction(ResourceBundle bundle, String prefix, IVerticalRulerInfo ruler, ITextEditor editor) {
		super(bundle, prefix, editor, ruler);
	}
}

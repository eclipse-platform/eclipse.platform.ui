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
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.jface.text.source.IVerticalRulerInfo;


/**
 * @deprecated As of 2.1, replaced by {@link org.eclipse.ui.texteditor.SelectMarkerRulerAction}
 * @since 2.0
 */
@Deprecated
public class SelectMarkerRulerInfoAction extends SelectMarkerRulerAction {

	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 * @param ruler the ruler
	 * @param editor the editor
	 * @deprecated As of 2.1 replaced by {@link org.eclipse.ui.texteditor.SelectMarkerRulerAction#SelectMarkerRulerAction(ResourceBundle, String, ITextEditor, IVerticalRulerInfo)}
	 */
	@Deprecated
	public SelectMarkerRulerInfoAction(ResourceBundle bundle, String prefix, IVerticalRulerInfo ruler, ITextEditor editor) {
		super(bundle, prefix, editor, ruler);
	}
}

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
        IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

public class SelectRulerAction extends AbstractRulerActionDelegate {

	/**
	 * @see AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		return new SelectMarkerRulerInfoAction(EditorMessages.getResourceBundle(), "Editor.SelectMarker.", rulerInfo, editor); //$NON-NLS-1$
	}

}

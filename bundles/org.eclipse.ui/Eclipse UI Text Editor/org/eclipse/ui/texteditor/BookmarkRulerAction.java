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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

public class BookmarkRulerAction extends AbstractRulerActionDelegate {
	
	/*
	 * @see AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		return new MarkerRulerAction(EditorMessages.getResourceBundle(), "Editor.ManageBookmarks.", editor, rulerInfo, IMarker.BOOKMARK, true); //$NON-NLS-1$
	}
}

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
package org.eclipse.ui.forms.editor;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;

/**
 * Extends <code>MultiPageEditorSite</code> with a "virtualized" <code>IKeyBindingService</code>.
 * 
 * @since 3.0
 */
public class MultiPageKeyBindingEditorSite extends MultiPageEditorSite {
	private MultiPageKeyBindingService keyBindingService;

	public MultiPageKeyBindingEditorSite(
		MultiPageEditorPart multiPageEditor,
		IEditorPart editor) {
		super(multiPageEditor, editor);
		keyBindingService = new MultiPageKeyBindingService(this);
	}

	public IKeyBindingService getKeyBindingService() {
		return keyBindingService;
	}

	public void activate() {
		keyBindingService.activate();
	}

	public void deactivate() {
		keyBindingService.deactivate();
	}

	public boolean isActive() {
		//return getEditor() == ((MultiPageKeyBindingEditorPart)
		// getMultiPageEditor()).getActiveEditor();
		return true;
	}
}

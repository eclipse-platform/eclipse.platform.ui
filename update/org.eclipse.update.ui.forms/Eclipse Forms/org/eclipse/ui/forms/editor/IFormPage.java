/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.editor;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IManagedForm;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IFormPage extends IEditorPart {
/**
 * 
 *@param editor the form editor that this page belongs to
 */
	void initialize(FormEditor editor);
	
	FormEditor getEditor();
/**
 * Returns the managed form of this page.
 * @return the managed form
 */
	IManagedForm getManagedForm();
/**
 * Indicates whether the page has become the active in the editor.
 * Classes that implement this
 * interface may use this method to commit the page (on <code>false</code>)
 * or lazily create and/or populate the content on <code>true</code>.
 * @param active <code>true</code> if page should be visible,
 * <code>false</code> otherwise. 
 */
	void setActive(boolean active);
	boolean isActive();
}
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

import org.eclipse.swt.widgets.Control;
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
	
/**
 * Returns the editor this page belongs to.
 * @return the form editor
 */
	FormEditor getEditor();
/**
 * Returns the managed form of this page, unless this is a source page.
 * @return the managed form or <samp>null</samp> if this is a source
 * page.
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
/**
 * Returns <samp>true</samp> if page is currently active,
 * false if not. 
 * @return <samp>true</samp> for active page.
 */
	boolean isActive();
/**
 * Returns the control associated with this page.
 * @return the control of this page if created or <samp>null</samp>
 * if the page has not been shown yet.
 */
	Control getPartControl();
/**
 * Page must have a unique id that can be used to show it 
 * without knowing its relative position in the editor.
 * @return the unique page identifier
 */
	String getId();
/**
 * Returns the position of the page in the editor.
 * @return the zero-based index of the page in the editor.
 */
	int getIndex();
/**
 * Sets the position of the page in the editor.
 * @param index the zero-based index of the page in the editor.
 */
	void setIndex(int index);
/**
 * Tests whether this page shows the editor input in the raw (source)
 * form.
 * @return <samp>true</samp> if the page shows editor input source,
 * <samp>false</samp> if this is a form page.
 */
	boolean isSource();
/**
 * A hint to bring the provided object into focus. If the object
 * is in a tree or table control, select it. If it is shown 
 * on a scrollable page, ensure that it is visible.
 * @param object object to bring into focus
 */
	void focusOn(Object object);
}
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
package org.eclipse.ui.forms.editor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.IManagedForm;
/**
 * Interface that all GUI pages need to implement in order
 * to be added to FormEditor part. The interface makes 
 * several assumptions:
 * <ul>
 * <li>The form page has a managed form</li>
 * <li>The form page has a unique id</li>
 * <li>The form page can be GUI but can also wrap a complete
 * editor class (in that case, it should return <code>true</code>
 * from <code>isEditor()</code> method).</li>
 * <li>The form page is lazy i.e. understands that 
 * its part control will be created at the last possible
 * moment.</li>.
 * </ul>
 * <p>Existing editors can be wrapped by implementing
 * this interface. In this case, 'isEditor' should return <code>true</code>.
 * A common editor to wrap in <code>TextEditor</code> that is
 * often added to show the raw source code of the file open into
 * the multi-page editor.
 * 
 * @since 3.0
 */
public interface IFormPage extends IEditorPart {
	/**
	 * @param editor
	 *            the form editor that this page belongs to
	 */
	void initialize(FormEditor editor);
	/**
	 * Returns the editor this page belongs to.
	 * 
	 * @return the form editor
	 */
	FormEditor getEditor();
	/**
	 * Returns the managed form of this page, unless this is a source page.
	 * 
	 * @return the managed form or <samp>null </samp> if this is a source page.
	 */
	IManagedForm getManagedForm();
	/**
	 * Indicates whether the page has become the active in the editor. Classes
	 * that implement this interface may use this method to commit the page (on
	 * <code>false</code>) or lazily create and/or populate the content on
	 * <code>true</code>.
	 * 
	 * @param active
	 *            <code>true</code> if page should be visible, <code>false</code>
	 *            otherwise.
	 */
	void setActive(boolean active);
	/**
	 * Returns <samp>true </samp> if page is currently active, false if not.
	 * 
	 * @return <samp>true </samp> for active page.
	 */
	boolean isActive();
	/**
	 * Tests if the content of the page is in a state that allows the
	 * editor to flip to another page. Typically, pages that contain
	 * raw source with syntax errors should not allow editors to 
	 * leave them until errors are corrected.
	 * @return <code>true</code> if the editor can flip to another page,
	 * <code>false</code> otherwise.
	 */
	boolean canLeaveThePage();
	/**
	 * Returns the control associated with this page.
	 * 
	 * @return the control of this page if created or <samp>null </samp> if the
	 *         page has not been shown yet.
	 */
	Control getPartControl();
	/**
	 * Page must have a unique id that can be used to show it without knowing
	 * its relative position in the editor.
	 * 
	 * @return the unique page identifier
	 */
	String getId();
	/**
	 * Returns the position of the page in the editor.
	 * 
	 * @return the zero-based index of the page in the editor.
	 */
	int getIndex();
	/**
	 * Sets the position of the page in the editor.
	 * 
	 * @param index
	 *            the zero-based index of the page in the editor.
	 */
	void setIndex(int index);
	/**
	 * Tests whether this page wraps a complete editor that
	 * can be registered on its own, or represents a page
	 * that cannot exist outside the multi-page editor context.
	 * 
	 * @return <samp>true </samp> if the page wraps an editor,
	 *         <samp>false </samp> if this is a form page.
	 */
	boolean isEditor();
	/**
	 * A hint to bring the provided object into focus. If the object is in a
	 * tree or table control, select it. If it is shown on a scrollable page,
	 * ensure that it is visible. If the object is not presented in 
	 * the page, <code>false</code> should be returned to allow another
	 * page to try.
	 * 
	 * @param object
	 *            object to select and reveal
	 * @return <code>true</code> if the request was successful, <code>false</code>
	 *         otherwise.
	 */
	boolean selectReveal(Object object);
}

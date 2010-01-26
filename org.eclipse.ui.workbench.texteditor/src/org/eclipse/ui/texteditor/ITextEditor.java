/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.IRegion;

import org.eclipse.ui.IEditorPart;


/**
 * Interface to a text editor. This interface defines functional extensions to
 * <code>IEditorPart</code> as well as the configuration capabilities of a text editor.
 * <p>
 * Text editors are configured with an <code>IDocumentProvider</code> which delivers a textual
 * presentation (<code>IDocument</code>) of the editor's input. The editor works on the document and
 * forwards all input element related calls, such as <code>save</code>, to the document provider.
 * The provider also delivers the input's annotation model which is used by the editor's vertical
 * ruler.
 * </p>
 * <p>
 * Clients may implement this interface from scratch, but the recommended way is to subclass the
 * abstract base class <code>AbstractTextEditor</code>.
 * </p>
 * <p>
 * In order to provided backward compatibility for clients of <code>ITextEditor</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.ui.texteditor.ITextEditorExtension} since version 2.0 introducing status
 * fields, read-only state and ruler context menu listeners.</li>
 * <li>{@link org.eclipse.ui.texteditor.ITextEditorExtension2} since version 2.1 introducing
 * modifiable state for the editor input and validate state handling.</li>
 * <li>{@link org.eclipse.ui.texteditor.ITextEditorExtension3} since version 3.0 adding input state
 * and change information control.</li>
 * <li>{@link org.eclipse.ui.texteditor.ITextEditorExtension4} since version 3.2 adding annotation
 * navigation and revision information display.</li>
 * <li>{@link org.eclipse.ui.texteditor.ITextEditorExtension5} since version 3.5 adding block
 * selection mode.</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.ui.texteditor.IDocumentProvider
 * @see org.eclipse.jface.text.source.IAnnotationModel
 * @see org.eclipse.ui.texteditor.ITextEditorExtension
 * @see org.eclipse.ui.texteditor.ITextEditorExtension2
 * @see org.eclipse.ui.texteditor.ITextEditorExtension3
 * @see org.eclipse.ui.texteditor.ITextEditorExtension4
 * @see org.eclipse.ui.texteditor.ITextEditorExtension5
 */
public interface ITextEditor extends IEditorPart {

	/**
	 * Returns this text editor's document provider.
	 *
	 * @return the document provider or <code>null</code> if none, e.g. after closing the editor
	 */
	IDocumentProvider getDocumentProvider();

	/**
	 * Closes this text editor after optionally saving changes.
	 *
	 * @param save <code>true</code> if unsaved changed should be saved, and
	 *   <code>false</code> if unsaved changed should be discarded
	 */
	void close(boolean save);

	/**
	 * Returns whether the text in this text editor can be changed by the user.
	 *
	 * @return <code>true</code> if it can be edited, and <code>false</code> if it is read-only
	 */
	boolean isEditable();

	/**
	 * Abandons all modifications applied to this text editor's input element's
	 * textual presentation since the last save operation.
	 */
	void doRevertToSaved();

	/**
	 * Installs the given action under the given action id.
	 *
	 * @param actionID the action id
	 * @param action the action, or <code>null</code> to clear it
	 * @see #getAction(String)
	 */
	void setAction(String actionID, IAction action);

	/**
	 * Returns the action installed under the given action id.
	 *
	 * @param actionId the action id
	 * @return the action, or <code>null</code> if none
	 * @see #setAction(String, IAction)
	 */
	IAction getAction(String actionId);

	/**
	 * Sets the given activation code for the specified action. If
	 * there is an activation code already registered, it is replaced.
	 * The activation code consists of the same information as
	 * a <code>KeyEvent</code>. If the activation code is triggered
	 * and the associated action is enabled, the action is performed
	 * and the triggering <code>KeyEvent</code> is considered consumed.
	 * If the action is disabled, the <code>KeyEvent</code> is passed
	 * on unmodified. Thus, action activation codes and action accelerators
	 * differ in their model of event consumption. The key code parameter
	 * can be <code>-1</code> to indicate a wild card. The state mask
	 * parameter can be SWT.DEFAULT to indicate a wild card.
	 *
	 * @param actionId the action id
	 * @param activationCharacter the activation code character
	 * @param activationKeyCode the activation code key code or <code>-1</code> for wild card
	 * @param activationStateMask the activation code state mask or <code>SWT.DEFAULT</code> for wild card
	 */
	void setActionActivationCode(String actionId, char activationCharacter, int activationKeyCode, int activationStateMask);

	/**
	 * Removes any installed activation code for the specified action.
	 * If no activation code is installed, this method does not have
	 * any effect.
	 *
	 * @param actionId the action id
	 */
	void removeActionActivationCode(String actionId);

	/**
	 * Returns whether this text editor is configured to show only the
	 * highlighted range of the text.
	 *
	 * @return <code>true</code> if only the highlighted range is shown, and
	 *   <code>false</code> if this editor shows the entire text of the document
	 * @see #showHighlightRangeOnly(boolean)
	 */
	boolean showsHighlightRangeOnly();

	/**
	 * Configures this text editor to show only the highlighted range of the
	 * text.
	 *
	 * @param showHighlightRangeOnly <code>true</code> if only the highlighted
	 *   range is shown, and <code>false</code> if this editor shows the entire
	 *   text of the document
	 * @see #showsHighlightRangeOnly()
	 */
	void showHighlightRangeOnly(boolean showHighlightRangeOnly);

	/**
	 * Sets the highlighted range of this text editor to the specified region.
	 * 
	 * @param offset the offset of the highlighted range
	 * @param length the length of the highlighted range
	 * @param moveCursor <code>true</code> if the cursor should be moved to the start of the
	 *            highlighted range, and <code>false</code> to leave the cursor unaffected - has no
	 *            effect if the range to highlight is already the highlighted one
	 * @see #getHighlightRange()
	 */
	void setHighlightRange(int offset, int length, boolean moveCursor);

	/**
	 * Returns the highlighted range of this text editor.
	 *
	 * @return the highlighted range
	 * @see #setHighlightRange(int, int, boolean)
	 */
	IRegion getHighlightRange();

	/**
	 * Resets the highlighted range of this text editor.
	 */
	void resetHighlightRange();

	/**
	 * Returns this text editor's selection provider. Repeated calls to this
	 * method return the same selection provider.
	 *
	 * @return the selection provider
	 */
	ISelectionProvider getSelectionProvider();

	/**
	 * Selects and reveals the specified range in this text editor.
	 *
	 * @param offset the offset of the selection
	 * @param length the length of the selection
	 */
	void selectAndReveal(int offset, int length);
}

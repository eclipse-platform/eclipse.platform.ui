package org.eclipse.ui.texteditor;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.ui.IEditorPart;


/**
 * Interface to a text editor. This interface defines functional extensions to
 * <code>IEditorPart</code> as well as the configuration capabilities of a text editor. 
 * <p>
 * Text editors are configured with an <code>IDocumentProvider</code> which 
 * delivers a textual presentation (<code>IDocument</code>) of the editor's input. 
 * The editor works on the document and forwards all input element related calls,
 * such as  <code>save</code>, to the document provider. The provider also delivers
 * the input's annotation model which is used to control the editor's vertical ruler.
 * </p>
 * <p>
 * Clients may implement this interface from scratch, but the recommented way is to 
 * subclass the abstract base class <code>AbstractTextEditor</code>.
 * </p>
 * 
 * @see IDocumentProvider
 * @see org.eclipse.jface.text.source.IAnnotationModel
 */
public interface ITextEditor extends IEditorPart {
		
	/**
	 * Closes this text editor after optionally saving changes.
	 *
	 * @param save <code>true</code> if unsaved changed should be saved, and
	 *   <code>false</code> if unsaved changed should be discarded
	 */
	void close(boolean save);
	/**
	 * Abandons all modifications applied to this text editor's input element's 
	 * textual presentation since the last save operation.
	 */
	void doRevertToSaved();
	/**
	 * Returns the action installed under the given action id.
	 *
	 * @param actionId the action id
	 * @return the action, or <code>null</code> if none
	 * @see #setAction
	 */
	IAction getAction(String actionId);
	/**
	 * Returns this text editor's document provider.
	 *
	 * @return the document provider
	 */
	IDocumentProvider getDocumentProvider();
	/**
	 * Returns the highlighted range of this text editor.
	 *
	 * @return the highlighted range
	 * @see #setHighlightRange
	 */
	IRegion getHighlightRange();
	/**
	 * Returns this text editor's selection provider. Repeated calls to this
	 * method return the same selection provider.
	 *
	 * @return the selection provider
	 */
	ISelectionProvider getSelectionProvider();
	/**
	 * Returns whether the text in this text editor can be changed by the user.
	 *
	 * @return <code>true</code> if it can be edited, and <code>false</code>
	 *   if it is read-only
	 */
	boolean isEditable();
	/**
	 * Resets the highlighted range of this text editor.
	 */
	void resetHighlightRange();
	/**
	 * Selects and reveals the specified range in this text editor.
	 *
	 * @param offset the offset of the selection
	 * @param length the length of the selection
	 */
	void selectAndReveal(int offset, int length);
	/**
	 * Installs the given action under the given action id.
	 *
	 * @param actionId the action id
	 * @param action the action, or <code>null</code> to clear it
	 * @see #getAction
	 */
	void setAction(String actionID, IAction action);
	/**
	 * Sets the highlighted range of this text editor to the specified region.
	 *
	 * @param offset the offset of the highlighted range
	 * @param length the length of the highlighted range
	 * @param moveCursor <code>true</code> if the cursor should be moved to
	 *   the start of the highlighted range, and <code>false</code> to leave
	 *   the cursor unaffected
	 * @see #getHighlightRange
	 */
	void setHighlightRange(int offset, int length, boolean moveCursor);
	/**
	 * Configures this text editor to show only the highlighted range of the
	 * text.
	 *
	 * @param showHighlightRangeOnly <code>true</code> if only the highlighted
	 *   range is shown, and <code>false</code> if this editor shows the entire
	 *   text of the document
	 * @see #showsHighlightRangeOnly
	 */
	void showHighlightRangeOnly(boolean showHighlightRangeOnly);
	/**
	 * Returns whether this text editor is configured to show only the 
	 * highlighted range of the text.
	 *
	 * @return <code>true</code> if only the highlighted range is shown, and
	 *   <code>false</code> if this editor shows the entire text of the document
	 * @see #showHighlightRangeOnly
	 */
	boolean showsHighlightRangeOnly();
}

package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
 * Clients may implement this interface from scratch, but the recommended way is to 
 * subclass the abstract base class <code>AbstractTextEditor</code>.
 * </p>
 * 
 * @see IDocumentProvider
 * @see org.eclipse.jface.text.source.IAnnotationModel
 */
public interface ITextEditor extends IEditorPart {
		
	/**
	 * Returns this text editor's document provider.
	 *
	 * @return the document provider
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
	 * @return <code>true</code> if it can be edited, and <code>false</code>
	 *   if it is read-only
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
	 * @param actionId the action id
	 * @param action the action, or <code>null</code> to clear it
	 * @see #getAction
	 */
	void setAction(String actionID, IAction action);
	
	/**
	 * Returns the action installed under the given action id.
	 *
	 * @param actionId the action id
	 * @return the action, or <code>null</code> if none
	 * @see #setAction
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
	 * differ in their model of event consumption.
	 * 
	 * @param actionId the action id
	 * @param character the activation code character
	 * @param keyCode the activation code key code
	 * @param stateMask the activation code state mask
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
	 * @see #showHighlightRangeOnly
	 */
	boolean showsHighlightRangeOnly();
	
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
	 * Returns the highlighted range of this text editor.
	 *
	 * @return the highlighted range
	 * @see #setHighlightRange
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
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

package org.eclipse.jface.text;
 

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.viewers.ISelectionProvider;


/**
 * A text viewer turns a text widget into a document-based text widget.
 * It supports the following kinds of listeners:
 * <ul>
 * <li> view port listeners to inform about changes of the viewer's view port
 * <li> text listeners to inform about changes of the document and the subsequent viewer change
 * <li> text input listeners to inform about changes of the viewer's input document.
 * </ul>
 * A text viewer supports a set of plug-ins which define its behavior:
 * <ul>
 * <li> undo manager
 * <li> double click behavior
 * <li> auto indentation
 * <li> text hover
 * </ul>
 * Installed plug-ins are not automatically activated. Plug-ins must be activated with the
 * <code>activatePlugins</code> call. Most plug-ins can be defined per content type. 
 * Content types are derived from the partitioning of the text viewer's input document.<p>
 * A text viewer also provides the concept of event consumption. Events handled by the 
 * viewer can be filtered and processed by a dynamic event consumer.<p>
 * A text viewer provides several text editing functions, some of them are configurable,
 * through a text operation target interface. It also supports a presentation mode
 * in which it only shows specified sections of its document. The viewer's presentation mode
 * does not affect any client of the viewer other than text listeners.<p>
 * Clients may implement this interface or use the standard implementation
 * <code>TextViewer</code>.
 *
 * @see IDocument
 * @see ITextInputListener
 * @see IViewportListener
 * @see ITextListener
 * @see IEventConsumer
 */
public interface ITextViewer {
	
	
	/* ---------- widget --------- */
	
	/**
	 * Returns this viewer's SWT control, <code>null</code> if the control is disposed.
	 * 
	 * @return the SWT control
	 */
	StyledText getTextWidget();
	
		
	/* --------- plug-ins --------- */
		
	/**
	 * Sets this viewer's undo manager.
	 * 
	 * @param undoManager the new undo manager. <code>null</code> is a valid argument.
	 */
	void setUndoManager(IUndoManager undoManager);	
		
	/**
	 * Sets this viewer's text double click strategy for the given content type.
	 *
	 * @param strategy the new double click strategy. <code>null</code> is a valid argument.
	 * @param contentType the type for which the strategy is registered
	 */
	void setTextDoubleClickStrategy(ITextDoubleClickStrategy strategy, String contentType);
	
	/**
	 * Sets this viewer's auto indent strategy for the given content type.
	 *
	 * @param strategy the new auto indent strategy. <code>null</code> is a valid argument.
	 * @param contentType the type for which the strategy is registered
	 */
	void setAutoIndentStrategy(IAutoIndentStrategy strategy, String contentType);
		
	/**
	 * Sets this viewer's text hover for the given content type. 
	 *
	 * @param textViewerHover the new hover. <code>null</code> is a valid argument.
	 * @param contentType the type for which the hover is registered
	 */
	void setTextHover(ITextHover textViewerHover, String contentType);
	
	/**
	 * Activates the installed plug-ins. If the plug-ins are already activated 
	 * this call has no effect.
	 */
	void activatePlugins();
	
	/**
	 * Resets the installed plug-ins. If plug-ins change their state or 
	 * behavior over the course of time, this method causes them to be set
	 * back to their initial state and behavior. E.g., if an <code>IUndoManager</code>
	 * has been installed on this text viewer, the manager's list of remembered 
     * text editing operations is removed.
	 */
	void resetPlugins();
	
	
	
	/* ---------- listeners ------------- */
		
	/**
	 * Adds the given view port listener to this viewer. The listener
	 * is informed about all changes to the visible area of this viewer.
	 * If the listener is already registered with this viewer, this call
	 * has no effect.
	 *
	 * @param listener the listener to be added
	 */
	void addViewportListener(IViewportListener listener);
	
	/**
	 * Removes the given listener from this viewer's set of view port listeners.
	 * If the listener is not registered with this viewer, this call has
	 * no effect.
	 *
	 * @param listener the listener to be removed
	 */
	void removeViewportListener(IViewportListener listener);
		
	/**
	 * Adds a text listener to this viewer. If the listener is already registered
	 * with this viewer, this call has no effect.
	 *
	 * @param listener the listener to be added
	 */
	void addTextListener(ITextListener listener);
	
	/**
	 * Removes the given listener from this viewer's set of text listeners.
	 * If the listener is not registered with this viewer, this call has
	 * no effect.
	 *
	 * @param listener the listener to be removed
	 */
	void removeTextListener(ITextListener listener);
	
	/**
	 * Adds a text input listener to this viewer. If the listener is already registered
	 * with this viewer, this call has no effect.
	 *
	 * @param listener the listener to be added
	 */
	void addTextInputListener(ITextInputListener listener);
	
	/**
	 * Removes the given listener from this viewer's set of text input listeners.
	 * If the listener is not registered with this viewer, this call has
	 * no effect.
	 *
	 * @param listener the listener to be removed
	 */
	void removeTextInputListener(ITextInputListener listener);
	
	
	
	/* -------------- model manipulation ------------- */
		
	/**
	 * Sets the given document as the text viewer's model and updates the 
	 * presentation accordingly. An appropriate <code>TextEvent</code> is
	 * issued. This text event does not carry a related document event.
	 *
	 * @param document the viewer's new input document
	 */
	void setDocument(IDocument document);
	
	/**
	 * Returns the text viewer's input document.
	 *
	 * @return the viewer's input document
	 */
	IDocument getDocument();
		
	
	/* -------------- event handling ----------------- */
	
	/**
	 * Registers an event consumer with this viewer.
	 *
	 * @param consumer the viewer's event consumer. <code>null</code> is a valid argument.
	 */
	void setEventConsumer(IEventConsumer consumer);
		
	/**
	 * Sets the editable flag.
	 *
	 * @param editable the editable flag
	 */
	void setEditable(boolean editable);

	/**
	 * Returns whether the shown text can be manipulated.
	 *
	 * @return the viewer's editable flag
	 */
	boolean isEditable();
	
	
	/* ----------- visible region support ------------- */
	
	/**
	 * Sets the given document as this viewer's model and 
	 * exposes the specified region. An appropriate
	 * <code>TextEvent</code> is issued. The text event does not carry a 
	 * related document event. This method is a convenience method for
	 * <code>setDocument(document);setVisibleRegion(offset, length)</code>.
	 *
	 * @param document the new input document
	 * @param modelRangeOffset the offset of the model range
	 * @param modelRangeLength the length of the model range
	 */
	void setDocument(IDocument document, int modelRangeOffset, int modelRangeLength);
	
	/**
	 * Sets the region of this viewer's document which will be visible in the presentation.
	 *
	 * @param offset the offset of the visible region
	 * @param length the length of the visible region
	 */
	void setVisibleRegion(int offset, int length);
	
	/**
	 * Resets the region of this viewer's document which is visible in the presentation.
	 * Afterwards, the whole document is presented again.
	 */
	void resetVisibleRegion();
	
	/**
	 * Returns the current visible region of this viewer's document.
	 * The result may differ from the argument passed to <code>setVisibleRegion</code>
	 * if the document has been modified since then.
	 *
	 * @return this viewer's current visible region
	 */
	IRegion getVisibleRegion();
	
	/**
	 * Returns whether a given range overlaps with the visible region of this viewer's document.
	 *
	 * @param offset the offset
	 * @param length the length
	 * @return <code>true</code> if the specified range overlaps with the visible region
	 */
	boolean overlapsWithVisibleRegion(int offset, int length);	
	
	
	
	/* ------------- presentation manipulation ----------- */
	
	/**
	 * Applies the color information encoded in the given text presentation.
	 * <code>controlRedraw</code> tells this viewer whether it should take care of 
	 * redraw management or not. If, e.g., this call is one in a sequence of multiple
	 * coloring calls, it is more appropriate to explicitly control redrawing at the
	 * beginning and the end of the sequence.
	 *
	 * @param presentation the presentation to be applied to this viewer
	 * @param controlRedraw indicates whether this viewer should manage redraws
	 */
	void changeTextPresentation(TextPresentation presentation, boolean controlRedraw);
	
	/**
	 * Marks the currently applied text presentation as invalid. It is the viewer's
	 * responsibility to take any action it can to repair the text presentation.
	 * 
	 * @since 2.0
	 */
	void invalidateTextPresentation();
		
	/**
	 * Applies the given color to this viewer's selection.
	 *
	 * @param color the color to be applied
	 */
	void setTextColor(Color color);
	
	/**
	 * Applies the given color to the specified section of this viewer. 
	 * <code>controlRedraw</code> tells this viewer whether it should take care of
	 * redraw management or not.
	 *
	 * @param color the color to be applied
	 * @param offset the offset of the range to be colored
	 * @param length the length of the range to be colored
	 * @param controlRedraw indicates whether this viewer should manage redraws
	 */
	void setTextColor(Color color, int offset, int length, boolean controlRedraw);
	
	
	/* --------- target handling and configuration ------------ */
	
	/**
	 * Returns the text operation target of this viewer.
	 *
	 * @return the text operation target of this viewer
	 */
	ITextOperationTarget getTextOperationTarget();
	
	/**
	 * Returns the find/replace operation target of this viewer.
	 * 
	 * @return the find/replace operation target of this viewer
	 */
	IFindReplaceTarget getFindReplaceTarget();
	
	/**
	 * Sets the string that is used as prefix when lines of the given 
	 * content type are prefixed by the prefix text operation.
	 * Sets the strings that are used as prefixes when lines of the given content type 
	 * are prefixed using the prefix text operation. The prefixes are considered equivalent.
	 * Inserting a prefix always inserts the defaultPrefixes[0].
	 * Removing a prefix removes all of the specified prefixes.
	 *
	 * @param defaultPrefixes the prefixes to be used
	 * @param contentType the content type for which the prefixes are specified
	 * @since 2.0
	 */
	void setDefaultPrefixes(String[] defaultPrefixes, String contentType);
	
	/**
	 * Sets the strings that are used as prefixes when lines of the given content type 
	 * are shifted using the shift text operation. The prefixes are considered equivalent.
	 * Thus "\t" and "    " can both be used as prefix characters.
	 * Shift right always inserts the indentPrefixes[0].
	 * Shift left removes all of the specified prefixes.
	 *
	 * @param indentPrefixes the prefixes to be used
	 * @param contentType the content type for which the prefixes are specified
	 */
	void setIndentPrefixes(String[] indentPrefixes, String contentType);
	
	
	
	/* --------- selection handling -------------- */
	
	/**
	 * Sets the selection to the specified range.
	 *
	 * @param offset the offset of the selection range
	 * @param length the length of the selection range
	 */
	void setSelectedRange(int offset, int length);
	
	/**
	 * Returns the range of the current selection in coordinates of this viewer's document.
	 *
	 * @return the current selection
	 */
	Point getSelectedRange();
	
	/**
	 * Returns a selection provider dedicated to this viewer. Subsequent
	 * calls to this method return always the same selection provider.
	 *
	 * @return this viewer's selection provider
	 */
	ISelectionProvider getSelectionProvider();
	
	
	/* ------------- appearance manipulation --------------- */
		
	/**
	 * Ensures that the given range is visible.
	 *
	 * @param offset the offset of the range to be revealed
	 * @param length the length of the range to be revealed 
	 */
	void revealRange(int offset, int length);
	
	/**
	 * Scrolls the widget so the the given index is the line
	 * with the smallest line number of all visible lines.
	 *
	 * @param index the line which should become the top most line
	 */
	void setTopIndex(int index);
	
	/**
	 * Returns the visible line with the smallest line number.
	 *
	 * @return the number of the top most visible line
	 */
	int getTopIndex();
	
	/**
	 * Returns the document offset of the upper left corner of this viewer's view port.
	 *
	 * @return the upper left corner offset
	 */
	int getTopIndexStartOffset();
	
	/**
	 * Returns the visible line with the highest line number.
	 *
	 * @return the number of the bottom most line
	 */
	int getBottomIndex();
	
	/**
	 * Returns the document offset of the lower right 
	 * corner of this viewer's view port. This is the visible character
	 * with the highest character position. If the content of this viewer
	 * is shorter, the position of the last character of the content is returned.
	 *
	 * @return the lower right corner offset
	 */
	int getBottomIndexEndOffset();
	
	/**
	 * Returns the vertical offset of the first visible line.
	 *
	 * @return the vertical offset of the first visible line
	 */
	int getTopInset();
}

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.contentassist;

import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;


/**
 * A content assist subject control can request assistance provided by a
 * {@linkplain org.eclipse.jface.contentassist.ISubjectControlContentAssistant subject control content assistant}.
 *
 * @since 3.0
 */
public interface IContentAssistSubjectControl {

	/**
	 * Returns the control of this content assist subject control.
	 *
	 * @return the control of this content assist subject control
	 */
	Control getControl();

	/**
	 * Returns the line height.
	 *
	 * @return line height in pixel
	 * @throws org.eclipse.swt.SWTException in these cases:
	 *               <ul>
	 *               	<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been
	 *               		disposed</li>
	 *              	<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the
	 *              		 thread that created the receiver</li>
	 *               </ul>
	 */
	int getLineHeight();

	/**
	 * Returns the caret position relative to the start of the text in widget
	 * coordinates.
	 *
	 * @return the caret position relative to the start of the text in widget
	 *         coordinates
	 * @throws org.eclipse.swt.SWTException in these cases:
	 *               <ul>
	 *               	<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been
	 *               		disposed</li>
	 *               	<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the
	 *               		thread that created the receiver</li>
	 *               </ul>
	 */
	int getCaretOffset();

	/**
	 * Returns the x, y location of the upper left corner of the character
	 * bounding box at the specified offset in the text. The point is relative
	 * to the upper left corner of the widget client area.
	 *
	 * @param offset widget offset relative to the start of the content 0
	 *           <= offset <= getCharCount()
	 * @return x, y location of the upper left corner of the character bounding
	 *         box at the specified offset in the text
	 * @throws org.eclipse.swt.SWTException in these cases:
	 *			<ul>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been disposed</li>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the thread that created the receiver</li>
	 *			</ul>
	 * @exception IllegalArgumentException when the offset is outside the valid range
	 */
	Point getLocationAtOffset(int offset);

	/**
	 * Returns the line delimiter used for entering new lines by key down or
	 * paste operation.
	 *
	 * @return line delimiter used for entering new lines by key down or paste
	 *         operation
	 * @throws org.eclipse.swt.SWTException in these cases:
	 *			<ul>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been disposed</li>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the thread that created the receiver</li>
	 *			</ul>
	 */
	String getLineDelimiter();

	/**
	 * Returns the selected range in the subject's widget.
	 *
	 * @return start and length of the selection, x is the offset of the
	 * @throws org.eclipse.swt.SWTException in these cases:
	 *			<ul>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been disposed</li>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the thread that created the receiver</li>
	 *			</ul>
	 */
	Point getWidgetSelectionRange();

	/**
	 * Returns the selected range.
	 *
	 * @return start and length of the selection, x is the offset and y the
	 *         length based on the subject's model (e.g. document)
	 */
	Point getSelectedRange();

	/**
	 * Sets the selected range. Offset and length based on the subject's
	 * model (e.g. document).
	 *
	 * @param offset the offset of the selection based on the subject's model e.g. document
	 * @param length the length of the selection based on the subject's model e.g. document
	 */
	void setSelectedRange(int offset, int length);

	/**
	 * Reveals the given region. Offset and length based on the subject's
	 * model (e.g. document).
	 *
	 * @param offset the offset of the selection based on the subject's model e.g. document
	 * @param length the length of the selection based on the subject's model e.g. document
	 */
	void revealRange(int offset, int length);

	/**
	 * Returns this content assist subject control's document.
	 *
	 * @return the viewer's input document
	 */
	IDocument getDocument();

	/**
	 * If supported, appends a verify key listener to the viewer's list of verify key
	 * listeners. If the listener is already registered with the viewer this
	 * call moves the listener to the end of the list.
	 * <p>
	 * Note: This content assist subject control may not support appending a verify
	 * listener, in which case <code>false</code> will be returned. If this
	 * content assist subject control only supports <code>addVerifyKeyListener</code>
	 * then this method can be used but <code>prependVerifyKeyListener</code>
	 * must return <code>false</code>.
	 * </p>
	 *
	 * @param verifyKeyListener the listener to be added
	 * @return <code>true</code> if the listener was added
	 */
	boolean appendVerifyKeyListener(VerifyKeyListener verifyKeyListener);

	/**
	 * If supported, inserts the verify key listener at the beginning of this content assist
	 * subject's list of verify key listeners. If the listener is already
	 * registered with the viewer this call moves the listener to the beginning
	 * of the list.
	 * <p>
	 * Note: This content assist subject control may not support prepending a verify
	 * listener, in which case <code>false</code> will be returned. However,
	 * {@link #appendVerifyKeyListener(VerifyKeyListener)} might work.
	 * </p>
	 *
	 * @param verifyKeyListener the listener to be inserted
	 * @return <code>true</code> if the listener was added
	 */
	boolean prependVerifyKeyListener(VerifyKeyListener verifyKeyListener);

	/**
	 * Removes the verify key listener from this content assist subject control's
	 * list of verify key listeners. If the listener is not registered, this
	 * call has no effect.
	 *
	 * @param verifyKeyListener the listener to be removed
	 */
	void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener);

	/**
	 * Tests whether a verify key listener can be added either using <code>prependVerifyKeyListener</code>
	 * or {@link #appendVerifyKeyListener(VerifyKeyListener)}.
	 *
	 * @return <code>true</code> if adding verify key listeners is supported
	 */
	boolean supportsVerifyKeyListener();

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when keys are pressed and released on the system keyboard, by sending it
	 * one of the messages defined in the {@link KeyListener} interface.
	 *
	 * @param keyListener the listener which should be notified
	 * @exception IllegalArgumentException if the listener is <code>null</code>
	 * @throws org.eclipse.swt.SWTException in these cases:
	 *			<ul>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been disposed</li>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the thread that created the receiver</li>
	 *			</ul>
	 *
	 * @see KeyListener
	 * @see #removeKeyListener(KeyListener)
	 */
	void addKeyListener(KeyListener keyListener);

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when keys are pressed and released on the system keyboard.
	 *
	 * @param keyListener the listener which should be notified
	 * @exception IllegalArgumentException if the listener is null</li>
	 * @throws org.eclipse.swt.SWTException in these cases:
	 *			<ul>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been disposed</li>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the thread that created the receiver</li>
	 *			</ul>
	 * @see KeyListener
	 * @see #addKeyListener(KeyListener)
	 */
	void removeKeyListener(KeyListener keyListener);

	/**
	 * If supported, registers an event consumer with this content assist
	 * subject.
	 *
	 * @param eventConsumer the content assist subject control's event consumer. <code>null</code>
	 *           is a valid argument.
	 */
	void setEventConsumer(IEventConsumer eventConsumer);

	/**
	 * Removes the specified selection listener.
	 * <p>
	 *
	 * @param selectionListener the listener
	 * @exception org.eclipse.swt.SWTException <ul>
	 *			<ul>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been disposed</li>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the thread that created the receiver</li>
	 *			</ul>
	 * @exception IllegalArgumentException if listener is <code>null</code>
	 */
	void removeSelectionListener(SelectionListener selectionListener);

	/**
	 * If supported, adds a selection listener. A Selection event is sent by the widget when the
	 * selection has changed.
	 * <p>
	 *
	 * @param selectionListener the listener
	 * @return <code>true</code> if adding a selection listener is supported
	 *			<ul>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_WIDGET_DISPOSED} - if the receiver has been disposed</li>
	 *				<li>{@link org.eclipse.swt.SWT#ERROR_THREAD_INVALID_ACCESS} - if not called from the thread that created the receiver</li>
	 *			</ul>
	 * @exception IllegalArgumentException if listener is <code>null</code>
	 */
	boolean addSelectionListener(SelectionListener selectionListener);
}

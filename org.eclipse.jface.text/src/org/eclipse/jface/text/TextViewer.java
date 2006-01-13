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

package org.eclipse.jface.text;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

import org.eclipse.jface.internal.text.MigrationHelper;
import org.eclipse.jface.internal.text.NonDeletingPositionUpdater;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jface.text.hyperlink.HyperlinkManager;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.projection.ChildDocument;
import org.eclipse.jface.text.projection.ChildDocumentManager;

/**
 * SWT based implementation of {@link ITextViewer} and its extension interfaces.
 * Once the viewer and its SWT control have been created the viewer can only
 * indirectly be disposed by disposing its SWT control.
 * <p>
 * Clients are supposed to instantiate a text viewer and subsequently to
 * communicate with it exclusively using the
 * {@link org.eclipse.jface.text.ITextViewer} interface or any of the
 * implemented extension interfaces.
 * <p>
 * A text viewer serves as text operation target. It only partially supports the
 * external control of the enable state of its text operations. A text viewer is
 * also a widget token owner. Anything that wants to display an overlay window
 * on top of a text viewer should implement the
 * {@link org.eclipse.jface.text.IWidgetTokenKeeper} interface and participate
 * in the widget token negotiation between the text viewer and all its potential
 * widget token keepers.
 * <p>
 * This class is not intended to be subclassed outside the JFace Text component.</p>
 */
public class TextViewer extends Viewer implements
					ITextViewer, ITextViewerExtension, ITextViewerExtension2, ITextViewerExtension4, ITextViewerExtension6, IEditingSupportRegistry,
					ITextOperationTarget, ITextOperationTargetExtension,
					IWidgetTokenOwner, IWidgetTokenOwnerExtension, IPostSelectionProvider {

	/** Internal flag to indicate the debug state. */
	public static final boolean TRACE_ERRORS= false;

	/**
	 * Represents a replace command that brings the text viewer's text widget
	 * back in synchronization with text viewer's document after the document
	 * has been changed.
	 */
	protected class WidgetCommand {

		/** The document event encapsulated by this command. */
		public DocumentEvent event;
		/** The start of the event. */
		public int start;
		/** The length of the event. */
		public int length;
		/** The inserted and replaced text segments of <code>event</code>. */
		public String text;
		/** The replaced text segments of <code>event</code>. */
		public String preservedText;

		/**
		 * Translates a document event into the presentation coordinates of this text viewer.
		 *
		 * @param e the event to be translated
		 */
		public void setEvent(DocumentEvent e) {

			event= e;

			start= e.getOffset();
			length= e.getLength();
			text= e.getText();

			if (length != 0) {
				try {

					if (e instanceof SlaveDocumentEvent) {
						SlaveDocumentEvent slave= (SlaveDocumentEvent) e;
						DocumentEvent master= slave.getMasterEvent();
						if (master != null)
							preservedText= master.getDocument().get(master.getOffset(), master.getLength());
					} else {
						preservedText= e.getDocument().get(e.getOffset(), e.getLength());
					}

				} catch (BadLocationException x) {
					preservedText= null;
					if (TRACE_ERRORS)
						System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.WidgetCommand.setEvent")); //$NON-NLS-1$
				}
			} else
				preservedText= null;
		}
	}

	/**
	 * Connects a text double click strategy to this viewer's text widget.
	 * Calls the double click strategy when the mouse has been double clicked
	 * inside the text editor.
	 */
	class TextDoubleClickStrategyConnector extends MouseAdapter {

		/** Internal flag to remember that a double clicked occurred. */
		private boolean fDoubleClicked= false;

		/**
		 * Creates a new text double click strategy adapter.
		 */
		public TextDoubleClickStrategyConnector() {
		}

		/*
		 * @see MouseListener#mouseDoubleClick(MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			fDoubleClicked= true;
		}

		/*
		 * @see MouseListener#mouseUp(MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {
			if (fDoubleClicked) {
				fDoubleClicked= false;
				ITextDoubleClickStrategy s= (ITextDoubleClickStrategy) selectContentTypePlugin(getSelectedRange().x, fDoubleClickStrategies);
				if (s != null) {
					StyledText textWidget= getTextWidget();
					Point oldSelection= textWidget.getSelection();
					s.doubleClicked(TextViewer.this);
					Point newSelection= textWidget.getSelection();
					if (newSelection.y > 0 && !oldSelection.equals(newSelection))
						textWidget.copy(DND.SELECTION_CLIPBOARD);
				}
			}
		}
	}

	/**
	 * Monitors the area of the viewer's document that is visible in the viewer.
	 * If the area might have changed, it informs the text viewer about this
	 * potential change and its origin. The origin is internally used for optimization
	 * purposes.
	 */
	class ViewportGuard extends MouseAdapter
		implements ControlListener, KeyListener, SelectionListener {

		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */
		public void controlResized(ControlEvent e) {
			updateViewportListeners(RESIZE);
		}

		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */
		public void controlMoved(ControlEvent e) {
		}

		/*
		 * @see KeyListener#keyReleased
		 */
		public void keyReleased(KeyEvent e) {
			updateViewportListeners(KEY);
		}

		/*
		 * @see KeyListener#keyPressed
		 */
		public void keyPressed(KeyEvent e) {
			updateViewportListeners(KEY);
		}

		/*
		 * @see MouseListener#mouseUp
		 */
		public void mouseUp(MouseEvent e) {
			if (fTextWidget != null)
				fTextWidget.removeSelectionListener(this);
			updateViewportListeners(MOUSE_END);
		}

		/*
		 * @see MouseListener#mouseDown
		 */
		public void mouseDown(MouseEvent e) {
			if (fTextWidget != null)
				fTextWidget.addSelectionListener(this);
		}

		/*
		 * @see SelectionListener#widgetSelected
		 */
		public void widgetSelected(SelectionEvent e) {
			if (e.widget == fScroller)
				updateViewportListeners(SCROLLER);
			else
				updateViewportListeners(MOUSE);
		}

		/*
		 * @see SelectionListener#widgetDefaultSelected
		 */
		public void widgetDefaultSelected(SelectionEvent e) {}
	}

	/**
	 * This position updater is used to keep the selection during text shift operations.
	 */
	static class ShiftPositionUpdater extends DefaultPositionUpdater {

		/**
		 * Creates the position updater for the given category.
		 *
		 * @param category the category this updater takes care of
		 */
		protected ShiftPositionUpdater(String category) {
			super(category);
		}

		/**
		 * If an insertion happens at the selection's start offset,
		 * the position is extended rather than shifted.
		 */
		protected void adaptToInsert() {

			int myStart= fPosition.offset;
			int myEnd=   fPosition.offset + fPosition.length -1;
			myEnd= Math.max(myStart, myEnd);

			int yoursStart= fOffset;
			int yoursEnd=   fOffset + fReplaceLength -1;
			yoursEnd= Math.max(yoursStart, yoursEnd);

			if (myEnd < yoursStart)
				return;

			if (myStart <= yoursStart) {
				fPosition.length += fReplaceLength;
				return;
			}

			if (myStart > yoursStart)
				fPosition.offset += fReplaceLength;
		}
	}

	/**
	 * Internal document listener on the visible document.
	 */
	class VisibleDocumentListener implements IDocumentListener {

		/*
		 * @see IDocumentListener#documentAboutToBeChanged
		 */
		public void documentAboutToBeChanged(DocumentEvent e) {
			if (e.getDocument() == getVisibleDocument())
				fWidgetCommand.setEvent(e);
			handleVisibleDocumentAboutToBeChanged(e);
		}

		/*
		 * @see IDocumentListener#documentChanged
		 */
		public void documentChanged(DocumentEvent e) {
			if (fWidgetCommand.event == e)
				updateTextListeners(fWidgetCommand);
			fLastSentSelectionChange= null;
			handleVisibleDocumentChanged(e);
		}
	}

	/**
	 * Internal verify listener.
	 */
	class TextVerifyListener implements VerifyListener {

		/**
		 * Indicates whether verify events are forwarded or ignored.
		 * @since 2.0
		 */
		private boolean fForward= true;

		/**
		 * Tells the listener to forward received events.
		 *
		 * @param forward <code>true</code> if forwarding should be enabled.
		 * @since 2.0
		 */
		public void forward(boolean forward) {
			fForward= forward;
		}

		/*
		 * @see VerifyListener#verifyText(VerifyEvent)
		 */
		public void verifyText(VerifyEvent e) {
			if (fForward)
				handleVerifyEvent(e);
		}
	}

	/**
	 * The viewer's manager responsible for registered verify key listeners.
	 * Uses batches rather than robust iterators because of performance issues.
	 * <p>
	 * The implementation is reentrant, i.e. installed listeners may trigger
	 * further <code>VerifyKeyEvent</code>s that may cause other listeners to be
	 * installed, but not thread safe.
	 * </p>
	 * @since 2.0
	 */
	class VerifyKeyListenersManager implements VerifyKeyListener {

		/**
		 * Represents a batched addListener/removeListener command.
		 */
		class Batch {
			/** The index at which to insert the listener. */
			int index;
			/** The listener to be inserted. */
			VerifyKeyListener listener;

			/**
			 * Creates a new batch containing the given listener for the given index.
			 *
			 * @param l the listener to be added
			 * @param i the index at which to insert the listener
			 */
			public Batch(VerifyKeyListener l, int i) {
				listener= l;
				index= i;
			}
		}

		/** List of registered verify key listeners. */
		private List fListeners= new ArrayList();
		/** List of pending batches. */
		private List fBatched= new ArrayList();
		/** The reentrance count. */
		private int fReentranceCount= 0;

		/*
		 * @see VerifyKeyListener#verifyKey(VerifyEvent)
		 */
		public void verifyKey(VerifyEvent event) {
			if (fListeners.isEmpty())
				return;

			try {
				fReentranceCount++;
				Iterator iterator= fListeners.iterator();
				while (iterator.hasNext() && event.doit) {
					VerifyKeyListener listener= (VerifyKeyListener) iterator.next();
					listener.verifyKey(event); // we might trigger reentrant calls on GTK
				}
			} finally {
				fReentranceCount--;
			}
			if (fReentranceCount == 0)
				processBatchedRequests();
		}

		/**
		 * Processes the pending batched requests.
		 */
		private void processBatchedRequests() {
			if (!fBatched.isEmpty()) {
				Iterator e= fBatched.iterator();
				while (e.hasNext()) {
					Batch batch= (Batch) e.next();
					insertListener(batch.listener, batch.index);
				}
				fBatched.clear();
			}
		}

		/**
		 * Returns the number of registered verify key listeners.
		 *
		 * @return the number of registered verify key listeners
		 */
		public int numberOfListeners() {
			return fListeners.size();
		}

		/**
		 * Inserts the given listener at the given index or moves it
		 * to that index.
		 *
		 * @param listener the listener to be inserted
		 * @param index the index of the listener or -1 for remove
		 */
		public void insertListener(VerifyKeyListener listener, int index) {

			if (index == -1) {
				removeListener(listener);
			} else if (listener != null) {

				if (fReentranceCount > 0) {

					fBatched.add(new Batch(listener, index));

				} else {

					int idx= -1;

					// find index based on identity
					int size= fListeners.size();
					for (int i= 0; i < size; i++) {
						if (listener == fListeners.get(i)) {
							idx= i;
							break;
						}
					}

					// move or add it
					if (idx != index) {

						if (idx != -1)
							fListeners.remove(idx);

						if (index > fListeners.size())
							fListeners.add(listener);
						else
							fListeners.add(index, listener);
					}

					if (size == 0)  // checking old size, i.e. current size == size + 1
						install();
				}
			}
		}

		/**
		 * Removes the given listener.
		 *
		 * @param listener the listener to be removed
		 */
		public void removeListener(VerifyKeyListener listener) {
			if (listener == null)
				return;

			if (fReentranceCount > 0) {

				fBatched.add(new Batch(listener, -1));

			} else {

				int size= fListeners.size();
				for (int i= 0; i < size; i++) {
					if (listener == fListeners.get(i)) {
						fListeners.remove(i);
						if (size == 1)  // checking old size, i.e. current size == size - 1
							uninstall();
						return;
					}
				}
			}
		}

		/**
		 * Installs this manager.
		 */
		private void install() {
			StyledText textWidget= getTextWidget();
			if (textWidget != null && !textWidget.isDisposed())
				textWidget.addVerifyKeyListener(this);
		}

		/**
		 * Uninstalls this manager.
		 */
		private void uninstall() {
			StyledText textWidget= getTextWidget();
			if (textWidget != null && !textWidget.isDisposed())
				textWidget.removeVerifyKeyListener(this);
		}
	}


	/**
	 * Reification of a range in which a find replace operation is performed. This range is visually
	 * highlighted in the viewer as long as the replace operation is in progress.
	 *
	 * @since 2.0
	 */
	class FindReplaceRange implements LineBackgroundListener, ITextListener, IPositionUpdater {

		/** Internal name for the position category used to update the range. */
		private final static String RANGE_CATEGORY= "org.eclipse.jface.text.TextViewer.find.range"; //$NON-NLS-1$

		/** The highlight color of this range. */
		private Color fHighlightColor;
		/** The position used to lively update this range's extent. */
		private Position fPosition;

		/** Creates a new find/replace range with the given extent.
		 *
		 * @param range the extent of this range
		 */
		public FindReplaceRange(IRegion range) {
			setRange(range);
		}

		/**
		 * Sets the extent of this range.
		 *
		 * @param range the extent of this range
		 */
		public void setRange(IRegion range) {
			fPosition= new Position(range.getOffset(), range.getLength());
		}

		/**
		 * Returns the extent of this range.
		 *
		 * @return the extent of this range
		 */
		public IRegion getRange() {
			return new Region(fPosition.getOffset(), fPosition.getLength());
		}

		/**
		 * Sets the highlight color of this range. Causes the range to be redrawn.
		 *
		 * @param color the highlight color
		 */
		public void setHighlightColor(Color color) {
			fHighlightColor= color;
			paint();
		}

		/*
		 * @see LineBackgroundListener#lineGetBackground(LineBackgroundEvent)
		 * @since 2.0
		 */
		public void lineGetBackground(LineBackgroundEvent event) {
			/* Don't use cached line information because of patched redrawing events. */

			if (fTextWidget != null) {
				int offset= widgetOffset2ModelOffset(event.lineOffset);
				if (fPosition.includes(offset))
					event.lineBackground= fHighlightColor;
			}
		}

		/**
		 * Installs this range. The range registers itself as background
		 * line painter and text listener. Also, it creates a category with the
		 * viewer's document to maintain its own extent.
		 */
		public void install() {
			TextViewer.this.addTextListener(this);
			fTextWidget.addLineBackgroundListener(this);

			IDocument document= TextViewer.this.getDocument();
			try {
				document.addPositionCategory(RANGE_CATEGORY);
				document.addPosition(RANGE_CATEGORY, fPosition);
				document.addPositionUpdater(this);
			} catch (BadPositionCategoryException e) {
				// should not happen
			} catch (BadLocationException e) {
				// should not happen
			}

			paint();
		}

		/**
		 * Uninstalls this range.
		 * @see #install()
		 */
		public void uninstall() {

			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=19612

			IDocument document= TextViewer.this.getDocument();
			if (document != null) {
				document.removePositionUpdater(this);
				document.removePosition(fPosition);
			}

			if (fTextWidget != null && !fTextWidget.isDisposed())
				fTextWidget.removeLineBackgroundListener(this);

			TextViewer.this.removeTextListener(this);

			clear();
		}

		/**
		 * Clears the highlighting of this range.
		 */
		private void clear() {
			if (fTextWidget != null && !fTextWidget.isDisposed())
				fTextWidget.redraw();
		}

		/**
		 * Paints the highlighting of this range.
		 */
		private void paint() {

			IRegion widgetRegion= modelRange2WidgetRange(fPosition);
			int offset= widgetRegion.getOffset();
			int length= widgetRegion.getLength();

			int count= fTextWidget.getCharCount();
			if (offset + length >= count) {
				length= count - offset; // clip

				Point upperLeft= fTextWidget.getLocationAtOffset(offset);
				Point lowerRight= fTextWidget.getLocationAtOffset(offset + length);
				int width= fTextWidget.getClientArea().width;
				int height= fTextWidget.getLineHeight(offset + length) + lowerRight.y - upperLeft.y;
				fTextWidget.redraw(upperLeft.x, upperLeft.y, width, height, false);
			}

			fTextWidget.redrawRange(offset, length, true);
		}

		/*
		 * @see ITextListener#textChanged(TextEvent)
		 * @since 2.0
		 */
		public void textChanged(TextEvent event) {
			if (event.getViewerRedrawState())
				paint();
		}

		/*
		 * @see IPositionUpdater#update(DocumentEvent)
		 * @since 2.0
		 */
		public void update(DocumentEvent event) {
			int offset= event.getOffset();
			int length= event.getLength();
			int delta= event.getText().length() - length;

			if (offset < fPosition.getOffset())
				fPosition.setOffset(fPosition.getOffset() + delta);
			else if (offset < fPosition.getOffset() + fPosition.getLength())
				fPosition.setLength(fPosition.getLength() + delta);
		}
	}

	/**
	 * This viewer's find/replace target.
	 */
	class FindReplaceTarget implements IFindReplaceTarget, IFindReplaceTargetExtension, IFindReplaceTargetExtension3 {

		/** The range for this target. */
		private FindReplaceRange fRange;
		/** The highlight color of the range of this target. */
		private Color fScopeHighlightColor;
		/** The document partitioner remembered in case of a "Replace All". */
		private Map fRememberedPartitioners;
		/**
		 * The active rewrite session.
		 * @since 3.1
		 */
		private DocumentRewriteSession fRewriteSession;

		/*
		 * @see IFindReplaceTarget#getSelectionText()
		 */
		public String getSelectionText() {
			Point s= TextViewer.this.getSelectedRange();
			if (s.x > -1 && s.y > -1) {
				try {
					IDocument document= TextViewer.this.getDocument();
					return document.get(s.x, s.y);
				} catch (BadLocationException x) {
				}
			}
			return null;
		}

		/*
		 * @see IFindReplaceTarget#replaceSelection(String)
		 */
		public void replaceSelection(String text) {
			replaceSelection(text, false);
		}

		/*
		 * @see IFindReplaceTarget#replaceSelection(String)
		 */
		public void replaceSelection(String text, boolean regExReplace) {
			Point s= TextViewer.this.getSelectedRange();
			if (s.x > -1 && s.y > -1) {
				try {
					IRegion matchRegion= TextViewer.this.getFindReplaceDocumentAdapter().replace(text, regExReplace);
					int length= -1;
					if (matchRegion != null)
						length= matchRegion.getLength();

					if (text != null && length > 0)
						TextViewer.this.setSelectedRange(s.x, length);
				} catch (BadLocationException x) {
				}
			}
		}

		/*
		 * @see IFindReplaceTarget#isEditable()
		 */
		public boolean isEditable() {
			return TextViewer.this.isEditable();
		}

		/*
		 * @see IFindReplaceTarget#getSelection()
		 */
		public Point getSelection() {
			Point modelSelection= TextViewer.this.getSelectedRange();
			return modelSelection2WidgetSelection(modelSelection);
		}

		/*
		 * @see IFindReplaceTarget#findAndSelect(int, String, boolean, boolean, boolean)
		 */
		public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
			try {
				return findAndSelect(widgetOffset, findString, searchForward, caseSensitive, wholeWord, false);
			} catch (PatternSyntaxException x) {
				return -1;
			}
		}

		/*
		 * @see IFindReplaceTarget#findAndSelect(int, String, boolean, boolean, boolean)
		 */
		public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {

			int modelOffset= widgetOffset == -1 ? -1 : widgetOffset2ModelOffset(widgetOffset);

			if (fRange != null) {
				IRegion range= fRange.getRange();
				modelOffset= TextViewer.this.findAndSelectInRange(modelOffset, findString, searchForward, caseSensitive, wholeWord, range.getOffset(), range.getLength(), regExSearch);
			} else {
				modelOffset= TextViewer.this.findAndSelect(modelOffset, findString, searchForward, caseSensitive, wholeWord, regExSearch);
			}

			widgetOffset= modelOffset == -1 ? -1 : modelOffset2WidgetOffset(modelOffset);
			return widgetOffset;
		}

		/*
		 * @see IFindReplaceTarget#canPerformFind()
		 */
		public boolean canPerformFind() {
			return TextViewer.this.canPerformFind();
		}

		/*
		 * @see IFindReplaceTargetExtension#beginSession()
		 * @since 2.0
		 */
		public void beginSession() {
			fRange= null;
		}

		/*
		 * @see IFindReplaceTargetExtension#endSession()
		 * @since 2.0
		 */
		public void endSession() {
			if (fRange != null) {
				fRange.uninstall();
				fRange= null;
			}
		}

		/*
		 * @see IFindReplaceTargetExtension#getScope()
		 * @since 2.0
		 */
		public IRegion getScope() {
			return fRange == null ? null : fRange.getRange();
		}

		/*
		 * @see IFindReplaceTargetExtension#getLineSelection()
		 * @since 2.0
		 */
		public Point getLineSelection() {
			Point point= TextViewer.this.getSelectedRange();

			try {
				IDocument document= TextViewer.this.getDocument();

				// beginning of line
				int line= document.getLineOfOffset(point.x);
				int offset= document.getLineOffset(line);

				// end of line
				IRegion lastLineInfo= document.getLineInformationOfOffset(point.x + point.y);
				int lastLine= document.getLineOfOffset(point.x + point.y);
				int length;
				if (lastLineInfo.getOffset() == point.x + point.y && lastLine > 0)
					length= document.getLineOffset(lastLine - 1) + document.getLineLength(lastLine - 1)	- offset;
				else
					length= lastLineInfo.getOffset() + lastLineInfo.getLength() - offset;

				return new Point(offset, length);

			} catch (BadLocationException e) {
				// should not happen
				return new Point(point.x, 0);
			}
		}

		/*
		 * @see IFindReplaceTargetExtension#setSelection(int, int)
		 * @since 2.0
		 */
		public void setSelection(int modelOffset, int modelLength) {
			TextViewer.this.setSelectedRange(modelOffset, modelLength);
		}

		/*
		 * @see IFindReplaceTargetExtension#setScope(IRegion)
		 * @since 2.0
		 */
		public void setScope(IRegion scope) {
			if (fRange != null)
				fRange.uninstall();

			if (scope == null) {
				fRange= null;
				return;
			}

			fRange= new FindReplaceRange(scope);
			fRange.setHighlightColor(fScopeHighlightColor);
			fRange.install();
		}

		/*
		 * @see IFindReplaceTargetExtension#setScopeHighlightColor(Color)
		 * @since 2.0
		 */
		public void setScopeHighlightColor(Color color) {
			if (fRange != null)
				fRange.setHighlightColor(color);
			fScopeHighlightColor= color;
		}

		/*
		 * @see IFindReplaceTargetExtension#setReplaceAllMode(boolean)
		 * @since 2.0
		 */
		public void setReplaceAllMode(boolean replaceAll) {

			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=18232

			IDocument document= TextViewer.this.getDocument();

			if (replaceAll) {

				if (document instanceof IDocumentExtension4) {
					IDocumentExtension4 extension= (IDocumentExtension4) document;
					fRewriteSession= extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
				} else {
					TextViewer.this.setRedraw(false);
					TextViewer.this.startSequentialRewriteMode(false);

					if (fUndoManager != null)
						fUndoManager.beginCompoundChange();

					fRememberedPartitioners= TextUtilities.removeDocumentPartitioners(document);
				}

			} else {

				if (document instanceof IDocumentExtension4) {
					IDocumentExtension4 extension= (IDocumentExtension4) document;
					extension.stopRewriteSession(fRewriteSession);
				} else {
					TextViewer.this.setRedraw(true);
					TextViewer.this.stopSequentialRewriteMode();

					if (fUndoManager != null)
						fUndoManager.endCompoundChange();

					if (fRememberedPartitioners != null)
						TextUtilities.addDocumentPartitioners(document, fRememberedPartitioners);
				}
			}
		}
	}


	/**
	 * The viewer's rewrite target.
	 * @since 2.0
	 */
	class RewriteTarget implements IRewriteTarget {

		/*
		 * @see org.eclipse.jface.text.IRewriteTarget#beginCompoundChange()
		 */
		public void beginCompoundChange() {
			if (fUndoManager != null)
				fUndoManager.beginCompoundChange();
		}

		/*
		 * @see org.eclipse.jface.text.IRewriteTarget#endCompoundChange()
		 */
		public void endCompoundChange() {
			if (fUndoManager != null)
				fUndoManager.endCompoundChange();
		}

		/*
		 * @see org.eclipse.jface.text.IRewriteTarget#getDocument()
		 */
		public IDocument getDocument() {
			return TextViewer.this.getDocument();
		}

		/*
		 * @see org.eclipse.jface.text.IRewriteTarget#setRedraw(boolean)
		 */
		public void setRedraw(boolean redraw) {
			TextViewer.this.setRedraw(redraw);
		}
	}

	/**
	 * Value object used as key in the text hover configuration table. It is
	 * modifiable only inside this compilation unit to allow the reuse of created
	 * objects for efficiency reasons
	 *
	 * @since 2.1
	 */
	protected class TextHoverKey {

		/** The content type this key belongs to */
		private String fContentType;
		/** The state mask */
		private int fStateMask;

		/**
		 * Creates a new text hover key for the given content type and state mask.
		 *
		 * @param contentType the content type
		 * @param stateMask the state mask
		 */
		protected TextHoverKey(String contentType, int stateMask) {
			Assert.isNotNull(contentType);
			fContentType= contentType;
			fStateMask= stateMask;
		}

		/*
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj == null || obj.getClass() != getClass())
				return false;
			TextHoverKey textHoverKey= (TextHoverKey)obj;
			return textHoverKey.fContentType.equals(fContentType) && textHoverKey.fStateMask == fStateMask;
		}

		/*
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
	 		return fStateMask << 16 | fContentType.hashCode();
		}

		/**
		 * Sets the state mask of this text hover key.
		 *
		 * @param stateMask the state mask
		 */
		private void setStateMask(int stateMask) {
			fStateMask= stateMask;
		}
	}

	/**
	 * A position reflecting a viewer selection and the selection anchor.
	 * The anchor is represented by the caret.
	 *
	 * @since 2.1
	 */
	static class SelectionPosition extends Position {

		/** The flag indicating the anchor of this selection */
		private boolean reverse;

		/**
		 * Creates a new selection position for the specified selection.
		 *
		 * @param point the specified selection
		 */
		public SelectionPosition(Point point) {
			super();
			reverse= point.y < 0;
			if (reverse) {
				offset= point.x + point.y;
				length= -point.y;
			} else {
				offset= point.x;
				length= point.y;
			}
		}

		/**
		 * Returns the selection in a format where the selection length
		 * is always non-negative.
		 *
		 * @return the normalized selection
		 */
		public Point getNormalizedSelection() {
			return new Point(offset, length);
		}

		/**
		 * Returns the selection reflecting its anchor.
		 *
		 * @return the selection reflecting the selection anchor.
		 */
		public Point getSelection() {
			return reverse ? new Point(offset - length, -length) : new Point(offset, length);
		}
	}

	/**
	 * Internal cursor listener i.e. aggregation of mouse and key listener.
	 *
	 * @since 3.0
	 */
	private class CursorListener implements KeyListener, MouseListener {

		/**
		 * Installs this cursor listener.
		 */
		private void install() {
			if (fTextWidget != null && !fTextWidget.isDisposed()) {
				fTextWidget.addKeyListener(this);
				fTextWidget.addMouseListener(this);
			}
		}

		/**
		 * Uninstalls this cursor listener.
		 */
		private void uninstall() {
			if (fTextWidget != null && !fTextWidget.isDisposed()) {
				fTextWidget.removeKeyListener(this);
				fTextWidget.removeMouseListener(this);
			}
		}

		/*
		 * @see KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(KeyEvent event) {
		}

		/*
		 * @see KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
		 */
		public void keyReleased(KeyEvent e) {
			if (fTextWidget.getSelectionCount() == 0)
				queuePostSelectionChanged(false);
		}

		/*
		 * @see MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
		}

		/*
		 * @see MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent e) {
		}

		/*
		 * @see MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent event) {
			if (fTextWidget.getSelectionCount() == 0)
				queuePostSelectionChanged(false);
		}
	}

	/**
	 * Internal listener to document rewrite session state changes.
	 * @since 3.1
	 */
	private class DocumentRewriteSessionListener implements IDocumentRewriteSessionListener {

		/*
		 * @see org.eclipse.jface.text.IDocumentRewriteSessionListener#documentRewriteSessionChanged(org.eclipse.jface.text.DocumentRewriteSessionEvent)
		 */
		public void documentRewriteSessionChanged(DocumentRewriteSessionEvent event) {
			IRewriteTarget target= TextViewer.this.getRewriteTarget();
			if (DocumentRewriteSessionEvent.SESSION_START == event.getChangeType()) {
				target.setRedraw(false);
				target.beginCompoundChange();
			} else if (DocumentRewriteSessionEvent.SESSION_STOP == event.getChangeType()) {
				target.endCompoundChange();
				target.setRedraw(true);
			}
		}
	}

	/**
	 * Identifies the scrollbars as originators of a view port change.
	 */
	protected static final int SCROLLER=	1;
	/**
	 * Identifies  mouse moves as originators of a view port change.
	 */
	protected static final int MOUSE=		2;
	/**
	 * Identifies mouse button up as originator of a view port change.
	 */
	protected static final int MOUSE_END=	3;
	/**
	 * Identifies key strokes as originators of a view port change.
	 */
	protected static final int KEY=			4;
	/**
	 * Identifies window resizing as originator of a view port change.
	 */
	protected static final int RESIZE=		5;
	/**
	 * Identifies internal reasons as originators of a view port change.
	 */
	protected static final int INTERNAL=	6;

	/** Internal name of the position category used selection preservation during shift. */
	protected static final String SHIFTING= "__TextViewer_shifting"; //$NON-NLS-1$

	/**
	 * Base position category name used by the selection updater
	 * @since 3.1
	 */
	private static final String SELECTION_POSITION_CATEGORY= "_textviewer_selection_category"; //$NON-NLS-1$
	/** The viewer's text widget */
	private StyledText fTextWidget;
	/** The viewer's input document */
	private IDocument fDocument;
	/** The viewer's visible document */
	private IDocument fVisibleDocument;
	/** The viewer's document adapter */
	private IDocumentAdapter fDocumentAdapter;
	/** The slave document manager */
	private ISlaveDocumentManager fSlaveDocumentManager;
	/** The text viewer's double click strategies connector */
	private TextDoubleClickStrategyConnector fDoubleClickStrategyConnector;
	/** The text viewer's view port guard */
	private ViewportGuard fViewportGuard;
	/** Caches the graphical coordinate of the first visible line */
	private int fTopInset= 0;
	/** The most recent document modification as widget command */
	private WidgetCommand fWidgetCommand= new WidgetCommand();
	/** The SWT control's scrollbars */
	private ScrollBar fScroller;
	/** Listener on the visible document */
	private VisibleDocumentListener fVisibleDocumentListener= new VisibleDocumentListener();
	/** Verify listener */
	private TextVerifyListener fVerifyListener= new TextVerifyListener();
	/** The most recent widget modification as document command */
	private DocumentCommand fDocumentCommand= new DocumentCommand();
	/** The viewer's find/replace target */
	private IFindReplaceTarget fFindReplaceTarget;
	/**
	 * The text viewer's hovering controller
	 * @since 2.0
	 */
	private TextViewerHoverManager fTextHoverManager;
	/**
	 * The viewer widget token keeper
	 * @since 2.0
	 */
	private IWidgetTokenKeeper fWidgetTokenKeeper;
	/**
	 * The viewer's manager of verify key listeners
	 * @since 2.0
	 */
	private VerifyKeyListenersManager fVerifyKeyListenersManager= new VerifyKeyListenersManager();
	/**
	 * The mark position.
	 * @since 2.0
	 */
	protected Position fMarkPosition;
	/**
	 * The mark position category.
	 * @since 2.0
	 */
	private final String MARK_POSITION_CATEGORY="__mark_category_" + hashCode(); //$NON-NLS-1$
	/**
	 * The mark position updater
	 * @since 2.0
	 */
	private final IPositionUpdater fMarkPositionUpdater= new DefaultPositionUpdater(MARK_POSITION_CATEGORY);
	/**
	 * The flag indicating the redraw behavior
	 * @since 2.0
	 */
	private int fRedrawCounter= 0;
	/**
	 * The selection when working in non-redraw state
	 * @since 2.0
	 */
	private SelectionPosition fDocumentSelection;
	/**
	 * The viewer's rewrite target
	 * @since 2.0
	 */
	private IRewriteTarget fRewriteTarget;
	/**
	 * The viewer's cursor listener.
	 * @since 3.0
	 */
	private CursorListener fCursorListener;
	/**
	 * Last selection range sent to selection change listeners.
	 * @since 3.0
	 */
	private IRegion fLastSentSelectionChange;
	/**
	 * The registered post selection changed listeners.
	 * @since 3.0
	 */
	private List fPostSelectionChangedListeners;
	/**
	 * Queued post selection changed events count.
	 * @since 3.0
	 */
	private final int[] fNumberOfPostSelectionChangedEvents= new int[1];
	/**
	 * Last selection range sent to post selection change listeners.
	 * @since 3.0
	 */
	private IRegion fLastSentPostSelectionChange;
	/**
	 * The set of registered editor helpers.
	 * @since 3.1
	 */
	private Set fEditorHelpers= new HashSet();
	/**
	 * The internal rewrite session listener.
	 * @since 3.1
	 */
	private DocumentRewriteSessionListener fDocumentRewriteSessionListener= new DocumentRewriteSessionListener();

	/** Should the auto indent strategies ignore the next edit operation */
	protected boolean  fIgnoreAutoIndent= false;
	/** The strings a line is prefixed with on SHIFT_RIGHT and removed from each line on SHIFT_LEFT */
	protected Map fIndentChars;
	/** The string a line is prefixed with on PREFIX and removed from each line on STRIP_PREFIX */
	protected Map fDefaultPrefixChars;
	/** The text viewer's text double click strategies */
	protected Map fDoubleClickStrategies;
	/** The text viewer's undo manager */
	protected IUndoManager fUndoManager;
	/** The text viewer's auto indent strategies */
	protected Map fAutoIndentStrategies;
	/** The text viewer's text hovers */
	protected Map fTextHovers;
	/** All registered view port listeners> */
	protected List fViewportListeners;
	/** The last visible vertical position of the top line */
	protected int fLastTopPixel;
	/** All registered text listeners */
	protected List fTextListeners;
	/** All registered text input listeners */
	protected List fTextInputListeners;
	/** The text viewer's event consumer */
	protected IEventConsumer fEventConsumer;
	/** Indicates whether the viewer's text presentation should be replaced are modified. */
	protected boolean fReplaceTextPresentation= false;
	/**
	 * The creator of the text hover control
	 * @since 2.0
	 */
	protected IInformationControlCreator fHoverControlCreator;
	/**
	 * The mapping between model and visible document.
	 * @since 2.1
	 */
	protected IDocumentInformationMapping fInformationMapping;
	/**
	 * The viewer's paint manager.
	 * @since 2.1
	 */
	protected PaintManager fPaintManager;
	/**
	 * The viewers partitioning. I.e. the partitioning name the viewer uses to access partitioning information of its input document.
	 * @since 3.0
	 */
	protected String fPartitioning;
	/**
	 * All registered text presentation listeners.
	 * since 3.0
	 */
	protected List fTextPresentationListeners;
	/**
	 * The find/replace document adapter.
	 * @since 3.0
	 */
	protected FindReplaceDocumentAdapter fFindReplaceDocumentAdapter;

	/**
	 * The text viewer's hyperlink detectors.
	 * @since 3.1
	 */
	protected IHyperlinkDetector[] fHyperlinkDetectors;
	/**
	 * The text viewer's hyperlink presenter.
	 * @since 3.1
	 */
	protected IHyperlinkPresenter fHyperlinkPresenter;
	/**
	 * The text viewer's hyperlink manager.
	 * @since 3.1
	 */
	protected HyperlinkManager fHyperlinkManager;
	/**
	 * The SWT key modifier mask which in combination
	 * with the left mouse button triggers the hyperlink mode.
	 * @since 3.1
	 */
	protected int fHyperlinkStateMask;
	/**
	 * Position category used by the selection updater
	 * @since 3.1
	 */
	private String fRememberedSelectionCategory;
	/**
	 * Position updater for saved selections
	 * @since 3.1
	 */
	private IPositionUpdater fSelectionUpdater;


	//---- Construction and disposal ------------------


	/**
	 * Internal use only
	 */
	protected TextViewer() {
	}

	/**
	 * Create a new text viewer with the given SWT style bits.
	 * The viewer is ready to use but does not have any plug-in installed.
	 *
	 * @param parent the parent of the viewer's control
	 * @param styles the SWT style bits for the viewer's control
	 */
	public TextViewer(Composite parent, int styles) {
		createControl(parent, styles);
	}

	/**
	 * Factory method to create the text widget to be used as the viewer's text widget.
	 *
	 * @param parent the parent of the styled text
	 * @param styles the styles for the styled text
	 * @return the text widget to be used
	 */
	protected StyledText createTextWidget(Composite parent, int styles) {
		return new StyledText(parent, styles);
	}

	/**
	 * Factory method to create the document adapter to be used by this viewer.
	 *
	 * @return the document adapter to be used
	 */
	protected IDocumentAdapter createDocumentAdapter() {
		return new DefaultDocumentAdapter();
	}

	/**
	 * Creates the viewer's SWT control. The viewer's text widget either is
	 * the control or is a child of the control.
	 *
	 * @param parent the parent of the viewer's control
	 * @param styles the SWT style bits for the viewer's control
	 */
	protected void createControl(Composite parent, int styles) {

		fTextWidget= createTextWidget(parent, styles);
		fTextWidget.addDisposeListener(
			new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					setDocument(null);
					handleDispose();
					fTextWidget= null;
				}
			}
		);

		fTextWidget.setFont(parent.getFont());
		fTextWidget.setDoubleClickEnabled(false);

		/*
		 * Disable SWT Shift+TAB traversal in this viewer
		 * 1GIYQ9K: ITPUI:WINNT - StyledText swallows Shift+TAB
		 */
		fTextWidget.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if ((SWT.SHIFT == e.stateMask) && ('\t' == e.character))
					e.doit = false;
			}
		});

		// where does the first line start
		fTopInset= -fTextWidget.computeTrim(0, 0, 0, 0).y;

		fVerifyListener.forward(true);
		fTextWidget.addVerifyListener(fVerifyListener);

		fTextWidget.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {
				selectionChanged(event.x, event.y - event.x);
			}
			public void widgetSelected(SelectionEvent event) {
				selectionChanged(event.x, event.y - event.x);
			}
		});

		fCursorListener= new CursorListener();
		fCursorListener.install();

		initializeViewportUpdate();
	}

	/*
	 * @see Viewer#getControl()
	 */
	public Control getControl() {
		return fTextWidget;
	}

	/*
	 * @see ITextViewer#activatePlugins()
	 */
	public void activatePlugins() {

		if (fDoubleClickStrategies != null && !fDoubleClickStrategies.isEmpty() && fDoubleClickStrategyConnector == null) {
			fDoubleClickStrategyConnector= new TextDoubleClickStrategyConnector();
			fTextWidget.addMouseListener(fDoubleClickStrategyConnector);
		}

		ensureHoverControlManagerInstalled();
		ensureHyperlinkManagerInstalled();

		if (fUndoManager != null) {
			fUndoManager.connect(this);
			fUndoManager.reset();
		}
	}

	/**
	 * After this method has been executed the caller knows that any installed text hover has been installed.
	 */
	private void ensureHoverControlManagerInstalled() {
		if (fTextHovers != null && !fTextHovers.isEmpty() && fHoverControlCreator != null && fTextHoverManager == null) {
			fTextHoverManager= new TextViewerHoverManager(this, fHoverControlCreator);
			fTextHoverManager.install(this.getTextWidget());
			fTextHoverManager.setSizeConstraints(60, 10, false, true);
		}
	}

	/*
	 * @see ITextViewer#resetPlugins()
	 */
	public void resetPlugins() {
		if (fUndoManager != null)
			fUndoManager.reset();
	}

	/**
	 * Frees all resources allocated by this viewer. Internally called when the viewer's
	 * control has been disposed.
	 */
	protected void handleDispose() {

		if (fPaintManager != null) {
			fPaintManager.dispose();
			fPaintManager= null;
		}

		removeViewPortUpdate();
		fViewportGuard= null;

		if (fViewportListeners != null) {
			fViewportListeners.clear();
			fViewportListeners= null;
		}

		if (fTextListeners != null) {
			fTextListeners.clear();
			fTextListeners= null;
		}

		if (fTextInputListeners != null)  {
			fTextInputListeners.clear();
			fTextInputListeners= null;
		}

		if (fPostSelectionChangedListeners != null)  {
			fPostSelectionChangedListeners.clear();
			fPostSelectionChangedListeners= null;
		}

		if (fAutoIndentStrategies != null) {
			fAutoIndentStrategies.clear();
			fAutoIndentStrategies= null;
		}

		if (fUndoManager != null) {
			fUndoManager.disconnect();
			fUndoManager= null;
		}

		if (fDoubleClickStrategies != null) {
			fDoubleClickStrategies.clear();
			fDoubleClickStrategies= null;
		}

		if (fTextHovers != null) {
			fTextHovers.clear();
			fTextHovers= null;
		}

		fDoubleClickStrategyConnector= null;

		if (fTextHoverManager != null) {
			fTextHoverManager.dispose();
			fTextHoverManager= null;
		}

		if (fVisibleDocumentListener !=null) {
			if (fVisibleDocument != null)
				fVisibleDocument.removeDocumentListener(fVisibleDocumentListener);
			fVisibleDocumentListener= null;
		}

		if (fDocumentAdapter != null) {
			fDocumentAdapter.setDocument(null);
			fDocumentAdapter= null;
		}

		if (fSlaveDocumentManager != null) {
			if (fVisibleDocument != null)
				fSlaveDocumentManager.freeSlaveDocument(fVisibleDocument);
			fSlaveDocumentManager= null;
		}

		if (fCursorListener != null) {
			fCursorListener.uninstall();
			fCursorListener= null;
		}

		if (fHyperlinkManager != null) {
			fHyperlinkManager.uninstall();
			fHyperlinkManager= null;
		}

		fHyperlinkDetectors= null;
		fVisibleDocument= null;
		fDocument= null;
		fScroller= null;
	}


	//---- simple getters and setters

	/**
	 * Returns viewer's text widget.
	 *
	 * @return the viewer's text widget
	 */
	public StyledText getTextWidget() {
		return fTextWidget;
	}

	/**
	 * The delay in milliseconds before an empty selection
	 * changed event is sent by the cursor listener.
	 * <p>
	 * Note: The return value is used to initialize the cursor
	 * listener. To return a non-constant value has no effect.</p>
	 * <p>
	 * The same value (<code>500</code>) is used in <code>OpenStrategy.TIME</code>.</p>
	 *
	 * @return delay in milliseconds
	 * @see org.eclipse.jface.util.OpenStrategy
	 * @since 3.0
	 */
	protected int getEmptySelectionChangedEventDelay() {
		return 500;
	}

	/**
	 * {@inheritDoc}
	 * @deprecated since 3.1, use
	 *             {@link ITextViewerExtension2#prependAutoEditStrategy(IAutoEditStrategy, String)} and
	 *             {@link ITextViewerExtension2#removeAutoEditStrategy(IAutoEditStrategy, String)} instead
	 */
	public void setAutoIndentStrategy(IAutoIndentStrategy strategy, String contentType) {
		setAutoEditStrategies(new IAutoEditStrategy[] { strategy }, contentType);
	}

	/**
	 * Sets the given edit strategy as the only strategy for the given content type.
	 *
	 * @param strategies the auto edit strategies
	 * @param contentType the content type
	 * @since 3.1
	 */
	protected final void setAutoEditStrategies(IAutoEditStrategy[] strategies, String contentType) {
		if (fAutoIndentStrategies == null)
			fAutoIndentStrategies= new HashMap();

		List autoEditStrategies= (List) fAutoIndentStrategies.get(contentType);

		if (strategies == null) {
			if (autoEditStrategies == null)
				return;

			fAutoIndentStrategies.put(contentType, null);

		} else {
			if (autoEditStrategies == null) {
				autoEditStrategies= new ArrayList();
				fAutoIndentStrategies.put(contentType, autoEditStrategies);
			}

			autoEditStrategies.clear();
			autoEditStrategies.addAll(Arrays.asList(strategies));
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension2#prependAutoEditStrategy(org.eclipse.jface.text.IAutoEditStrategy, java.lang.String)
	 * @since 2.1
	 */
	public void prependAutoEditStrategy(IAutoEditStrategy strategy, String contentType) {

		if (strategy == null || contentType == null)
			throw new IllegalArgumentException();

		if (fAutoIndentStrategies == null)
			fAutoIndentStrategies= new HashMap();

		List autoEditStrategies= (List) fAutoIndentStrategies.get(contentType);
		if (autoEditStrategies == null) {
			autoEditStrategies= new ArrayList();
			fAutoIndentStrategies.put(contentType, autoEditStrategies);
		}

		autoEditStrategies.add(0, strategy);
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension2#removeAutoEditStrategy(org.eclipse.jface.text.IAutoEditStrategy, java.lang.String)
	 * @since 2.1
	 */
	public void removeAutoEditStrategy(IAutoEditStrategy strategy, String contentType) {
		if (fAutoIndentStrategies == null)
			return;

		List autoEditStrategies= (List) fAutoIndentStrategies.get(contentType);
		if (autoEditStrategies == null)
			return;

		for (final Iterator iterator= autoEditStrategies.iterator(); iterator.hasNext(); ) {
			if (iterator.next().equals(strategy)) {
				iterator.remove();
				break;
			}
		}

		if (autoEditStrategies.isEmpty())
			fAutoIndentStrategies.put(contentType, null);
	}

	/*
	 * @see ITextViewer#setEventConsumer(IEventConsumer)
	 */
	public void setEventConsumer(IEventConsumer consumer) {
		fEventConsumer= consumer;
	}

	/*
	 * @see ITextViewer#setIndentPrefixes(String[], String)
	 */
	public void setIndentPrefixes(String[] indentPrefixes, String contentType) {

		int i= -1;
		boolean ok= (indentPrefixes != null);
		while (ok &&  ++i < indentPrefixes.length)
			ok= (indentPrefixes[i] != null);

		if (ok) {

			if (fIndentChars == null)
				fIndentChars= new HashMap();

			fIndentChars.put(contentType, indentPrefixes);

		} else if (fIndentChars != null)
			fIndentChars.remove(contentType);
	}

	/*
	 * @see ITextViewer#getTopInset()
	 */
	public int getTopInset() {
		return fTopInset;
	}

	/*
	 * @see ITextViewer#isEditable()
	 */
	public boolean isEditable() {
		if (fTextWidget == null)
			return false;
		return fTextWidget.getEditable();
	}

	/*
	 * @see ITextViewer#setEditable(boolean)
	 */
	public void setEditable(boolean editable) {
		if (fTextWidget != null)
			fTextWidget.setEditable(editable);
	}

	/*
	 * @see ITextViewer#setDefaultPrefixes
	 * @since 2.0
	 */
	public void setDefaultPrefixes(String[] defaultPrefixes, String contentType) {

		if (defaultPrefixes != null && defaultPrefixes.length > 0) {
			if (fDefaultPrefixChars == null)
				fDefaultPrefixChars= new HashMap();
			fDefaultPrefixChars.put(contentType, defaultPrefixes);
		} else if (fDefaultPrefixChars != null)
			fDefaultPrefixChars.remove(contentType);
	}

	/*
	 * @see ITextViewer#setUndoManager(IUndoManager)
	 */
	public void setUndoManager(IUndoManager undoManager) {
		fUndoManager= undoManager;
	}

	/*
	 * @see ITextViewerExtension6#getUndoManager()
	 * @since 3.1
	 */
	public IUndoManager getUndoManager() {
		return fUndoManager;
	}

	/*
	 * @see ITextViewer#setTextHover(ITextHover, String)
	 */
	public void setTextHover(ITextHover hover, String contentType) {
		setTextHover(hover, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}

	/*
	 * @see ITextViewerExtension2#setTextHover(ITextHover, String, int)
	 * @since 2.1
	 */
	public void setTextHover(ITextHover hover, String contentType, int stateMask) {
		TextHoverKey key= new TextHoverKey(contentType, stateMask);
		if (hover != null) {
			if (fTextHovers == null) {
				fTextHovers= new HashMap();
			}
			fTextHovers.put(key, hover);
		} else if (fTextHovers != null)
			fTextHovers.remove(key);

		ensureHoverControlManagerInstalled();
	}

	/*
	 * @see ITextViewerExtension2#removeTextHovers(String)
	 * @since 2.1
	 */
	public void removeTextHovers(String contentType) {
		if (fTextHovers == null)
			return;

		Iterator iter= new HashSet(fTextHovers.keySet()).iterator();
		while (iter.hasNext()) {
			TextHoverKey key= (TextHoverKey)iter.next();
			if (key.fContentType.equals(contentType))
				fTextHovers.remove(key);
		}
	}

	/**
	 * Returns the text hover for a given offset.
	 *
	 * @param offset the offset for which to return the text hover
	 * @return the text hover for the given offset
	 */
	protected ITextHover getTextHover(int offset) {
		return getTextHover(offset, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}

	/**
	 * Returns the text hover for a given offset and a given state mask.
	 *
	 * @param offset the offset for which to return the text hover
	 * @param stateMask the SWT event state mask
	 * @return the text hover for the given offset and state mask
	 * @since 2.1
	 */
	protected ITextHover getTextHover(int offset, int stateMask) {
		if (fTextHovers == null)
			return null;

		IDocument document= getDocument();
		if (document == null)
			return null;

		try {
			TextHoverKey key= new TextHoverKey(TextUtilities.getContentType(document, getDocumentPartitioning(), offset, true), stateMask);
			Object textHover= fTextHovers.get(key);
			if (textHover == null) {
				// Use default text hover
				key.setStateMask(ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
				textHover= fTextHovers.get(key);
			}
			return (ITextHover) textHover;
		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.selectContentTypePlugin")); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Returns the text hovering controller of this viewer.
	 *
	 * @return the text hovering controller of this viewer
	 * @since 2.0
	 */
	protected AbstractInformationControlManager getTextHoveringController() {
		return fTextHoverManager;
	}

	/**
	 * Sets the creator for the hover controls.
	 *
	 * @param creator the hover control creator
	 * @since 2.0
	 */
	public void setHoverControlCreator(IInformationControlCreator creator) {
		fHoverControlCreator= creator;
	}

	/*
	 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
	 * @since 2.0
	 */
	 public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
		 if (fTextWidget != null) {
			 if (fWidgetTokenKeeper != null) {
				 if (fWidgetTokenKeeper == requester)
					 return true;
				 if (fWidgetTokenKeeper.requestWidgetToken(this)) {
					 fWidgetTokenKeeper= requester;
					 return true;
				 }
			} else {
				fWidgetTokenKeeper= requester;
				return true;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenOwnerExtension#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenKeeper, int)
	 * @since 3.0
	 */
	public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
		if (fTextWidget != null) {
			if (fWidgetTokenKeeper != null) {

				if (fWidgetTokenKeeper == requester)
					return true;

				boolean accepted= false;
				if (fWidgetTokenKeeper instanceof IWidgetTokenKeeperExtension)  {
					IWidgetTokenKeeperExtension extension= (IWidgetTokenKeeperExtension) fWidgetTokenKeeper;
					accepted= extension.requestWidgetToken(this, priority);
				} else  {
					accepted= fWidgetTokenKeeper.requestWidgetToken(this);
				}

				if (accepted) {
					fWidgetTokenKeeper= requester;
					return true;
				}

		   } else {
			   fWidgetTokenKeeper= requester;
			   return true;
		   }
	   }
	   return false;
   }

	/*
	 * @see IWidgetTokenOwner#releaseWidgetToken(IWidgetTokenKeeper)
	 * @since 2.0
	 */
	public void releaseWidgetToken(IWidgetTokenKeeper tokenKeeper) {
		if (fWidgetTokenKeeper == tokenKeeper)
			fWidgetTokenKeeper= null;
	}


	//---- Selection

	/*
	 * @see ITextViewer#getSelectedRange()
	 */
	public Point getSelectedRange() {

		if (!redraws() && fDocumentSelection != null)
			return fDocumentSelection.getNormalizedSelection();

		if (fTextWidget != null) {
			Point p= fTextWidget.getSelectionRange();
			p= widgetSelection2ModelSelection(p);
			if (p != null)
				return p;
		}

		return new Point(-1, -1);
	}

	/*
	 * @see ITextViewer#setSelectedRange(int, int)
	 */
	public void setSelectedRange(int selectionOffset, int selectionLength) {

		if (!redraws()) {
			if (fDocumentSelection != null) {
				fDocumentSelection.offset= selectionOffset;
				fDocumentSelection.length= selectionLength;
				// http://bugs.eclipse.org/bugs/show_bug.cgi?id=32795
				fDocumentSelection.isDeleted= false;
			}
			return;
		}

		if (fTextWidget == null)
			return;

		IRegion widgetSelection= modelRange2ClosestWidgetRange(new Region(selectionOffset, selectionLength));
		if (widgetSelection != null) {

			int[] selectionRange= new int[] { widgetSelection.getOffset(), widgetSelection.getLength() };
			validateSelectionRange(selectionRange);
			if (selectionRange[0] >= 0) {
				fTextWidget.setSelectionRange(selectionRange[0], selectionRange[1]);
				selectionChanged(selectionRange[0], selectionRange[1]);
			}
		}
	}

	/**
	 * Validates and adapts the given selection range if it is not a valid
	 * widget selection. The widget selection is invalid if it starts or ends
	 * inside a multi-character line delimiter. If so, the selection is adapted to
	 * start <b>after</b> the divided line delimiter and to end <b>before</b>
	 * the divided line delimiter.  The parameter passed in is changed in-place
	 * when being adapted. An adaptation to <code>[-1, -1]</code> indicates
	 * that the selection range could not be validated.
	 * Subclasses may reimplement this method.
	 *
	 * @param selectionRange selectionRange[0] is the offset, selectionRange[1]
	 * 				the length of the selection to validate.
	 * @since 2.0
	 */
	protected void validateSelectionRange(int[] selectionRange) {

		IDocument document= getVisibleDocument();
		if (document == null) {
			selectionRange[0]= -1;
			selectionRange[1]= -1;
			return;
		}

		int documentLength= document.getLength();
		int offset= selectionRange[0];
		int length= selectionRange[1];

		if (length < 0) {
			length= - length;
			offset -= length;
		}

		if (offset <0)
			offset= 0;

		if (offset > documentLength)
			offset= documentLength;

		int delta= (offset + length) - documentLength;
		if (delta > 0)
			length -= delta;

		try {

			int lineNumber= document.getLineOfOffset(offset);
			IRegion lineInformation= document.getLineInformation(lineNumber);

			int lineEnd= lineInformation.getOffset() + lineInformation.getLength();
			delta= offset - lineEnd;
			if (delta > 0) {
				// in the middle of a multi-character line delimiter
				offset= lineEnd;
				String delimiter= document.getLineDelimiter(lineNumber);
				if (delimiter != null)
					offset += delimiter.length();
			}

			int end= offset + length;
			lineInformation= document.getLineInformationOfOffset(end);
			lineEnd= lineInformation.getOffset() + lineInformation.getLength();
			delta= end - lineEnd;
			if (delta > 0) {
				// in the middle of a multi-character line delimiter
				length -= delta;
			}

		} catch (BadLocationException x) {
			selectionRange[0]= -1;
			selectionRange[1]= -1;
			return;
		}

		if (selectionRange[1] < 0) {
			selectionRange[0]= offset + length;
			selectionRange[1]= -length;
		} else {
			selectionRange[0]= offset;
			selectionRange[1]= length;
		}
	}

	/*
	 * @see Viewer#setSelection(ISelection)
	 */
	public void setSelection(ISelection selection, boolean reveal) {
		if (selection instanceof ITextSelection) {
			ITextSelection s= (ITextSelection) selection;
			setSelectedRange(s.getOffset(), s.getLength());
			if (reveal)
				revealRange(s.getOffset(), s.getLength());
		}
	}

	/*
	 * @see Viewer#getSelection()
	 */
	public ISelection getSelection() {
		Point p= getSelectedRange();
		if (p.x == -1 || p.y == -1)
			return TextSelection.emptySelection();

		return new TextSelection(getDocument(), p.x, p.y);
	}

	/*
	 * @see ITextViewer#getSelectionProvider()
	 */
	public ISelectionProvider getSelectionProvider() {
		return this;
	}

	/*
	 * @see org.eclipse.jface.text.IPostSelectionProvider#addPostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 * @since 3.0
	 */
	public void addPostSelectionChangedListener(ISelectionChangedListener listener)  {

		Assert.isNotNull(listener);

		if (fPostSelectionChangedListeners == null)
			fPostSelectionChangedListeners= new ArrayList();

		if (! fPostSelectionChangedListeners.contains(listener))
			fPostSelectionChangedListeners.add(listener);
	}

	/*
	 * @see org.eclipse.jface.text.IPostSelectionProvider#removePostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 * @since 3.0
	 */
	public void removePostSelectionChangedListener(ISelectionChangedListener listener)  {

		Assert.isNotNull(listener);

		if (fPostSelectionChangedListeners != null)  {
			fPostSelectionChangedListeners.remove(listener);
			if (fPostSelectionChangedListeners.size() == 0)
				fPostSelectionChangedListeners= null;
		}
	}

	/**
	 * Get the text widget's display.
	 *
	 * @return the display or <code>null</code> if the display cannot be retrieved or if the display is disposed
	 * @since 3.0
	 */
	private Display getDisplay() {
		if (fTextWidget == null || fTextWidget.isDisposed())
			return null;

		Display display= fTextWidget.getDisplay();
		if (display != null && display.isDisposed())
			return null;

		return display;
	}

	/**
	 * Starts a timer to send out a post selection changed event.
	 *
	 * @param fireEqualSelection <code>true</code> iff the event must be fired if the selection does not change 
	 * @since 3.0
	 */
	private void queuePostSelectionChanged(final boolean fireEqualSelection) {
		Display display= getDisplay();
		if (display == null)
			return;

		fNumberOfPostSelectionChangedEvents[0]++;
		display.timerExec(getEmptySelectionChangedEventDelay(), new Runnable() {
			final int id= fNumberOfPostSelectionChangedEvents[0];
			public void run() {
				if (id == fNumberOfPostSelectionChangedEvents[0]) {
					// Check again because this is executed after the delay
					if (getDisplay() != null)  {
						Point selection= fTextWidget.getSelectionRange();
						if (selection != null) {
							IRegion r= widgetRange2ModelRange(new Region(selection.x, selection.y));
							if (fireEqualSelection || (r != null && !r.equals(fLastSentPostSelectionChange)) || r == null)  {
								fLastSentPostSelectionChange= r;
								firePostSelectionChanged(selection.x, selection.y);
							}
						}
					}
				}
			}
		});
	}

	/**
	 * Sends out a text selection changed event to all registered post selection changed listeners.
	 *
	 * @param offset the offset of the newly selected range in the visible document
	 * @param length the length of the newly selected range in the visible document
	 * @since 3.0
	 */
	protected void firePostSelectionChanged(int offset, int length) {
		if (redraws()) {
			IRegion r= widgetRange2ModelRange(new Region(offset, length));
			ISelection selection= r != null ? new TextSelection(getDocument(), r.getOffset(), r.getLength()) : TextSelection.emptySelection();
			SelectionChangedEvent event= new SelectionChangedEvent(this, selection);
			firePostSelectionChanged(event);
		}
	}

	/**
	 * Sends out a text selection changed event to all registered listeners and
	 * registers the selection changed event to be sent out to all post selection
	 * listeners.
	 *
	 * @param offset the offset of the newly selected range in the visible document
	 * @param length the length of the newly selected range in the visible document
	 */
	protected void selectionChanged(int offset, int length) {
		queuePostSelectionChanged(true);
		fireSelectionChanged(offset, length);
	}

	/**
	 * Sends out a text selection changed event to all registered listeners.
	 *
	 * @param offset the offset of the newly selected range in the visible document
	 * @param length the length of the newly selected range in the visible document
	 * @since 3.0
	 */
	protected void fireSelectionChanged(int offset, int length) {
		if (redraws()) {
			IRegion r= widgetRange2ModelRange(new Region(offset, length));
			if ((r != null && !r.equals(fLastSentSelectionChange)) || r == null)  {
				fLastSentSelectionChange= r;
				ISelection selection= r != null ? new TextSelection(getDocument(), r.getOffset(), r.getLength()) : TextSelection.emptySelection();
				SelectionChangedEvent event= new SelectionChangedEvent(this, selection);
				fireSelectionChanged(event);
			}
		}
	}

	/**
	 * Sends the given event to all registered post selection changed listeners.
	 *
	 * @param event the selection event
	 * @since 3.0
	 */
	private void firePostSelectionChanged(SelectionChangedEvent event) {
		List listeners= fPostSelectionChangedListeners;
		if (listeners != null) {
			listeners= new ArrayList(listeners);
			for (int i= 0; i < listeners.size(); i++) {
				ISelectionChangedListener l= (ISelectionChangedListener) listeners.get(i);
				l.selectionChanged(event);
			}
		}
	}

	/**
	 * Sends out a mark selection changed event to all registered listeners.
	 *
	 * @param offset the offset of the mark selection in the visible document, the offset is <code>-1</code> if the mark was cleared
	 * @param length the length of the mark selection, may be negative if the caret is before the mark.
	 * @since 2.0
	 */
	protected void markChanged(int offset, int length) {
		if (redraws()) {

			if (offset != -1) {
				IRegion r= widgetRange2ModelRange(new Region(offset, length));
				offset= r.getOffset();
				length= r.getLength();
			}

			ISelection selection= new MarkSelection(getDocument(), offset, length);
			SelectionChangedEvent event= new SelectionChangedEvent(this, selection);
			fireSelectionChanged(event);
		}
	}


	//---- Text listeners

	/*
	 * @see ITextViewer#addTextListener(ITextListener)
	 */
	public void addTextListener(ITextListener listener) {

		Assert.isNotNull(listener);

		if (fTextListeners == null)
			fTextListeners= new ArrayList();

		if (!fTextListeners.contains(listener))
			fTextListeners.add(listener);
	}

	/*
	 * @see ITextViewer#removeTextListener(ITextListener)
	 */
	public void removeTextListener(ITextListener listener) {

		Assert.isNotNull(listener);

		if (fTextListeners != null) {
			fTextListeners.remove(listener);
			if (fTextListeners.size() == 0)
				fTextListeners= null;
		}
	}

	/**
	 * Informs all registered text listeners about the change specified by the
	 * widget command. This method does not use a robust iterator.
	 *
	 * @param cmd the widget command translated into a text event sent to all text listeners
	 */
	protected void updateTextListeners(WidgetCommand cmd) {

		if (fTextListeners != null) {

			DocumentEvent event= cmd.event;
			if (event instanceof SlaveDocumentEvent)
				event= ((SlaveDocumentEvent) event).getMasterEvent();

			TextEvent e= new TextEvent(cmd.start, cmd.length, cmd.text, cmd.preservedText, event, redraws());
			for (int i= 0; i < fTextListeners.size(); i++) {
				ITextListener l= (ITextListener) fTextListeners.get(i);
				l.textChanged(e);
			}
		}
	}

	//---- Text input listeners

	/*
	 * @see ITextViewer#addTextInputListener(ITextInputListener)
	 */
	public void addTextInputListener(ITextInputListener listener) {

		Assert.isNotNull(listener);

		if (fTextInputListeners == null)
			fTextInputListeners= new ArrayList();

		if (!fTextInputListeners.contains(listener))
			fTextInputListeners.add(listener);
	}

	/*
	 * @see ITextViewer#removeTextInputListener(ITextInputListener)
	 */
	public void removeTextInputListener(ITextInputListener listener) {

		Assert.isNotNull(listener);

		if (fTextInputListeners != null) {
			fTextInputListeners.remove(listener);
			if (fTextInputListeners.size() == 0)
				fTextInputListeners= null;
		}
	}

	/**
	 * Informs all registered text input listeners about the forthcoming input change,
	 * This method does not use a robust iterator.
	 *
	 * @param oldInput the old input document
	 * @param newInput the new input document
	 */
	protected void fireInputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (fTextInputListeners != null) {
			for (int i= 0; i < fTextInputListeners.size(); i++) {
				ITextInputListener l= (ITextInputListener) fTextInputListeners.get(i);
				l.inputDocumentAboutToBeChanged(oldInput, newInput);
			}
		}
	}

	/**
	 * Informs all registered text input listeners about the successful input change,
	 * This method does not use a robust iterator.
	 *
	 * @param oldInput the old input document
	 * @param newInput the new input document
	 */
	protected void fireInputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (fTextInputListeners != null) {
			for (int i= 0; i < fTextInputListeners.size(); i++) {
				ITextInputListener l= (ITextInputListener) fTextInputListeners.get(i);
				l.inputDocumentChanged(oldInput, newInput);
			}
		}
	}

	//---- Document

	/*
	 * @see Viewer#getInput()
	 */
	public Object getInput() {
		return getDocument();
	}

	/*
	 * @see ITextViewer#getDocument()
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/*
	 * @see Viewer#setInput(Object)
	 */
	public void setInput(Object input) {

		IDocument document= null;
		if (input instanceof IDocument)
			document= (IDocument) input;

		setDocument(document);
	}

	/*
	 * @see ITextViewer#setDocument(IDocument)
	 */
	public void setDocument(IDocument document) {

		fReplaceTextPresentation= true;
		fireInputDocumentAboutToBeChanged(fDocument, document);

		IDocument oldDocument= fDocument;
		fDocument= document;

		setVisibleDocument(fDocument);

		resetPlugins();
		inputChanged(fDocument, oldDocument);

		fireInputDocumentChanged(oldDocument, fDocument);
		fLastSentSelectionChange= null;
		fReplaceTextPresentation= false;
	}

	/*
	 * @see ITextViewer#setDocument(IDocument, int, int)
	 */
	public void setDocument(IDocument document, int modelRangeOffset, int modelRangeLength) {

		fReplaceTextPresentation= true;
		fireInputDocumentAboutToBeChanged(fDocument, document);

		IDocument oldDocument= fDocument;
		fDocument= document;

		try {

			IDocument slaveDocument= createSlaveDocument(document);
			updateSlaveDocument(slaveDocument, modelRangeOffset, modelRangeLength);
			setVisibleDocument(slaveDocument);

		} catch (BadLocationException x) {
			throw new IllegalArgumentException(JFaceTextMessages.getString("TextViewer.error.invalid_visible_region_1")); //$NON-NLS-1$
		}

		resetPlugins();
		inputChanged(fDocument, oldDocument);

		fireInputDocumentChanged(oldDocument, fDocument);
		fLastSentSelectionChange= null;
		fReplaceTextPresentation= false;
	}

	/**
	 * Creates a slave document for the given document if there is a slave document manager
	 * associated with this viewer.
	 *
	 * @param document the master document
	 * @return the newly created slave document
	 * @since 2.1
	 */
	protected IDocument createSlaveDocument(IDocument document) {
		ISlaveDocumentManager manager= getSlaveDocumentManager();
		if (manager != null) {
			if (manager.isSlaveDocument(document))
				return document;
			return manager.createSlaveDocument(document);
		}
		return document;
	}

	/**
	 * Sets the given slave document to the specified range of its master document.
	 *
	 * @param visibleDocument the slave document
	 * @param visibleRegionOffset the offset of the master document range
	 * @param visibleRegionLength the length of the master document range
	 * @return <code>true</code> if the slave has been adapted successfully
	 * @throws BadLocationException in case the specified range is not valid in the master document
	 * @since 2.1
 	 * @deprecated use <code>updateSlaveDocument</code> instead
	 */
	protected boolean updateVisibleDocument(IDocument visibleDocument, int visibleRegionOffset, int visibleRegionLength) throws BadLocationException {
		if (visibleDocument instanceof ChildDocument) {
			ChildDocument childDocument= (ChildDocument) visibleDocument;

			IDocument document= childDocument.getParentDocument();
			int line= document.getLineOfOffset(visibleRegionOffset);
			int offset= document.getLineOffset(line);
			int length= (visibleRegionOffset - offset) + visibleRegionLength;

			Position parentRange= childDocument.getParentDocumentRange();
			if (offset != parentRange.getOffset() || length != parentRange.getLength()) {
				childDocument.setParentDocumentRange(offset, length);
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates the given slave document to show the specified range of its master document.
	 *
	 * @param slaveDocument the slave document
	 * @param modelRangeOffset the offset of the master document range
	 * @param modelRangeLength the length of the master document range
	 * @return <code>true</code> if the slave has been adapted successfully
	 * @throws BadLocationException in case the specified range is not valid in the master document
	 * @since 3.0
	 */
	protected boolean updateSlaveDocument(IDocument slaveDocument, int modelRangeOffset, int modelRangeLength) throws BadLocationException {
		return updateVisibleDocument(slaveDocument, modelRangeOffset, modelRangeLength);
	}



	//---- View ports

	/**
	 * Initializes all listeners and structures required to set up view port listeners.
	 */
	private void initializeViewportUpdate() {

		if (fViewportGuard != null)
			return;

		if (fTextWidget != null) {

			fViewportGuard= new ViewportGuard();
			fLastTopPixel= -1;

			fTextWidget.addKeyListener(fViewportGuard);
			fTextWidget.addMouseListener(fViewportGuard);

			fScroller= fTextWidget.getVerticalBar();
			if (fScroller != null)
				fScroller.addSelectionListener(fViewportGuard);
		}
	}

	/**
	 * Removes all listeners and structures required to set up view port listeners.
	 */
	private void removeViewPortUpdate() {

		if (fTextWidget != null) {

			fTextWidget.removeKeyListener(fViewportGuard);
			fTextWidget.removeMouseListener(fViewportGuard);

			if (fScroller != null && !fScroller.isDisposed()) {
				fScroller.removeSelectionListener(fViewportGuard);
				fScroller= null;
			}

			fViewportGuard= null;
		}
	}

	/*
	 * @see ITextViewer#addViewportListener(IViewportListener)
	 */
	public void addViewportListener(IViewportListener listener) {

		if (fViewportListeners == null) {
			fViewportListeners= new ArrayList();
			initializeViewportUpdate();
		}

		if (!fViewportListeners.contains(listener))
			fViewportListeners.add(listener);
	}

	/*
	 * @see ITextViewer#removeViewportListener(IVewportListener)
	 */
	public void removeViewportListener(IViewportListener listener) {
		if (fViewportListeners != null)
			fViewportListeners.remove(listener);
	}

	/**
	 * Checks whether the view port changed and if so informs all registered
	 * listeners about the change.
	 *
	 * @param origin describes under which circumstances this method has been called.
	 *
	 * @see IViewportListener
	 */
	protected void updateViewportListeners(int origin) {

		if (redraws()) {
			int topPixel= fTextWidget.getTopPixel();
			if (topPixel >= 0 && topPixel != fLastTopPixel) {
				if (fViewportListeners != null) {
					for (int i= 0; i < fViewportListeners.size(); i++) {
						IViewportListener l= (IViewportListener) fViewportListeners.get(i);
						l.viewportChanged(topPixel);
					}
				}
				fLastTopPixel= topPixel;
			}
		}
	}

	//---- scrolling and revealing

	/*
	 * @see ITextViewer#getTopIndex()
	 */
	public int getTopIndex() {

		if (fTextWidget != null) {
			int top= fTextWidget.getTopIndex();
			return widgetLine2ModelLine(top);
		}

		return -1;
	}

	/*
	 * @see ITextViewer#setTopIndex(int)
	 */
	public void setTopIndex(int index) {

		if (fTextWidget != null) {

			int widgetLine= modelLine2WidgetLine(index);
			if (widgetLine == -1)
				widgetLine= getClosestWidgetLineForModelLine(index);

			if (widgetLine > -1) {
				fTextWidget.setTopIndex(widgetLine);
					updateViewportListeners(INTERNAL);
			}
		}
	}

	/**
	 * Returns the number of lines that can fully fit into the viewport. This is computed by
	 * dividing the widget's client area height by the widget's line height. The result is only
	 * accurate if the widget does not use variable line heights - for that reason, clients should not use this
	 * method any longer and either use XXX add replacements
	 * 
	 * @return the view port height in lines
	 * @deprecated as of 3.2 XXX add replacements
	 */
	protected int getVisibleLinesInViewport() {
		if (fTextWidget != null) {
			Rectangle clArea= fTextWidget.getClientArea();
			if (!clArea.isEmpty())
				return clArea.height / fTextWidget.getLineHeight();
		}
		return -1;
	}

	/*
	 * @see ITextViewer#getBottomIndex()
	 */
	public int getBottomIndex() {

		if (fTextWidget == null)
			return -1;
		
		int widgetBottom= MigrationHelper.getBottomIndex(fTextWidget);
		return widgetLine2ModelLine(widgetBottom);
	}

	/*
	 * @see ITextViewer#getTopIndexStartOffset()
	 */
	public int getTopIndexStartOffset() {

		if (fTextWidget != null) {
			int top= fTextWidget.getTopIndex();
			try {
				top= getVisibleDocument().getLineOffset(top);
				return widgetLine2ModelLine(top);
			} catch (BadLocationException ex) {
				if (TRACE_ERRORS)
					System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.getTopIndexStartOffset")); //$NON-NLS-1$
			}
		}

		return -1;
	}

	/*
	 * @see ITextViewer#getBottomIndexEndOffset()
	 */
	public int getBottomIndexEndOffset() {
		try {

			IRegion line= getDocument().getLineInformation(getBottomIndex());
			int bottomEndOffset= line.getOffset() + line.getLength() - 1;

			IRegion coverage= getModelCoverage();
			if (coverage == null)
				return -1;

			int coverageEndOffset=  coverage.getOffset() + coverage.getLength() - 1;
			return Math.min(coverageEndOffset, bottomEndOffset);

		} catch (BadLocationException ex) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.getBottomIndexEndOffset")); //$NON-NLS-1$
			return getDocument().getLength() - 1;
		}
	}

	/*
	 * @see ITextViewer#revealRange(int, int)
	 */
	public void revealRange(int start, int length) {

		if (fTextWidget == null || !redraws())
			return;

		IRegion modelRange= new Region(start, length);
		IRegion widgetRange= modelRange2ClosestWidgetRange(modelRange);
		if (widgetRange != null) {

			int[] range= new int[] { widgetRange.getOffset(), widgetRange.getLength() };
			validateSelectionRange(range);
			if (range[0] >= 0)
				internalRevealRange(range[0], range[0] + range[1]);

		} else {

			IRegion coverage= getModelCoverage();
			int cursor= (coverage == null || start < coverage.getOffset()) ? 0 : getVisibleDocument().getLength();
			internalRevealRange(cursor, cursor);
		}
	}

	/**
	 * Reveals the given range of the visible document.
	 *
	 * @param start the start offset of the range
	 * @param end the end offset of the range
	 */
	protected void internalRevealRange(int start, int end) {

		try {

			IDocument doc= getVisibleDocument();

			int startLine= doc.getLineOfOffset(start);
			int endLine= doc.getLineOfOffset(end);

			int top= fTextWidget.getTopIndex();
			if (top > -1) {

				// scroll vertically
				int bottom= MigrationHelper.getBottomIndex(fTextWidget);
				int lines= bottom - top;
				
				// if the widget is not scrollable as it is displaying the entire content
				// setTopIndex won't have any effect.

				// two lines at the top and the bottom should always be left
				// if window is smaller than 5 lines, always center position is chosen
				int bufferZone= 2;

				if (startLine >= top + bufferZone
						&& startLine <= bottom - bufferZone
						&& endLine >= top + bufferZone
						&& endLine <= bottom - bufferZone) {

					// do not scroll at all as it is already visible

				} else {

					int delta= Math.max(0, lines - (endLine - startLine));
					fTextWidget.setTopIndex(startLine - delta/3);
					updateViewportListeners(INTERNAL);
				}

				// scroll horizontally

				if (endLine < startLine) {
					endLine += startLine;
					startLine= endLine - startLine;
					endLine -= startLine;
				}

				int startPixel= -1;
				int endPixel= -1;

				if (endLine > startLine) {
					// reveal the beginning of the range in the start line
					IRegion extent= getExtent(start, start);
					startPixel= extent.getOffset() + fTextWidget.getHorizontalPixel();
					endPixel= startPixel;

				} else {
					IRegion extent= getExtent(start, end);
					startPixel= extent.getOffset() + fTextWidget.getHorizontalPixel();
					endPixel= startPixel + extent.getLength();
				}

				int visibleStart= fTextWidget.getHorizontalPixel();
				int visibleEnd= visibleStart + fTextWidget.getClientArea().width;

				// scroll only if not yet visible
				if (startPixel < visibleStart || visibleEnd < endPixel) {

					// set buffer zone to 10 pixels
					bufferZone= 10;

					int newOffset= visibleStart;

					int visibleWidth= visibleEnd - visibleStart;
					int selectionPixelWidth= endPixel - startPixel;

					if (startPixel < visibleStart)
						newOffset= startPixel;
					else if (selectionPixelWidth  + bufferZone < visibleWidth)
						newOffset= endPixel + bufferZone - visibleWidth;
					else
						newOffset= startPixel;

					float index= ((float)newOffset) / ((float)getAverageCharWidth());

					fTextWidget.setHorizontalIndex(Math.round(index));
				}

			}
		} catch (BadLocationException e) {
			throw new IllegalArgumentException(JFaceTextMessages.getString("TextViewer.error.invalid_range")); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the width of the text when being drawn into this viewer's widget.
	 *
	 * @param text the string to measure
	 * @return the width of the presentation of the given string
	 * @deprecated use <code>getWidthInPixels(int, int)</code> instead
	 */
	final protected int getWidthInPixels(String text) {
		GC gc= new GC(fTextWidget);
		gc.setFont(fTextWidget.getFont());
		Point extent= gc.textExtent(text);
		gc.dispose();
		return extent.x;
	}

	/**
	 * Returns the region covered by the given start and end offset.
	 * The result is relative to the upper left corner of the widget
	 * client area.
	 *
	 * @param start offset relative to the start of this viewer's view port
	 * 	0 <= offset <= getCharCount()
 	 * @param end offset relative to the start of this viewer's view port
	 * 	0 <= offset <= getCharCount()
	 * @return the region covered by start and end offset
	 */
	final protected IRegion getExtent(int start, int end) {
		if (end > 0 && start < end) {
			Rectangle bounds= fTextWidget.getTextBounds(start, end - 1);
			return new Region(bounds.x, bounds.width);
		}
		
		return new Region(fTextWidget.getLocationAtOffset(start).x, 0);
	}

	/**
	 * Returns the width of the representation of a text range in the
	 * visible region of the viewer's document as drawn in this viewer's
	 * widget.
	 *
	 * @param offset the offset of the text range in the visible region
	 * @param length the length of the text range in the visible region
	 * @return the width of the presentation of the specified text range
	 * @since 2.0
	 */
	final protected int getWidthInPixels(int offset, int length) {
		return getExtent(offset, offset + length).getLength();
	}

	/**
	 * Returns the average character width of this viewer's widget.
	 *
	 * @return the average character width of this viewer's widget
	 */
	final protected int getAverageCharWidth() {
		GC gc= new GC(fTextWidget);
		gc.setFont(fTextWidget.getFont());
		int increment= gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();
		return increment;
	}

	/*
	 * @see Viewer#refresh()
	 */
	public void refresh() {
		setDocument(getDocument());
	}

	//---- visible range support

	/**
	 * Returns the slave document manager
	 *
	 * @return the slave document manager
	 * @since 2.1
	 */
	protected ISlaveDocumentManager getSlaveDocumentManager() {
		if (fSlaveDocumentManager == null)
			fSlaveDocumentManager= createSlaveDocumentManager();
		return fSlaveDocumentManager;
	}

	/**
	 * Creates a new slave document manager. This implementation always
	 * returns a <code>ChildDocumentManager</code>.
	 *
	 * @return ISlaveDocumentManager
	 * @since 2.1
	 */
	protected ISlaveDocumentManager createSlaveDocumentManager() {
		return new ChildDocumentManager();
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewer#invalidateTextPresentation()
	 */
	public final void invalidateTextPresentation() {
		if (fVisibleDocument != null) {
			fWidgetCommand.event= null;
			fWidgetCommand.start= 0;
			fWidgetCommand.length= fVisibleDocument.getLength();
			fWidgetCommand.text= fVisibleDocument.get();
			updateTextListeners(fWidgetCommand);
		}
	}

	/**
	 * Invalidates the given range of the text presentation.
	 *
	 * @param offset the offset of the range to be invalidated
	 * @param length the length of the range to be invalidated
	 * @since 2.1
	 */
	public final void invalidateTextPresentation(int offset, int length) {
		if (fVisibleDocument != null) {

			IRegion widgetRange= modelRange2WidgetRange(new Region(offset, length));
			if (widgetRange != null) {

				fWidgetCommand.event= null;
				fWidgetCommand.start= widgetRange.getOffset();
				fWidgetCommand.length= widgetRange.getLength();

				try {
					fWidgetCommand.text= fVisibleDocument.get(widgetRange.getOffset(), widgetRange.getLength());
					updateTextListeners(fWidgetCommand);
				} catch (BadLocationException x) {
					// can not happen because of previous checking
				}
			}
		}
	}

	/**
	 * Initializes the text widget with the visual document and
	 * invalidates the overall presentation.
	 */
	private void initializeWidgetContents() {

		if (fTextWidget != null && fVisibleDocument != null) {

			// set widget content
			if (fDocumentAdapter == null)
				fDocumentAdapter= createDocumentAdapter();

			fDocumentAdapter.setDocument(fVisibleDocument);
			fTextWidget.setContent(fDocumentAdapter);

			// invalidate presentation
			invalidateTextPresentation();
		}
	}

	/**
	 * Frees the given document if it is a slave document.
	 *
	 * @param slave the potential slave document
	 * @since 3.0
	 */
	protected void freeSlaveDocument(IDocument slave) {
		ISlaveDocumentManager manager= getSlaveDocumentManager();
		if (manager != null && manager.isSlaveDocument(slave))
			manager.freeSlaveDocument(slave);
	}

	/**
	 * Sets this viewer's visible document. The visible document represents the
	 * visible region of the viewer's input document.
	 *
	 * @param document the visible document
	 */
	protected void setVisibleDocument(IDocument document) {

		if (fVisibleDocument == document && fVisibleDocument instanceof ChildDocument) {
			// optimization for new child documents
			return;
		}

		if (fVisibleDocument != null) {
			if (fVisibleDocumentListener != null)
				fVisibleDocument.removeDocumentListener(fVisibleDocumentListener);
			if (fVisibleDocument != document)
				freeSlaveDocument(fVisibleDocument);
		}

		fVisibleDocument= document;
		initializeDocumentInformationMapping(fVisibleDocument);

		initializeWidgetContents();

		fFindReplaceDocumentAdapter= null;
		if (fVisibleDocument != null && fVisibleDocumentListener != null)
			fVisibleDocument.addDocumentListener(fVisibleDocumentListener);
	}

	/**
	 * Hook method called when the visible document is about to be changed.
	 * <p>
	 * Subclasses may override.
	 *
	 * @param event the document event
	 * @since 3.0
	 */
	protected void handleVisibleDocumentAboutToBeChanged(DocumentEvent event) {
	}

	/**
	 * Hook method called when the visible document has been changed.
	 * <p>
	 * Subclasses may override.
	 *
	 * @param event the document event
	 * @since 3.0
	 */
	protected void handleVisibleDocumentChanged(DocumentEvent event) {
	}

	/**
	 * Initializes the document information mapping between the given slave document and
	 * its master document.
	 *
	 * @param visibleDocument the slave document
	 * @since 2.1
	 */
	protected void initializeDocumentInformationMapping(IDocument visibleDocument) {
		ISlaveDocumentManager manager= getSlaveDocumentManager();
		fInformationMapping= manager == null ? null : manager.createMasterSlaveMapping(visibleDocument);
	}

	/**
	 * Returns the viewer's visible document.
	 *
	 * @return the viewer's visible document
	 */
	protected IDocument getVisibleDocument() {
		return fVisibleDocument;
	}

	/**
	 * Returns the offset of the visible region.
	 *
	 * @return the offset of the visible region
	 */
	protected int _getVisibleRegionOffset() {

		IDocument document= getVisibleDocument();
		if (document instanceof ChildDocument) {
			ChildDocument cdoc= (ChildDocument) document;
			return cdoc.getParentDocumentRange().getOffset();
		}

		return 0;
	}

	/*
	 * @see ITextViewer#getVisibleRegion()
	 */
	public IRegion getVisibleRegion() {

		IDocument document= getVisibleDocument();
		if (document instanceof ChildDocument) {
			Position p= ((ChildDocument) document).getParentDocumentRange();
			return new Region(p.getOffset(), p.getLength());
		}

		return new Region(0, document == null ? 0 : document.getLength());
	}

	/*
	 * @see ITextViewer#overlapsWithVisibleRegion(int, int)
	 */
	public boolean overlapsWithVisibleRegion(int start, int length) {
		IDocument document= getVisibleDocument();
		if (document instanceof ChildDocument) {
			ChildDocument cdoc= (ChildDocument) document;
			return cdoc.getParentDocumentRange().overlapsWith(start, length);
		} else if (document != null) {
			int size= document.getLength();
			return (start >= 0 && length >= 0 && start + length <= size);
		}
		return false;
	}

	/*
	 * @see ITextViewer#setVisibleRegion(int, int)
	 */
	public void setVisibleRegion(int start, int length) {

		IRegion region= getVisibleRegion();
		if (start == region.getOffset() && length == region.getLength()) {
			// nothing to change
			return;
		}

		setRedraw(false);
		try {

			IDocument slaveDocument= createSlaveDocument(getVisibleDocument());
			if (updateSlaveDocument(slaveDocument, start, length))
				setVisibleDocument(slaveDocument);

		} catch (BadLocationException x) {
			throw new IllegalArgumentException(JFaceTextMessages.getString("TextViewer.error.invalid_visible_region_2")); //$NON-NLS-1$
		} finally {
			setRedraw(true);
		}
	}

	/*
	 * @see ITextViewer#resetVisibleRegion()
	 */
	public void resetVisibleRegion() {
		ISlaveDocumentManager manager= getSlaveDocumentManager();
		if (manager != null) {
			IDocument slave= getVisibleDocument();
			IDocument master= manager.getMasterDocument(slave);
			if (master != null) {
				setVisibleDocument(master);
				manager.freeSlaveDocument(slave);
			}
		}
	}


	//--------------------------------------

	/*
	 * @see ITextViewer#setTextDoubleClickStrategy(ITextDoubleClickStrategy, String)
	 */
	public void setTextDoubleClickStrategy(ITextDoubleClickStrategy strategy, String contentType) {

		if (strategy != null) {
			if (fDoubleClickStrategies == null)
				fDoubleClickStrategies= new HashMap();
			fDoubleClickStrategies.put(contentType, strategy);
		} else if (fDoubleClickStrategies != null)
			fDoubleClickStrategies.remove(contentType);
	}

	/**
	 * Selects from the given map the one which is registered under
	 * the content type of the partition in which the given offset is located.
	 *
	 * @param plugins the map from which to choose
	 * @param offset the offset for which to find the plug-in
	 * @return the plug-in registered under the offset's content type
	 */
	protected Object selectContentTypePlugin(int offset, Map plugins) {
		try {
			return selectContentTypePlugin(TextUtilities.getContentType(getDocument(), getDocumentPartitioning(), offset, true), plugins);
		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.selectContentTypePlugin")); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Selects from the given <code>plug-ins</code> this one which is
	 * registered for the given content <code>type</code>.
	 *
	 * @param type the type to be used as lookup key
	 * @param plugins the table to be searched
	 * @return the plug-in in the map for the given content type
	 */
	private Object selectContentTypePlugin(String type, Map plugins) {

		if (plugins == null)
			return null;

		return plugins.get(type);
	}

	/**
	 * Hook called on receipt of a <code>VerifyEvent</code>. The event has
	 * been translated into a <code>DocumentCommand</code> which can now be
	 * manipulated by interested parties. By default, the hook forwards the command
	 * to the installed instances of <code>IAutoEditStrategy</code>.
	 *
	 * @param command the document command representing the verify event
	 */
	protected void customizeDocumentCommand(DocumentCommand command) {
		if (isIgnoringAutoEditStrategies())
			return;

		List strategies= (List) selectContentTypePlugin(command.offset, fAutoIndentStrategies);
		if (strategies == null)
			return;

		switch (strategies.size()) {
		// optimization
		case 0:
			break;

		case 1:
			((IAutoEditStrategy) strategies.iterator().next()).customizeDocumentCommand(getDocument(), command);
			break;

		// make iterator robust against adding/removing strategies from within strategies
		default:
			strategies= new ArrayList(strategies);

			IDocument document= getDocument();
			for (final Iterator iterator= strategies.iterator(); iterator.hasNext(); )
				((IAutoEditStrategy) iterator.next()).customizeDocumentCommand(document, command);

			break;
		}
	}

	/**
	 * Handles the verify event issued by the viewer's text widget.
	 *
	 * @see VerifyListener#verifyText(VerifyEvent)
	 * @param e the verify event
	 */
	protected void handleVerifyEvent(VerifyEvent e) {

		if (fEventConsumer != null) {
			fEventConsumer.processEvent(e);
			if (!e.doit)
				return;
		}

		IRegion modelRange= event2ModelRange(e);
		fDocumentCommand.setEvent(e, modelRange);
		customizeDocumentCommand(fDocumentCommand);
		if (!fDocumentCommand.fillEvent(e, modelRange)) {

			boolean compoundChange= fDocumentCommand.getCommandCount() > 1;
			try {

				fVerifyListener.forward(false);

				if (compoundChange && fUndoManager != null)
					fUndoManager.beginCompoundChange();

				if (fSlaveDocumentManager != null) {
					IDocument visible= getVisibleDocument();
					try {
						fSlaveDocumentManager.setAutoExpandMode(visible, true);
						fDocumentCommand.execute(getDocument());
					} finally {
						fSlaveDocumentManager.setAutoExpandMode(visible, false);
					}
				} else {
					fDocumentCommand.execute(getDocument());
				}

				if (fTextWidget != null) {
					int documentCaret= fDocumentCommand.caretOffset;
					if (documentCaret == -1) {
						// old behavior of document command
						documentCaret= fDocumentCommand.offset + (fDocumentCommand.text == null ? 0 : fDocumentCommand.text.length());
					}

					int widgetCaret= modelOffset2WidgetOffset(documentCaret);
					if (widgetCaret == -1) {
						// try to move it to the closest spot
						IRegion region= getModelCoverage();
						if (region != null) {
							if (documentCaret <= region.getOffset())
								widgetCaret= 0;
							else if (documentCaret >= region.getOffset() + region.getLength())
								widgetCaret= getVisibleRegion().getLength();
						}
					}

					if (widgetCaret != -1) {
						// there is a valid widget caret
						fTextWidget.setCaretOffset(widgetCaret);
					}

					fTextWidget.showSelection();
				}
			} catch (BadLocationException x) {

				if (TRACE_ERRORS)
					System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.verifyText")); //$NON-NLS-1$

			} finally {

				if (compoundChange && fUndoManager != null)
					fUndoManager.endCompoundChange();

				fVerifyListener.forward(true);

			}
		}
	}

	//---- text manipulation

	/**
	 * Returns whether the marked region of this viewer is empty.
	 *
	 * @return <code>true</code> if the marked region of this viewer is empty, otherwise <code>false</code>
	 * @since 2.0
	 */
	private boolean isMarkedRegionEmpty() {
		return
			fTextWidget == null ||
			fMarkPosition == null ||
			fMarkPosition.isDeleted() ||
			modelRange2WidgetRange(fMarkPosition) == null;
	}

	/*
	 * @see ITextViewer#canDoOperation(int)
	 */
	public boolean canDoOperation(int operation) {

		if (fTextWidget == null || !redraws())
			return false;

		switch (operation) {
			case CUT:
				return isEditable() &&(fTextWidget.getSelectionCount() > 0 || !isMarkedRegionEmpty());
			case COPY:
				return fTextWidget.getSelectionCount() > 0 || !isMarkedRegionEmpty();
			case DELETE:
			case PASTE:
				return isEditable();
			case SELECT_ALL:
				return true;
			case SHIFT_LEFT:
				return isEditable() && fIndentChars != null;
			case SHIFT_RIGHT:
				return isEditable() && fIndentChars != null && areMultipleLinesSelected();
			case PREFIX:
			case STRIP_PREFIX:
				return isEditable() && fDefaultPrefixChars != null;
			case UNDO:
				return fUndoManager != null && fUndoManager.undoable();
			case REDO:
				return fUndoManager != null && fUndoManager.redoable();
			case PRINT:
				return isPrintable();
		}

		return false;
	}

	/*
	 * @see ITextViewer#doOperation(int)
	 */
	public void doOperation(int operation) {

		if (fTextWidget == null || !redraws())
			return;

		Point selection= null;

		switch (operation) {

			case UNDO:
				if (fUndoManager != null) {
					ignoreAutoEditStrategies(true);
					fUndoManager.undo();
					ignoreAutoEditStrategies(false);
				}
				break;
			case REDO:
				if (fUndoManager != null) {
					ignoreAutoEditStrategies(true);
					fUndoManager.redo();
					ignoreAutoEditStrategies(false);
				}
				break;
			case CUT:
				if (fTextWidget.getSelectionCount() == 0)
					copyMarkedRegion(true);
				else
					fTextWidget.cut();

				selection= fTextWidget.getSelectionRange();
				fireSelectionChanged(selection.x, selection.y);

				break;
			case COPY:
				if (fTextWidget.getSelectionCount() == 0)
					copyMarkedRegion(false);
				else
					fTextWidget.copy();
				break;
			case PASTE:
//				ignoreAutoEditStrategies(true);
				fTextWidget.paste();
				selection= fTextWidget.getSelectionRange();
				fireSelectionChanged(selection.x, selection.y);
//				ignoreAutoEditStrategies(false);
				break;
			case DELETE:
				fTextWidget.invokeAction(ST.DELETE_NEXT);
				selection= fTextWidget.getSelectionRange();
				fireSelectionChanged(selection.x, selection.y);
				break;
			case SELECT_ALL: {
				if (getDocument() != null)
					setSelectedRange(0, getDocument().getLength());
				break;
			}
			case SHIFT_RIGHT:
				shift(false, true, false);
				break;
			case SHIFT_LEFT:
				shift(false, false, false);
				break;
			case PREFIX:
				shift(true, true, true);
				break;
			case STRIP_PREFIX:
				shift(true, false, true);
				break;
			case PRINT:
				print();
				break;
		}
	}

	/**
	 * Tells this viewer whether the registered auto edit strategies should be ignored.
	 *
	 * @param ignore <code>true</code> if the strategies should be ignored.
	 * @since 2.1
	 */
	protected void ignoreAutoEditStrategies(boolean ignore) {
		if (fIgnoreAutoIndent == ignore)
			return;

		fIgnoreAutoIndent= ignore;

		IDocument document= getDocument();
		if (document instanceof IDocumentExtension2) {
			IDocumentExtension2 extension= (IDocumentExtension2) document;
			if (ignore)
				extension.ignorePostNotificationReplaces();
			else
				extension.acceptPostNotificationReplaces();
		}
	}

	/**
	 * Returns whether this viewer ignores the registered auto edit strategies.
	 *
	 * @return <code>true</code> if the strategies are ignored
	 * @since 2.1
	 */
	protected boolean isIgnoringAutoEditStrategies() {
		return fIgnoreAutoIndent;
	}

	/*
	 * @see ITextOperationTargetExtension#enableOperation(int, boolean)
	 * @since 2.0
	 */
	public void enableOperation(int operation, boolean enable) {
		/*
		 * No-op by default.
		 * Will be changed to regularly disable the known operations.
		 */
	}

	/**
	 * Copies/cuts the marked region.
	 *
	 * @param delete <code>true</code> if the region should be deleted rather than copied.
	 * @since 2.0
	 */
	protected void copyMarkedRegion(boolean delete) {

		if (fTextWidget == null)
			return;

		if (fMarkPosition == null || fMarkPosition.isDeleted() || modelRange2WidgetRange(fMarkPosition) == null)
			return;

		int widgetMarkOffset= modelOffset2WidgetOffset(fMarkPosition.offset);
		Point selection= fTextWidget.getSelection();
		if (selection.x <= widgetMarkOffset)
			fTextWidget.setSelection(selection.x, widgetMarkOffset);
		else
			fTextWidget.setSelection(widgetMarkOffset, selection.x);

		if (delete) {
			fTextWidget.cut();
		} else {
			fTextWidget.copy();
			fTextWidget.setSelection(selection.x); // restore old cursor position
		}
	}

	/**
	 * Deletes the current selection. If the selection has the length 0
	 * the selection is automatically extended to the right - either by 1
	 * or by the length of line delimiter if at the end of a line.
	 *
	 * @deprecated use <code>StyledText.invokeAction</code> instead
	 */
	protected void deleteText() {
		fTextWidget.invokeAction(ST.DELETE_NEXT);
	}

	/**
	 * A block is selected if the character preceding the start of the
	 * selection is a new line character.
	 *
	 * @return <code>true</code> if a block is selected
	 */
	protected boolean isBlockSelected() {

		Point s= getSelectedRange();
		if (s.y == 0)
			return false;

		try {

			IDocument document= getDocument();
			int line= document.getLineOfOffset(s.x);
			int start= document.getLineOffset(line);
			return (s.x == start);

		} catch (BadLocationException x) {
		}

		return false;
	}

	/**
	 * Returns <code>true</code> if one line is completely selected or if multiple lines are selected.
	 * Being completely selected means that all characters except the new line characters are
	 * selected.
	 *
	 * @return <code>true</code> if one or multiple lines are selected
	 * @since 2.0
	 */
	protected boolean areMultipleLinesSelected() {
		Point s= getSelectedRange();
		if (s.y == 0)
			return false;

		try {

			IDocument document= getDocument();
			int startLine= document.getLineOfOffset(s.x);
			int endLine= document.getLineOfOffset(s.x + s.y);
			IRegion line= document.getLineInformation(startLine);
			return startLine != endLine || (s.x == line.getOffset() && s.y == line.getLength());

		} catch (BadLocationException x) {
		}

		return false;
	}

	/**
	 * Returns the index of the first line whose start offset is in the given text range.
	 *
	 * @param region the text range in characters where to find the line
	 * @return the first line whose start index is in the given range, -1 if there is no such line
	 */
	private int getFirstCompleteLineOfRegion(IRegion region) {

		try {

			IDocument d= getDocument();

			int startLine= d.getLineOfOffset(region.getOffset());

			int offset= d.getLineOffset(startLine);
			if (offset >= region.getOffset())
				return startLine;

			offset= d.getLineOffset(startLine + 1);
			return (offset > region.getOffset() + region.getLength() ? -1 : startLine + 1);

		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.getFirstCompleteLineOfRegion")); //$NON-NLS-1$
		}

		return -1;
	}


	/**
	 * Creates a region describing the text block (something that starts at
	 * the beginning of a line) completely containing the current selection.
	 *
	 * @param selection the selection to use
	 * @return the region describing the text block comprising the given selection
	 * @since 2.0
	 */
	private IRegion getTextBlockFromSelection(Point selection) {

		try {
			IDocument document= getDocument();
			IRegion line= document.getLineInformationOfOffset(selection.x);
			int length= selection.y == 0 ? line.getLength() : selection.y + (selection.x - line.getOffset());
			return new Region(line.getOffset(), length);

		} catch (BadLocationException x) {
		}

		return null;
	}

	/**
	 * Shifts a text block to the right or left using the specified set of prefix characters.
	 * The prefixes must start at the beginning of the line.
	 *
	 * @param useDefaultPrefixes says whether the configured default or indent prefixes should be used
	 * @param right says whether to shift to the right or the left
	 *
	 * @deprecated use shift(boolean, boolean, boolean) instead
	 */
	protected void shift(boolean useDefaultPrefixes, boolean right) {
		shift(useDefaultPrefixes, right, false);
	}

	/**
	 * Shifts a text block to the right or left using the specified set of prefix characters.
	 * If white space should be ignored the prefix characters must not be at the beginning of
	 * the line when shifting to the left. There may be whitespace in front of the prefixes.
	 *
	 * @param useDefaultPrefixes says whether the configured default or indent prefixes should be used
	 * @param right says whether to shift to the right or the left
	 * @param ignoreWhitespace says whether whitespace in front of prefixes is allowed
	 * @since 2.0
	 */
	protected void shift(boolean useDefaultPrefixes, boolean right, boolean ignoreWhitespace) {

		if (fUndoManager != null)
			fUndoManager.beginCompoundChange();

		setRedraw(false);
		startSequentialRewriteMode(true);

		IDocument d= getDocument();
		Map partitioners= null;

		try {

			Point selection= getSelectedRange();
			IRegion block= getTextBlockFromSelection(selection);
			ITypedRegion[] regions= TextUtilities.computePartitioning(d, getDocumentPartitioning(), block.getOffset(), block.getLength(), false);

			int lineCount= 0;
			int[] lines= new int[regions.length * 2]; // [start line, end line, start line, end line, ...]
			for (int i= 0, j= 0; i < regions.length; i++, j+= 2) {
				// start line of region
				lines[j]= getFirstCompleteLineOfRegion(regions[i]);
				// end line of region
				int length= regions[i].getLength();
				int offset= regions[i].getOffset() + length;
				if (length > 0)
					offset--;
				lines[j + 1]= (lines[j] == -1 ? -1 : d.getLineOfOffset(offset));
				lineCount += lines[j + 1] - lines[j] + 1;
			}

			if (lineCount >= 20)
				partitioners= TextUtilities.removeDocumentPartitioners(d);

			// Remember the selection range.
			IPositionUpdater positionUpdater= new ShiftPositionUpdater(SHIFTING);
			Position rememberedSelection= new Position(selection.x, selection.y);
			d.addPositionCategory(SHIFTING);
			d.addPositionUpdater(positionUpdater);
			try {
				d.addPosition(SHIFTING, rememberedSelection);
			} catch (BadPositionCategoryException ex) {
				// should not happen
			}

			// Perform the shift operation.
			Map map= (useDefaultPrefixes ? fDefaultPrefixChars : fIndentChars);
			for (int i= 0, j= 0; i < regions.length; i++, j += 2) {
				String[] prefixes= (String[]) selectContentTypePlugin(regions[i].getType(), map);
				if (prefixes != null && prefixes.length > 0 && lines[j] >= 0 && lines[j + 1] >= 0) {
					if (right)
						shiftRight(lines[j], lines[j + 1], prefixes[0]);
					else
						shiftLeft(lines[j], lines[j + 1], prefixes, ignoreWhitespace);
				}
			}

			// Restore the selection.
			setSelectedRange(rememberedSelection.getOffset(), rememberedSelection.getLength());

			try {
				d.removePositionUpdater(positionUpdater);
				d.removePositionCategory(SHIFTING);
			} catch (BadPositionCategoryException ex) {
				// should not happen
			}

		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.shift_1")); //$NON-NLS-1$

		} finally {

			if (partitioners != null)
				TextUtilities.addDocumentPartitioners(d, partitioners);

			stopSequentialRewriteMode();
			setRedraw(true);

			if (fUndoManager != null)
				fUndoManager.endCompoundChange();
		}
	}

	/**
	 * Shifts the specified lines to the right inserting the given prefix
	 * at the beginning of each line
	 *
	 * @param prefix the prefix to be inserted
	 * @param startLine the first line to shift
	 * @param endLine the last line to shift
	 * @since 2.0
	 */
	private void shiftRight(int startLine, int endLine, String prefix) {

		try {

			IDocument d= getDocument();
			while (startLine <= endLine) {
				d.replace(d.getLineOffset(startLine++), 0, prefix);
			}

		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println("TextViewer.shiftRight: BadLocationException"); //$NON-NLS-1$
		}
	}

	/**
	 * Shifts the specified lines to the right or to the left. On shifting to the right
	 * it insert <code>prefixes[0]</code> at the beginning of each line. On shifting to the
	 * left it tests whether each of the specified lines starts with one of the specified
	 * prefixes and if so, removes the prefix.
	 *
	 * @param startLine the first line to shift
	 * @param endLine the last line to shift
	 * @param prefixes the prefixes to be used for shifting
	 * @param ignoreWhitespace <code>true</code> if whitespace should be ignored, <code>false</code> otherwise
	 * @since 2.0
	 */
	private void shiftLeft(int startLine, int endLine, String[] prefixes, boolean ignoreWhitespace) {

		IDocument d= getDocument();

		try {

			IRegion[] occurrences= new IRegion[endLine - startLine + 1];

			// find all the first occurrences of prefix in the given lines
			for (int i= 0; i < occurrences.length; i++) {

				IRegion line= d.getLineInformation(startLine + i);
				String text= d.get(line.getOffset(), line.getLength());

				int index= -1;
				int[] found= TextUtilities.indexOf(prefixes, text, 0);
				if (found[0] != -1) {
					if (ignoreWhitespace) {
						String s= d.get(line.getOffset(), found[0]);
						s= s.trim();
						if (s.length() == 0)
							index= line.getOffset() + found[0];
					} else if (found[0] == 0)
						index= line.getOffset();
				}

				if (index > -1) {
					// remember where prefix is in line, so that it can be removed
					int length= prefixes[found[1]].length();
					if (length == 0 && !ignoreWhitespace && line.getLength() > 0) {
						// found a non-empty line which cannot be shifted
						return;
					}
					occurrences[i]= new Region(index, length);
				} else {
					// found a line which cannot be shifted
					return;
				}
			}

			// OK - change the document
			int decrement= 0;
			for (int i= 0; i < occurrences.length; i++) {
				IRegion r= occurrences[i];
				d.replace(r.getOffset() - decrement, r.getLength(), ""); //$NON-NLS-1$
				decrement += r.getLength();
			}

		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println("TextViewer.shiftLeft: BadLocationException"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns whether the shown text can be printed.
	 *
	 * @return the viewer's printable mode
	 */
	protected boolean isPrintable() {
		/*
		 * 1GK7Q10: ITPUI:WIN98 - internal error after invoking print at editor view
		 * Changed from returning true to testing the length of the printer queue
		 */
		PrinterData[] printerList= Printer.getPrinterList();
		return (printerList != null && printerList.length > 0);
	}

	/**
	 * Brings up a print dialog and calls <code>printContents(Printer)</code> which
	 * performs the actual print.
	 */
	protected void print() {

		final PrintDialog dialog= new PrintDialog(fTextWidget.getShell(), SWT.PRIMARY_MODAL);
		final PrinterData data= dialog.open();

		if (data != null) {

			final Printer printer= new Printer(data);
			final Runnable styledTextPrinter= fTextWidget.print(printer);

			Thread printingThread= new Thread("Printing") { //$NON-NLS-1$
				public void run() {
					styledTextPrinter.run();
					printer.dispose();
				}
			};
			printingThread.start();
		}
    }


	//------ find support

	/**
	 * Adheres to the contract of {@link IFindReplaceTarget#canPerformFind()}.
	 *
	 * @return <code>true</code> if find can be performed, <code>false</code> otherwise
	 */
	protected boolean canPerformFind() {
		IDocument d= getVisibleDocument();
		return (fTextWidget != null && d != null && d.getLength() > 0);
	}

	/**
	 * Adheres to the contract of {@link IFindReplaceTarget#findAndSelect(int, String, boolean, boolean, boolean)}.
	 *
	 * @param startPosition the start position
	 * @param findString the find string specification
	 * @param forwardSearch the search direction
	 * @param caseSensitive <code>true</code> if case sensitive, <code>false</code> otherwise
	 * @param wholeWord <code>true</code> if match must be whole words, <code>false</code> otherwise
	 * @return the model offset of the first match
	 * @deprecated as of 3.0 use {@link #findAndSelect(int, String, boolean, boolean, boolean, boolean)}
	 */
	protected int findAndSelect(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord) {
		try {
			return findAndSelect(startPosition, findString, forwardSearch, caseSensitive, wholeWord, false);
		} catch (IllegalStateException ex) {
			return -1;
		} catch (PatternSyntaxException ex) {
			return -1;
		}
	}

	/**
	 * Adheres to the contract of
	 * {@link org.eclipse.jface.text.IFindReplaceTargetExtension3#findAndSelect(int, String, boolean, boolean, boolean, boolean)}.
	 *
	 * @param startPosition the start position
	 * @param findString the find string specification
	 * @param forwardSearch the search direction
	 * @param caseSensitive <code>true</code> if case sensitive, <code>false</code> otherwise
	 * @param wholeWord <code>true</code> if matches must be whole words, <code>false</code> otherwise
	 * @param regExSearch <code>true</code> if <code>findString</code> is a regular expression, <code>false</code> otherwise
	 * @return the model offset of the first match
	 *
	 */
	protected int findAndSelect(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		if (fTextWidget == null)
			return -1;

		try {

			int widgetOffset= (startPosition == -1 ? startPosition : modelOffset2WidgetOffset(startPosition));
			FindReplaceDocumentAdapter adapter= getFindReplaceDocumentAdapter();
			IRegion matchRegion= adapter.find(widgetOffset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
			if (matchRegion != null) {
				int widgetPos= matchRegion.getOffset();
				int length= matchRegion.getLength();

				// Prevents setting of widget selection with line delimiters at beginning or end
				char startChar= adapter.charAt(widgetPos);
				char endChar= adapter.charAt(widgetPos+length-1);
				boolean borderHasLineDelimiter= startChar == '\n' || startChar == '\r' || endChar == '\n' || endChar == '\r';
				boolean redraws= redraws();
				if (borderHasLineDelimiter && redraws)
					setRedraw(false);

				if (redraws()) {
					fTextWidget.setSelectionRange(widgetPos, length);
					internalRevealRange(widgetPos, widgetPos + length);
					selectionChanged(widgetPos, length);
				} else {
					setSelectedRange(widgetOffset2ModelOffset(widgetPos), length);
					if (redraws)
						setRedraw(true);
				}

				return widgetOffset2ModelOffset(widgetPos);
			}

		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.findAndSelect")); //$NON-NLS-1$
		}

		return -1;
	}

	/**
	 * Adheres to the contract of {@link org.eclipse.jface.text.IFindReplaceTargetExtension3#findAndSelect(int, String, boolean, boolean, boolean, boolean)}.
	 *
	 * @param startPosition the start position
	 * @param findString the find string specification
	 * @param forwardSearch the search direction
	 * @param caseSensitive <code>true</code> if case sensitive, <code>false</code> otherwise
	 * @param wholeWord <code>true</code> if matches must be whole words, <code>false</code> otherwise
	 * @param rangeOffset the search scope offset
	 * @param rangeLength the search scope length
	 * @param regExSearch <code>true</code> if <code>findString</code> is a regular expression, <code>false</code> otherwise
	 * @return the model offset of the first match
	 * @since 3.0
	 */
	protected int findAndSelectInRange(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, int rangeOffset, int rangeLength, boolean regExSearch) {
		if (fTextWidget == null)
			return -1;

		try {

			int modelOffset;
			if (forwardSearch && (startPosition == -1 || startPosition < rangeOffset)) {
				modelOffset= rangeOffset;
			} else if (!forwardSearch && (startPosition == -1 || startPosition > rangeOffset + rangeLength)) {
				modelOffset= rangeOffset + rangeLength;
			} else {
				modelOffset= startPosition;
			}

			int widgetOffset= modelOffset2WidgetOffset(modelOffset);
			if (widgetOffset == -1)
				return -1;

			FindReplaceDocumentAdapter adapter= getFindReplaceDocumentAdapter();
			IRegion matchRegion= adapter.find(widgetOffset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
			int widgetPos= -1;
			int length= 0;
			if (matchRegion != null) {
				widgetPos= matchRegion.getOffset();
				length= matchRegion.getLength();
			}
			int modelPos= widgetPos == -1 ? -1 : widgetOffset2ModelOffset(widgetPos);

			if (widgetPos != -1 && (modelPos < rangeOffset || modelPos + length > rangeOffset + rangeLength))
				widgetPos= -1;

			if (widgetPos > -1) {

				// Prevents setting of widget selection with line delimiters at beginning or end
				char startChar= adapter.charAt(widgetPos);
				char endChar= adapter.charAt(widgetPos+length-1);
				boolean borderHasLineDelimiter= startChar == '\n' || startChar == '\r' || endChar == '\n' || endChar == '\r';
				boolean redraws= redraws();
				if (borderHasLineDelimiter && redraws)
					setRedraw(false);

				if (redraws()) {
					fTextWidget.setSelectionRange(widgetPos, length);
					internalRevealRange(widgetPos, widgetPos + length);
					selectionChanged(widgetPos, length);
				} else {
					setSelectedRange(modelPos, length);
					if (redraws)
						setRedraw(true);
				}

				return modelPos;
			}


		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.findAndSelect")); //$NON-NLS-1$
		}

		return -1;
	}

	//---------- text presentation support

	/*
	 * @see ITextViewer#setTextColor(Color)
	 */
	public void setTextColor(Color color) {
		if (color != null)
			setTextColor(color, 0, getDocument().getLength(), true);
	}

	/*
	 * @see ITextViewer#setTextColor(Color, start, length, boolean)
	 */
	public void setTextColor(Color color, int start, int length, boolean controlRedraw) {

		if (fTextWidget != null) {

			if (controlRedraw)
				fTextWidget.setRedraw(false);

			StyleRange s= new StyleRange();
			s.foreground= color;
			s.start= start;
			s.length= length;

			s= modelStyleRange2WidgetStyleRange(s);
			if (s != null) {

				if (controlRedraw)
					fTextWidget.setRedraw(false);

				fTextWidget.setStyleRange(s);

				if (controlRedraw)
					fTextWidget.setRedraw(true);
			}
		}
	}

	/**
	 * Adds the given presentation to the viewer's style information.
	 *
	 * @param presentation the presentation to be added
	 */
	private void addPresentation(TextPresentation presentation) {

		StyleRange range= presentation.getDefaultStyleRange();
		if (range != null) {

			range= modelStyleRange2WidgetStyleRange(range);
			if (range != null)
				fTextWidget.setStyleRange(range);

			Iterator e= presentation.getNonDefaultStyleRangeIterator();
			while (e.hasNext()) {
				range= (StyleRange) e.next();
				range= modelStyleRange2WidgetStyleRange(range);
				if (range != null)
					fTextWidget.setStyleRange(range);
			}

		} else {
			IRegion region= modelRange2WidgetRange(presentation.getCoverage());
			if (region == null)
				return;

			List list= new ArrayList(presentation.getDenumerableRanges());
			Iterator e= presentation.getAllStyleRangeIterator();
			while (e.hasNext()) {
				range= (StyleRange) e.next();
				range= modelStyleRange2WidgetStyleRange(range);
				if (range != null)
					list.add(range);
			}

			if (!list.isEmpty()) {
				StyleRange[] ranges= new StyleRange[list.size()];
				list.toArray(ranges);
				fTextWidget.replaceStyleRanges(region.getOffset(), region.getLength(), ranges);
			}
		}
	}

	/**
	 * Applies the given presentation to the given text widget. Helper method.
	 *
	 * @param presentation the style information
	 * @since 2.1
	 */
	private void applyTextPresentation(TextPresentation presentation) {

		List list= new ArrayList(presentation.getDenumerableRanges());
		Iterator e= presentation.getAllStyleRangeIterator();
		while (e.hasNext()) {
			StyleRange range= (StyleRange) e.next();
			range= modelStyleRange2WidgetStyleRange(range);
			if (range != null)
				list.add(range);
		}

		if (!list.isEmpty()) {
			StyleRange[] ranges= new StyleRange[list.size()];
			list.toArray(ranges);
			fTextWidget.setStyleRanges(ranges);
		}
	}

	/**
	 * Returns the visible region if it is not equal to the whole document.
	 * Otherwise returns <code>null</code>.
	 *
	 * @return the viewer's visible region if smaller than input document, otherwise <code>null</code>
	 */
	protected IRegion _internalGetVisibleRegion() {

		IDocument document= getVisibleDocument();
		if (document instanceof ChildDocument) {
			Position p= ((ChildDocument) document).getParentDocumentRange();
			return new Region(p.getOffset(), p.getLength());
		}

		return null;
	}

	/*
	 * @see ITextViewer#changeTextPresentation(TextPresentation, boolean)
	 */
	public void changeTextPresentation(TextPresentation presentation, boolean controlRedraw) {

		if (presentation == null || !redraws())
			return;

		if (fTextWidget == null)
			return;


		/*
		 * Call registered text presentation listeners
		 * and let them apply their presentation.
		 */
		if (fTextPresentationListeners != null) {
			ArrayList listeners= new ArrayList(fTextPresentationListeners);
			for (int i= 0, size= listeners.size(); i < size; i++) {
				ITextPresentationListener listener= (ITextPresentationListener)listeners.get(i);
				listener.applyTextPresentation(presentation);
			}
		}

		if (presentation.isEmpty())
			return;

		if (controlRedraw)
			fTextWidget.setRedraw(false);

		if (fReplaceTextPresentation)
			applyTextPresentation(presentation);
		else
			addPresentation(presentation);

		if (controlRedraw)
			fTextWidget.setRedraw(true);
	}

	/*
	 * @see ITextViewer#getFindReplaceTarget()
	 */
	public IFindReplaceTarget getFindReplaceTarget() {
		if (fFindReplaceTarget == null)
			fFindReplaceTarget= new FindReplaceTarget();
		return fFindReplaceTarget;
	}

	/**
	 * Returns the find/replace document adapter.
	 *
	 * @return the find/replace document adapter.
	 * @since 3.0
	 */
	protected FindReplaceDocumentAdapter getFindReplaceDocumentAdapter() {
		if (fFindReplaceDocumentAdapter == null)
			fFindReplaceDocumentAdapter= new FindReplaceDocumentAdapter(getVisibleDocument());
		return fFindReplaceDocumentAdapter;
	}

	/*
	 * @see ITextViewer#getTextOperationTarget()
	 */
	public ITextOperationTarget getTextOperationTarget() {
		return this;
	}

	/*
	 * @see ITextViewerExtension#appendVerifyKeyListener(VerifyKeyListener)
	 * @since 2.0
	 */
	public void appendVerifyKeyListener(VerifyKeyListener listener) {
		int index= fVerifyKeyListenersManager.numberOfListeners();
		fVerifyKeyListenersManager.insertListener(listener, index);
	}

	/*
	 * @see ITextViewerExtension#prependVerifyKeyListener(VerifyKeyListener)
	 * @since 2.0
	 */
	public void prependVerifyKeyListener(VerifyKeyListener listener) {
		fVerifyKeyListenersManager.insertListener(listener, 0);

	}

	/*
	 * @see ITextViewerExtension#removeVerifyKeyListener(VerifyKeyListener)
	 * @since 2.0
	 */
	public void removeVerifyKeyListener(VerifyKeyListener listener) {
		fVerifyKeyListenersManager.removeListener(listener);
	}

	/*
	 * @see ITextViewerExtension#getMark()
	 * @since 2.0
	 */
	public int getMark() {
		return fMarkPosition == null || fMarkPosition.isDeleted() ? -1 : fMarkPosition.getOffset();
	}

	/*
	 * @see ITextViewerExtension#setMark(int)
	 * @since 2.0
	 */
	public void setMark(int offset) {

		// clear
		if (offset == -1) {
			if (fMarkPosition != null && !fMarkPosition.isDeleted()) {

				IDocument document= getDocument();
				if (document != null)
					document.removePosition(fMarkPosition);
			}

			fMarkPosition= null;

			markChanged(-1, 0);

		// set
		} else {

			IDocument document= getDocument();
			if (document == null) {
				fMarkPosition= null;
				return;
			}

			if (fMarkPosition != null)
				document.removePosition(fMarkPosition);

			fMarkPosition= null;

			try {

				Position position= new Position(offset);
				document.addPosition(MARK_POSITION_CATEGORY, position);
				fMarkPosition= position;

			} catch (BadLocationException e) {
				return;
			} catch (BadPositionCategoryException e) {
				return;
			}

			markChanged(modelOffset2WidgetOffset(fMarkPosition.offset), 0);
		}
	}

	/*
	 * @see Viewer#inputChanged(Object, Object)
	 * @since 2.0
	 */
	protected void inputChanged(Object newInput, Object oldInput) {

		IDocument oldDocument= (IDocument) oldInput;
		if (oldDocument != null) {

			if (fMarkPosition != null && !fMarkPosition.isDeleted())
				oldDocument.removePosition(fMarkPosition);

			try {
				oldDocument.removePositionUpdater(fMarkPositionUpdater);
				oldDocument.removePositionCategory(MARK_POSITION_CATEGORY);

			} catch (BadPositionCategoryException e) {
			}
		}

		fMarkPosition= null;

		if (oldDocument instanceof IDocumentExtension4) {
			IDocumentExtension4 document= (IDocumentExtension4) oldDocument;
			document.removeDocumentRewriteSessionListener(fDocumentRewriteSessionListener);
		}

		super.inputChanged(newInput, oldInput);

		if (newInput instanceof IDocumentExtension4) {
			IDocumentExtension4 document= (IDocumentExtension4) newInput;
			document.addDocumentRewriteSessionListener(fDocumentRewriteSessionListener);
		}

		IDocument newDocument= (IDocument) newInput;
		if (newDocument != null) {
			newDocument.addPositionCategory(MARK_POSITION_CATEGORY);
			newDocument.addPositionUpdater(fMarkPositionUpdater);
		}
	}

	/**
	 * Informs all text listeners about the change of the viewer's redraw state.
	 * @since 2.0
	 */
	private void fireRedrawChanged() {
		fWidgetCommand.start= 0;
		fWidgetCommand.length= 0;
		fWidgetCommand.text= null;
		fWidgetCommand.event= null;
		updateTextListeners(fWidgetCommand);
	}

	/**
	 * Forgets the previously remembered  selection position.  After that
	 * call the selection position can no longer be queried and is longer updated
	 * in responds to content changes of the viewer.
	 *
	 * @return the remembered and updated  selection position or <code>null</code> if no selection position has been remembered
	 * @since 2.1
	 */
	private Point forgetDocumentSelection() {
		if (fDocumentSelection == null)
			return null;

		Point selection= fDocumentSelection.isDeleted() ? null : fDocumentSelection.getSelection();
		IDocument document= getDocument();
		if (document != null) {
			try {
				document.removePosition(fRememberedSelectionCategory, fDocumentSelection);
				document.removePositionUpdater(fSelectionUpdater);
				fSelectionUpdater= null;
				document.removePositionCategory(fRememberedSelectionCategory);
				fRememberedSelectionCategory= null;
			} catch (BadPositionCategoryException exception) {
				// Should not happen
			}
		}

		fDocumentSelection= null;

		return selection;
	}

	/**
	 * Enables the redrawing of this text viewer.
	 * @since 2.0
	 */
	protected void enabledRedrawing() {
		enabledRedrawing(-1);
	}
	/**
	 * Enables the redrawing of this text viewer.
	 *
	 * @param topIndex the top index to be set or <code>-1</code>
	 * @since 3.0
	 */
	protected void enabledRedrawing(int topIndex) {
		if (fDocumentAdapter instanceof IDocumentAdapterExtension) {
			IDocumentAdapterExtension extension= (IDocumentAdapterExtension) fDocumentAdapter;
			StyledText textWidget= getTextWidget();
			if (textWidget != null && !textWidget.isDisposed()) {
				int topPixel= textWidget.getTopPixel();
				extension.resumeForwardingDocumentChanges();
				if (topIndex > -1) {
					try {
						setTopIndex(topIndex);
					} catch (IllegalArgumentException x) {
						// changes don't allow for the previous top pixel
					}
				} else if (topPixel > -1) {
					try {
						textWidget.setTopPixel(topPixel);
					} catch (IllegalArgumentException x) {
						// changes don't allow for the previous top pixel
					}
				}
			}
		}

		Point selection= forgetDocumentSelection();
		if (selection != null) {
			setSelectedRange(selection.x, selection.y);
			if (topIndex == -1)
				revealRange(selection.x, selection.y);
		}

		if (fTextWidget != null && !fTextWidget.isDisposed())
			fTextWidget.setRedraw(true);

		fireRedrawChanged();
	}

	/**
	 * Remembers the current selection in a <code>SelectionPosition</code>	. The selection
	 * position can be queried and is updated to changes made to the viewer's contents.
	 * @since 2.1
	 */
	private void rememberDocumentSelection() {
		Point selection= getSelectedRange();
		if (selection != null) {
			SelectionPosition p= new SelectionPosition(selection);
			IDocument document= getDocument();
			if (document != null) {
				try {
					fRememberedSelectionCategory= SELECTION_POSITION_CATEGORY + hashCode();
					fSelectionUpdater= new NonDeletingPositionUpdater(fRememberedSelectionCategory);
					document.addPositionCategory(fRememberedSelectionCategory);
					document.addPositionUpdater(fSelectionUpdater);
					document.addPosition(fRememberedSelectionCategory, p);
					fDocumentSelection= p;
				} catch (BadLocationException exception) {
					// Should not happen
				} catch (BadPositionCategoryException exception) {
					// Should not happen
				}
			}
		}
	}

	/**
	 * Disables the redrawing of this text viewer. Subclasses may extend.
	 * @since 2.0
	 */
	protected void disableRedrawing() {

		rememberDocumentSelection();

		if (fDocumentAdapter instanceof IDocumentAdapterExtension) {
			IDocumentAdapterExtension extension= (IDocumentAdapterExtension) fDocumentAdapter;
			extension.stopForwardingDocumentChanges();
		}

		if (fTextWidget != null && !fTextWidget.isDisposed())
			fTextWidget.setRedraw(false);

		fireRedrawChanged();
	}

	/*
	 * @see ITextViewerExtension#setRedraw(boolean)
	 * @since 2.0
	 */
	public final void setRedraw(boolean redraw) {
		setRedraw(redraw, -1);
	}

	/**
	 * Basically same functionality as
	 * <code>ITextViewerExtension.setRedraw(boolean)</code>. Adds a way for
	 * subclasses to pass in a desired top index that should be used when
	 * <code>redraw</code> is <code>true</code>. If <code>topIndex</code>
	 * is -1, this method is identical to
	 * <code>ITextViewerExtension.setRedraw(boolean)</code>.
	 *
	 * @see ITextViewerExtension#setRedraw(boolean)
	 *
	 * @param redraw
	 * @param topIndex
	 * @since 3.0
	 */
	protected final void setRedraw(boolean redraw, int topIndex) {
		if (!redraw) {

			++ fRedrawCounter;
			if (fRedrawCounter == 1)
				disableRedrawing();

		} else {
			-- fRedrawCounter;
			if (fRedrawCounter == 0) {
				if (topIndex == -1)
					enabledRedrawing();
				else
					enabledRedrawing(topIndex);
			}
		}
	}

	/**
	 * Returns whether this viewer redraws itself.
	 *
	 * @return <code>true</code> if this viewer redraws itself
	 * @since 2.0
	 */
	protected final boolean redraws() {
		return fRedrawCounter <= 0;
	}

	/**
	 * Starts  the sequential rewrite mode of the viewer's document.
	 *
	 * @param normalized <code>true</code> if the rewrite is performed from the start to the end of the document
	 * @since 2.0
	 * @deprecated since 3.1 use {@link IDocumentExtension4#startRewriteSession(DocumentRewriteSessionType)} instead
	 */
	protected final void startSequentialRewriteMode(boolean normalized) {
		IDocument document= getDocument();
		if (document instanceof IDocumentExtension) {
			IDocumentExtension extension= (IDocumentExtension) document;
			extension.startSequentialRewrite(normalized);
		}
	}

	/**
	 * Sets the sequential rewrite mode of the viewer's document.
	 *
	 * @since 2.0
	 * @deprecated since 3.1 use {@link IDocumentExtension4#stopRewriteSession(DocumentRewriteSession)} instead
	 */
	protected final void stopSequentialRewriteMode() {
		IDocument document= getDocument();
		if (document instanceof IDocumentExtension) {
			IDocumentExtension extension= (IDocumentExtension) document;
			extension.stopSequentialRewrite();
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension#getRewriteTarget()
	 * @since 2.0
	 */
	public IRewriteTarget getRewriteTarget() {
		if (fRewriteTarget == null)
			fRewriteTarget= new RewriteTarget();
		return fRewriteTarget;
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension2#getCurrentTextHover()
	 */
	public ITextHover getCurrentTextHover() {
		if (fTextHoverManager == null)
			return null;
		return fTextHoverManager.getCurrentTextHover();
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension2#getHoverEventLocation()
	 */
	public Point getHoverEventLocation() {
		if (fTextHoverManager == null)
			return null;
		return fTextHoverManager.getHoverEventLocation();
	}

	/**
	 * Returns the paint manager of this viewer.
	 *
	 * @return the paint manager of this viewer
	 * @since 2.1
	 */
	protected PaintManager getPaintManager() {
		if (fPaintManager == null)
			fPaintManager= new PaintManager(this);
		return fPaintManager;
	}

	/**
	 * Adds the given  painter to this viewer. If the painter is already registered
	 * this method is without effect.
	 *
	 * @param painter the painter to be added
	 * @since 2.1
	 */
	public void addPainter(IPainter painter) {
		getPaintManager().addPainter(painter);
	}

	/**
	 * Removes the given painter from this viewer. If the painter has previously not been
	 * added to this viewer this method is without effect.
	 *
	 * @param painter the painter to be removed
	 * @since 2.1
	 */
	public void removePainter(IPainter painter) {
		getPaintManager().removePainter(painter);
	}

	// ----------------------------------- conversions -------------------------------------------------------

	/**
	 * Implements the contract of {@link ITextViewerExtension5#modelLine2WidgetLine(int)}.
	 *
	 * @param modelLine the model line
	 * @return the corresponding widget line or <code>-1</code>
	 * @since 2.1
	 */
	public int modelLine2WidgetLine(int modelLine) {
		if (fInformationMapping == null)
			return modelLine;

		try {
			return fInformationMapping.toImageLine(modelLine);
		} catch (BadLocationException x) {
	}

		return -1;
	}

	/**
	 * Implements the contract of {@link ITextViewerExtension5#modelOffset2WidgetOffset(int)}.
	 *
	 * @param modelOffset the model offset
	 * @return the corresponding widget offset or <code>-1</code>
	 * @since 2.1
	 */
	public int modelOffset2WidgetOffset(int modelOffset) {
		if (fInformationMapping == null)
			return modelOffset;

		try {
			return fInformationMapping.toImageOffset(modelOffset);
		} catch (BadLocationException x) {
		}

		return -1;
	}

	/**
	 * Implements the contract of {@link ITextViewerExtension5#modelRange2WidgetRange(IRegion)}.
	 *
	 * @param modelRange the model range
	 * @return the corresponding widget range or <code>null</code>
	 * @since 2.1
	 */
	public IRegion modelRange2WidgetRange(IRegion modelRange) {
		if (fInformationMapping == null)
			return modelRange;

		try {

			if (modelRange.getLength() < 0) {
				Region reversed= new Region(modelRange.getOffset() + modelRange.getLength(), -modelRange.getLength());
				IRegion result= fInformationMapping.toImageRegion(reversed);
				if (result != null)
					return new Region(result.getOffset() + result.getLength(), -result.getLength());
			}
			return fInformationMapping.toImageRegion(modelRange);

		} catch (BadLocationException x) {
		}

		return null;
	}

	/**
	 * Similar to {@link #modelRange2WidgetRange(IRegion)}, but more forgiving:
	 * if <code>modelRange</code> describes a region entirely hidden in the
	 * image, then this method returns the zero-length region at the offset of
	 * the folded region.
	 *
	 * @param modelRange the model range
	 * @return the corresponding widget range, or <code>null</code>
	 * @since 3.1
	 */
	protected IRegion modelRange2ClosestWidgetRange(IRegion modelRange) {
		if (!(fInformationMapping instanceof IDocumentInformationMappingExtension2))
			return modelRange2WidgetRange(modelRange);

		try {
			if (modelRange.getLength() < 0) {
				Region reversed= new Region(modelRange.getOffset() + modelRange.getLength(), -modelRange.getLength());
				IRegion result= ((IDocumentInformationMappingExtension2) fInformationMapping).toClosestImageRegion(reversed);
				if (result != null)
					return new Region(result.getOffset() + result.getLength(), -result.getLength());
			}
			return ((IDocumentInformationMappingExtension2) fInformationMapping).toClosestImageRegion(modelRange);

		} catch (BadLocationException x) {
		}

		return null;
	}

	/**
	 * Implements the contract of {@link ITextViewerExtension5#widgetLine2ModelLine(int)}.
	 *
	 * @param widgetLine the widget line
	 * @return the corresponding model line
	 * @since 2.1
	 */
	public int widgetlLine2ModelLine(int widgetLine) {
		return widgetLine2ModelLine(widgetLine);
	}

	/**
	 * Implements the contract of {@link ITextViewerExtension5#widgetLine2ModelLine(int)}.
	 *
	 * @param widgetLine the widget line
	 * @return the corresponding model line or <code>-1</code>
	 * @since 3.0
	 */
	public int widgetLine2ModelLine(int widgetLine) {
		if (fInformationMapping == null)
			return widgetLine;

		try {
			return fInformationMapping.toOriginLine(widgetLine);
		} catch (BadLocationException x) {
		}

		return -1;
	}

	/**
	 * Implements the contract of {@link ITextViewerExtension5#widgetOffset2ModelOffset(int)}.
	 *
	 * @param widgetOffset the widget offset
	 * @return the corresponding model offset or <code>-1</code>
	 * @since 2.1
	 */
	public int widgetOffset2ModelOffset(int widgetOffset) {
		if (fInformationMapping == null)
			return widgetOffset;

		try {
			return fInformationMapping.toOriginOffset(widgetOffset);
		} catch (BadLocationException x) {
			if (widgetOffset == getVisibleDocument().getLength()) {
				IRegion coverage= fInformationMapping.getCoverage();
				return coverage.getOffset() + coverage.getLength();
			}
		}

		return -1;
	}

	/**
	 * Implements the contract of {@link ITextViewerExtension5#widgetRange2ModelRange(IRegion)}.
	 *
	 * @param widgetRange the widget range
	 * @return the corresponding model range or <code>null</code>
	 * @since 2.1
	 */
	public IRegion widgetRange2ModelRange(IRegion widgetRange) {
		if (fInformationMapping == null)
			return widgetRange;

		try {

			if (widgetRange.getLength() < 0) {
				Region reveresed= new Region(widgetRange.getOffset() + widgetRange.getLength(), -widgetRange.getLength());
				IRegion result= fInformationMapping.toOriginRegion(reveresed);
				return new Region(result.getOffset() + result.getLength(), -result.getLength());
			}

			return fInformationMapping.toOriginRegion(widgetRange);

		} catch (BadLocationException x) {
			int modelOffset= widgetOffset2ModelOffset(widgetRange.getOffset());
			if (modelOffset > -1) {
				int modelEndOffset= widgetOffset2ModelOffset(widgetRange.getOffset() + widgetRange.getLength());
				if (modelEndOffset > -1)
					return new Region(modelOffset, modelEndOffset - modelOffset);
			}
		}

		return null;
	}

	/**
	 * Implements the contract of {@link ITextViewerExtension5#getModelCoverage()}.
	 *
	 * @return the model coverage
	 * @since 2.1
	 */
	public IRegion getModelCoverage() {
		if (fInformationMapping == null) {
			IDocument document= getDocument();
			if (document == null)
				return null;
			return new Region(0, document.getLength());
		}

		return fInformationMapping.getCoverage();
	}

	/**
	 * Returns the line of the widget whose corresponding line in the viewer's document
	 * is closest to the given line in the viewer's document or <code>-1</code>.
	 *
	 * @param modelLine the line in the viewer's document
	 * @return the line in the widget that corresponds best to the given line in the viewer's document or <code>-1</code>
	 * @since 2.1
	 */
	protected int getClosestWidgetLineForModelLine(int modelLine) {
		if (fInformationMapping == null)
			return modelLine;

		try {
			return fInformationMapping.toClosestImageLine(modelLine);
		} catch (BadLocationException x) {
		}

		return -1;
	}

	/**
	 * Translates a style range given relative to the viewer's document into style
	 * ranges relative to the viewer's widget or <code>null</code>.
	 *
	 * @param range the style range in the coordinates of the viewer's document
	 * @return the style range in the coordinates of the viewer's widget or <code>null</code>
	 * @since 2.1
	 */
	protected StyleRange modelStyleRange2WidgetStyleRange(StyleRange range) {
		IRegion region= modelRange2WidgetRange(new Region(range.start, range.length));
		if (region != null) {
			StyleRange result= (StyleRange) range.clone();
			result.start= region.getOffset();
			result.length= region.getLength();
			return result;
		}
		return null;
	}

	/**
	 * Same as {@link #modelRange2WidgetRange(IRegion)} just for a {@link org.eclipse.jface.text.Position}.
	 *
	 * @param modelPosition the position describing a range in the viewer's document
	 * @return a region describing a range in the viewer's widget
	 * @since 2.1
	 */
	protected IRegion modelRange2WidgetRange(Position modelPosition) {
		return modelRange2WidgetRange(new Region(modelPosition.getOffset(), modelPosition.getLength()));
	}

	/**
	 * Translates the widget region of the given verify event into
	 * the corresponding region of the viewer's document.
	 *
	 * @param event the verify event
	 * @return the region of the viewer's document corresponding to the verify event
	 * @since 2.1
	 */
	protected IRegion event2ModelRange(VerifyEvent event) {

		Region region= null;
		if (event.start <= event.end)
			region= new Region(event.start, event.end - event.start);
		else
			region= new Region(event.end, event.start - event.end);

		return widgetRange2ModelRange(region);
	}

	/**
	 * Translates the given widget selection into the corresponding region
	 * of the viewer's document or returns <code>null</code> if this fails.
	 *
	 * @param widgetSelection the widget selection
	 * @return the region of the viewer's document corresponding to the widget selection or <code>null</code>
	 * @since 2.1
	 */
	protected Point widgetSelection2ModelSelection(Point widgetSelection) {
		IRegion region= new Region(widgetSelection.x, widgetSelection.y);
		region= widgetRange2ModelRange(region);
		return region == null ? null : new Point(region.getOffset(), region.getLength());
	}

	/**
	 * Translates the given selection range of the viewer's document into
	 * the corresponding widget range or returns <code>null</code> of this fails.
	 *
	 * @param modelSelection the selection range of the viewer's document
	 * @return the widget range corresponding to the selection range or <code>null</code>
	 * @since 2.1
	 */
	protected Point modelSelection2WidgetSelection(Point modelSelection) {
		if (fInformationMapping == null)
			return modelSelection;

		try {
			IRegion region= new Region(modelSelection.x, modelSelection.y);
			region= fInformationMapping.toImageRegion(region);
			if (region != null)
				return new Point(region.getOffset(), region.getLength());
		} catch (BadLocationException x) {
		}

		return null;
	}

	/**
	 * Implements the contract of {@link ITextViewerExtension5#widgetLineOfWidgetOffset(int)}.
	 *
	 * @param widgetOffset the widget offset
	 * @return  the corresponding widget line or <code>-1</code>
	 * @since 2.1
	 */
	public int widgetLineOfWidgetOffset(int widgetOffset) {
		IDocument document= getVisibleDocument();
		if (document != null) {
			try {
				return document.getLineOfOffset(widgetOffset);
			} catch (BadLocationException e) {
			}
		}
		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension4#moveFocusToWidgetToken()
	 * @since 3.0
	 */
	public boolean moveFocusToWidgetToken() {
		if (fWidgetTokenKeeper instanceof IWidgetTokenKeeperExtension) {
			IWidgetTokenKeeperExtension extension= (IWidgetTokenKeeperExtension) fWidgetTokenKeeper;
			return extension.setFocus(this);
		}
		return false;
	}

	/**
	 * Sets the document partitioning of this viewer. The partitioning is used by this viewer to
	 * access partitioning information of the viewers input document.
	 *
	 * @param partitioning the partitioning name
	 * @since 3.0
	 */
	public void setDocumentPartitioning(String partitioning) {
		fPartitioning= partitioning;
	}

	/**
	 * Returns the document partitioning for this viewer.
	 *
	 * @return the document partitioning for this viewer
	 * @since 3.0
	 */
	protected String getDocumentPartitioning() {
		return fPartitioning;
	}

	//---- Text presentation listeners ----

	/*
	 * @see ITextViewerExtension4#addTextPresentationListener(ITextPresentationListener)
	 * @since 3.0
	 */
	public void addTextPresentationListener(ITextPresentationListener listener) {

		Assert.isNotNull(listener);

		if (fTextPresentationListeners == null)
			fTextPresentationListeners= new ArrayList();

		if (!fTextPresentationListeners.contains(listener))
			fTextPresentationListeners.add(listener);
	}

	/*
	 * @see ITextViewerExtension4#removeTextPresentationListener(ITextPresentationListener)
	 * @since 3.0
	 */
	public void removeTextPresentationListener(ITextPresentationListener listener) {

		Assert.isNotNull(listener);

		if (fTextPresentationListeners != null) {
			fTextPresentationListeners.remove(listener);
			if (fTextPresentationListeners.size() == 0)
				fTextPresentationListeners= null;
		}
	}

	/*
	 * @see org.eclipse.jface.text.IEditingSupportRegistry#registerHelper(org.eclipse.jface.text.IEditingSupport)
	 * @since 3.1
	 */
	public void register(IEditingSupport helper) {
		Assert.isLegal(helper != null);
		fEditorHelpers.add(helper);
	}

	/*
	 * @see org.eclipse.jface.text.IEditingSupportRegistry#deregisterHelper(org.eclipse.jface.text.IEditingSupport)
	 * @since 3.1
	 */
	public void unregister(IEditingSupport helper) {
		fEditorHelpers.remove(helper);
	}

	/*
	 * @see org.eclipse.jface.text.IEditingSupportRegistry#getCurrentHelpers()
	 * @since 3.1
	 */
	public IEditingSupport[] getRegisteredSupports() {
		return (IEditingSupport[]) fEditorHelpers.toArray(new IEditingSupport[fEditorHelpers.size()]);
	}

	/*
	 * @see org.eclipse.jface.text.ITextViewerExtension6#setHyperlinkDetectors(org.eclipse.jface.text.hyperlink.IHyperlinkDetector[], int)
	 * @since 3.1
	 */
	public void setHyperlinkDetectors(IHyperlinkDetector[] hyperlinkDetectors, int eventStateMask) {
		boolean enable= hyperlinkDetectors != null && hyperlinkDetectors.length > 0;
		fHyperlinkStateMask= eventStateMask;
		fHyperlinkDetectors= hyperlinkDetectors;
		if (enable) {
			if (fHyperlinkManager != null) {
				fHyperlinkManager.setHyperlinkDetectors(fHyperlinkDetectors);
				fHyperlinkManager.setHyperlinkStateMask(fHyperlinkStateMask);
			}
			ensureHyperlinkManagerInstalled();
		} else {
			if (fHyperlinkManager != null)
				fHyperlinkManager.uninstall();
			fHyperlinkManager= null;
		}
	}

	/**
	 * Sets the hyperlink presenter.
	 * <p>
	 * This is only valid as long as the hyperlink manager hasn't
	 * been created yet.
	 * </p>
	 *
	 * @param hyperlinkPresenter the hyperlink presenter
	 * @throws IllegalStateException if the hyperlink manager has already been created
	 * @since 3.1
	 */
	public void setHyperlinkPresenter(IHyperlinkPresenter hyperlinkPresenter) throws IllegalStateException {
		if (fHyperlinkManager != null)
			throw new IllegalStateException();

		fHyperlinkPresenter= hyperlinkPresenter;
		ensureHyperlinkManagerInstalled();
	}

	/**
	 * Ensures that the hyperlink manager has been
	 * installed if a hyperlink detector is available.
	 *
	 * @since 3.1
	 */
	private void ensureHyperlinkManagerInstalled() {
		if (fHyperlinkDetectors != null && fHyperlinkDetectors.length > 0 && fHyperlinkPresenter != null && fHyperlinkManager == null) {
			fHyperlinkManager= new HyperlinkManager(HyperlinkManager.FIRST);
			fHyperlinkManager.install(this, fHyperlinkPresenter, fHyperlinkDetectors, fHyperlinkStateMask);
		}
	}
}

package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
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

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;



/**
 * SWT based implementation of <code>ITextViewer</code>. Once the viewer and its SWT control
 * have been created the viewer can only indirectly be disposed by disposing its SWT control.<p>
 * Clients are supposed to instantiate a text viewer and subsequently to communicate with it 
 * exclusively using the <code>ITextViewer</code> interface. Clients should no subclass this
 * class as it is rather likely that subclasses will be broken by future releases.
 * 
 * @see ITextViewer
 */  
public class TextViewer extends Viewer implements 
		ITextViewer, ITextViewerExtension, 
		ITextOperationTarget, ITextOperationTargetExtension,
		IWidgetTokenOwner {
	
	
	public static boolean TRACE_ERRORS= false;
	
	/**
	 * Represent a replace command that brings the text viewer's text widget
	 * back in sync with text viewer's document after the document has been changed.
	 */
	protected class WidgetCommand {
		
		public DocumentEvent event;
		public int start, length;
		public String text, preservedText;
				
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
					preservedText= e.getDocument().get(e.getOffset(), e.getLength());
				} catch (BadLocationException x) {
					preservedText= null;
					if (TRACE_ERRORS)
						System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.WidgetCommand.setEvent")); //$NON-NLS-1$
				}
			} else
				preservedText= null;
		}
	};
		
	/**
	 * Connects a text double click strategy to this viewer's text widget. 
	 * Calls the double click strategy when the mouse has been double clicked 
	 * inside the text editor.
	 */	
	class TextDoubleClickStrategyConnector extends MouseAdapter {
		
		private boolean fDoubleClicked= false;
		
		public TextDoubleClickStrategyConnector() {
		}
				
		public void mouseDoubleClick(MouseEvent e) {
			fDoubleClicked= true;
		}
			
		public void mouseUp(MouseEvent e) {
			if (fDoubleClicked) {
				fDoubleClicked= false;
				ITextDoubleClickStrategy s= (ITextDoubleClickStrategy) selectContentTypePlugin(getSelectedRange().x, fDoubleClickStrategies);
				if (s != null)
					s.doubleClicked(TextViewer.this);
			}
		}
	};
	
	/**
	 * Monitors the area of the viewer's document that is visible in the viewer. 
	 * If the area might have been changed, it informs the text viewer about this
	 * potential change and its origin. The origin is internally used for optimization purposes.
	 */
	class ViewportGuard extends MouseAdapter 
		implements ControlListener, KeyListener, MouseMoveListener, SelectionListener {
		
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
				fTextWidget.removeMouseMoveListener(this);
			updateViewportListeners(MOUSE_END);
		}

		/*
		 * @see MouseListener#mouseDown
		 */
		public void mouseDown(MouseEvent e) {
			if (fTextWidget != null)
				fTextWidget.addMouseMoveListener(this);
		}

		/*
		 * @see MouseMoveListener#mouseMove
		 */
		public void mouseMove(MouseEvent e) {
			updateViewportListeners(MOUSE);
		}

		/*
		 * @see SelectionListener#widgetSelected
		 */
		public void widgetSelected(SelectionEvent e) {
			updateViewportListeners(SCROLLER);
		}

		/*
		 * @see SelectionListener#widgetDefaultSelected
		 */
		public void widgetDefaultSelected(SelectionEvent e) {}
	};
		
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
		
		/*
		 * If an insertion happens at the selection's start offset,
		 * the position is extended rather than shifted.
		 *
		 * @see DefaultPositionUpdater#adaptToInsert
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
	};
	
	/**
	 * Internal document listener.
	 */
	class DocumentListener implements IDocumentListener {
		
		/*
		 * @see IDocumentListener#documentAboutToBeChanged
		 */
		public void documentAboutToBeChanged(DocumentEvent e) {
			if (e.getDocument() == getVisibleDocument())
				fWidgetCommand.setEvent(e);
		}
		
		/*
		 * @see IDocumentListener#documentChanged
		 */
		public void documentChanged(final DocumentEvent e) {
			if (fWidgetCommand.event == e)
				updateTextListeners(fWidgetCommand);
		}
	};
	
	
	/**
	 * Internal verify listener.
	 */
	class TextVerifyListener implements VerifyListener {
		
		private boolean fForward= true;
		
		/**
		 * Tells the listener to forward received events.
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
	};
	
	/**
	 * The viewer's manager of registered verify key listeners.
	 * Uses batches rather than robust iterators because of
	 * performance issues.
	 */
	class VerifyKeyListenersManager implements VerifyKeyListener {
		
		class Batch {
			int index;
			VerifyKeyListener listener;
			
			public Batch(VerifyKeyListener l, int i) {
				listener= l;
				index= i;
			}
		};
		
		private List fListeners= new ArrayList();
		private List fBatched= new ArrayList();
		private Iterator fIterator;
		
		/*
		 * @see VerifyKeyListener#verifyKey(VerifyEvent)
		 */
		public void verifyKey(VerifyEvent event) {
			if (fListeners.isEmpty())
				return;
				
			fIterator= fListeners.iterator();
			while (fIterator.hasNext() && event.doit) {
				VerifyKeyListener listener= (VerifyKeyListener) fIterator.next();
				listener.verifyKey(event);
			}
			fIterator= null;
			
			processBatchedRequests();
		}
		
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
				
				if (fIterator != null) {
					
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
			
			if (fIterator != null) {
				
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
	};
	
	
	/**
	 * MISSING
	 */
	class FindReplaceRange implements LineBackgroundListener, ITextListener, IPositionUpdater {		

		private final static String RANGE_CATEGORY= "org.eclipse.jface.text.TextViewer.find.range"; //$NON-NLS-1$

		private Color fHighlightColor;
		private IRegion fRange;
		private Position fPosition;
		
		public FindReplaceRange(IRegion range) {
			setRange(range);
		}
		
		public void setRange(IRegion range) {
			fPosition= new Position(range.getOffset(), range.getLength());
		}
		
		public IRegion getRange() {
			return new Region(fPosition.getOffset(), fPosition.getLength());
		}
		
		public void setHighlightColor(Color color) {
			fHighlightColor= color;
			paint();
		}

		/*
		 * @see LineBackgroundListener#lineGetBackground(LineBackgroundEvent)
		 */
		public void lineGetBackground(LineBackgroundEvent event) {
			/* Don't use cached line information because of patched redrawing events. */
			
			if (fTextWidget != null) {
				int offset= event.lineOffset + TextViewer.this.getVisibleRegionOffset();
				
				if (fPosition.includes(offset))
					event.lineBackground= fHighlightColor;
			}
		}
		
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
		
		private void clear() {
			if (fTextWidget != null && !fTextWidget.isDisposed())
				fTextWidget.redraw();
		}
		
		private void paint() {
			int offset= fPosition.getOffset() - TextViewer.this.getVisibleRegionOffset();
			int length= fPosition.getLength();

			int count= fTextWidget.getCharCount();
			if (offset + length >= count) {
				length= count - offset; // clip

				Point upperLeft= fTextWidget.getLocationAtOffset(offset);
				Point lowerRight= fTextWidget.getLocationAtOffset(offset + length);
				int width= fTextWidget.getClientArea().width;
				int height= fTextWidget.getLineHeight() + lowerRight.y - upperLeft.y;
				fTextWidget.redraw(upperLeft.x, upperLeft.y, width, height, false);
			}			
			
			fTextWidget.redrawRange(offset, length, true);
		}

		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent event) {
			if (event.getViewerRedrawState())
				paint();
		}

		/*
		 * @see IPositionUpdater#update(DocumentEvent)
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
	};
	
	/**
	 * This viewer's find/replace target.
	 */
	class FindReplaceTarget implements IFindReplaceTarget, IFindReplaceTargetExtension {

		private FindReplaceRange fRange;
		private Color fScopeHighlightColor;
		private IDocumentPartitioner fRememberedPartitioner;
		
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
			Point s= TextViewer.this.getSelectedRange();
			if (s.x > -1 && s.y > -1) {
				try {
					IDocument document= TextViewer.this.getDocument();
					document.replace(s.x, s.y, text);
					if (text != null && text.length() > 0)
						TextViewer.this.setSelectedRange(s.x, text.length());
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
			Point point= TextViewer.this.getSelectedRange();
			point.x -= TextViewer.this.getVisibleRegionOffset();
			return point;
		}
		
		/*
		 * @see IFindReplaceTarget#findAndSelect(int, String, boolean, boolean, boolean)
		 */
		public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
			if (offset != -1)
				offset += TextViewer.this.getVisibleRegionOffset();

			if (fRange != null) {
				IRegion range= fRange.getRange();
				offset= TextViewer.this.findAndSelectInRange(offset, findString, searchForward, caseSensitive, wholeWord, range.getOffset(), range.getLength());
			} else {
				offset= TextViewer.this.findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord);
			}

			if (offset != -1)
				offset -= TextViewer.this.getVisibleRegionOffset();

			return offset;
		}
		
		/*
		 * @see IFindReplaceTarget#canPerformFind()
		 */
		public boolean canPerformFind() {
			return TextViewer.this.canPerformFind();
		}	

		/*
		 * @see IFindReplaceTargetExtension#beginSession()
		 */
		public void beginSession() {
			fRange= null;
		}

		/*
		 * @see IFindReplaceTargetExtension#endSession()
		 */
		public void endSession() {
			if (fRange != null) {
				fRange.uninstall();
				fRange= null;
			}
		}

		/*
		 * @see IFindReplaceTargetExtension#getScope()
		 */
		public IRegion getScope() {			
			return fRange == null ? null : fRange.getRange();
		}

		/*
		 * @see IFindReplaceTargetExtension#getLineSelection()
		 */
		public Point getLineSelection() {
			Point point= TextViewer.this.getSelectedRange();

			try {
				IDocument document= TextViewer.this.getDocument();

				// beginning of line
				int line= document.getLineOfOffset(point.x);
				int offset= document.getLineOffset(line);

				// end of line
				line= document.getLineOfOffset(point.x + point.y);
				int length= document.getLineOffset(line) + document.getLineLength(line)	- offset;

				return new Point(offset, length);

			} catch (BadLocationException e) {
				// should not happen			
				return null;
			}
		}

		/*
		 * @see IFindReplaceTargetExtension#setSelection(int, int)
		 */
		public void setSelection(int offset, int length) {
			TextViewer.this.setSelectedRange(offset /*+ TextViewer.this.getVisibleRegionOffset()*/, length);
		}

		/*
		 * @see IFindReplaceTargetExtension#setScope(IRegion)
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
		 */
		public void setScopeHighlightColor(Color color) {
			if (fRange != null)
				fRange.setHighlightColor(color);
			fScopeHighlightColor= color;
		}

		/*
		 * @see IFindReplaceTargetExtension#setReplaceAllMode(boolean)
		 */
		public void setReplaceAllMode(boolean replaceAll) {
			
			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=18232
			
			if (replaceAll) {
				
				TextViewer.this.setRedraw(false);
				TextViewer.this.startSequentialRewriteMode(false);
				
				if (fUndoManager != null)
					fUndoManager.beginCompoundChange();
				
				IDocument document= TextViewer.this.getDocument();
				fRememberedPartitioner= document.getDocumentPartitioner();
				if (fRememberedPartitioner != null) {
					fRememberedPartitioner.disconnect();
					document.setDocumentPartitioner(null);
				}

			} else {
				
				TextViewer.this.setRedraw(true);
				TextViewer.this.stopSequentialRewriteMode();
				
				if (fUndoManager != null)
					fUndoManager.endCompoundChange();
					
				if (fRememberedPartitioner != null) {
					IDocument document= TextViewer.this.getDocument();
					fRememberedPartitioner.connect(document);
					document.setDocumentPartitioner(fRememberedPartitioner);
				}
			}
		}
	};
	
	
	/**
	 * The viewer's rewrite target.
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
	};
	
		
	/** ID for originators of view port changes */
	protected static final int SCROLLER=		1;
	protected static final int MOUSE=			2;
	protected static final int MOUSE_END=	3;
	protected static final int KEY=				4;
	protected static final int RESIZE=			5;
	protected static final int INTERNAL=		6;
		
	/** Internal name of the position category used selection preservation during shift */
	protected static final String SHIFTING= "__TextViewer_shifting"; //$NON-NLS-1$

	/** The viewer's text widget */
	private StyledText fTextWidget;
	/** The viewer's input document */
	private IDocument fDocument;
	/** The viewer's visible document */
	private IDocument fVisibleDocument;
	/** The viewer's document adapter */
	private IDocumentAdapter fDocumentAdapter;
	/** The child document manager */
	private ChildDocumentManager fChildDocumentManager;
	/** The text viewer's double click strategies connector */
	private TextDoubleClickStrategyConnector fDoubleClickStrategyConnector;
	/** The text viewer's hovering controller */
	private AbstractHoverInformationControlManager fTextHoverManager;
	/** The text viewer's viewport guard */
	private ViewportGuard fViewportGuard;
	/** Caches the graphical coordinate of the first visible line */ 
	private int fTopInset= 0;
	/** The most recent document modification as widget command */
	private WidgetCommand fWidgetCommand= new WidgetCommand();	
	/** The SWT control's scrollbars */
	private ScrollBar fScroller;
	/** Document listener */
	private DocumentListener fDocumentListener= new DocumentListener();
	/** Verify listener */
	private TextVerifyListener fVerifyListener= new TextVerifyListener();
	/** The most recent widget modification as document command */
	private DocumentCommand fDocumentCommand= new DocumentCommand();
	/** The viewer's find/replace target */
	private IFindReplaceTarget fFindReplaceTarget;
	/** The viewer widget token keeper */
	private IWidgetTokenKeeper fWidgetTokenKeeper;
	/** The viewer's manager of verify key listeners */
	private VerifyKeyListenersManager fVerifyKeyListenersManager= new VerifyKeyListenersManager();
	/** The mark position. */
	private Position fMarkPosition;
	/** The mark position category. */
	private final String MARK_POSITION_CATEGORY="__mark_category_" + hashCode();
	/** The mark position updater */
	private final IPositionUpdater fMarkPositionUpdater= new DefaultPositionUpdater(MARK_POSITION_CATEGORY);
	/** The flag indicating the redraw behavior */
	private int fRedrawCounter= 0;
	/** The selection when working in non-redraw state */
	private Point fDocumentSelection;
	/** The viewer's rewrite target */
	private IRewriteTarget fRewriteTarget;
	
	
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
	/** The creator of the text hover control */
	protected IInformationControlCreator fHoverControlCreator;
	/** All registered viewport listeners> */
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
		return new DocumentAdapter();
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
		
		initializeViewportUpdate();
	}
		
	/*
	 * @see Viewer#getControl
	 */
	public Control getControl() {
		return fTextWidget;
	}
	
	/*
	 * @see ITextViewer#activatePlugin
	 */
	public void activatePlugins() {
		
		if (fDoubleClickStrategies != null && !fDoubleClickStrategies.isEmpty() && fDoubleClickStrategyConnector == null) {
			fDoubleClickStrategyConnector= new TextDoubleClickStrategyConnector();
			fTextWidget.addMouseListener(fDoubleClickStrategyConnector);
		}
		
		if (fTextHovers != null && !fTextHovers.isEmpty() && fHoverControlCreator != null && fTextHoverManager == null) {			
			fTextHoverManager= new TextViewerHoverManager(this, fHoverControlCreator);
			fTextHoverManager.install(this.getTextWidget());
		}
		
		if (fUndoManager != null) {
			fUndoManager.connect(this);
			fUndoManager.reset();
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
	 * Frees all resources allocated by this viewer. Internally called when the viewer's control
	 * has been disposed.
	 */
	protected void handleDispose() {
		
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
		
		if (fDocumentListener != null)
			fDocumentListener= null;
		
		if (fVisibleDocument instanceof ChildDocument) {
			ChildDocument child = (ChildDocument) fVisibleDocument;
			child.removeDocumentListener(fDocumentListener);
			getChildDocumentManager().freeChildDocument(child);
		}
		
		if (fDocumentAdapter != null) {
			fDocumentAdapter.setDocument(null);
			fDocumentAdapter= null;
		}
		
		fVisibleDocument= null;
		fDocument= null;
		fChildDocumentManager= null;
		fScroller= null;
	}
	
			
	//---- simple getters and setters
			
	/**
	 * Returns viewer's text widget.
	 */
	public StyledText getTextWidget() {
		return fTextWidget;
	}
			
	/*
	 * @see ITextViewer#setAutoIndentStrategy
	 */
	public void setAutoIndentStrategy(IAutoIndentStrategy strategy, String contentType) {
		
		if (strategy != null) {
			if (fAutoIndentStrategies == null)
				fAutoIndentStrategies= new HashMap();
			fAutoIndentStrategies.put(contentType, strategy);
		} else if (fAutoIndentStrategies != null)
			fAutoIndentStrategies.remove(contentType);
	}
	
	/*
	 * @see ITextViewer#setEventConsumer
	 */
	public void setEventConsumer(IEventConsumer consumer) {
		fEventConsumer= consumer;
	}
		
	/*
	 * @see ITextViewer#setIndentPrefixes 
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
	 * @see ITextViewer#getTopInset
	 */
	public int getTopInset() {
		return fTopInset;
	}
	
	/*
	 * @see ITextViewer#isEditable
	 */
	public boolean isEditable() {
		if (fTextWidget == null)
			return false;
		return fTextWidget.getEditable();
	}
	
	/*
	 * @see ITextViewer#setEditable
	 */
	public void setEditable(boolean editable) {
		if (fTextWidget != null)
			fTextWidget.setEditable(editable);
	}
			
	/*
	 * @see ITextViewer#setDefaultPrefixes
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
	 * @see ITextViewer#setUndoManager
	 */
	public void setUndoManager(IUndoManager undoManager) {
		fUndoManager= undoManager;
	}
	
	/*
	 * @see ITextViewer#setTextHover
	 */
	public void setTextHover(ITextHover hover, String contentType) {
		
		if (hover != null) {
			if (fTextHovers == null)
				fTextHovers= new HashMap();
			fTextHovers.put(contentType, hover);
		} else if (fTextHovers != null)
			fTextHovers.remove(contentType);
	}
	
	/**
	 * Returns the text hover for a given offset.
	 * 
	 * @param offset the offset for which to return the text hover
	 * @return the text hover for the given offset
	 */
	protected ITextHover getTextHover(int offset) {
		return (ITextHover) selectContentTypePlugin(offset, fTextHovers);
	}
	
	/**
	 * Returns the text hovering controller of this viewer.
	 * 
	 * @return the text hovering controller of this viewer
	 */
	protected AbstractInformationControlManager getTextHoveringController() {
		return fTextHoverManager;
	}
	
	/**
	 * Sets the creator for the hover controls.
	 *  
	 * @param creator the hover control creator
	 */
	public void setHoverControlCreator(IInformationControlCreator creator) {
		fHoverControlCreator= creator;
	}
	
	/*
	 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
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
	 * @see IWidgetTokenOwner#releaseWidgetToken(IWidgetTokenKeeper)
	 */
	public void releaseWidgetToken(IWidgetTokenKeeper tokenKeeper) {
		if (fWidgetTokenKeeper == tokenKeeper)
			fWidgetTokenKeeper= null;
	}
	
	
	//---- Selection
	
	/*
	 * @see ITextViewer#getSelectedRange
	 */
	public Point getSelectedRange() {
		
		if (!redraws())
			return new Point(fDocumentSelection.x, fDocumentSelection.y);
			
		if (fTextWidget != null) {
			Point p= fTextWidget.getSelectionRange();
			int offset= getVisibleRegionOffset();			
			return new Point(p.x + offset, p.y);
		}
		
		return new Point(-1, -1);
	}
	
	/*
	 * @see ITextViewer#setSelectedRange
	 */
	public void setSelectedRange(int offset, int length) {
		
		if (!redraws()) {
			fDocumentSelection.x= offset;
			fDocumentSelection.y= length;
			return;
		}
		
		if (fTextWidget == null)
			return;
			
		int end= offset + length;
		
		IDocument document= getVisibleDocument();
		if (document == null)
			return;
			
		if (document instanceof ChildDocument) {
			Position p= ((ChildDocument) document).getParentDocumentRange();
			if (p.overlapsWith(offset, length)) {
				
				if (offset < p.getOffset())
					offset= p.getOffset();
				offset -= p.getOffset();	
				
				int e= p.getOffset() + p.getLength();
				if (end > e)
					end= e;
				end -= p.getOffset();
				
			} else
				return; 
		}			
		
		length= end - offset;
		
		int[] selectionRange= new int[] { offset, length };
		validateSelectionRange(selectionRange);
		if (selectionRange[0] >= 0 && selectionRange[1] >= 0) {
			fTextWidget.setSelectionRange(selectionRange[0], selectionRange[1]);
			selectionChanged(selectionRange[0], selectionRange[1]);
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
	 */
	protected void validateSelectionRange(int[] selectionRange) {
		
		IDocument document= getVisibleDocument();
		int documentLength= document.getLength();
		
		int offset= selectionRange[0];
		int length= selectionRange[1];
		
		
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
		
		selectionRange[0]= offset;
		selectionRange[1]= length;
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
	 * @see ITextViewer#getSelectionProvider
	 */
	public ISelectionProvider getSelectionProvider() {
		return this;
	}
	
	/**
	 * Sends out a text selection changed event to all registered listeners.
	 *
	 * @param offset the offset of the newly selected range in the visible document
	 * @param length the length of the newly selected range in the visible document
	 */
	protected void selectionChanged(int offset, int length) {
		if (redraws()) {
			ISelection selection= new TextSelection(getDocument(), getVisibleRegionOffset() + offset, length);
			SelectionChangedEvent event= new SelectionChangedEvent(this, selection);
			fireSelectionChanged(event);
		}
	}
	
	/**
	 * Sends out a mark selection changed event to all registered listeners.
	 * 
	 * @param offset the offset of the mark selection in the visible document, the offset is <code>-1</code> if the mark was cleared
	 * @param length the length of the mark selection, may be negative if the caret is before the mark.
	 */
	protected void markChanged(int offset, int length) {
		if (redraws()) {
			if (offset != -1)
				offset += getVisibleRegionOffset();
			ISelection selection= new MarkSelection(getDocument(), offset, length);
			SelectionChangedEvent event= new SelectionChangedEvent(this, selection);
			fireSelectionChanged(event);
		}
	}
	
	
	//---- Text listeners
	
	/*
	 * @see ITextViewer#addTextListener
	 */
	public void addTextListener(ITextListener listener) {
		if (fTextListeners == null)
			fTextListeners= new ArrayList();
	
		if (!fTextListeners.contains(listener))
			fTextListeners.add(listener);
	}
	
	/*
	 * @see ITextViewer#removeTextListener
	 */
	public void removeTextListener(ITextListener listener) {
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
			if (event instanceof ChildDocumentEvent)
				event= ((ChildDocumentEvent) event).getParentEvent();
				
			TextEvent e= new TextEvent(cmd.start, cmd.length, cmd.text, cmd.preservedText, event, redraws());
			for (int i= 0; i < fTextListeners.size(); i++) {
				ITextListener l= (ITextListener) fTextListeners.get(i);
				l.textChanged(e);
			}
		}
	}
	
	//---- Text input listeners
	
	/*
	 * @see ITextViewer#addTextInputListener
	 */
	public void addTextInputListener(ITextInputListener listener) {
		if (fTextInputListeners == null)
			fTextInputListeners= new ArrayList();
	
		if (!fTextInputListeners.contains(listener))
			fTextInputListeners.add(listener);
	}
	
	/*
	 * @see ITextViewer#removeTextInputListener
	 */
	public void removeTextInputListener(ITextInputListener listener) {
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
	 * Informs all registered text input listeners about the sucessful input change,
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
	 * @see Viewer#getInput
	 */
	public Object getInput() {
		return getDocument();
	}
	
	/*
	 * @see ITextViewer#getDocument
	 */
	public IDocument getDocument() {
		return fDocument;
	}
	
	/*
	 * @see Viewer#setInput
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
		
		inputChanged(fDocument, oldDocument);
		
		fireInputDocumentChanged(oldDocument, fDocument);
		fReplaceTextPresentation= false;
	}
	
	/*
	 * @see ITextViewer#setDocument(IDocument, int int)
	 */
	public void setDocument(IDocument document, int visibleRegionOffset, int visibleRegionLength) {
		
		fReplaceTextPresentation= true;
		fireInputDocumentAboutToBeChanged(fDocument, document);
				
		IDocument oldDocument= fDocument;
		fDocument= document;
		
		try {
			int line= fDocument.getLineOfOffset(visibleRegionOffset);
			int offset= fDocument.getLineOffset(line);
			int length= (visibleRegionOffset - offset) + visibleRegionLength;
			setVisibleDocument(getChildDocumentManager().createChildDocument(fDocument, offset, length));
		} catch (BadLocationException x) {
			throw new IllegalArgumentException(JFaceTextMessages.getString("TextViewer.error.invalid_visible_region_1")); //$NON-NLS-1$
		}
		
		inputChanged(fDocument, oldDocument);
		
		fireInputDocumentChanged(oldDocument, fDocument);
		fReplaceTextPresentation= false;
	}		
	
	//---- Viewports	
	
	/**
	 * Initializes all listeners and structures required to set up viewport listeners.
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
	 * Removes all listeners and structures required to set up viewport listeners.
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
	 * @see ITextViewer#addViewportListener
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
	 * @see ITextViewer#removeViewportListener
	 */
	public void removeViewportListener(IViewportListener listener) {
		if (fViewportListeners != null)
			fViewportListeners.remove(listener);
	}
		
	/**
	 * Checks whether the viewport changed and if so informs all registered 
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
	 * @see ITextViewer#getTopIndex
	 */
	public int getTopIndex() {
		
		if (fTextWidget != null) {
			
			int top= fTextWidget.getTopIndex();
			
			int offset= getVisibleRegionOffset();
			if (offset > 0) {
				try {
					top += getDocument().getLineOfOffset(offset);
				} catch (BadLocationException x) {
					if (TRACE_ERRORS)
						System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.getTopIndex")); //$NON-NLS-1$
					return -1;
				}
			}
			
			return top;
		}
					
		return -1;
	}
		
	/*
	 * @see ITextViewer#setTopIndex
	 */
	public void setTopIndex(int index) {
		
		if (fTextWidget != null) {
			
			int offset= getVisibleRegionOffset();
			if (offset > 0) {
				try {
					index -= getDocument().getLineOfOffset(offset);
				} catch (BadLocationException x) {
					if (TRACE_ERRORS)
						System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.setTopIndex_1")); //$NON-NLS-1$
					return;
				}
			}
			
			if (index >= 0) {
				
				int lines= getVisibleLinesInViewport();
				if (lines > -1 ) {					
					IDocument d= getVisibleDocument();
					int last= d.getNumberOfLines() - lines;
					if (last > 0 && index  > last)
						index= last;
					
					fTextWidget.setTopIndex(index);
					updateViewportListeners(INTERNAL);
				
				} else
					fTextWidget.setTopIndex(index);
			}
		}
	}
	
	/**
	 * Returns the viewport height in lines.
	 * The actual visible lines can be fewer if the document is shorter than the viewport.
	 *
	 * @return the viewport height in lines
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
	 * @see ITextViewer#getBottomIndex
	 */
	public int getBottomIndex() {
		
		if (fTextWidget == null)
			return -1;
		
		IRegion r= getVisibleRegion();
		
		try {
			
			IDocument d= getDocument();
			int startLine= d.getLineOfOffset(r.getOffset());
			int endLine= d.getLineOfOffset(r.getOffset()  + r.getLength() - 1);
			int lines= getVisibleLinesInViewport();
			
			if (startLine + lines < endLine)
				return getTopIndex() + lines - 1;
				
			return endLine;
			
		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.getBottomIndex")); //$NON-NLS-1$
		}
		
		return -1;
	}
	
	/*
	 * @see ITextViewer#getTopIndexStartOffset
	 */
	public int getTopIndexStartOffset() {
		
		if (fTextWidget != null) {	
			int top= fTextWidget.getTopIndex();
			try {
				top= getVisibleDocument().getLineOffset(top);
				return top + getVisibleRegionOffset();
			} catch (BadLocationException ex) {
				if (TRACE_ERRORS)
					System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.getTopIndexStartOffset")); //$NON-NLS-1$
			}
		}
		
		return -1;
	}
	
	/*
	 * @see ITextViewer#getBottomIndexEndOffset
	 */
	public int getBottomIndexEndOffset() {
		try {
			
			IRegion line= getDocument().getLineInformation(getBottomIndex());
			int bottomEndOffset= line.getOffset() + line.getLength() - 1;
			
			IRegion region= getVisibleRegion();
			int visibleRegionEndOffset=  region.getOffset() + region.getLength() - 1;
			return visibleRegionEndOffset < bottomEndOffset ? visibleRegionEndOffset : bottomEndOffset;
				
		} catch (BadLocationException ex) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.getBottomIndexEndOffset")); //$NON-NLS-1$
			return getDocument().getLength() - 1;
		}
	}
	
	/*
	 * @see ITextViewer#revealRange
	 */
	public void revealRange(int start, int length) {
		
		if (fTextWidget == null || !redraws())
			return;
		
		int end= start + length;

		IDocument document= getVisibleDocument();
		if (document == null)
			return;
			
		Position p= (document instanceof ChildDocument)
			? ((ChildDocument) document).getParentDocumentRange()
			: new Position(0, document.getLength());
			
		if (p.overlapsWith(start, length)) {
				
			if (start < p.getOffset())
				start= p.getOffset();
			start -= p.getOffset();	
				
			int e= p.getOffset() + p.getLength();				
			if (end > e)
				end= e;
			end -= p.getOffset();
				
		} else {
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=15159
			start= start < p.getOffset() ? 0 : p.getLength();
			end= start;
		}
		
		internalRevealRange(start, end);
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
				
				int lines= getVisibleLinesInViewport();
				int bottom= top + lines;
				
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
					IRegion line= doc.getLineInformation(startLine);
					startPixel= getWidthInPixels(line.getOffset(), start - line.getOffset());
					endPixel= getWidthInPixels(line.getOffset(), line.getLength());
				} else {
					int lineStart= doc.getLineOffset(startLine);
					startPixel= getWidthInPixels(lineStart, start - lineStart);
					endPixel= getWidthInPixels(lineStart, end - lineStart);
				}
				
				int visibleStart= fTextWidget.getHorizontalPixel();
				int visibleEnd= visibleStart + fTextWidget.getClientArea().width;
				
				// scroll only if not yet visible
				if (startPixel < visibleStart || visibleEnd < endPixel) {
					
					// set buffer zone to 10 pixels
					bufferZone= 10;
					
					int newOffset= visibleStart;
					if (startPixel < visibleStart)
						newOffset= startPixel;
					else if (endPixel - startPixel  + bufferZone < visibleEnd - visibleStart)
						newOffset= visibleStart + (endPixel - visibleEnd + bufferZone);
					else
						newOffset= startPixel;
						
					fTextWidget.setHorizontalIndex(newOffset / getAverageCharWidth());
				}
				
			}
		} catch (BadLocationException e) {
			throw new IllegalArgumentException(JFaceTextMessages.getString("TextViewer.error.invalid_range")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the width of the text when being drawed into this viewer's widget.
	 * 
	 * @param the string to messure
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
	 * Returns the width of the representation of a text range in the
	 * visible region of the viewer's document as drawn in this viewer's
	 * widget.
	 * 
	 * @param offset the offset of the text range in the visible region
	 * @param length the length of the text range in the visible region
	 * @return the width of the presentation of the specified text range
	 */
	final protected int getWidthInPixels(int offset, int length) {		
		
		Point left= fTextWidget.getLocationAtOffset(offset);
		Point right= new Point(left.x, left.y);
		
		int end= offset + length;
		for (int i= offset +1; i <= end; i++) {
			
			Point p= fTextWidget.getLocationAtOffset(i);
			
			if (left.x > p.x)
				left.x= p.x;
								
			if (right.x  < p.x)
				right.x= p.x;				
		}
		
		return  right.x - left.x;
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
	 * @see Viewer#refresh
	 */
	public void refresh() {
		setDocument(getDocument());
	}
	
	//---- visible range support
	
	/**
	 * Returns the child document manager
	 *
	 * @return the child document manager
	 */
	private ChildDocumentManager getChildDocumentManager() {
		if (fChildDocumentManager == null)
			fChildDocumentManager= new ChildDocumentManager();
		return fChildDocumentManager;
	}
	
	/**
	 * Invalidates the current presentation by sending an initialization
	 * event to all text listener.
	 */
	public final void invalidateTextPresentation() {
		if (fVisibleDocument != null) {
			fWidgetCommand.start= 0;
			fWidgetCommand.length= 0;
			fWidgetCommand.text= fVisibleDocument.get();
			fWidgetCommand.event= null;
			updateTextListeners(fWidgetCommand);
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
	 * Sets this viewer's visible document. The visible document represents the 
	 * visible region of the viewer's input document.
	 *
	 * @param document the visible document
	 */
	private void setVisibleDocument(IDocument document) {
		
		if (fVisibleDocument != null && fDocumentListener != null)
			fVisibleDocument.removeDocumentListener(fDocumentListener);
		
		fVisibleDocument= document;
		
		initializeWidgetContents();
		resetPlugins();
		
		if (fVisibleDocument != null && fDocumentListener != null)
			fVisibleDocument.addDocumentListener(fDocumentListener);
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
	protected int getVisibleRegionOffset() {
		
		IDocument document= getVisibleDocument();
		if (document instanceof ChildDocument) {
			ChildDocument cdoc= (ChildDocument) document;
			return cdoc.getParentDocumentRange().getOffset();
		}
		
		return 0;
	}
	
	/*
	 * @see ITextViewer#getVisibleRegion
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
	 * @see ITextViewer#setVisibleRegion
	 */
	public void setVisibleRegion(int start, int length) {
		
		IRegion region= getVisibleRegion();
		if (start == region.getOffset() && length == region.getLength()) {
			// nothing to change
			return;
		}
		
		ChildDocument child= null;
		IDocument parent= getVisibleDocument();
		
		if (parent instanceof ChildDocument) {
			child= (ChildDocument) parent;
			parent= child.getParentDocument();
		}
		
		try {
			
			int line= parent.getLineOfOffset(start);
			int offset= parent.getLineOffset(line);
			length += (start - offset);
			
			if (child != null) {
				child.setParentDocumentRange(offset, length);
			} else {
				child= getChildDocumentManager().createChildDocument(parent, offset, length);
			}
			
			setVisibleDocument(child);
							
		} catch (BadLocationException x) {
			throw new IllegalArgumentException(JFaceTextMessages.getString("TextViewer.error.invalid_visible_region_2")); //$NON-NLS-1$
		}
	}
				
	/*
	 * @see ITextViewer#resetVisibleRegion
	 */
	public void resetVisibleRegion() {
		IDocument document= getVisibleDocument();
		if (document instanceof ChildDocument) {			
			ChildDocument child = (ChildDocument) document;
			setVisibleDocument(child.getParentDocument());
			getChildDocumentManager().freeChildDocument(child);
		}
	}
	
	/*
	 * @see ITextViewer#overlapsWithVisibleRegion
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
		
	
	
	//--------------------------------------
	
	/*
	 * @see ITextViewer#setTextDoubleClickStrategy
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
	 * @param offset the offset for which to find the plugin
	 * @return the plugin registered under the offset's content type 
	 */
	protected Object selectContentTypePlugin(int offset, Map plugins) {
		try {
			return selectContentTypePlugin(getDocument().getContentType(offset), plugins);
		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.selectContentTypePlugin")); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Selects from the given <code>plugins</code> this one which is registered for
	 * the given content <code>type</code>.
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
	 * to the installed <code>IAutoIndentStrategy</code>.
	 *
	 * @param command the document command representing the verify event
	 */
	protected void customizeDocumentCommand(DocumentCommand command) {
		if (!fIgnoreAutoIndent) {
			IAutoIndentStrategy s= (IAutoIndentStrategy) selectContentTypePlugin(command.offset, fAutoIndentStrategies);
			if (s != null)
				s.customizeDocumentCommand(getDocument(), command);
		}
		fIgnoreAutoIndent= false;
	}
	
	/**
	 * @see VerifyListener#verifyText
	 */
	protected void handleVerifyEvent(VerifyEvent e) {
				
		if (fEventConsumer != null) {
			fEventConsumer.processEvent(e);
			if (!e.doit)
				return;
		}
		
		int offset= getVisibleRegionOffset();
		fDocumentCommand.setEvent(e, offset);
		customizeDocumentCommand(fDocumentCommand);
		if (!fDocumentCommand.fillEvent(e, offset)) {
			try {
				fVerifyListener.forward(false);
				getDocument().replace(fDocumentCommand.offset, fDocumentCommand.length, fDocumentCommand.text);
			} catch (BadLocationException x) {
				if (TRACE_ERRORS)
					System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.verifyText")); //$NON-NLS-1$
			} finally {
				fVerifyListener.forward(true);
			}
		}	
	}
	
	//---- text manipulation

	private boolean isMarkedRegionEmpty() {

		if (fTextWidget == null)
			return true;

		IRegion region= getVisibleRegion();
		int offset= region.getOffset();
		int length= region.getLength();

		return
			fMarkPosition == null ||
			fMarkPosition.isDeleted() ||
			fMarkPosition.offset < offset ||
			fMarkPosition.offset > offset + length;
	}

	/*
	 * @see ITextViewer#canDoOperation
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
			case SHIFT_RIGHT:
			case SHIFT_LEFT:
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
	 * @see ITextViewer#doOperation
	 */
	public void doOperation(int operation) {
		
		if (fTextWidget == null || !redraws())
			return;

		switch (operation) {

			case UNDO:
				if (fUndoManager != null) {
					fIgnoreAutoIndent= true;
					fUndoManager.undo();
				}
				break;
			case REDO:
				if (fUndoManager != null) {
					fIgnoreAutoIndent= true;
					fUndoManager.redo();
				}
				break;
			case CUT:
				if (fTextWidget.getSelectionCount() == 0)
					copyMarkedRegion(true);
				else
					fTextWidget.cut();
				break;
			case COPY:
				if (fTextWidget.getSelectionCount() == 0)
					copyMarkedRegion(false);
				else
					fTextWidget.copy();
				break;
			case PASTE:
				fIgnoreAutoIndent= true;
				fTextWidget.paste();
				break;
			case DELETE:
				deleteText();
				break;
			case SELECT_ALL:
				setSelectedRange(getVisibleRegionOffset(), getVisibleDocument().getLength());
				break;
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
	
	/*
	 * @see ITextOperationTargetExtension#enableOperation(int, boolean)
	 */
	public void enableOperation(int operation, boolean enable) {
		/* 
		 * No-op by default.
		 * Will be changed to regularily disable the known operations.
		 */
	}
	
	/*
	 * Copies/cuts the marked region.
	 */
	private void copyMarkedRegion(boolean delete) {
		
		if (fTextWidget == null)
			return;

		IRegion region= getVisibleRegion();
		int offset= region.getOffset();
		int length= region.getLength();

		if (fMarkPosition == null || fMarkPosition.isDeleted() ||
			fMarkPosition.offset < offset || fMarkPosition.offset > offset + length)
			return;
					
		int markOffset= fMarkPosition.offset - offset;
		
		Point selection= fTextWidget.getSelection();		
		if (selection.x <= markOffset)
			fTextWidget.setSelection(selection.x, markOffset);
		else
			fTextWidget.setSelection(markOffset, selection.x);

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
	 * Returns whether one or multiple lines are selected.
	 * 
	 * @return <code>true</code> if one or multiple lines are selected
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
	 * The prefixes must start at the beginnig of the line.
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
	 * @param ignoreWhitespace says whether whitepsace in front of prefixes is allowed
	 */
	protected void shift(boolean useDefaultPrefixes, boolean right, boolean ignoreWhitespace) {
		
		if (fUndoManager != null)
			fUndoManager.beginCompoundChange();
			
		setRedraw(false);
		startSequentialRewriteMode(true);

		IDocument d= getDocument();
		IDocumentPartitioner partitioner= null;
		
		try {
			
			Point selection= getSelectedRange();
			IRegion block= getTextBlockFromSelection(selection);
			ITypedRegion[] regions= d.computePartitioning(block.getOffset(), block.getLength());

			int lineCount= 0;			
			int[] lines= new int[regions.length * 2]; // [startline, endline, startline, endline, ...]
			for (int i= 0, j= 0; i < regions.length; i++, j+= 2) {
				// start line of region
				lines[j]= getFirstCompleteLineOfRegion(regions[i]);
				// end line of region
				int offset= regions[i].getOffset() + regions[i].getLength() - 1;
				lines[j + 1]= (lines[j] == -1 ? -1 : d.getLineOfOffset(offset));
				lineCount += lines[j + 1] - lines[j] + 1;
			}
			
			if (lineCount >= 20) {
				partitioner= d.getDocumentPartitioner();
				if (partitioner != null) {
					partitioner.disconnect();
					d.setDocumentPartitioner(null);
				}
			}
			
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

			if (partitioner != null) {
				partitioner.connect(d);
				d.setDocumentPartitioner(partitioner);
			}
			
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
	 * @param prefixes the prefixes to be used for shifting
	 * @param right if <code>true</code> shift to the right otherwise to the left
	 * @param startLine the first line to shift
	 * @param endLine the last line to shift
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
					} else 
						occurrences[i]= new Region(index, length);
				} else {
					// found a line which cannot be shifted
					return;
				}
			}
			
			// ok - change the document
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
	 * This implementation brings up a print dialog, then 
	 * calls printContents(Printer), which performs the actual print.
	 *
	 * Subclasses may override.
	 */
	protected void print() {
		
		final PrintDialog dialog= new PrintDialog(fTextWidget.getShell(), SWT.NULL);
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
	 * @see IFindReplaceTarget#canPerformFind
	 */
	protected boolean canPerformFind() {
		IDocument d= getVisibleDocument();
		return (fTextWidget != null && d != null && d.getLength() > 0);
	}
	
	/**
	 * @see IFindReplaceTarget#findAndSelect(int, String, boolean, boolean, boolean)
	 */
	protected int findAndSelect(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord) {
		if (fTextWidget == null)
			return -1;
			
		try {
			int offset= (startPosition == -1 ? startPosition : startPosition - getVisibleRegionOffset());
			int pos= getVisibleDocument().search(offset, findString, forwardSearch, caseSensitive, wholeWord);
			if (pos > -1) {
				int length= findString.length();
				if (redraws()) {
					fTextWidget.setSelectionRange(pos, length);
					internalRevealRange(pos, pos + length);
					selectionChanged(pos, length);
				} else {
					setSelectedRange(pos, length);
				}
			}
			return pos + getVisibleRegionOffset();
		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.findAndSelect")); //$NON-NLS-1$
		}
		
		return -1;
	}
	
	/**
	 * Performs a search similar to <code>IFindReplaceTarget.findAndSelect()</code> within the given
	 * range of the viewer's visible document.
	 */
	private int findAndSelectInRange(int startPosition, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, int rangeOffset, int rangeLength) {
		if (fTextWidget == null)
			return -1;
			
		try {
			int offset;
			if (forwardSearch && (startPosition == -1 || startPosition < rangeOffset)) {
				offset= rangeOffset;
			} else if (!forwardSearch && (startPosition == -1 || startPosition > rangeOffset + rangeLength)) {
				offset= rangeOffset + rangeLength;
			} else {
				offset= startPosition;
			}
			offset -= getVisibleRegionOffset();
			
			int pos= getVisibleDocument().search(offset, findString, forwardSearch, caseSensitive, wholeWord);

			int length =  findString.length();
			if (pos != -1 && (pos + getVisibleRegionOffset() < rangeOffset || pos + getVisibleRegionOffset() + length > rangeOffset + rangeLength))
				pos= -1;

			if (pos > -1) {
				if (redraws()) {
					fTextWidget.setSelectionRange(pos, length);
					internalRevealRange(pos, pos + length);
					selectionChanged(pos, length);
				} else {
					setSelectedRange(pos, length);
				}
			}
			return pos + getVisibleRegionOffset();
		} catch (BadLocationException x) {
			if (TRACE_ERRORS)
				System.out.println(JFaceTextMessages.getString("TextViewer.error.bad_location.findAndSelect")); //$NON-NLS-1$
		}
		
		return -1;
	}	
	
	//---------- text presentation support
	
	/*
	 * @see ITextViewer#setTextColor
	 */
	public void setTextColor(Color color) {
		if (color != null)
			setTextColor(color, 0, getDocument().getLength(), true);
	}
	
	/*
	 * @see ITextViewer#setTextColor 	 
	 */
	public void setTextColor(Color color, int start, int length, boolean controlRedraw) {	
		
		if (fTextWidget != null) {
									
			if (controlRedraw)
				fTextWidget.setRedraw(false); 
			
			StyleRange s= new StyleRange();
			s.foreground= color;
			s.start= start;
			s.length= length;
			
			fTextWidget.setStyleRange(s);
			
			if (controlRedraw)
				fTextWidget.setRedraw(true);
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
			
			fTextWidget.setStyleRange(range);
			Iterator e= presentation.getNonDefaultStyleRangeIterator();
			while (e.hasNext()) {
				range= (StyleRange) e.next();
				fTextWidget.setStyleRange(range);
			}
		
		} else {
			
			Iterator e= presentation.getAllStyleRangeIterator();
			
			// use optimized StyledText
			StyleRange[] ranges= new StyleRange[presentation.getDenumerableRanges()];
			for (int i= 0; i < ranges.length; i++)
				ranges[i]= (StyleRange) e.next();
				
			IRegion region= presentation.getCoverage();
			fTextWidget.replaceStyleRanges(region.getOffset(), region.getLength(), ranges);
		}	
	}
	
	/**
	 * Returns the visible region if it is not equal to the whole document.
	 * Otherwise returns <code>null</code>.
	 *
	 * @return the viewer's visible region if smaller than input document, otherwise <code>null</code>
	 */
	protected IRegion internalGetVisibleRegion() {
		
		IDocument document= getVisibleDocument();
		if (document instanceof ChildDocument) {
			Position p= ((ChildDocument) document).getParentDocumentRange();
			return new Region(p.getOffset(), p.getLength());
		}		
		
		return null;
	}
	
	/*
	 * @see ITextViewer#changeTextPresentation
	 */
	public void changeTextPresentation(TextPresentation presentation, boolean controlRedraw) {
				
		if (presentation == null)
			return;
			
		presentation.setResultWindow(internalGetVisibleRegion());
		if (presentation.isEmpty() || fTextWidget == null)
			return;
					
		if (controlRedraw)
			fTextWidget.setRedraw(false);
		
		if (fReplaceTextPresentation)
			TextPresentation.applyTextPresentation(presentation, fTextWidget);
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

	/*
	 * @see ITextViewer#getTextOperationTarget()
	 */
	public ITextOperationTarget getTextOperationTarget() {
		return this;
	}

	/*
	 * @see ITextViewerExtension#appendVerifyKeyListener(VerifyKeyListener)
	 */
	public void appendVerifyKeyListener(VerifyKeyListener listener) {
		int index= fVerifyKeyListenersManager.numberOfListeners();
		fVerifyKeyListenersManager.insertListener(listener, index);
	}
	
	/*
	 * @see ITextViewerExtension#prependVerifyKeyListener(VerifyKeyListener)
	 */
	public void prependVerifyKeyListener(VerifyKeyListener listener) {
		fVerifyKeyListenersManager.insertListener(listener, 0);
		
	}
	
	/*
	 * @see ITextViewerExtension#removeVerifyKeyListener(VerifyKeyListener)
	 */
	public void removeVerifyKeyListener(VerifyKeyListener listener) {
		fVerifyKeyListenersManager.removeListener(listener);
	}

	/*
	 * @see ITextViewerExtension#getMark()
	 */
	public int getMark() {
		return fMarkPosition == null || fMarkPosition.isDeleted()
			? -1
			: fMarkPosition.getOffset();
	}

	/*
	 * @see ITextViewerExtension#setMark(int)
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
			if (fMarkPosition == null) {

				IDocument document= getDocument();
				if (document == null)
					return;			

				if (offset < 0 || offset > document.getLength())
					return;

				try {	
					Position position= new Position(offset);			
					document.addPosition(MARK_POSITION_CATEGORY, position);
					fMarkPosition= position;

				} catch (BadLocationException e) {
					return;
				} catch (BadPositionCategoryException e) {
					return;
				}
		
			} else {

				IDocument document= getDocument();
				if (document == null) {
					fMarkPosition= null;
					return;			
				}				
				
				if (offset < 0 || offset > document.getLength())
					return;

				fMarkPosition.setOffset(offset);
				fMarkPosition.undelete();
			}

			markChanged(fMarkPosition.offset - getVisibleRegionOffset(), 0);
		}		
	}

	/**
	 * @see Viewer#inputChanged(Object, Object)
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

		super.inputChanged(newInput, oldInput);

		IDocument newDocument= (IDocument) newInput;
		if (newDocument != null) {
			newDocument.addPositionCategory(MARK_POSITION_CATEGORY);
			newDocument.addPositionUpdater(fMarkPositionUpdater);			
		}
	}
	
	/**
	 * Informs all text listeners about the change of the viewer's redraw state.
	 */
	private void fireRedrawChanged() {
		fWidgetCommand.start= 0;
		fWidgetCommand.length= 0;
		fWidgetCommand.text= null;
		fWidgetCommand.event= null;
		updateTextListeners(fWidgetCommand);
	}
	
	/**
	 * Enables the redrawing of this text viewer. Subclasses may extend.
	 */
	protected void enabledRedrawing() {
		if (fDocumentAdapter instanceof IDocumentAdapterExtension) {
			IDocumentAdapterExtension extension= (IDocumentAdapterExtension) fDocumentAdapter;
			StyledText textWidget= getTextWidget();
			if (textWidget != null && !textWidget.isDisposed()) {
				int topPixel= textWidget.getTopPixel();	
				extension.resumeForwardingDocumentChanges();
				if (topPixel > -1) {
					try {
						textWidget.setTopPixel(topPixel);
					} catch (IllegalArgumentException x) {
						// changes don't allow for the previous top pixel
					}
				}
			}
		}
		
		setSelectedRange(fDocumentSelection.x, fDocumentSelection.y);
		revealRange(fDocumentSelection.x, fDocumentSelection.y);
		
		if (fTextWidget != null && !fTextWidget.isDisposed())
			fTextWidget.setRedraw(true);
			
		fireRedrawChanged();
	}
	
	/**
	 * Disables the redrawing of this text viewer. Subclasses may extend.
	 */
	protected void disableRedrawing() {
		
		fDocumentSelection= getSelectedRange();
		
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
	 */
	public final void setRedraw(boolean redraw) {
		if (!redraw) {
			if (fRedrawCounter == 0)
				disableRedrawing();
			++ fRedrawCounter;
		} else {
			-- fRedrawCounter;
			if (fRedrawCounter == 0)
				enabledRedrawing();
		}
	}
	
	/**
	 * Returns whether this viewer redraws itself.
	 * 
	 * @return <code>true</code> if this viewer redraws itself
	 */
	protected final boolean redraws() {
		return fRedrawCounter <= 0;
	}
	
	/**
	 * Starts  the sequential rewrite mode of the viewer's document.
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
	 */
	public IRewriteTarget getRewriteTarget() {
		if (fRewriteTarget == null)
			fRewriteTarget= new RewriteTarget();
		return fRewriteTarget;
	}
}

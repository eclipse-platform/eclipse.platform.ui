/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.contentmergeviewer;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.io.InputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.compare.internal.BufferedCanvas;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.internal.TokenComparator;
import org.eclipse.compare.internal.ChangePropertyAction;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;

/**
 * A text merge viewer uses the <code>RangeDifferencer</code> to perform a
 * textual, line-by-line comparison of two (or three) input documents.
 * It is based on the <code>ContentMergeViewer</code> and uses <code>TextViewer</code>s
 * to implement the ancestor, left, and right content areas.
 * <p>
 * In the three-way compare case ranges of differing lines are highlighted and framed
 * with different colors to show whether the difference is an incoming, outgoing, or conflicting change.
 * The <code>TextMergeViewer</code> supports the notion of a current "differing range"
 * and provides toolbar buttons to navigate from one range to the next (or previous).
 * <p>
 * If there is a current "differing range" and the underlying document is editable
 * the <code>TextMergeViewer</code> enables actions in context menu and toolbar to
 * copy a range from one side to the other side, thereby performing a merge operation.
 * <p>
 * In addition to a line-by-line comparison the <code>TextMergeViewer</code>
 * uses a token based compare on differing lines.
 * The token compare is activated when navigating into
 * a range of differing lines. At first the lines are selected as a block.
 * When navigating into this block the token compare shows for every line 
 * the differing token by selecting them.
 * <p>
 * The <code>TextMergeViewer</code>'s default token compare works on characters separated
 * by whitespace. If a different strategy is needed (for example, Java tokens in
 * a Java-aware merge viewer), clients can create their own token
 * comparators by implementing the <code>ITokenComparator</code> interface and overriding the
 * <code>TextMergeViewer.createTokenComparator</code> factory method).
 * <p>
 * Access to the <code>TextMergeViewer</code>'s model is by means of an
 * <code>IMergeViewerContentProvider</code>. Its <code>get<it>X</it></code>Content</code> methods must return
 * either an <code>IDocument</code>, an <code>IDocumentRange</code>, or an <code>IStreamContentAccessor</code>.
 * In the <code>IDocumentRange</code> case the <code>TextMergeViewer</code>
 * works on a subrange of a document. In the <code>IStreamContentAccessor</code> case
 * a document is created internally and initialized from the stream.
 * <p>
 * A <code>TextMergeViewer</code> can be used as is. However clients may subclass
 * to customize the behavior. For example a <code>MergeTextViewer</code> for Java would override
 * the <code>configureTextViewer</code> method to configure the <code>TextViewer</code> for Java source code,
 * the <code>createTokenComparator</code> method to create a Java specific tokenizer.
 *
 * @see org.eclipse.compare.rangedifferencer.RangeDifferencer
 * @see org.eclipse.jface.text.TextViewer
 * @see ITokenComparator
 * @see IDocumentRange
 * @see org.eclipse.compare.IStreamContentAccessor
 */
public class TextMergeViewer extends ContentMergeViewer  {
	
	private static final String[] GLOBAL_ACTIONS= {
		IWorkbenchActionConstants.UNDO,
		IWorkbenchActionConstants.REDO,
		IWorkbenchActionConstants.CUT,
		IWorkbenchActionConstants.COPY,
		IWorkbenchActionConstants.PASTE,
		IWorkbenchActionConstants.DELETE,
		IWorkbenchActionConstants.SELECT_ALL
	};
	private static final String[] TEXT_ACTIONS= {
		MergeSourceViewer.UNDO_ID,
		MergeSourceViewer.REDO_ID,
		MergeSourceViewer.CUT_ID,
		MergeSourceViewer.COPY_ID,
		MergeSourceViewer.PASTE_ID,
		MergeSourceViewer.DELETE_ID,
		MergeSourceViewer.SELECT_ALL_ID
	};
		
	private static final String MY_UPDATER= "my_updater";
	
	private static final String SYNC_SCROLLING= "SYNC_SCROLLING";
	
	private static final String BUNDLE_NAME= "org.eclipse.compare.contentmergeviewer.TextMergeViewerResources";
		
	// constants
	/** Width of left and right vertical bar */
	private static final int MARGIN_WIDTH= 10;
	/** Width of center bar */
	private static final int CENTER_WIDTH= 40;
	/** */
	private static final int LW= 1;
	/** Provide more merge controls in Pane toolbar */
	private static final boolean USE_MORE_CONTROLS= true;
	/** Selects between smartTokenDiff and mergingTokenDiff */
	private static final boolean USE_MERGING_TOKEN_DIFF= false;
	/** if DEAD_STEP is true navigation with the next/previous buttons needs an extra step 
	when wrapping around the beginning or end */
	private static final boolean DEAD_STEP= false;
	
	private static final boolean IS_MOTIF= false;
		
	// Colors to use
	private static final RGB INCOMING= new RGB(100, 100, 200);
	private static final RGB INCOMING_FILL= new RGB(230, 230, 240);
	private static final RGB SELECTED_INCOMING= new RGB(0, 0, 255);
	private static final RGB SELECTED_INCOMING_FILL= new RGB(255, 255, 255);
	
	private static final RGB CONFLICT= new RGB(200, 100, 100);
	private static final RGB CONFLICT_FILL= new RGB(240, 230, 230);
	private static final RGB SELECTED_CONFLICT= new RGB(255, 0, 0);
	private static final RGB SELECTED_CONFLICT_FILL= new RGB(255, 255, 255);
	
	private static final RGB OUTGOING= new RGB(100, 100, 100);
	private static final RGB OUTGOING_FILL= new RGB(230, 230, 230);
	private static final RGB SELECTED_OUTGOING= new RGB(0, 0, 0);
	private static final RGB SELECTED_OUTGOING_FILL= new RGB(255, 255, 255);
	
	private IDocumentListener fDocumentListener;
	
	private	IPropertyChangeListener fPreferenceChangeListener;
	
	/** All diffs for calculating scrolling position (includes line ranges without changes) */
	private ArrayList fAllDiffs;
	/** Subset of above: just real differences. */
	private ArrayList fChangeDiffs;
	/** The current diff */
	private Diff fCurrentDiff;
	
	private MergeSourceViewer fAncestor;
	private MergeSourceViewer fLeft;
	private MergeSourceViewer fRight;
	
	private int fLeftLineCount;
	private int fRightLineCount;
	
	private boolean fLeftContentsChanged;
	private boolean fRightContentsChanged;

	private boolean fInScrolling;
	
	private int fPts[]= new int[8];	// scratch area for polygon drawing
	
	private ActionContributionItem fNextItem;	// goto next difference
	private ActionContributionItem fPreviousItem;	// goto previous difference
	private ActionContributionItem fCopyDiffLeftToRightItem;
	private ActionContributionItem fCopyDiffRightToLeftItem;
	
	private boolean fSynchronizedScrolling= true;
	
	private MergeSourceViewer fFocusPart;
	
	private boolean fSubDoc= true;
	private IPositionUpdater fPositionUpdater;
	

	// SWT widgets
	private BufferedCanvas fAncestorCanvas;
	private BufferedCanvas fLeftCanvas;
	private BufferedCanvas fRightCanvas;
	private ScrollBar fVScrollBar;
		
	// SWT resources to be disposed
	private Map fColors;


	/**
	 * A Diff represents synchronized character ranges in two or three Documents.
	 * The MergeTextViewer uses Diffs to find differences in line and token ranges.
	 */
	/* package */ class Diff {
		/** character range in ancestor document */
		Position fAncestorPos;
		/** character range in left document */
		Position fLeftPos;
		/** character range in right document */
		Position fRightPos;
		/** if this is a TokenDiff fParent points to the enclosing LineDiff */
		Diff fParent;	
		/** if Diff has been resolved */
		boolean fResolved;
		int fDirection;
		boolean fIsToken= false;
		ArrayList fDiffs;

		/**
		 * Create Diff from two ranges and an optional parent diff.
		 */
		Diff(Diff parent, int dir, IDocument ancestorDoc, int ancestorStart, int ancestorEnd,
							 IDocument leftDoc, int leftStart, int leftEnd,
							 IDocument rightDoc, int rightStart, int rightEnd) {
			fParent= parent != null ? parent : this;
			fDirection= dir;
			
			fLeftPos= createPosition(leftDoc, leftStart, leftEnd);
			fRightPos= createPosition(rightDoc, rightStart, rightEnd);
			if (ancestorDoc != null)
				fAncestorPos= createPosition(ancestorDoc, ancestorStart, ancestorEnd);
		}
		
		Position createPosition(IDocument doc, int start, int end) {
			try {
				int dl= doc.getLength();
				int l= end-start;
				if (start+l > dl)
					l= dl-start;
					
				Position p= null;
				try {
					p= new Position(start, l);
				} catch (RuntimeException ex) {
					System.out.println("Diff.createPosition: " + start + " " + l);
				}
				
				try {
					doc.addPosition(MY_UPDATER, p);
				} catch (BadPositionCategoryException ex) {
				}
				return p;
			} catch (BadLocationException ee) {
				//System.out.println("Diff.createPosition: " + start + " " + end);
			}
			return null;
		}

		void add(Diff d) {
			if (fDiffs == null)
				fDiffs= new ArrayList();
			fDiffs.add(d);
		}
		
		boolean isDeleted() {
			if (fAncestorPos != null && fAncestorPos.isDeleted())
				return true;
			return fLeftPos.isDeleted() || fRightPos.isDeleted();
		}

		void setResolved(boolean r) {
			fResolved= r;
			if (r)
				fDiffs= null;
		}

		boolean isResolved() {
			if (!fResolved && fDiffs != null) {
				Iterator e= fDiffs.iterator();
				while (e.hasNext()) {
					Diff d= (Diff) e.next();
					if (!d.isResolved())
						return false;
				}
				return true;
			}
			return fResolved;
		}

		Position getPosition(MergeSourceViewer w) {
			if (w == fLeft)
				return fLeftPos;
			if (w == fRight)
				return fRightPos;
			if (w == fAncestor)
				return fAncestorPos;
			return null;
		}
		
		/**
		 * Returns true if given character range overlaps with this Diff.
		 */
		boolean contains(MergeSourceViewer w, int start, int end) {
			Position h= getPosition(w);
			if (h != null) {
				int offset= h.getOffset();
				if (start >= offset) {
					int endPos= offset+h.getLength();
					if (end < endPos)
						return true;
					if (endPos == w.getDocument().getLength())
						return true;
				}
			}
			return false;
		}
				
		int getMaxDiffHeight(boolean withAncestor) {
			Point region= new Point(0, 0);
			int h= fLeft.getLineRange(fLeftPos, region).y;
			if (withAncestor)
				h= Math.max(h, fAncestor.getLineRange(fAncestorPos, region).y);
			return Math.max(h, fRight.getLineRange(fRightPos, region).y);
		}
	}

	//---- MergeTextViewer
	
	/**
	 * Creates a text merge viewer under the given parent control.
	 *
	 * @param parent the parent control
	 * @param configuration the configuration object
	 */
	public TextMergeViewer(Composite parent, CompareConfiguration configuration) {
		this(parent, SWT.NULL, configuration);
	}
	
	/**
	 * Creates a text merge viewer under the given parent control.
	 *
	 * @param parent the parent control
	 * @param style SWT style bits for top level composite of this viewer
	 * @param configuration the configuration object
	 */
	public TextMergeViewer(Composite parent, int style, CompareConfiguration configuration) {
		super(style, ResourceBundle.getBundle(BUNDLE_NAME), configuration);
		
		buildControl(parent);

		IPreferenceStore ps= CompareUIPlugin.getDefault().getPreferenceStore();
		if (ps != null) {
			fPreferenceChangeListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					TextMergeViewer.this.propertyChange(event);
				}
			};
			ps.addPropertyChangeListener(fPreferenceChangeListener);
			//fSynchronizedScrolling= ps.getBoolean(ComparePreferencePage.SYNCHRONIZE_SCROLLING);
		}
		//fSynchronizedScrolling= Utilities.getBoolean(configuration, SYNC_SCROLLING, fSynchronizedScrolling);
		
		fDocumentListener= new IDocumentListener() {
			
			public void documentAboutToBeChanged(DocumentEvent e) {
			}
			
			public void documentChanged(DocumentEvent e) {
				TextMergeViewer.this.documentChanged(e);
			}
		};
	}	
	
	public String getTitle() {
		return "Text Compare";
	}

	/**
	 * Configures the passed text viewer.
	 * This method is called after the three text viewers have been created for the
	 * content areas.
	 * The <code>TextMergeViewer</code> implementation of this method does nothing.
	 * Subclasses may reimplement to provide a specific configuration for the text viewer.
	 *
	 * @param textViewer the text viewer to configure
	 */
	protected void configureTextViewer(TextViewer textViewer) {
	}
				
	/**
	 * Creates an <code>ITokenComparator</code> which is used to show the
	 * intra line differences.
	 * The <code>TextMergeViewer</code> implementation of this method returns a 
	 * tokenizer that breaks a line into words separated by whitespace.
	 * Subclasses may reimplement to provide a specific tokenizer.
	 *
	 * @return a ITokenComparator which is used for a second level token compare.
	 */
	protected ITokenComparator createTokenComparator(String s) {
		return new TokenComparator(s);
	}
	
	/**
	 * Returns a document partitioner which is suitable for the underlying content type.
	 * This method is only called if the input provided by the content provider is a
	 * <code>IStreamContentAccessor</code> and an internal document must be created. This
	 * document is initialized with the partitioner returned from this method.
	 * <p>
	 * The <code>TextMergeViewer</code> implementation of this method returns 
	 * <code>null</code>. Subclasses may reimplement to create a partitioner for a 
	 * specific content type.
	 *
	 * @return a document partitioner, or <code>null</code>
	 */
	protected IDocumentPartitioner getDocumentPartitioner() {
		return null;
	}
	
	/**
	 * Called on the viewer disposal.
	 * Unregisters from the compare configuration.
	 * Clients may extend if they have to do additional cleanup.
	 */
	protected void handleDispose(DisposeEvent event) {
		
		if (fPreferenceChangeListener != null) {
			IPreferenceStore ps= CompareUIPlugin.getDefault().getPreferenceStore();
			if (ps != null)
				ps.removePropertyChangeListener(fPreferenceChangeListener);
			fPreferenceChangeListener= null;
		}
		
		fLeftCanvas= null;
		fRightCanvas= null;
		fVScrollBar= null;

		unsetDocument(fAncestor);
		unsetDocument(fLeft);
		unsetDocument(fRight);

		if (fColors != null) {
			Iterator i= fColors.values().iterator();
			while (i.hasNext()) {
				Color color= (Color) i.next();
				if (!color.isDisposed())
					color.dispose();
			}
		}
		
		super.handleDispose(event);
  	}
  	  				 		
	//-------------------------------------------------------------------------------------------------------------
	//--- internal ------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------
	
	/**
	 * Creates the specific SWT controls for the content areas.
	 * Clients must not call or override this method.
	 */
	protected void createControls(Composite composite) {
		
		// 1st row
		fAncestorCanvas= new BufferedCanvas(composite, SWT.NONE) {
			public void doPaint(GC gc) {
				paintSides(gc, fAncestor, fAncestorCanvas, false);
			}
		};
									
		fAncestor= createPart(composite);
		fAncestor.setEditable(false);
		
		// 2nd row
		fLeftCanvas= new BufferedCanvas(composite, SWT.NONE) {
			public void doPaint(GC gc) {
				paintSides(gc, fLeft, fLeftCanvas, false);
			}
		};
		
		fLeft= createPart(composite);
		fLeft.getTextWidget().getVerticalBar().setVisible(false);
			
		fRight= createPart(composite);
		fRight.getTextWidget().getVerticalBar().setVisible(false);
		
		fRightCanvas= new BufferedCanvas(composite, SWT.V_SCROLL) {
			public void doPaint(GC gc) {
				paintSides(gc, fRight, fRightCanvas, fSynchronizedScrolling);
			}
		};
				
		fVScrollBar= fRightCanvas.getVerticalBar();
		fVScrollBar.setIncrement(1);
		fVScrollBar.setVisible(true);
		fVScrollBar.addListener(SWT.Selection,
			new Listener() {
				public void handleEvent(Event e) {
					scrollVertical(((ScrollBar)e.widget).getSelection(), null);
				}
			}
		);
	}
	
	/* package */ boolean internalSetFocus() {
		//System.out.println("internalSetFocus: ");
		if (fFocusPart != null) {
			StyledText st= fFocusPart.getTextWidget();
			if (st != null)
				return st.setFocus();
		}
		return false;	// could not set focus
	}
	
	/**
	 * Creates the central Canvas.
	 * Called from ContentMergeViewer.
	 */
	/* package */ Control createCenter(Composite parent) {
		if (fSynchronizedScrolling) {
			final Canvas canvas= new BufferedCanvas(parent, SWT.NONE) {
				public void doPaint(GC gc) {
					paintCenter(this, gc);
				}
			};
			new Resizer(canvas, HORIZONTAL);
			return canvas;
		}
		return super.createCenter(parent);
	}
	
	/**
	 * Returns width of central canvas.
	 * Overridden from ContentMergeViewer.
	 */
	/* package */ int getCenterWidth() {
		if (fSynchronizedScrolling)
			return CENTER_WIDTH;
		return super.getCenterWidth();
	}

	/**
	 * Creates and initializes a text part.
	 */
	private MergeSourceViewer createPart(Composite parent) {
		
		final MergeSourceViewer part= new MergeSourceViewer(parent, getResourceBundle());
		final StyledText te= part.getTextWidget();
		
		te.addPaintListener(
			new PaintListener() {
				public void paintControl(PaintEvent e) {
					paint(e, part);
				}
			}
		);		
		te.addKeyListener(
			new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					handleSelectionChanged(part);
				}
			}
		);
		te.addMouseListener(
			new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					//syncViewport(part);
					handleSelectionChanged(part);
				}
			}
		);		
					
		te.addFocusListener(
			new FocusAdapter() {
				public void focusGained(FocusEvent fe) {
					fFocusPart= part;
					connectGlobalActions(fFocusPart);
				}
				public void focusLost(FocusEvent fe) {
					connectGlobalActions(null);
				}
			}
		);
		
		part.addViewportListener(
			new IViewportListener() {
				public void viewportChanged(int verticalPosition) {
					syncViewport(part);
				}
			}
		);
		
		configureTextViewer(part);
		
		return part;
	}
	
	private void connectGlobalActions(MergeSourceViewer part) {
		IActionBars actionBars= CompareEditor.findActionBars(fComposite);
		if (actionBars != null) {
			for (int i= 0; i < GLOBAL_ACTIONS.length; i++) {
				Action action= null;
				if (part != null)
					action= part.getAction(TEXT_ACTIONS[i]);
				actionBars.setGlobalActionHandler(GLOBAL_ACTIONS[i], action);
			}
			actionBars.updateActionBars();
		}
	}

	/**
	 * Initializes the text viewers of the three content areas with the given input objects.
	 * Subclasses may extend.
	 */
	protected void updateContent(Object ancestor, Object left, Object right) {
		
		// clear stuff
		fCurrentDiff= null;
	 	fChangeDiffs= null;
		fAllDiffs= null;
		
		fLeftContentsChanged= false;
		fRightContentsChanged= false;
		
		CompareConfiguration cc= getCompareConfiguration();
		IMergeViewerContentProvider cp= getMergeContentProvider();
		
		boolean rightEditable= cc.isRightEditable() && cp.isRightEditable(getInput());
		boolean leftEditable= cc.isLeftEditable() && cp.isLeftEditable(getInput());
		
		fRight.setEditable(rightEditable);
		fLeft.setEditable(leftEditable);
																			
		// set new documents
		setDocument(fAncestor, ancestor);
		
		setDocument(fLeft, left);
		fLeftLineCount= fLeft.getLineCount();

		setDocument(fRight, right);
		fRightLineCount= fRight.getLineCount();
		
		doDiff();
				
		invalidateLines();
		updateVScrollBar();
		selectFirstDiff();
	}
	
	private void updateDiffBackground(Diff diff) {
		
		if (diff == null || diff.fIsToken)
			return;
		Point region= new Point(0, 0);
						
		Color c= getColor(getFillColor(diff));
		if (c == null)
			return;
			
		if (isThreeWay())
			fAncestor.setLineBackground(diff.fAncestorPos, c);
		fLeft.setLineBackground(diff.fLeftPos, c);
		fRight.setLineBackground(diff.fRightPos, c);
	}
	
	private void unsetDocument(MergeSourceViewer tp) {
		IDocument oldDoc= tp.getDocument();
		if (oldDoc != null) {	// deinstall old positions
			if (fPositionUpdater != null)
				oldDoc.removePositionUpdater(fPositionUpdater);
			try {
				oldDoc.removePositionCategory(MY_UPDATER);
			} catch (BadPositionCategoryException ex) {
			}
		}
	}
	
	/**
	 * Called whenver one of the documents changes.
	 * Sets the dirty state of this viewer and updates the lines.
	 * Implements IDocumentListener.
	 */
	private void documentChanged(DocumentEvent e) {
		
		IDocument doc= e.getDocument();
		
		if (doc == fLeft.getDocument()) {
			fLeftContentsChanged= true;
			setLeftDirty(true);
		} else if (doc == fRight.getDocument()) {
			setRightDirty(true);
			fRightContentsChanged= true;
		}

		updateLines(doc);
	}
	
	/**
	 * Returns true if a new Document could be installed.
	 */
	private boolean setDocument(MergeSourceViewer tp, Object o) {
		
		if (tp == null)
			return false;
				
		IDocument newDoc= null;
		
		if (o instanceof IDocumentRange) {
			newDoc= ((IDocumentRange)o).getDocument();

		} else if (o instanceof Document) {
			newDoc= (Document) o;
			
		} else if (o instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca= (IStreamContentAccessor) o;
			if (sca != null) {
				String s= null;
				
				try {
					s= Utilities.readString(sca.getContents());
				} catch (CoreException ex) {
				}

				newDoc= new Document(s != null ? s : "");
				IDocumentPartitioner partitioner= getDocumentPartitioner();
				if (partitioner != null) {
					newDoc.setDocumentPartitioner(partitioner);
					partitioner.connect(newDoc);
				}
			}
		}

		boolean enabled= true;
		if (newDoc == null) {
			newDoc= new Document("");
			enabled= false;
		}
		
		IDocument oldDoc= tp.getDocument();
		
		unsetDocument(tp);
		
		if (newDoc != null) {
			newDoc.addPositionCategory(MY_UPDATER);
			if (fPositionUpdater == null)
				fPositionUpdater= new DefaultPositionUpdater(MY_UPDATER);
			newDoc.addPositionUpdater(fPositionUpdater);
		}
		
		if (newDoc != oldDoc) {	// new document
			
			// deinstall old document
			if (oldDoc != null)
				oldDoc.removeDocumentListener(fDocumentListener);
			
			// install new document
			if (newDoc != null) {
						
				IRegion region= null;
				if (o instanceof IDocumentRange) {
					Position range= ((IDocumentRange) o).getRange();
					if (range != null)
						region= new Region(range.getOffset(), range.getLength());
				}
					
				tp.setRegion(region);
				if (fSubDoc) {
					if (region != null) {
						IRegion r= normalizeDocumentRegion(newDoc, region);
						tp.setDocument(newDoc, r.getOffset(), r.getLength());
					} else
						tp.setDocument(newDoc);
				} else
					tp.setDocument(newDoc);
								
				newDoc.addDocumentListener(fDocumentListener);
			}
			
		} else {	// just different range
			
			IRegion region= null;
			if (o instanceof IDocumentRange) {
				Position range= ((IDocumentRange) o).getRange();
				if (range != null)
					region= new Region(range.getOffset(), range.getLength());
			}
				
			tp.setRegion(region);
			if (fSubDoc) {
				if (region != null) {
					IRegion r= normalizeDocumentRegion(tp.getDocument(), region);
					tp.setVisibleRegion(r.getOffset(), r.getLength());
				}
			}			
		}
		
		tp.setEnabled(enabled);

		return enabled;
	}
	
	/**
	 * Returns the contents of the underlying document as an array of bytes.
	 * 
	 * @param left if <code>true</code> the contents of the left side is returned; otherwise the right side
	 * @return the contents of the left or right document
	 */
	protected byte[] getContents(boolean left) {
		
		if (left) {
			if (fLeftContentsChanged)
				return fLeft.getDocument().get().getBytes();
		} else {
			if (fRightContentsChanged)
				return fRight.getDocument().get().getBytes();
		}
		return null;
	}
				
	private IRegion normalizeDocumentRegion(IDocument doc, IRegion region) {
		
		if (region == null || doc == null)
			return region;
			
		int maxLength= doc.getLength();
		
		int start= region.getOffset();
		if (start < 0)
			start= 0;
		else if (start > maxLength)
			start= maxLength;
			
		int length= region.getLength();
		if (length < 0)
			length= 0;
		else if (start + length > maxLength)
			length= maxLength - start;
			
		return new Region(start, length);
	}
		
	protected final void handleResizeAncestor(int x, int y, int width, int height) {
		if (width > 0) {
			Rectangle trim= fLeft.getTextWidget().computeTrim(0, 0, 0, 0);
			int scrollbarHeight= trim.height;
			if (Utilities.okToUse(fAncestorCanvas))
				fAncestorCanvas.setVisible(true);
			if (fAncestor.isControlOkToUse())
				fAncestor.getTextWidget().setVisible(true);
			fAncestorCanvas.setBounds(x, y, MARGIN_WIDTH, height-scrollbarHeight);
			fAncestor.getTextWidget().setBounds(x+MARGIN_WIDTH, y, width-MARGIN_WIDTH, height);
		} else {
			if (Utilities.okToUse(fAncestorCanvas))
				fAncestorCanvas.setVisible(false);
			if (fAncestor.isControlOkToUse()) {
				StyledText t= fAncestor.getTextWidget();
				t.setVisible(false);
				t.setBounds(0, 0, 0, 0);
			}
		}
	}

	/**
	 * Lays out everything.
	 */
  	protected final void handleResizeLeftRight(int x, int y, int width1, int centerWidth, int width2,  int height) {
  				
		// determine some minimal sizes
		int scrollbarWidth= 0;
		if (fSynchronizedScrolling && fRightCanvas != null)
			scrollbarWidth= fRightCanvas.computeTrim(0, 0, 0, 0).width;
		
		Rectangle trim= fLeft.getTextWidget().computeTrim(0, 0, 0, 0);
		int scrollbarHeight= trim.height;
		
		// determine some derived sizes
		Composite composite= (Composite) getControl();

		int leftTextWidth= width1-MARGIN_WIDTH;
		int rightTextWidth= width2-MARGIN_WIDTH-scrollbarWidth;		
				
		fLeftCanvas.setBounds(x, y, MARGIN_WIDTH, height-scrollbarHeight);
		x+= MARGIN_WIDTH;
		
		fLeft.getTextWidget().setBounds(x, y, leftTextWidth, height);
		x+= leftTextWidth;
		
		if (fCenter == null || fCenter.isDisposed())
			fCenter= createCenter(composite);
		fCenter.setBounds(x, y, centerWidth, height-scrollbarHeight);
		x+= centerWidth;
		
		if (!fSynchronizedScrolling) {
			if (fRightCanvas != null) {
				fRightCanvas.setBounds(x, y, MARGIN_WIDTH, height-scrollbarHeight);
				fRightCanvas.redraw();
			}
			// we draw the canvas to the left of the text widget
			x+= MARGIN_WIDTH;
		}
		
		fRight.getTextWidget().setBounds(x, y, rightTextWidth, height);
		x+= rightTextWidth;
			
		if (fSynchronizedScrolling && fRightCanvas != null)
			fRightCanvas.setBounds(x, y, scrollbarWidth+MARGIN_WIDTH, height-scrollbarHeight);
		
		// doesn't work since TextEditors don't have their correct size yet.
		updateVScrollBar(); 
	}
							
	/**
	 * Track selection changes to update the current Diff.
	 */
	private void handleSelectionChanged(MergeSourceViewer tw) {
		Point p= tw.getSelectedRange();
		Diff d= findDiff(tw, p.x, p.x+p.y);
		setCurrentDiff(d, false);	// don't select or reveal
	}

	//---- the differencing
	
	/**
	 * Perform a two level 2- or 3-way diff.
	 * The first level is based on line comparison, the second level on token comparison.
	 */
	private void doDiff() {
						
		fAllDiffs= new ArrayList();
		fChangeDiffs= new ArrayList();

		IDocument aDoc= null;
		IDocument iDoc= fLeft.getDocument();
		IDocument oDoc= fRight.getDocument();

		if (iDoc == null || oDoc == null)
			return;
			
		IRegion aRegion= null;
		IRegion iRegion= fLeft.getRegion();
		IRegion oRegion= fRight.getRegion();
		
		boolean threeWay= isThreeWay();
		
		if (threeWay) {
			aDoc= fAncestor.getDocument();
			aRegion= fAncestor.getRegion();
		}
		
		fAncestor.resetLineBackground();
		fLeft.resetLineBackground();
		fRight.resetLineBackground();
		
		boolean ignoreWhiteSpace= Utilities.getBoolean(getCompareConfiguration(), CompareConfiguration.IGNORE_WHITESPACE, false);		
		
		DocLineComparator sright= new DocLineComparator(oDoc, oRegion, ignoreWhiteSpace);
		DocLineComparator sleft= new DocLineComparator(iDoc, iRegion, ignoreWhiteSpace);
		DocLineComparator sancestor= null;
		if (aDoc != null)
			sancestor= new DocLineComparator(aDoc, aRegion, ignoreWhiteSpace);
			
		if (!fSubDoc && oRegion != null && iRegion != null) {
			// we have to add a diff for the ignored lines
			
			int astart= 0;
			int as= 0;
			if (aRegion != null) {
				astart= aRegion.getOffset();
				as= Math.max(0, astart-1);
			}
			int ys= Math.max(0, iRegion.getOffset()-1);
			int ms= Math.max(0, oRegion.getOffset()-1);
			
			if (as > 0 || ys > 0 || ms > 0) {
				Diff diff= new Diff(null, RangeDifference.NOCHANGE,
					aDoc, 0, astart,
					iDoc, 0, iRegion.getOffset(),
					oDoc, 0, oRegion.getOffset());
				fAllDiffs.add(diff);
			}
		}
		
		RangeDifference[] e= RangeDifferencer.findRanges(sancestor, sleft, sright);
		
		for (int i= 0; i < e.length; i++) {
			String a= null, s= null, d= null;
			RangeDifference es= e[i];
			
			int kind= es.kind();
			
			int ancestorStart= 0;
			int ancestorEnd= 0;
			if (sancestor != null) {
				ancestorStart= sancestor.getTokenStart(es.ancestorStart());
				ancestorEnd= sancestor.getTokenEnd(es.ancestorStart(), es.ancestorLength());
			}
			
			int leftStart= sleft.getTokenStart(es.leftStart());
			int leftEnd= sleft.getTokenEnd(es.leftStart(), es.leftLength());
			
			int rightStart= sright.getTokenStart(es.rightStart());
			int rightEnd= sright.getTokenEnd(es.rightStart(), es.rightLength());
			
			Diff diff= new Diff(null, kind,
				aDoc, ancestorStart, ancestorEnd,
				iDoc, leftStart, leftEnd,
				oDoc, rightStart, rightEnd);	
			
			fAllDiffs.add(diff);	// remember all range diffs for scrolling

			if (ignoreWhiteSpace) {
				if (sancestor != null)
					a= sancestor.extract(es.ancestorStart(), es.ancestorLength());
				s= sleft.extract(es.leftStart(), es.leftLength());
				d= sright.extract(es.rightStart(), es.rightLength());
			
				if ((a == null || a.trim().length() == 0) && s.trim().length() == 0 && d.trim().length() == 0)
					continue;
			}

			if (kind != RangeDifference.NOCHANGE && kind != RangeDifference.ANCESTOR) {
				fChangeDiffs.add(diff);	// here we remember only the real diffs
				updateDiffBackground(diff);

				if (s == null)
					s= sleft.extract(es.leftStart(), es.leftLength());
				if (d == null)
					d= sright.extract(es.rightStart(), es.rightLength());
				
				if (s.length() > 0 && d.length() > 0) {
					if (a == null && sancestor != null)
						a= sancestor.extract(es.ancestorStart(), es.ancestorLength());
					if (USE_MERGING_TOKEN_DIFF)
						mergingTokenDiff(diff, aDoc, a, oDoc, d, iDoc, s);
					else
						simpleTokenDiff(diff, aDoc, a, oDoc, d, iDoc, s);
				}
			}
		}
		
		if (!fSubDoc && oRegion != null && iRegion != null) {
			// we have to add a diff for the ignored lines
			
			int aEnd= 0;
			int aLen= 0;
			if (aRegion != null && aDoc != null) {
				aEnd= aRegion.getOffset()+aRegion.getLength();
				aLen= aDoc.getLength();
			}
			Diff diff= new Diff(null, RangeDifference.NOCHANGE,
				aDoc, aEnd, aLen,
				iDoc, iRegion.getOffset()+iRegion.getLength(), iDoc.getLength(),
				oDoc, oRegion.getOffset()+oRegion.getLength(), oDoc.getLength());
			fAllDiffs.add(diff);
		}
	}
	
	private int getTokenEnd(ITokenComparator tc, int start, int count) {
		if (count <= 0)
			return tc.getTokenStart(start);
		int index= start + count - 1;
		return tc.getTokenStart(index) + tc.getTokenLength(index);
	}
	
	/**
	 * Performs a token based 3-way diff on the character range specified by the given baseDiff.
	 */
	private void simpleTokenDiff(final Diff baseDiff,
				IDocument ancestorDoc, String a,
				IDocument rightDoc, String d,
				IDocument leftDoc, String s) {

		int ancestorStart= 0;
		int ancestorEnd= 0;
		ITokenComparator sa= null;
		if (ancestorDoc != null) {
			ancestorStart= baseDiff.fAncestorPos.getOffset();
			ancestorEnd= ancestorStart + baseDiff.fAncestorPos.getLength();
			sa= createTokenComparator(a);
		}
		
		int rightStart= baseDiff.fRightPos.getOffset();	
		int rightEnd= rightStart + baseDiff.fRightPos.getLength();
		ITokenComparator sm= createTokenComparator(d);
		
		int leftStart= baseDiff.fLeftPos.getOffset();
		int leftEnd= leftStart + baseDiff.fLeftPos.getLength();
		ITokenComparator sy= createTokenComparator(s);
		
		RangeDifference[] e= RangeDifferencer.findRanges(sa, sy, sm);
		for (int i= 0; i < e.length; i++) {
			RangeDifference es= e[i];
			int kind= es.kind();
			if (kind != RangeDifference.NOCHANGE && kind != RangeDifference.ANCESTOR) {
				
				int ancestorStart2= ancestorStart;
				int ancestorEnd2= ancestorStart;
				if (ancestorDoc != null) {
					ancestorStart2 += sa.getTokenStart(es.ancestorStart());
					ancestorEnd2 += getTokenEnd(sa, es.ancestorStart(), es.ancestorLength());
				}
				
				int leftStart2= leftStart + sy.getTokenStart(es.leftStart());
				int leftEnd2= leftStart + getTokenEnd(sy, es.leftStart(), es.leftLength());
				
				int rightStart2= rightStart + sm.getTokenStart(es.rightStart());
				int rightEnd2= rightStart + getTokenEnd(sm, es.rightStart(), es.rightLength());
				
				Diff diff= new Diff(baseDiff, kind,
						ancestorDoc, ancestorStart2, ancestorEnd2,
						leftDoc, leftStart2, leftEnd2,
						rightDoc, rightStart2, rightEnd2);
				diff.fIsToken= true;
				// add to base Diff
				baseDiff.add(diff);
			}
		}
	}
	
	/**
	 * Performs a "smart" token based 3-way diff on the character range specified by the given baseDiff.
	 * It is smart because it tries to minimize the number of token diffs by merging them.
	 */
	private void mergingTokenDiff(Diff baseDiff, 
				IDocument ancestorDoc, String a,
				IDocument rightDoc, String d,
				IDocument leftDoc, String s) {

		ITokenComparator sa= null;
		int ancestorStart= 0;
		int ancestorEnd= 0;
		if (ancestorDoc != null) {
			sa= createTokenComparator(a);
			ancestorStart= baseDiff.fAncestorPos.getOffset();
			ancestorEnd= ancestorStart + baseDiff.fAncestorPos.getLength();
		}
		
		int rightStart= baseDiff.fRightPos.getOffset();	
		int rightEnd= rightStart + baseDiff.fRightPos.getLength();
		ITokenComparator sm= createTokenComparator(d);
		
		int leftStart= baseDiff.fLeftPos.getOffset();
		int leftEnd= leftStart + baseDiff.fLeftPos.getLength();
		ITokenComparator sy= createTokenComparator(s);
		
		RangeDifference[] r= RangeDifferencer.findRanges(sa, sy, sm);

		for (int i= 0; i < r.length; i++) {
			RangeDifference  es= r[i];

			// determine range of diffs in one line
			int start= i;
			int leftLine= -1;
			int rightLine= -1;
			try {
				leftLine= leftDoc.getLineOfOffset(leftStart+sy.getTokenStart(es.leftStart()));
				rightLine= rightDoc.getLineOfOffset(rightStart+sm.getTokenStart(es.rightStart()));
			} catch (BadLocationException e) {
			}
			i++;
			for (; i < r.length; i++) {
				es= r[i];
				int ll, rl;
				try {
					if (leftLine != leftDoc.getLineOfOffset(leftStart+sy.getTokenStart(es.leftStart())))
						break;
					if (rightLine != rightDoc.getLineOfOffset(rightStart+sm.getTokenStart(es.rightStart())))
						break;
				} catch (BadLocationException e) {
				}
			}
			int end= i;
			
			// find first diff from left
			RangeDifference first= null;
			for (int ii= start; ii < end; ii++) {
				es= r[ii];
				int kind= es.kind();
				if (kind != RangeDifference.NOCHANGE && kind != RangeDifference.ANCESTOR) {
					first= es;
					break;
				}
			}
			
			// find first diff from mine
			RangeDifference last= null;
			for (int ii= end-1; ii >= start; ii--) {
				es= r[ii];
				int kind= es.kind();
				if (kind != RangeDifference.NOCHANGE && kind != RangeDifference.ANCESTOR) {
					last= es;
					break;
				}
			}

			
			if (first != null && last != null) {
				
				int ancestorStart2= 0;
				int ancestorEnd2= 0;
				if (ancestorDoc != null) {
					ancestorStart2= ancestorStart+sa.getTokenStart(first.ancestorStart());
					ancestorEnd2= ancestorStart+getTokenEnd(sa, last.ancestorStart(), last.ancestorLength());
				}
				
				int leftStart2= leftStart+sy.getTokenStart(first.leftStart());
				int leftEnd2= leftStart+getTokenEnd(sy, last.leftStart(), last.leftLength());
				
				int rightStart2= rightStart+sm.getTokenStart(first.rightStart());
				int rightEnd2= rightStart+getTokenEnd(sm, last.rightStart(), last.rightLength());

				Diff diff= new Diff(baseDiff, first.kind(),
							ancestorDoc, ancestorStart2, ancestorEnd2+1,
							leftDoc, leftStart2, leftEnd2+1,
							rightDoc, rightStart2, rightEnd2+1);	
				diff.fIsToken= true;
				baseDiff.add(diff);
			}
		}
	}
	
	//---- update UI stuff
	
	private void updateControls() {
		
		boolean leftToRight= false;
		boolean rightToLeft= false;
		
		if (fCurrentDiff != null) {
			IMergeViewerContentProvider cp= getMergeContentProvider();
			if (cp != null) {
				rightToLeft= cp.isLeftEditable(getInput());
				leftToRight= cp.isRightEditable(getInput());
			}
		}
		
		if (fCopyDiffLeftToRightItem != null)			
			((Action)fCopyDiffLeftToRightItem.getAction()).setEnabled(leftToRight);
		if (fCopyDiffRightToLeftItem != null)
			((Action)fCopyDiffRightToLeftItem.getAction()).setEnabled(rightToLeft);
//		
//		int fAutoResolve= 0;
//		int fUnresolvedDiffs= 0;
//		if (fChangeDiffs != null) {
//			fUnresolvedDiffs= fChangeDiffs.size();
//			if (fUnresolvedDiffs > 0) {
//				Iterator e= fChangeDiffs.iterator();
//				while (e.hasNext()) {
//					Diff diff= (Diff) e.next();
//					if (diff.isResolved()) {
//						fUnresolvedDiffs--;
//					} else {
//						if (diff.fDirection == RangeDifference.RIGHT || diff.fDirection == RangeDifference.LEFT) {
//							fAutoResolve++;
//						}
//					}
//				}
//			}
//		}
//		
//		boolean acceptReject= false;
//		boolean both= false;
//		
//		String s= "";
//
//		if (fCurrentDiff != null) {
//			if (fCurrentDiff.isResolved()) {
//				s= "resolved";
//			} else {
//				s= "unresolved";
//				
//				IMergeViewerContentProvider twr= getContentProvider();
//				Object input= getInput();
//				boolean rightEditable= twr.isRightEditable(input);
//				boolean leftEditable= twr.isLeftEditable(input);
//				
//				switch (fCurrentDiff.fDirection) {
//				case RangeDifference.RIGHT:	// outgoing
//					if (rightEditable)
//						acceptReject= true;
//					break;
//				case RangeDifference.LEFT:	// incoming
//					if (leftEditable)
//						acceptReject= true;
//					break;
//				case RangeDifference.CONFLICT:
//					if (rightEditable) {
//						acceptReject= true;
//						both= true;
//					}
//					break;
//				}
//			}
//		} else {
//			if (fUnresolvedDiffs <= 0)
//				s= "allresolved";
//			else
//				s= "same";
//		}
//		
//		getAction(fTakeLeftActionItem).setEnabled(acceptReject);
//		getAction(fRejectItem).setEnabled(acceptReject);
//		if (fBothItem != null)
//			getAction(fBothItem).setEnabled(both);
//		if (fAutoItem != null)
//			getAction(fAutoItem).setEnabled(fAutoResolve > 0);
//
//		if (s.length() > 0)
//			s= getBundle().getString("status." + s);
//
//		ApplicationWindow b= getApplicationWindow();
//		if (b != null) {
//			String format= fBundle.getString(fUnresolvedDiffs > 0
//								? "status.unresolvedformat"
//								: "status.resolvedformat");
//			b.setStatus(MessageFormat.format(format, new String[] { s, "" + fUnresolvedDiffs } ));
//		}
	}
	
	
	protected void updateHeader() {
		
		super.updateHeader();
				
		IMergeViewerContentProvider content= getMergeContentProvider();
		Object input= getInput();
		boolean m= content.isRightEditable(input);
		boolean y= content.isLeftEditable(input);

		CompareConfiguration mp= getCompareConfiguration();
		//fLeft.setEditable(y && mp.isLeftEditable());
		//fRight.setEditable(m && mp.isRightEditable());

		updateControls();
	}

	/**
	 * Creates the two items for copying a difference range from one side to the other 
	 * and adds them to the given toolbar manager.
	 */
	protected void createToolItems(ToolBarManager tbm) {
		
//		if (USE_MORE_CONTROLS) {
//			fBothItem= new ActionContributionItem(
//				new Action(fBundle, "action.AcceptBoth.") {
//					public void actionPerformed(Window w) {
//						accept(fCurrentDiff, true, false);
//					}
//				}
//			);
//			tbm.appendToGroup("merge", fBothItem);
//	
//			fAutoItem= new ActionContributionItem(
//				new Action(fBundle, "action.AcceptAll.") {
//					public void actionPerformed(Window w) {
//						autoResolve();
//					}
//				}
//			);
//			tbm.appendToGroup("merge", fAutoItem);
//		}
//		fRejectItem= new ActionContributionItem(
//			new Action(fBundle, "action.AcceptIgnoreNow.") {
//				public void actionPerformed(Window w) {
//					reject(fCurrentDiff, true);
//				}
//			}
//		);
//		tbm.appendToGroup("merge", fRejectItem);
//		
//		Action a= new ChangePropertyAction(getResourceBundle(), getCompareConfiguration(), "action.SynchMode.", SYNC_SCROLLING);
//		a.setChecked(fSynchronizedScrolling);
//		tbm.appendToGroup("modes", a);
		
		tbm.add(new Separator());
					
		Action a= new Action() {
			public void run() {
				navigate(true);
			}
		};
		Utilities.initAction(a, getResourceBundle(), "action.NextDiff.");
		fNextItem= new ActionContributionItem(a);
		tbm.appendToGroup("navigation", fNextItem);
		
		a= new Action() {
			public void run() {
				navigate(false);
			}
		};
		Utilities.initAction(a, getResourceBundle(), "action.PrevDiff.");
		fPreviousItem= new ActionContributionItem(a);
		tbm.appendToGroup("navigation", fPreviousItem);

		
		CompareConfiguration cc= getCompareConfiguration();
		
		if (cc.isRightEditable()) {
			a= new Action() {
				public void run() {
					copyDiffLeftToRight();
				}
			};
			Utilities.initAction(a, getResourceBundle(), "action.CopyDiffLeftToRight.");
			fCopyDiffLeftToRightItem= new ActionContributionItem(a);
			tbm.appendToGroup("merge", fCopyDiffLeftToRightItem);
		}
		
		if (cc.isLeftEditable()) {
			a= new Action() {
				public void run() {
					copyDiffRightToLeft();
				}
			};
			Utilities.initAction(a, getResourceBundle(), "action.CopyDiffRightToLeft.");
			fCopyDiffRightToLeftItem= new ActionContributionItem(a);
			tbm.appendToGroup("merge", fCopyDiffRightToLeftItem);
		}
	}
	
	/* package */ void propertyChange(PropertyChangeEvent event) {
		
		String key= event.getProperty();
		
		if (key.equals(CompareConfiguration.IGNORE_WHITESPACE)) {
			// clear stuff
			fCurrentDiff= null;
		 	fChangeDiffs= null;
			fAllDiffs= null;
					
			doDiff();
					
			invalidateLines();
			updateVScrollBar();
			
			selectFirstDiff();
//		} else if (key.equals(SYNC_SCROLLING)) {
//			
//			boolean b= Utilities.getBoolean(getCompareConfiguration(), SYNC_SCROLLING, true);
//			if (b != fSynchronizedScrolling)
//				toggleSynchMode();
	
		} else if (key.equals(ComparePreferencePage.SYNCHRONIZE_SCROLLING)) {
			
			IPreferenceStore ps= CompareUIPlugin.getDefault().getPreferenceStore();
			
			boolean b= ps.getBoolean(ComparePreferencePage.SYNCHRONIZE_SCROLLING);
			//boolean b= Utilities.getBoolean(getCompareConfiguration(), SYNC_SCROLLING, true);
			if (b != fSynchronizedScrolling)
				toggleSynchMode();
		
		} else
			super.propertyChange(event);
	}
	
	private void selectFirstDiff() {
		Diff firstDiff= findNext(fRight, fChangeDiffs, -1, -1);
		setCurrentDiff(firstDiff, true);
	}
	
	private void toggleSynchMode() {
		fSynchronizedScrolling= ! fSynchronizedScrolling;
		
		scrollVertical(0, null);
		
		// throw away central control (Sash or Canvas)
		Control center= getCenter();
		if (center != null && !center.isDisposed())
			center.dispose();
		
		fLeft.getTextWidget().getVerticalBar().setVisible(!fSynchronizedScrolling);
		fRight.getTextWidget().getVerticalBar().setVisible(!fSynchronizedScrolling);

		// recreates central control (Sash or Canvas)
		//handleResize();
		fComposite.layout(true);
	}
				
	protected void updateToolItems() {
					
		boolean visible= false;
		Object input= getInput();
		if (input != null) {
			visible= true;
			
			IMergeViewerContentProvider content= getMergeContentProvider();
			
			//boolean y= getMergePolicy().isLeftEditable();
			//boolean m= getMergePolicy().isRightEditable();
			
				//destinationEditable= content.isRightEditable(getInput());
				//destinationEditable= content.isLeftEditable(getInput());
			/*
			if (USE_MORE_CONTROLS) {	
				fBothItem.setVisible(destinationEditable);
				fAutoItem.setVisible(destinationEditable);
			}
			fRejectItem.setVisible(destinationEditable);
			*/
		}
		
		//fNextItem.setVisible(visible);
		//fPreviousItem.setVisible(visible);
		
		super.updateToolItems();
	}
	
	//---- painting lines
	
	/**
	 * 
	 */
	private void updateLines(IDocument d) {

		boolean left= false;
		boolean right= false;
		
		// FIXME: this optimization is incorrect because
		// it doesn't take replace operations into account where
		// the old and new line count does not differ
		if (d == fLeft.getDocument()) {
			int l= fLeft.getLineCount();
			left= fLeftLineCount != l;
			fLeftLineCount= l;
		} else if (d == fRight.getDocument()) {
			int l= fRight.getLineCount();
			right= fRightLineCount != l;
			fRightLineCount= l;
		}
		
		if (left || right) {
			
			if (left) {
				if (fLeftCanvas != null)
					fLeftCanvas.redraw();
			} else {
				if (fRightCanvas != null)
					fRightCanvas.redraw();
			}
			Control center= getCenter();
			if (center != null)
				center.redraw();

			updateVScrollBar();
		}
	}
	
	private void invalidateLines() {
		if (isThreeWay()) {
			if (Utilities.okToUse(fAncestorCanvas))
				fAncestorCanvas.redraw();
			if (fAncestor.isControlOkToUse())
				fAncestor.getTextWidget().redraw();
		}
		
		if (Utilities.okToUse(fLeftCanvas))
			fLeftCanvas.redraw();
			
		if (fLeft.isControlOkToUse())
			fLeft.getTextWidget().redraw();
			
		if (Utilities.okToUse(getCenter()))
			getCenter().redraw();
			
		if (fRight.isControlOkToUse())
			fRight.getTextWidget().redraw();
			
		if (Utilities.okToUse(fRightCanvas))
			fRightCanvas.redraw();
	}
	
	private void paintCenter(Canvas canvas, GC g) {
		
		if (! fSynchronizedScrolling)
			return;

		int lineHeight= fLeft.getTextWidget().getLineHeight();			
		int visibleHeight= fRight.getViewportHeight();

		Point size= canvas.getSize();
		int x= 0;
		int w= size.x;
				
		g.setBackground(canvas.getBackground());
		g.fillRectangle(x+1, 0, w-2, size.y);
		
		if (!IS_MOTIF) {
			// draw thin line between center ruler and both texts
			g.setBackground(fLeftCanvas.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			g.fillRectangle(0, 0, 1, size.y);
			g.fillRectangle(w-1, 0, 1, size.y);
		}
			
		if (fChangeDiffs != null) {
			int lshift= fLeft.getVerticalScrollOffset();
			int rshift= fRight.getVerticalScrollOffset();
					
			Point region= new Point(0, 0);
		
			Iterator e= fChangeDiffs.iterator();
			while (e.hasNext()) {
				Diff diff= (Diff) e.next();
				if (diff.isDeleted())
					continue;
				
				fLeft.getLineRange(diff.fLeftPos, region);
				int ly= (region.x * lineHeight) + lshift;
				int lh= region.y * lineHeight;
	
				fRight.getLineRange(diff.fRightPos, region);
				int ry= (region.x * lineHeight) + rshift;
				int rh= region.y * lineHeight;
	
				if (Math.max(ly+lh, ry+rh) < 0)
					continue;
				if (Math.min(ly, ry) >= visibleHeight)
					break;
	
				fPts[0]= x;	fPts[1]= ly;		fPts[2]= w;	fPts[3]= ry;
				fPts[6]= x;	fPts[7]= ly+lh;	fPts[4]= w;	fPts[5]= ry+rh;
							
				g.setBackground(getColor(getFillColor(diff)));
				g.fillPolygon(fPts);
	
				g.setLineWidth(LW);
				g.setForeground(getColor(getStrokeColor(diff)));
				g.drawLine(fPts[0], fPts[1], fPts[2], fPts[3]);
				g.drawLine(fPts[6], fPts[7], fPts[4], fPts[5]);
			}
		}
	}
	
	private void paintSides(GC g, MergeSourceViewer tp, Canvas canvas, boolean right) {

		int lineHeight= tp.getTextWidget().getLineHeight();
		int visibleHeight= tp.getViewportHeight();

		Point size= canvas.getSize();
		int x= 0;
		int w= MARGIN_WIDTH;
			
		g.setBackground(canvas.getBackground());
		g.fillRectangle(x, 0, w, size.y);

		if (!IS_MOTIF) {
			// draw thin line between ruler and text
			g.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			if (right)
				g.fillRectangle(0, 0, 1, size.y);
			else
				g.fillRectangle(size.x-1, 0, 1, size.y);
		}

		if (fChangeDiffs != null) {
			int shift= tp.getVerticalScrollOffset() + (2-LW);
				
			Point region= new Point(0, 0);
			Iterator e= fChangeDiffs.iterator();
			while (e.hasNext()) {
				Diff diff= (Diff) e.next();
				if (diff.isDeleted())
					continue;
				
				tp.getLineRange(diff.getPosition(tp), region);	
				int y= (region.x * lineHeight) + shift;
				int h= region.y * lineHeight;
	
				if (y+h < 0)
					continue;
				if (y >= visibleHeight)
					break;
					
				g.setBackground(getColor(getFillColor(diff)));
				if (right)
					g.fillRectangle(x, y, w-5, h);
				else
					g.fillRectangle(x+5, y, w-3, h);
	
				g.setBackground(getColor(getStrokeColor(diff)));
				if (right) {
					g.fillRectangle(x, y-1, w-4, LW);
					g.fillRectangle(x+5, y, LW, h);
					g.fillRectangle(x, y+h-1, w-4, LW);
				} else {
					g.fillRectangle(x+3, y-1, w-3, LW);
					g.fillRectangle(x+3, y, LW, h);
					g.fillRectangle(x+3, y+h-1, w-3, LW);
				}
			}
		}
	}
	
	private void paint(PaintEvent event, MergeSourceViewer tp) {
		
		if (fChangeDiffs == null)
			return;

		Control canvas= (Control) event.widget;
		GC g= event.gc;
		
		int lineHeight= tp.getTextWidget().getLineHeight();			
		int w= canvas.getSize().x;
		int shift= tp.getVerticalScrollOffset() + (2-LW);
		int maxh= event.y+event.height; 	// visibleHeight
				
		Point range= new Point(0, 0);
				
		Iterator e= fChangeDiffs.iterator();	
		while (e.hasNext()) {
			Diff diff= (Diff) e.next();
			if (diff.isDeleted())
				continue;
			
			tp.getLineRange(diff.getPosition(tp), range);
			int y= (range.x * lineHeight) + shift;
			int h= range.y * lineHeight;
			
			if (y+h < event.y)
				continue;
			if (y > maxh)
				break;
						
			g.setBackground(getColor(getStrokeColor(diff)));
			g.fillRectangle(0, y-1, w, LW);
			g.fillRectangle(0, y+h-1, w, LW);
		}
	}

	private RGB getFillColor(Diff diff) {
		boolean selected= fCurrentDiff != null && fCurrentDiff.fParent == diff;
		switch (diff.fDirection) {
		case RangeDifference.RIGHT:
			return selected ? SELECTED_OUTGOING_FILL : OUTGOING_FILL;
		case RangeDifference.ANCESTOR:
			return selected ? SELECTED_CONFLICT_FILL : CONFLICT_FILL;
		case RangeDifference.LEFT:
			return selected ? SELECTED_INCOMING_FILL : INCOMING_FILL;
		case RangeDifference.CONFLICT:
			return selected ? SELECTED_CONFLICT_FILL : CONFLICT_FILL;
		}
		return null;
	}
	
	private RGB getStrokeColor(Diff diff) {
		boolean selected= fCurrentDiff != null && fCurrentDiff.fParent == diff;
		switch (diff.fDirection) {
		case RangeDifference.RIGHT:
			return selected ? SELECTED_OUTGOING : OUTGOING;
		case RangeDifference.ANCESTOR:
			return selected ? SELECTED_CONFLICT : CONFLICT;
		case RangeDifference.LEFT:
			return selected ? SELECTED_INCOMING : INCOMING;
		case RangeDifference.CONFLICT:
			return selected ? SELECTED_CONFLICT : CONFLICT;
		}
		return null;
	}
	
	private Color getColor(RGB rgb) {
		if (rgb == null)
			return null;
		if (fColors == null)
			fColors= new HashMap(20);
		Color c= (Color) fColors.get(rgb);
		if (c == null) {
			c= new Color(fComposite.getDisplay(), rgb);
			fColors.put(rgb, c);
		}
		return c;
	}
	
	//---- Navigating and resolving Diffs
	
	/**
	 */
	private void navigate(boolean down) {

		Diff diff= null;
		if (fChangeDiffs != null) {
			MergeSourceViewer part= fFocusPart;
			if (part == null)
				part= fRight;
			
			if (part != null) {
				Point s= part.getSelectedRange();
				if (down)
					diff= findNext(part, fChangeDiffs, s.x, s.x+s.y);
				else
					diff= findPrev(part, fChangeDiffs, s.x, s.x+s.y);					
			}		
		}
	
		if (diff == null) {
			Control c= getControl();
			if (Utilities.okToUse(c))
				c.getDisplay().beep();
			if (DEAD_STEP)
				return;
			if (fChangeDiffs.size() > 0) {
				if (down)
					diff= (Diff) fChangeDiffs.get(0);
				else
					diff= (Diff) fChangeDiffs.get(fChangeDiffs.size()-1);
			}
		}
			
		setCurrentDiff(diff, true);
	}	
		
	/**
	 * Find the Diff that overlaps with the given TextPart's text range.
	 * If the range doesn't overlap with any range <code>null</code>
	 * is returned.
	 */
	private Diff findDiff(MergeSourceViewer tp, int rangeStart, int rangeEnd) {
		if (fChangeDiffs != null) {
			Iterator e= fChangeDiffs.iterator();
			while (e.hasNext()) {
				Diff diff= (Diff) e.next();
				if (diff.contains(tp, rangeStart, rangeEnd))
					return diff;
			}
		}
		return null;
	}
	
	private static Diff findNext(MergeSourceViewer tp, List v, int start, int end) {
		for (int i= 0; i < v.size(); i++) {
			Diff diff= (Diff) v.get(i);
			Position p= diff.getPosition(tp);
			if (p != null) {
				int startOffset= p.getOffset();
				if (end < startOffset)
					return diff;
				if (diff.fDiffs != null) {
					Diff d= null;
					int endOffset= startOffset + p.getLength();
					if (start == startOffset && end == endOffset) {
						d= findNext(tp, diff.fDiffs, start, start);
					} else if (end < endOffset) {
						d= findNext(tp, diff.fDiffs, start, end);
					}
					if (d != null)
						return d;
				}
			}
		}
		return null;
	}
	
	private static Diff findPrev(MergeSourceViewer tp, List v, int start, int end) {
		for (int i= v.size()-1; i >= 0; i--) {
			Diff diff= (Diff) v.get(i);
			Position p= diff.getPosition(tp);
			if (p != null) {
				int startOffset= p.getOffset();
				int endOffset= startOffset + p.getLength();
				if (start > endOffset)
					return diff;
				if (diff.fDiffs != null) {
					Diff d= null;
					if (start == startOffset && end == endOffset) {
						d= findPrev(tp, diff.fDiffs, end, end);
					} else if (start >= startOffset) {
						d= findPrev(tp, diff.fDiffs, start, end);
					}
					if (d != null)
						return d;
				}
			}
		}
		return null;
	}
		
	/*
	 * Set the currently active Diff and update the toolbars controls and lines.
	 * If <code>revealAndSelect</code> is <code>true</code> the Diff is revealed and
	 * selected in both TextParts.
	 */
	private void setCurrentDiff(Diff d, boolean revealAndSelect) {

		if (d == fCurrentDiff)
			return;
						
		Diff oldDiff= fCurrentDiff;
					
		if (d != null && revealAndSelect) {
			
			// before we set fCurrentDiff we change the selection
			// so that the paint code uses the old background colors
			// otherwise we get screen cheese
			if (isThreeWay())
				fAncestor.setSelection(d.fAncestorPos);
			fLeft.setSelection(d.fLeftPos);
			fRight.setSelection(d.fRightPos);
			
			// now switch diffs
			fCurrentDiff= d;
			revealDiff(d, d.fIsToken);
		} else {
			fCurrentDiff= d;
		}
		
		updateDiffBackground(oldDiff);
		updateDiffBackground(fCurrentDiff);
		
		updateControls();
		invalidateLines();
	}
	
	private void revealDiff(Diff d, boolean smart) {
		
		boolean ancestorIsVisible= false;
		boolean leftIsVisible= false;
		boolean rightIsVisible= false;

		if (smart) {
			Point region= new Point(0, 0);
			// find the starting line of the diff in all text widgets
			int ls= fLeft.getLineRange(d.fLeftPos, region).x;
			int rs= fRight.getLineRange(d.fRightPos, region).x;
			
			if (isThreeWay()) {
				int as= fAncestor.getLineRange(d.fAncestorPos, region).x;
				if (as >= fAncestor.getTopIndex() && as <= fAncestor.getBottomIndex())
					ancestorIsVisible= true;
			}

			if (ls >= fLeft.getTopIndex() && ls <= fLeft.getBottomIndex())
				leftIsVisible= true;

			if (rs >= fRight.getTopIndex() && rs <= fRight.getBottomIndex())
				rightIsVisible= true;

			if (leftIsVisible && rightIsVisible)
				return;
		}

		int vpos= 0;
		
		MergeSourceViewer allButThis= null;
		if (leftIsVisible) {
			vpos= realToVirtualPosition(fLeft, fLeft.getTopIndex());
			allButThis= fLeft;
		} else if (rightIsVisible) {
			vpos= realToVirtualPosition(fRight, fRight.getTopIndex());
			allButThis= fRight;
		} else if (ancestorIsVisible) {
			vpos= realToVirtualPosition(fAncestor, fAncestor.getTopIndex());
			allButThis= fAncestor;
		} else {
			if (fAllDiffs != null) {
				Iterator e= fAllDiffs.iterator();
				for (int i= 0; e.hasNext(); i++) {
					Diff diff= (Diff) e.next();
					if (diff == d)
						break;
					vpos+= diff.getMaxDiffHeight(fShowAncestor);
				}
			}
			//vpos-= fRight.getViewportLines()/4;
		}
						
		scrollVertical(vpos, allButThis);
		
		if (fVScrollBar != null) {
			//int value= Math.max(0, Math.min(vpos, getVirtualHeight() - maxExtentHeight));
			fVScrollBar.setSelection(vpos);
		}
	}
	
	//--------------------------------------------------------------------------------
	
	protected void copy(boolean leftToRight) {
		if (leftToRight) {
			if (fLeft.getEnabled()) {
				// copy text
				String text= fLeft.getTextWidget().getText();
				fRight.getTextWidget().setText(text);
				fRight.setEnabled(true);
			} else {
				// delete
				fRight.getTextWidget().setText("");
				fRight.setEnabled(false);
			}
			fRightLineCount= fRight.getLineCount();
			setRightDirty(true);
			fRightContentsChanged= false;
		} else {
			if (fRight.getEnabled()) {
				// copy text
				String text= fRight.getTextWidget().getText();
				fLeft.getTextWidget().setText(text);
				fLeft.setEnabled(true);
			} else {
				// delete
				fLeft.getTextWidget().setText("");
				fLeft.setEnabled(false);
			}
			fLeftLineCount= fLeft.getLineCount();
			setLeftDirty(true);
			fLeftContentsChanged= false;			
		}
		doDiff();
		invalidateLines();
		updateVScrollBar();
		selectFirstDiff();
	}

	private void copyDiffLeftToRight() {
		copy(fCurrentDiff, true, false, false);
	}

	private void copyDiffRightToLeft() {
		copy(fCurrentDiff, false, false, false);
	}
	
//	private void accept(Diff diff, boolean both, boolean gotoNext) {
//		if (getCompareConfiguration().isRightEditable())
//			copy(diff, true, both, gotoNext);
//		else if (getCompareConfiguration().isLeftEditable())
//			copy(diff, false, both, gotoNext);
//	}
	
	private void copy(Diff diff, boolean leftToRight, boolean both, boolean gotoNext) {
		
		if (diff != null && !diff.isResolved()) {

			Position fromPos= null;
			Position toPos= null;
			IDocument fromDoc= null;
			IDocument toDoc= null;

			if (leftToRight) {
				fRight.setEnabled(true);
				fromPos= diff.fLeftPos;
				toPos= diff.fRightPos;
				fromDoc= fLeft.getDocument();
				toDoc= fRight.getDocument();
			} else {
				fLeft.setEnabled(true);
				fromPos= diff.fRightPos;
				toPos= diff.fLeftPos;
				fromDoc= fRight.getDocument();
				toDoc= fLeft.getDocument();
			}
			
			if (fromDoc != null) {
				
				int fromStart= fromPos.getOffset();
				int fromLen= fromPos.getLength();
				
				int toStart= toPos.getOffset();
				int toLen= toPos.getLength();

				try {
					String s= null;
											
					switch (diff.fDirection) {
					case RangeDifference.RIGHT:
					case RangeDifference.LEFT:
						s= fromDoc.get(fromStart, fromLen);
						break;
					case RangeDifference.ANCESTOR:
						break;
					case RangeDifference.CONFLICT:
						s= fromDoc.get(fromStart, fromLen);
						if (both)
							s+= toDoc.get(toStart, toLen);
						break;
					}
					if (s != null) {
						toDoc.replace(toStart, toLen, s);						
						toPos.setOffset(toStart);
						toPos.setLength(s.length());
					}	
				
				} catch (BadLocationException e) {
				}
			}
		
			diff.setResolved(true);

			if (gotoNext) {
				navigate(true/*, true*/);
			} else {
				revealDiff(diff, true);
				updateControls();
			}
		}
	}

	/**
	 */
//	private void reject(Diff diff, boolean gotoNext) {
//		
//		if (diff != null && !diff.isResolved()) {
//							
//			switch (diff.fDirection) {
//			case RangeDifference.RIGHT:
//				setRightDirty(true);		// mark dirty to force save!
//				break;
//			case RangeDifference.LEFT:
//				setLeftDirty(true);		// mark dirty to force save!
//				break;
//			case RangeDifference.ANCESTOR:
//				break;
//			case RangeDifference.CONFLICT:
//				setLeftDirty(true);		// mark dirty to force save!
//				setRightDirty(true);		// mark dirty to force save!
//				break;
//			}
//		
//			diff.setResolved(true);
//
//			if (gotoNext) {
//				navigate(true/*, true*/);
//			} else {
//				revealDiff(diff, true);
//				updateControls();
//			}
//		}
//	}
	

//	private void autoResolve() {
//		fCurrentDiff= null;
//		Diff firstConflict= null;
//		
//		Iterator e= fChangeDiffs.iterator();
//		for (int i= 0; e.hasNext(); i++) {
//			Diff diff= (Diff) e.next();
//			if (!diff.isResolved()) {
//				switch (diff.fDirection) {
//				case RangeDifference.RIGHT:	// outgoing
//				case RangeDifference.LEFT:	// incoming
//					accept(diff, false, false);
//					break;
//				case RangeDifference.CONFLICT:	// incoming
//					if (firstConflict == null)
//						firstConflict= diff;
//					break;
//				}
//			}
//		}
//		
//		if (firstConflict == null)
//			firstConflict= (Diff) fChangeDiffs.get(0);
//		setCurrentDiff(firstConflict, true);
//	}
		
	//---- scrolling

	/**
	 * Calculates virtual height (in lines) of views by adding the maximum of corresponding diffs.
	 */
	private int getVirtualHeight() {
		int h= 1;
		if (fAllDiffs != null) {
			Iterator e= fAllDiffs.iterator();
			for (int i= 0; e.hasNext(); i++) {
				Diff diff= (Diff) e.next();
				h+= diff.getMaxDiffHeight(fShowAncestor);
			}
		}
		return h;
	}
	
	/**
	 * The height of the TextEditors in lines.
	 */
	private int getViewportHeight() {
		StyledText te= fLeft.getTextWidget();
		
		int vh= te.getClientArea().height;
		if (vh == 0) {
			// seems to be a bug in TextEditor.getClientArea(): returns bogus value on first
			// call; as a workaround we calculate the clientArea from its container...
			Rectangle trim= te.computeTrim(0, 0, 0, 0);
			int scrollbarHeight= trim.height;
			
			int headerHeight= getHeaderHeight();
	
			Composite composite= (Composite) getControl();
			Rectangle r= composite.getClientArea();
							
			vh= r.height-headerHeight-scrollbarHeight;
		}															

		return vh / te.getLineHeight();
	}
	
	/**
	 * Returns the virtual position for the given view position.
	 */
	private int realToVirtualPosition(MergeSourceViewer w, int vpos) {

		if (! fSynchronizedScrolling || fAllDiffs == null)
			return vpos;
				
		int viewPos= 0;		// real view position
		int virtualPos= 0;	// virtual position
		Point region= new Point(0, 0);
		
		Iterator e= fAllDiffs.iterator();
		while (e.hasNext()) {
			Diff diff= (Diff) e.next();
			Position pos= diff.getPosition(w);
			w.getLineRange(pos, region);
			int realHeight= region.y;
			int virtualHeight= diff.getMaxDiffHeight(fShowAncestor);
			if (vpos <= viewPos + realHeight) {	// OK, found!
				vpos-= viewPos;	// make relative to this slot
				// now scale position within this slot to virtual slot
				if (realHeight <= 0)
					vpos= 0;
				else
					vpos= (vpos*virtualHeight)/realHeight;
				return virtualPos+vpos;
			}
			viewPos+= realHeight;
			virtualPos+= virtualHeight;
		}
		return virtualPos;
	}
		
	private void scrollVertical(int virtualPos, MergeSourceViewer allBut) {
		
		if (virtualPos < 0)
			virtualPos= virtualPos;
		
		GC gc;
		if (fSynchronizedScrolling) {
			int s= 0;
			
			if (true) {
				s= getVirtualHeight() - virtualPos;
				int height= fRight.getViewportLines()/4;
				if (s < 0)
					s= 0;
				if (s > height)
					s= height;
			}
	
			fInScrolling= true;
					
			if (isThreeWay() && allBut != fAncestor) {
				int y= virtualToRealPosition(fAncestor, virtualPos+s)-s;
				fAncestor.vscroll(y);
			}
	
			if (allBut != fLeft) {
				int y= virtualToRealPosition(fLeft, virtualPos+s)-s;
				fLeft.vscroll(y);
			}
	
			if (allBut != fRight) {
				int y= virtualToRealPosition(fRight, virtualPos+s)-s;
				fRight.vscroll(y);
			}
			
			fInScrolling= false;
			
			if (isThreeWay())
				fAncestorCanvas.repaint();
				
			fLeftCanvas.repaint();
			
			Control center= getCenter();
			if (center instanceof BufferedCanvas)
				((BufferedCanvas)center).repaint();
				
			fRightCanvas.repaint();
		} else {
			if (allBut == fAncestor && isThreeWay())
				fAncestorCanvas.repaint();
		
			if (allBut == fLeft)
				fLeftCanvas.repaint();
			
			if (allBut == fRight)
				fRightCanvas.repaint();
		}
	}
		
	/**
	 * Updates Scrollbars with viewports.
	 */
	private void syncViewport(MergeSourceViewer w) {
		
		if (fInScrolling)
			return;

		int ix= w.getTopIndex();
		int ix2= w.getDocumentRegionOffset();
		
		int viewPosition= realToVirtualPosition(w, ix-ix2);
				
		scrollVertical(viewPosition, w);	// scroll all but the given views
		
		if (fVScrollBar != null) {
			int value= Math.max(0, Math.min(viewPosition, getVirtualHeight() - getViewportHeight()));
			fVScrollBar.setSelection(value);
		}
	}

	/**
	 */
	private void updateVScrollBar() {
		
		if (Utilities.okToUse(fVScrollBar) && fVScrollBar.isVisible()) {
			int virtualHeight= getVirtualHeight();
			int viewPortHeight= getViewportHeight();
			fVScrollBar.setPageIncrement(viewPortHeight-1);
			fVScrollBar.setMaximum(virtualHeight);
			if (viewPortHeight > virtualHeight)
				fVScrollBar.setThumb(virtualHeight);
			else
				fVScrollBar.setThumb(viewPortHeight);				
		}
	}
	
	/**
	 * maps given virtual position into a real view position of this view.
	 */
	private int virtualToRealPosition(MergeSourceViewer part, int v) {
			
		if (! fSynchronizedScrolling || fAllDiffs == null)
			return v;
					
		int virtualPos= 0;
		int viewPos= 0;
		Point region= new Point(0, 0);
		
		Iterator e= fAllDiffs.iterator();
		while (e.hasNext()) {
			Diff diff= (Diff) e.next();
			Position pos= diff.getPosition(part);
			int viewHeight= part.getLineRange(pos, region).y;
			int virtualHeight= diff.getMaxDiffHeight(fShowAncestor);
			if (v < (virtualPos + virtualHeight)) {
				v-= virtualPos;		// make relative to this slot
				if (viewHeight <= 0) {
					v= 0;
				} else {
					v= (v*viewHeight)/virtualHeight;
				}
				return viewPos+v;
			}
			virtualPos+= virtualHeight;
			viewPos+= viewHeight;
		}
		return viewPos;
	}
	
}
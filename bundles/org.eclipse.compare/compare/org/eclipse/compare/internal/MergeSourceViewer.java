/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.internal;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Color;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.jface.viewers.SelectionChangedEvent;import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.IWorkbenchActionConstants;

import org.eclipse.compare.contentmergeviewer.TextMergeViewer;

/**
 * Extends the SEF SourceViewer with some convenience methods.
 */
public class MergeSourceViewer extends SourceViewer {
						
	class TextMergeAction extends Action implements IUpdate, ISelectionChangedListener {
		
		int fOperationCode;
		
		TextMergeAction(int operationCode) {
			fOperationCode= operationCode;
			update();
		}
		
		public void run() {
			if (fOperationCode != -1 && canDoOperation(fOperationCode))
				doOperation(fOperationCode);
		}

		public boolean isEnabled() {
			return canDoOperation(fOperationCode);
		}
		
		public void update() {
			this.setEnabled(isEnabled());
		}
		
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection= event.getSelection();
			if (selection instanceof ITextSelection)
				update();
		}
}


	private IRegion fRegion;
	private boolean fEnabled= true;
	private HashMap fActions= new HashMap();
	
	
	public MergeSourceViewer(Composite parent) {
		super(parent, null, SWT.H_SCROLL + SWT.V_SCROLL);
	}
		
	public void setEnabled(boolean enabled) {
		if (enabled != fEnabled) {
			fEnabled= enabled;
			StyledText c= getTextWidget();
			if (c != null) {
				c.setEnabled(enabled);
				Display d= c.getDisplay();
				c.setBackground(enabled ? d.getSystemColor(SWT.COLOR_LIST_BACKGROUND) : null);
			}
		}
	}
	
	public boolean getEnabled() {
		return fEnabled;
	}

	public void setRegion(IRegion region) {
		fRegion= region;
	}
	
	public IRegion getRegion() {
		return fRegion;
	}
	
	public boolean isControlOkToUse() {
		StyledText t= getTextWidget();
		return t != null && !t.isDisposed();
	}
				
	public void setSelection(Position p) {
		setSelectedRange(p.getOffset(), p.getLength());
	}
	
	public void setLineBackground(Position p, Color c) {
		StyledText t= getTextWidget();
		if (t != null && !t.isDisposed()) {
			Point region= new Point(0, 0);
			getLineRange(p, region);
		
			region.x-= getDocumentRegionOffset();
		
			t.setLineBackground(region.x, region.y, c);
		}
	}
	
	public void resetLineBackground() {
		StyledText t= getTextWidget();
		if (t != null && !t.isDisposed()) {
			int lines= getLineCount();
			t.setLineBackground(0, lines, null);
		}
	}
	
	/**
	 * Returns number of lines in document region.
	 */
	public int getLineCount() {
		IRegion region= getVisibleRegion();

		int length= region.getLength();
		if (length == 0)
			return 0;
		
		IDocument doc= getDocument();
		int startLine= 0;
		int endLine= 0;

		int start= region.getOffset();
		try {
			startLine= doc.getLineOfOffset(start);
		} catch(BadLocationException ex) {
		}
		try {
			endLine= doc.getLineOfOffset(start+length);
		} catch(BadLocationException ex) {
		}
		
		return endLine-startLine+1;
	}
	
	public int getViewportLines() {
		StyledText te= getTextWidget();
		Rectangle clArea= te.getClientArea();
		if (!clArea.isEmpty())
			return clArea.height / te.getLineHeight();
		return 0;
	}

	public int getViewportHeight() {
		StyledText te= getTextWidget();
		Rectangle clArea= te.getClientArea();
		if (!clArea.isEmpty())
			return clArea.height;
		return 0;
	}
	
	/**
	 * Returns lines
	 */
	public int getDocumentRegionOffset() {
		int start= getVisibleRegion().getOffset();
		IDocument doc= getDocument();
		if (doc != null) {
			try {
				return doc.getLineOfOffset(start);
			} catch(BadLocationException ex) {
			}
		}
		return 0;
	}
	
	public int getVerticalScrollOffset() {
		StyledText st= getTextWidget();
		int lineHeight= st.getLineHeight();
		return getTopInset() - ((getDocumentRegionOffset()*lineHeight) + st.getTopPixel());
	}

	/**
	 * Returns the start line and the number of lines which correspond to the given position.
	 * Starting line number is 0 based.
	 */
	public Point getLineRange(Position p, Point region) {
		
		if (p == null) {
			region.x= 0;
			region.y= 0;
			return region;
		}
		
		IDocument doc= getDocument();
		
		int start= p.getOffset();
		int length= p.getLength();
		
		int startLine= 0;
		try {
			startLine= doc.getLineOfOffset(start);
		} catch (BadLocationException e) {
		}
		
		int lineCount= 0;
		
		if (length == 0) {
//			// if range length is 0 and if range starts a new line
//			try {
//				if (start == doc.getLineStartOffset(startLine)) {
//					lines--;
//				}
//			} catch (BadLocationException e) {
//				lines--;
//			}
			
		} else {
			int endLine= 0;
			try {
				endLine= doc.getLineOfOffset(start + length - 1);	// why -1?
			} catch (BadLocationException e) {
			}
			lineCount= endLine-startLine+1;
		}
				
		region.x= startLine;
		region.y= lineCount;
		return region;
	}
	
	/**
	 * Scroll TextPart to the given line.
	 */
	public void vscroll(int line) {

		int srcViewSize= getLineCount();
		int srcExtentSize= getViewportLines();

		if (srcViewSize > srcExtentSize) {
			//if (pos + srcExtentSize > srcViewSize)
			//	pos= srcViewSize-srcExtentSize;

			if (line < 0)
				line= 0;

			int cp= getTopIndex();
			if (cp != line)
				setTopIndex(line + getDocumentRegionOffset());
		}
	}
	
	/**
	 * Returns -1 on error
	 */
	private int getOperationCode(String operation) {
		if (IWorkbenchActionConstants.UNDO.equals(operation))
			return UNDO;
		if (IWorkbenchActionConstants.REDO.equals(operation))
			return REDO;
		if (IWorkbenchActionConstants.CUT.equals(operation))
			return CUT;
		if (IWorkbenchActionConstants.CUT.equals(operation))
			return CUT;
		if (IWorkbenchActionConstants.COPY.equals(operation))
			return COPY;
		if (IWorkbenchActionConstants.PASTE.equals(operation))
			return PASTE;
		if (IWorkbenchActionConstants.DELETE.equals(operation))
			return DELETE;
		if (IWorkbenchActionConstants.SELECT_ALL.equals(operation))
			return SELECT_ALL;
//		if (IWorkbenchActionConstants.SHIFT_RIGHT.equals(operation))
//			return SHIFT_RIGHT;
//		if (IWorkbenchActionConstants.SHIFT_LEFT.equals(operation))
//			return SHIFT_LEFT;
//		if (IWorkbenchActionConstants.PREFIX.equals(operation))
//			return PREFIX;
//		if (IWorkbenchActionConstants.STRIP_PREFIX.equals(operation))
//			return STRIP_PREFIX;
		return -1;	// error
	}
	
	public Action getAction(String operation) {
		Action action= (Action) fActions.get(operation);
		if (action == null) {
			action= new TextMergeAction(getOperationCode(operation));
//			if (action instanceof ISelectionChangedListener)
//				addSelectionChangedListener((ISelectionChangedListener)action);
			fActions.put(operation, action);
		}
		return action;
	}
}


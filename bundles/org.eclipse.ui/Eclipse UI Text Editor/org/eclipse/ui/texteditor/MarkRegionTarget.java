package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IMarkRegionTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;

/**
 * An implementation of <code>IMarkRegionTarget</code> using <code>ITextViewer</code> and
 * <code>IStatusLineManager</code>.
 */
public class MarkRegionTarget implements IMarkRegionTarget {
	
	/** The text viewer. */
	private final ITextViewer fViewer;	
	/** The status line. */
	private final IStatusLineManager fStatusLine;
	
	/**
	 * Creates a MarkRegionTaret.
	 * 
	 * @param viewer the text viewer
	 * @param manager the status line manager
	 */
	public MarkRegionTarget(ITextViewer viewer, IStatusLineManager manager) {
		fViewer= viewer;
		fStatusLine= manager;		
	}
	
	/*
	 * @see IMarkregion#setMarkAtCursor(boolean)
	 */
	public void setMarkAtCursor(boolean set) {
		
		if (!(fViewer instanceof ITextViewerExtension))
			return;

		ITextViewerExtension viewerExtension= ((ITextViewerExtension) fViewer);

		if (set) {
			Point selection= fViewer.getSelectedRange();
			viewerExtension.setMark(selection.x);
		
			fStatusLine.setErrorMessage(""); //$NON-NLS-1$
			fStatusLine.setMessage(EditorMessages.getString("Editor.mark.status.message.mark.set")); //$NON-NLS-1$
	
		} else {
			viewerExtension.setMark(-1);

			fStatusLine.setErrorMessage(""); //$NON-NLS-1$
			fStatusLine.setMessage(EditorMessages.getString("Editor.mark.status.message.mark.cleared")); //$NON-NLS-1$								
		}
	}

	/*
	 * @see IMarkregion#swapMarkAndCursor()
	 */
	public void swapMarkAndCursor() {

		if (!(fViewer instanceof ITextViewerExtension))
			return;

		ITextViewerExtension viewerExtension= ((ITextViewerExtension) fViewer);

		int markPosition= viewerExtension.getMark();		
		if (markPosition == -1) {
			fStatusLine.setErrorMessage("mark not set");
			fStatusLine.setMessage(""); //$NON-NLS-1$			
			return;
		}

		IRegion region= fViewer.getVisibleRegion();
		int offset= region.getOffset();
		int length= region.getLength();

		if (markPosition < offset || markPosition > offset + length) {
			fStatusLine.setErrorMessage("mark not in visible region");
			fStatusLine.setMessage(""); //$NON-NLS-1$
			return;
		}		
		
		Point selection= fViewer.getSelectedRange();		
		viewerExtension.setMark(selection.x);

		fViewer.setSelectedRange(markPosition, 0);
		fViewer.revealRange(markPosition, 0);

		fStatusLine.setErrorMessage(""); //$NON-NLS-1$
		fStatusLine.setMessage(EditorMessages.getString("Editor.mark.status.message.mark.swapped")); //$NON-NLS-1$
	}

}	

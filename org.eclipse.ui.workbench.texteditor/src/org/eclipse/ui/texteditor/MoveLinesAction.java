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
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.internal.texteditor.CompoundEditExitStrategy;
import org.eclipse.ui.internal.texteditor.ICompoundEditListener;

/**
 * Action for moving selected lines in an editor.
 * @since 3.0
 */
public class MoveLinesAction extends TextEditorAction {

	/* configuration variables - define what this action does */

	/** <code>true</code> if lines are shifted upwards, <code>false</code> otherwise. */
	private final boolean fUpwards;
	/** <code>true</code> if lines are to be copied instead of moved. */
	private final boolean fCopy;
	/** The editor we are working on. */
	private final  AbstractTextEditor fEditor;

	/* compound members of this action */

	/** The exit strategy that will detect the ending of a compound edit */
	private final CompoundEditExitStrategy fStrategy;

	/* process variables - may change in every run() */

	/**
	 * Set to <code>true</code> by <code>getMovingSelection</code> if the resulting selection
	 * should include the last delimiter.
	 */
	private boolean fAddDelimiter;
	/** <code>true</code> if a compound move / copy is going on. */
	private boolean fEditInProgress= false;

	/**
	 * Creates and initializes the action for the given text editor.
	 * The action configures its visual representation from the given resource
	 * bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or  <code>null</code> if none
	 * @param editor the text editor
	 * @param upwards <code>true</code>if the selected lines should be moved upwards,
	 * <code>false</code> if downwards
	 * @param copy if <code>true</code>, the action will copy lines instead of moving them
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public MoveLinesAction(ResourceBundle bundle, String prefix, AbstractTextEditor editor, boolean upwards, boolean copy) {
		super(bundle, prefix, editor);
		fEditor= editor;
		fUpwards= upwards;
		fCopy= copy;
		String[] commandIds= copy ? new String[] {ITextEditorActionDefinitionIds.COPY_LINES_UP, ITextEditorActionDefinitionIds.COPY_LINES_DOWN } : new String[] {ITextEditorActionDefinitionIds.MOVE_LINES_UP, ITextEditorActionDefinitionIds.MOVE_LINES_DOWN };
		fStrategy= new CompoundEditExitStrategy(commandIds);
		fStrategy.addCompoundListener(new ICompoundEditListener() {
			public void endCompoundEdit() {
				MoveLinesAction.this.endCompoundEdit();
			}
		});
		update();
	}

	/**
	 * Ends the compound change.
	 */
	private void beginCompoundEdit() {
		if (fEditInProgress || fEditor == null)
			return;

		fEditInProgress= true;

		fStrategy.arm(fEditor.getSourceViewer());

		IRewriteTarget target= (IRewriteTarget)fEditor.getAdapter(IRewriteTarget.class);
		if (target != null) {
			target.beginCompoundChange();
		}
	}

	/**
	 * Checks if <code>selection</code> is contained by the visible region of <code>viewer</code>.
	 * As a special case, a selection is considered contained even if it extends over the visible
	 * region, but the extension stays on a partially contained line and contains only white space.
	 *
	 * @param selection the selection to be checked
	 * @param viewer the viewer displaying a visible region of <code>selection</code>'s document.
	 * @return <code>true</code>, if <code>selection</code> is contained, <code>false</code> otherwise.
	 */
	private boolean containedByVisibleRegion(ITextSelection selection, ISourceViewer viewer) {
		int min= selection.getOffset();
		int max= min + selection.getLength();
		IDocument document= viewer.getDocument();

		IRegion visible;
		if (viewer instanceof ITextViewerExtension5)
			visible= ((ITextViewerExtension5) viewer).getModelCoverage();
		else
			visible= viewer.getVisibleRegion();

		int visOffset= visible.getOffset();
		try {
			if (visOffset > min) {
				if (document.getLineOfOffset(visOffset) != selection.getStartLine())
					return false;
				if (!isWhitespace(document.get(min, visOffset - min))) {
					showStatus();
					return false;
				}
			}
			int visEnd= visOffset + visible.getLength();
			if (visEnd < max) {
				if (document.getLineOfOffset(visEnd) != selection.getEndLine())
					return false;
				if (!isWhitespace(document.get(visEnd, max - visEnd))) {
					showStatus();
					return false;
				}
			}
			return true;
		} catch (BadLocationException e) {
		}
		return false;
	}

	/**
	 * Ends the compound change.
	 */
	private void endCompoundEdit() {
		if (!fEditInProgress || fEditor == null)
			return;

		IRewriteTarget target= (IRewriteTarget)fEditor.getAdapter(IRewriteTarget.class);
		if (target != null) {
			target.endCompoundChange();
		}

		fEditInProgress= false;
	}

	/**
	 * Given a selection on a document, computes the lines fully or partially covered by
	 * <code>selection</code>. A line in the document is considered covered if
	 * <code>selection</code> comprises any characters on it, including the terminating delimiter.
	 * <p>Note that the last line in a selection is not considered covered if the selection only
	 * comprises the line delimiter at its beginning (that is considered part of the second last
	 * line).
	 * As a special case, if the selection is empty, a line is considered covered if the caret is
	 * at any position in the line, including between the delimiter and the start of the line. The
	 * line containing the delimiter is not considered covered in that case.
	 * </p>
	 *
	 * @param document the document <code>selection</code> refers to
	 * @param selection a selection on <code>document</code>
	 * @param viewer the <code>ISourceViewer</code> displaying <code>document</code>
	 * @return a selection describing the range of lines (partially) covered by
	 * <code>selection</code>, without any terminating line delimiters
	 * @throws BadLocationException if the selection is out of bounds (when the underlying document has changed during the call)
	 */
	private ITextSelection getMovingSelection(IDocument document, ITextSelection selection, ISourceViewer viewer) throws BadLocationException {
		int low= document.getLineOffset(selection.getStartLine());
		int endLine= selection.getEndLine();
		int high= document.getLineOffset(endLine) + document.getLineLength(endLine);

		// get everything up to last line without its delimiter
		String delim= document.getLineDelimiter(endLine);
		if (delim != null)
			high -= delim.length();

		// the new selection will cover the entire lines being moved, except for the last line's
		// delimiter. The exception to this rule is an empty last line, which will stay covered
		// including its delimiter
		if (delim != null && document.getLineLength(endLine) == delim.length())
			fAddDelimiter= true;
		else
			fAddDelimiter= false;

		return new TextSelection(document, low, high - low);
	}

	/**
	 * Computes the region of the skipped line given the text block to be moved. If
	 * <code>fUpwards</code> is <code>true</code>, the line above <code>selection</code>
	 * is selected, otherwise the line below.
	 *
	 * @param document the document <code>selection</code> refers to
	 * @param selection the selection on <code>document</code> that will be moved.
	 * @return the region comprising the line that <code>selection</code> will be moved over, without its terminating delimiter.
	 */
	private ITextSelection getSkippedLine(IDocument document, ITextSelection selection) {
		int skippedLineN= (fUpwards ? selection.getStartLine() - 1 : selection.getEndLine() + 1);
		if (skippedLineN < 0 || skippedLineN >= document.getNumberOfLines())
			return null;
		try {
			IRegion line= document.getLineInformation(skippedLineN);
			return new TextSelection(document, line.getOffset(), line.getLength());
		} catch (BadLocationException e) {
			// only happens on concurrent modifications
			return null;
		}
	}

	/**
	 * Checks for white space in a string.
	 *
	 * @param string the string to be checked or <code>null</code>
	 * @return <code>true</code> if <code>string</code> contains only white space or is
	 * <code>null</code>, <code>false</code> otherwise
	 */
	private boolean isWhitespace(String string) {
		return string == null ? true : string.trim().length() == 0;
	}

	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void runWithEvent(Event event) {

		// get involved objects
		if (fEditor == null)
			return;

		if (!validateEditorInputState())
			return;

		ISourceViewer viewer= fEditor.getSourceViewer();
		if (viewer == null)
			return;

		IDocument document= viewer.getDocument();
		if (document == null)
			return;

		StyledText widget= viewer.getTextWidget();
		if (widget == null)
			return;

		// get selection
		Point p= viewer.getSelectedRange();
		if (p == null)
			return;

		ITextSelection sel= new TextSelection(document, p.x, p.y);

		ITextSelection skippedLine= getSkippedLine(document, sel);
		if (skippedLine == null)
			return;

		try {

			ITextSelection movingArea= getMovingSelection(document, sel, viewer);

			// if either the skipped line or the moving lines are outside the widget's
			// visible area, bail out
			if (!containedByVisibleRegion(movingArea, viewer) || !containedByVisibleRegion(skippedLine, viewer))
				return;

			// get the content to be moved around: the moving (selected) area and the skipped line
			String moving= movingArea.getText();
			String skipped= skippedLine.getText();
			if (moving == null || skipped == null)
				return;

			String delim;
			String insertion;
			int offset, deviation;
			if (fUpwards) {
				delim= document.getLineDelimiter(skippedLine.getEndLine());
				Assert.isNotNull(delim);
				if (fCopy) {
					insertion= moving + delim;
					offset= movingArea.getOffset();
					deviation= 0;
				} else {
					insertion= moving + delim + skipped;
					offset= skippedLine.getOffset();
					deviation= -skippedLine.getLength() - delim.length();
				}
			} else {
				delim= document.getLineDelimiter(movingArea.getEndLine());
				Assert.isNotNull(delim);
				if (fCopy) {
					insertion= moving + delim;
					offset= skippedLine.getOffset();
					deviation= movingArea.getLength() + delim.length();
				} else {
					insertion= skipped + delim + moving;
					offset= movingArea.getOffset();
					deviation= skipped.length() + delim.length();
				}
			}

			// modify the document
			beginCompoundEdit();
			if (fCopy) {
//				fDescription= new EditDescription(offset, 0, insertion.length());
				document.replace(offset, 0, insertion);
			} else {
//				fDescription= new EditDescription(offset, insertion.length(), insertion.length());
				document.replace(offset, insertion.length(), insertion);
			}

			// move the selection along
			int selOffset= movingArea.getOffset() + deviation;
			int selLength= movingArea.getLength() + (fAddDelimiter ? delim.length() : 0);
			if (! (viewer instanceof ITextViewerExtension5))
				selLength= Math.min(selLength, viewer.getVisibleRegion().getOffset() + viewer.getVisibleRegion().getLength() - selOffset);
			else {
				// TODO need to check what is necessary in the projection case
			}
			selectAndReveal(viewer, selOffset, selLength);
		} catch (BadLocationException x) {
			// won't happen without concurrent modification - bail out
			return;
		}
	}

	/**
	 * Performs similar to AbstractTextEditor.selectAndReveal, but does not update
	 * the viewers highlight area.
	 *
	 * @param viewer the viewer that we want to select on
	 * @param offset the offset of the selection
	 * @param length the length of the selection
	 */
	private void selectAndReveal(ITextViewer viewer, int offset, int length) {
		// invert selection to avoid jumping to the end of the selection in st.showSelection()
		viewer.setSelectedRange(offset + length, -length);
		//viewer.revealRange(offset, length); // will trigger jumping
		StyledText st= viewer.getTextWidget();
		if (st != null)
			st.showSelection(); // only minimal scrolling
	}

	/**
	 * Displays information in the status line why a line move is not possible
	 */
	private void showStatus() {
		IEditorStatusLine status= (IEditorStatusLine) fEditor.getAdapter(IEditorStatusLine.class);
		if (status == null)
			return;
		status.setMessage(false, EditorMessages.Editor_MoveLines_IllegalMove_status, null);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		super.update();

		if (isEnabled())
			setEnabled(canModifyEditor());

	}
}

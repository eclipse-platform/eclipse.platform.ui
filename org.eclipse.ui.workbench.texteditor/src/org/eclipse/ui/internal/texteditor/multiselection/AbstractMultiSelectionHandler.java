/*******************************************************************************
 * Copyright (c) 2022 Dirk Steinkamp
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Steinkamp <dirk.steinkamp@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.multiselection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.runtime.Adapters;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.MultiTextSelection;
import org.eclipse.jface.text.Region;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension5;

/**
 * Common super class for Multi-Selection-actions, containing various helper
 * methods. Subclasses need to overwrite {@link #execute()}, which is only
 * invoked if the {@link #textEditor} and {@link #document} could be properly
 * initialized.
 *
 * @see AddAllMatchesToMultiSelectionHandler
 * @see MultiSelectionDownHandler
 * @see MultiSelectionUpHandler
 * @see StopMultiSelectionHandler
 */
abstract class AbstractMultiSelectionHandler extends AbstractHandler {
	/**
	 * Each widget can have a different anchor selection, that is stored in the
	 * widget's data with this key.
	 */
	private static final String ANCHOR_REGION_KEY = "org.eclipse.ui.internal.texteditor.multiselection.AbstractMultiSelectionHandler.anchorRegion"; //$NON-NLS-1$
	private ExecutionEvent event;
	private ITextEditor textEditor;
	private IDocument document;
	/**
	 * SourceViewer Might be <code>null</code>, if {@link #textEditor} doesn't
	 * implement this interface.
	 */
	private ITextViewerExtension5 sourceViewer;

	/**
	 * This method needs to be overwritten from subclasses to handle the event.
	 *
	 * @throws ExecutionException an Exception the event handler might throw
	 */
	public abstract void execute() throws ExecutionException;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (initFrom(event)) {
			execute();
		}
		return null;
	}

	public ExecutionEvent getEvent() {
		return event;
	}

	protected boolean isMultiSelectionActive() {
		IRegion[] regions = getSelectedRegions();
		return regions != null && regions.length > 1;
	}

	protected boolean nothingSelected() {
		IRegion[] regions = getSelectedRegions();
		return regions == null || regions.length == 0 || (regions.length == 1 && regions[0].getLength() == 0);
	}

	protected IRegion[] getSelectedRegions() {
		ISelection selection = textEditor.getSelectionProvider().getSelection();

		if (!(selection instanceof IMultiTextSelection)) {
			return null;
		}

		return ((IMultiTextSelection) selection).getRegions();
	}

	protected IRegion offsetAsCaretRegion(int offset) {
		return createRegionIfValid(offset, 0);
	}

	protected void selectRegion(IRegion region) {
		selectRegions(new IRegion[] { region });
	}

	protected void selectRegions(IRegion[] regions) {
		setBlockSelectionMode(false);

		ISelection newSelection = new MultiTextSelection(document, regions);
		textEditor.getSelectionProvider().setSelection(newSelection);
	}

	protected void selectIdentifierUnderCaret() {
		int offset = getCaretOffset();

		Region identifierRegion = getIdentifierUnderCaretRegion(offset);
		if (identifierRegion != null) {
			selectRegion(identifierRegion);
			setAnchorRegion(identifierRegion);
		}
	}

	protected void selectCaretPosition() {
		IRegion caretRegion = offsetAsCaretRegion(getCaretOffset());
		selectRegion(caretRegion);
		setAnchorRegion(caretRegion);
	}

	protected boolean allRegionsHaveSameText() {
		return allRegionsHaveSameText(getSelectedRegions());
	}

	protected boolean allRegionsEmpty() {
		IRegion[] selectedRegions = getSelectedRegions();
		if (selectedRegions == null)
			return true;
		return isEmpty(selectedRegions[0]) && allRegionsHaveSameText(selectedRegions);
	}

	protected boolean isEmpty(IRegion region) {
		return region == null || region.getLength() == 0;
	}

	protected IRegion getAnchorRegion() {
		return (IRegion) getWidget().getData(ANCHOR_REGION_KEY);
	}

	protected void setAnchorRegion(IRegion selection) {
		if (selection == null) {
			getWidget().setData(ANCHOR_REGION_KEY, null);
		} else {
			getWidget().setData(ANCHOR_REGION_KEY, selection);
		}
	}

	private void initAnchorRegion() {
		IRegion[] regions = getSelectedRegions();
		if ((regions != null && regions.length == 1) || !contains(regions, getAnchorRegion())) {
			setAnchorRegion(regions[0]);
		}
	}

	private boolean contains(IRegion[] regions, IRegion region) {
		return Arrays.asList(regions).contains(region);
	}

	private boolean allRegionsHaveSameText(IRegion[] regions) {
		if (regions == null || regions.length == 1)
			return true;

		try {
			return allRegionsHaveText(regions, regionAsString(regions[0]));
		} catch (BadLocationException e) {
			return false;
		}
	}

	private boolean allRegionsHaveText(IRegion[] regions, String text) throws BadLocationException {
		for (IRegion iRegion : regions) {
			if (!text.equals(regionAsString(iRegion))) {
				return false;
			}
		}
		return true;
	}

	protected IRegion[] addRegion(IRegion[] regions, IRegion newRegion) {
		if (newRegion != null) {
			IRegion[] newRegions = Arrays.copyOf(regions, regions.length + 1);
			newRegions[newRegions.length - 1] = newRegion;
			return newRegions;
		} else {
			return regions;
		}
	}

	protected IRegion[] removeLastRegionButOne(IRegion[] regions) {
		if (regions == null || regions.length == 0)
			return null;
		if (regions.length == 1) {
			return regions;
		}

		return Arrays.copyOf(regions, regions.length - 1);
	}

	protected IRegion[] removeFirstRegionButOne(IRegion[] regions) {
		if (regions == null || regions.length == 0)
			return null;
		if (regions.length == 1) {
			return regions;
		}

		return Arrays.copyOfRange(regions, 1, regions.length);
	}

	protected int getCaretOffset() {
		IRegion[] regions = getSelectedRegions();
		if (regions == null) {
			return -1;
		}
		return regions[0].getOffset() + regions[0].getLength();
	}

	protected void setCaretOffset(int caretOffset) {
		selectRegion(offsetAsCaretRegion(caretOffset));
	}

	protected IRegion findNextMatch(IRegion region) throws ExecutionException {
		try {
			if (region.getLength() == 0) {
				return offsetAsCaretRegion(offsetInNextLine(region.getOffset()));
			} else {
				String searchString = getTextOfRegion(region);

				String fullText = getFullText();
				int matchPos = fullText.indexOf(searchString, offsetAfter(region));
				return createRegionIfValid(matchPos, region.getLength());
			}
		} catch (BadLocationException e) {
			throw new ExecutionException("Internal error in findNextMatch", e);
		}
	}

	protected IRegion findPreviousMatch(IRegion region) throws ExecutionException {
		try {
			if (region.getLength() == 0) {
				return offsetAsCaretRegion(offsetInPreviousLine(region.getOffset()));
			} else {
				String searchString = getTextOfRegion(region);

				String fullText = getFullText();
				int matchPos = fullText.lastIndexOf(searchString, region.getOffset() - 1);
				return createRegionIfValid(matchPos, region.getLength());
			}
		} catch (BadLocationException e) {
			throw new ExecutionException("Internal error in findPreviousMatch", e);
		}
	}

	protected IRegion createRegionIfValid(int offset, int length) {
		if ((offset < 0) || (offset > document.getLength()))
			return null;

		return new Region(offset, Math.min(length, document.getLength() - offset));
	}

	protected IRegion[] findAllMatches(IRegion region) throws ExecutionException {
		try {
			String searchString = getTextOfRegion(region);

			String fullText = getFullText();
			List<IRegion> regions = findAllMatches(fullText, searchString);
			return toArray(regions);
		} catch (BadLocationException e) {
			throw new ExecutionException("Internal error in findAllMatches", e);
		}
	}

	private List<IRegion> findAllMatches(String fullText, String searchString) {
		List<IRegion> regions = new ArrayList<>();
		int length = searchString.length();
		int matchPos = 0;
		while ((matchPos = fullText.indexOf(searchString, matchPos)) >= 0) {
			regions.add(new Region(matchPos, length));
			matchPos += length;
		}
		return regions;
	}

	protected int offsetInNextLine(int offset) throws BadLocationException {
		return moveOffsetByLines(offset, 1);
	}

	protected int offsetInPreviousLine(int offset) throws BadLocationException {
		return moveOffsetByLines(offset, -1);
	}

	private int moveOffsetByLines(int offset, int lineDelta) throws BadLocationException {
		int newLineNo = document.getLineOfOffset(offset) + lineDelta;
		if ((newLineNo < 0) || (newLineNo >= document.getNumberOfLines()))
			return -1;

		int newOffset;
		if (sourceViewer == null) {
			// we don't have a sourceViewer and thus as a fallback
			// assume the widget offsets are identical to the document offsets
			newOffset = moveWidgetOffsetByLines(offset, lineDelta);
		} else {
			int widgetOffset = sourceViewer.modelOffset2WidgetOffset(offset);
			int newWidgetOffset = moveWidgetOffsetByLines(widgetOffset, lineDelta);
			newOffset = sourceViewer.widgetOffset2ModelOffset(newWidgetOffset);
		}
		if (newOffset == -1) {
			return endOfLineOffset(newLineNo);
		}
		return newOffset;
	}

	private int moveWidgetOffsetByLines(int widgetOffset, int lineDelta) throws BadLocationException {
		Point location = getWidget().getLocationAtOffset(widgetOffset);
		Point newLocation = new Point(location.x, location.y + lineDelta * getWidget().getLineHeight(widgetOffset));
		return getWidget().getOffsetAtPoint(newLocation);
	}

	private int endOfLineOffset(int lineNo) throws BadLocationException {
		return document.getLineOffset(lineNo) + document.getLineInformation(lineNo).getLength();
	}

	private boolean initFrom(ExecutionEvent event) {
		this.event = event;
		initTextEditor();
		if (textEditor == null)
			return false;
		initDocument();
		initSourceViewer();
		initAnchorRegion();
		return true;
	}

	private void initTextEditor() {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		textEditor = Adapters.adapt(editor, ITextEditor.class);
	}

	private void initDocument() {
		document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
	}

	private void initSourceViewer() {
		ITextViewer textViewer = textEditor.getAdapter(ITextViewer.class);
		sourceViewer = Adapters.adapt(textViewer, ITextViewerExtension5.class);
	}

	private IRegion[] toArray(List<IRegion> regions) {
		return regions.toArray(new IRegion[regions.size()]);
	}

	private int offsetAfter(IRegion region) {
		return region.getOffset() + region.getLength();
	}

	private String getTextOfRegion(IRegion region) throws BadLocationException {
		return document.get(region.getOffset(), region.getLength());
	}

	private String getFullText() {
		return document.get();
	}

	private String regionAsString(IRegion region) throws BadLocationException {
		return document.get(region.getOffset(), region.getLength());
	}

	private Region getIdentifierUnderCaretRegion(int offset) {
		try {
			int startOffset = findStartOfIdentifier(offset);
			int endOffset = findEndOfIdentifier(startOffset);
			Region identifierRegion = new Region(startOffset, endOffset - startOffset);
			return identifierRegion;
		} catch (BadLocationException e) {
			return null;
		}
	}

	private int findStartOfIdentifier(int offset) throws BadLocationException {
		for (int i = offset - 1; i >= 0; i--) {
			if (!isJavaIdentifierCharAtPos(i)) {
				return i + 1;
			}
		}
		return 0; // start of document reached
	}

	private int findEndOfIdentifier(int offset) throws BadLocationException {
		for (int i = offset; i <= document.getLength(); i++) {
			if (i == document.getLength() || !isJavaIdentifierCharAtPos(i)) {
				return i;
			}
		}
		return offset;
	}

	private boolean isJavaIdentifierCharAtPos(int i) throws BadLocationException {
		return Character.isJavaIdentifierStart(document.getChar(i))
				|| Character.isJavaIdentifierPart(document.getChar(i));
	}

	private StyledText getWidget() {
		return (StyledText) textEditor.getAdapter(Control.class);
	}

	private void setBlockSelectionMode(boolean blockSelectionMode) {
		if (!(textEditor instanceof ITextEditorExtension5)) {
			return;
		}
		ITextEditorExtension5 ext = (ITextEditorExtension5) textEditor;
		ext.setBlockSelectionMode(blockSelectionMode);
	}

	protected boolean selectionIsAboveAnchorRegion() {
		IRegion[] selectedRegions = getSelectedRegions();
		if (selectedRegions == null || selectedRegions.length == 1)
			return false;
		return isLastRegion(getAnchorRegion(), selectedRegions);
	}

	protected boolean selectionIsBelowAnchorRegion() {
		IRegion[] selectedRegions = getSelectedRegions();
		if (selectedRegions == null || selectedRegions.length == 1)
			return false;
		return isFirstRegion(getAnchorRegion(), selectedRegions);
	}

	private boolean isLastRegion(IRegion region, IRegion[] regions) {
		if (region == null || regions == null || regions.length == 0)
			return false;

		return region.equals(regions[regions.length - 1]);
	}

	private boolean isFirstRegion(IRegion region, IRegion[] regions) {
		if (region == null || regions == null || regions.length == 0)
			return false;

		return region.equals(regions[0]);
	}
}

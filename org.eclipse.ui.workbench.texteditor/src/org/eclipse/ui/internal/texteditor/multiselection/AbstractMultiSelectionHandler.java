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
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
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
 * @see AddNextMatchToMultiSelectionHandler
 * @see RemoveLastMatchFromMultiSelectionHandler
 * @see StopMultiSelectionHandler
 */
abstract class AbstractMultiSelectionHandler extends AbstractHandler {
	private ExecutionEvent event;
	private ITextEditor textEditor;
	private IDocument document;

	/**
	 * This method needs to be overwritten from subclasses to handle the event.
	 *
	 * @throws ExecutionException
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
		return regions == null || regions.length == 0 || regions[0].getLength() == 0;
	}

	protected IRegion[] getSelectedRegions() {
		ISelection selection = textEditor.getSelectionProvider().getSelection();

		if (!(selection instanceof IMultiTextSelection)) {
			return null;
		}

		return ((IMultiTextSelection) selection).getRegions();
	}

	protected IRegion offsetAsCaretRegion(int offset) {
		return new Region(offset, 0);
	}

	protected void selectRegion(IRegion region) throws ExecutionException {
		selectRegions(new IRegion[] { region });
	}

	protected void selectRegions(IRegion[] regions) throws ExecutionException {
		setBlockSelectionMode(false);

		ISelection newSelection = new MultiTextSelection(document, regions);
		textEditor.getSelectionProvider().setSelection(newSelection);
	}

	protected void selectIdentifierUnderCaret() throws ExecutionException {
		int offset = getCaretOffset();

		Region identifierRegion = getIdentifierUnderCaretRegion(offset);
		if (identifierRegion != null)
			selectRegion(identifierRegion);
	}

	protected boolean allRegionsHaveSameText() {
		if (nothingSelected())
			return false;
		return allRegionsHaveSameText(getSelectedRegions());
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

	protected int getCaretOffset() {
		return getWidget().getCaretOffset();
	}

	protected void setCaretOffset(int offset) {
		getWidget().setCaretOffset(offset);
	}

	protected IRegion findNextMatch(IRegion region) throws ExecutionException {
		String fullText = getFullText();
		try {
			String searchString = getTextOfRegion(region);

			int matchPos = fullText.indexOf(searchString, offsetAfter(region));
			if (matchPos < 0)
				return null;

			return new Region(matchPos, region.getLength());
		} catch (BadLocationException e) {
			throw new ExecutionException("Internal error in findNextMatch", e);
		}
	}

	protected IRegion[] findAllMatches(IRegion region) throws ExecutionException {
		try {
			String fullText = getFullText();
			String searchString = getTextOfRegion(region);
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

	private boolean initFrom(ExecutionEvent event) {
		this.event = event;
		textEditor = getTextEditor(event);
		if (textEditor == null)
			return false;
		document = getDocument();
		return true;
	}

	private ITextEditor getTextEditor(ExecutionEvent event) {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		return editor instanceof ITextEditor ? (ITextEditor) editor : null;
	}

	private IDocument getDocument() {
		return textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
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
}

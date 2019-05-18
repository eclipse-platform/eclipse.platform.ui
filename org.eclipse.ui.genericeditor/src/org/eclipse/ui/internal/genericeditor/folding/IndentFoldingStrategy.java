/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Angelo Zerr <angelo.zerr@gmail.com> - adapt code org.eclipse.wst.sse.ui.internal.projection.AbstractStructuredFoldingStrategy to support generic indent folding strategy.
 *                                           [generic editor] Default Code folding for generic editor should use IndentFoldingStrategy - Bug 520659 
 */
package org.eclipse.ui.internal.genericeditor.folding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * Indent folding strategy to fold code by using indentation. The folding
 * strategy must be associated with a viewer for it to function.
 */
public class IndentFoldingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension, IProjectionListener {

	private IDocument document;
	private ProjectionViewer viewer;
	private ProjectionAnnotationModel projectionAnnotationModel;
	private final String lineStartsWithKeyword;

	public IndentFoldingStrategy() {
		this(null);
	}

	public IndentFoldingStrategy(String lineStartsWithKeyword) {
		this.lineStartsWithKeyword = lineStartsWithKeyword;
	}

	/**
	 * A FoldingAnnotation is a {@link ProjectionAnnotation} it is folding and
	 * overriding the paint method (in a hacky type way) to prevent one line folding
	 * annotations to be drawn.
	 */
	protected class FoldingAnnotation extends ProjectionAnnotation {
		private boolean visible; /* workaround for BUG85874 */

		/**
		 * Creates a new FoldingAnnotation.
		 * 
		 * @param isCollapsed true if this annotation should be collapsed, false
		 *                    otherwise
		 */
		public FoldingAnnotation(boolean isCollapsed) {
			super(isCollapsed);
			visible = false;
		}

		/**
		 * Does not paint hidden annotations. Annotations are hidden when they only span
		 * one line.
		 * 
		 * @see ProjectionAnnotation#paint(org.eclipse.swt.graphics.GC,
		 *      org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
		 */
		@Override
		public void paint(GC gc, Canvas canvas, Rectangle rectangle) {
			/* workaround for BUG85874 */
			/*
			 * only need to check annotations that are expanded because hidden annotations
			 * should never have been given the chance to collapse.
			 */
			if (!isCollapsed()) {
				// working with rectangle, so line height
				FontMetrics metrics = gc.getFontMetrics();
				if (metrics != null) {
					// do not draw annotations that only span one line and
					// mark them as not visible
					if ((rectangle.height / metrics.getHeight()) <= 1) {
						visible = false;
						return;
					}
				}
			}
			visible = true;
			super.paint(gc, canvas, rectangle);
		}

		@Override
		public void markCollapsed() {
			/* workaround for BUG85874 */
			// do not mark collapsed if annotation is not visible
			if (visible)
				super.markCollapsed();
		}
	}

	/**
	 * The folding strategy must be associated with a viewer for it to function
	 * 
	 * @param viewer the viewer to associate this folding strategy with
	 */
	public void setViewer(ProjectionViewer viewer) {
		if (this.viewer != null) {
			this.viewer.removeProjectionListener(this);
		}
		this.viewer = viewer;
		this.viewer.addProjectionListener(this);
		this.projectionAnnotationModel = this.viewer.getProjectionAnnotationModel();
	}

	public void uninstall() {
		setDocument(null);

		if (viewer != null) {
			viewer.removeProjectionListener(this);
			viewer = null;
		}

		projectionDisabled();
	}

	@Override
	public void setDocument(IDocument document) {
		this.document = document;
	}

	@Override
	public void projectionDisabled() {
		projectionAnnotationModel = null;
	}

	@Override
	public void projectionEnabled() {
		if (viewer != null) {
			projectionAnnotationModel = viewer.getProjectionAnnotationModel();
		}
	}

	private class LineIndent {
		public int line;
		public final int indent;

		public LineIndent(int line, int indent) {
			this.line = line;
			this.indent = indent;
		}
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		if (projectionAnnotationModel != null) {

			// these are what are passed off to the annotation model to
			// actually create and maintain the annotations
			List<Annotation> modifications = new ArrayList<Annotation>();
			List<FoldingAnnotation> deletions = new ArrayList<FoldingAnnotation>();
			List<FoldingAnnotation> existing = new ArrayList<FoldingAnnotation>();
			Map<Annotation, Position> additions = new HashMap<Annotation, Position>();

			// find and mark all folding annotations with length 0 for deletion
			markInvalidAnnotationsForDeletion(dirtyRegion, deletions, existing);

			List<LineIndent> previousRegions = new ArrayList<LineIndent>();

			int tabSize = 1;
			int minimumRangeSize = 1;
			try {

				// Today we recompute annotation from the whole document each
				// time.
				// performance s good even with large document, but it should be
				// better to loop for only DirtyRegion (and before/after)
				// int offset = dirtyRegion.getOffset();
				// int length = dirtyRegion.getLength();
				// int startLine = 0; //document.getLineOfOffset(offset);
				int endLine = document.getNumberOfLines() - 1; // startLine +
																// document.getNumberOfLines(offset,
																// length) - 1;

				// sentinel, to make sure there's at least one entry
				previousRegions.add(new LineIndent(endLine + 1, -1));

				int lastLineWhichIsNotEmpty = 0;
				int lineEmptyCount = 0;
				Integer lastLineForKeyword = null;
				int line = endLine;
				for (line = endLine; line >= 0; line--) {
					int lineOffset = document.getLineOffset(line);
					String delim = document.getLineDelimiter(line);
					int lineLength = document.getLineLength(line) - (delim != null ? delim.length() : 0);
					String lineContent = document.get(lineOffset, lineLength);

					LineState state = getLineState(lineContent, lastLineForKeyword);
					switch (state) {
					case StartWithKeyWord:
						lineEmptyCount = 0;
						lastLineWhichIsNotEmpty = line;
						if (lastLineForKeyword == null) {
							lastLineForKeyword = line;
						}
						break;
					case EmptyLine:
						lineEmptyCount++;
						break;
					default:
						addAnnotationForKeyword(modifications, deletions, existing, additions,
								line + 1 + lineEmptyCount, lastLineForKeyword);
						lastLineForKeyword = null;
						lineEmptyCount = 0;
						lastLineWhichIsNotEmpty = line;
						int indent = computeIndentLevel(lineContent, tabSize);
						if (indent == -1) {
							continue; // only whitespace
						}

						LineIndent previous = previousRegions.get(previousRegions.size() - 1);
						if (previous.indent > indent) {
							// discard all regions with larger indent
							do {
								previousRegions.remove(previousRegions.size() - 1);
								previous = previousRegions.get(previousRegions.size() - 1);
							} while (previous.indent > indent);

							// new folding range
							int endLineNumber = previous.line - 1;
							if (endLineNumber - line >= minimumRangeSize) {
								updateAnnotation(modifications, deletions, existing, additions, line, endLineNumber);
							}
						}
						if (previous.indent == indent) {
							previous.line = line;
						} else { // previous.indent < indent
							// new region with a bigger indent
							previousRegions.add(new LineIndent(line, indent));
						}
					}
				}
				addAnnotationForKeyword(modifications, deletions, existing, additions, lastLineWhichIsNotEmpty,
						lastLineForKeyword);
			} catch (BadLocationException e) {
				// should never done
				e.printStackTrace();
			}

			// be sure projection has not been disabled
			if (projectionAnnotationModel != null) {
				if (existing.size() > 0) {
					deletions.addAll(existing);
				}
				// send the calculated updates to the annotations to the
				// annotation model
				projectionAnnotationModel.modifyAnnotations(deletions.toArray(new Annotation[1]), additions,
						modifications.toArray(new Annotation[0]));
			}
		}
	}

	private void addAnnotationForKeyword(List<Annotation> modifications, List<FoldingAnnotation> deletions,
			List<FoldingAnnotation> existing, Map<Annotation, Position> additions, int startLine,
			Integer lastLineForKeyword) throws BadLocationException {
		if (lastLineForKeyword != null) {
			updateAnnotation(modifications, deletions, existing, additions, startLine, lastLineForKeyword);
		}
	}

	private enum LineState {
		StartWithKeyWord, DontStartWithKeyWord, EmptyLine
	}

	/**
	 * Returns the line state for line which starts with a given keyword.
	 * 
	 * @param lineContent        line content.
	 * @param lastLineForKeyword last line for the given keyword.
	 * @return
	 */
	private LineState getLineState(String lineContent, Integer lastLineForKeyword) {
		if (lineStartsWithKeyword == null) {
			// none keyword defined.
			return LineState.DontStartWithKeyWord;
		}
		if (lineContent != null && lineContent.trim().startsWith(lineStartsWithKeyword)) {
			// The line starts with the given keyword (ex: starts with "import")
			return LineState.StartWithKeyWord;
		}
		if (lastLineForKeyword != null && (lineContent == null || lineContent.trim().length() == 0)) {
			// a last line for keyword was defined, line is empty
			return LineState.EmptyLine;
		}
		return LineState.DontStartWithKeyWord;
	}

	/**
	 * Compute indentation level of the given line by using the given tab size.
	 * 
	 * @param line    the line text.
	 * @param tabSize the tab size.
	 * @return the indentation level of the given line by using the given tab size.
	 */
	private static int computeIndentLevel(String line, int tabSize) {
		int i = 0;
		int indent = 0;
		while (i < line.length()) {
			char ch = line.charAt(i);
			if (ch == ' ') {
				indent++;
			} else if (ch == '\t') {
				indent = indent - indent % tabSize + tabSize;
			} else {
				break;
			}
			i++;
		}
		if (i == line.length()) {
			return -1; // line only consists of whitespace
		}
		return indent;
	}

	/**
	 * Given a {@link DirtyRegion} returns an {@link Iterator} of the already
	 * existing annotations in that region.
	 * 
	 * @param dirtyRegion the {@link DirtyRegion} to check for existing annotations
	 *                    in
	 * 
	 * @return an {@link Iterator} over the annotations in the given
	 *         {@link DirtyRegion}. The iterator could have no annotations in it. Or
	 *         <code>null</code> if projection has been disabled.
	 */
	private Iterator<Annotation> getAnnotationIterator(DirtyRegion dirtyRegion) {
		Iterator<Annotation> annoIter = null;
		// be sure project has not been disabled
		if (projectionAnnotationModel != null) {
			// workaround for Platform Bug 299416
			annoIter = projectionAnnotationModel.getAnnotationIterator(0, document.getLength(), false, false);
		}
		return annoIter;
	}

	/**
	 * Update annotations.
	 * 
	 * @param modifications the folding annotations to update.
	 * @param deletions     the folding annotations to delete.
	 * @param existing      the existing folding annotations.
	 * @param additions     annoation to add
	 * @param line          the line index
	 * @param endLineNumber the end line number
	 * @throws BadLocationException
	 */
	private void updateAnnotation(List<Annotation> modifications, List<FoldingAnnotation> deletions,
			List<FoldingAnnotation> existing, Map<Annotation, Position> additions, int line, Integer endLineNumber)
			throws BadLocationException {
		int startOffset = document.getLineOffset(line);
		int endOffset = document.getLineOffset(endLineNumber) + document.getLineLength(endLineNumber);
		Position newPos = new Position(startOffset, endOffset - startOffset);
		if (existing.size() > 0) {
			FoldingAnnotation existingAnnotation = existing.remove(existing.size() - 1);
			updateAnnotations(existingAnnotation, newPos, modifications, deletions);
		} else {
			additions.put(new FoldingAnnotation(false), newPos);
		}
	}

	/**
	 * Update annotations.
	 * 
	 * @param existingAnnotation the existing annotations that need to be updated
	 *                           based on the given dirtied IndexRegion
	 * @param newPos             the new position that caused the annotations need
	 *                           for updating and null otherwise.
	 * @param modifications      the list of annotations to be modified
	 * @param deletions          the list of annotations to be deleted
	 */
	protected void updateAnnotations(Annotation existingAnnotation, Position newPos, List<Annotation> modifications,
			List<FoldingAnnotation> deletions) {
		if (existingAnnotation instanceof FoldingAnnotation) {
			FoldingAnnotation foldingAnnotation = (FoldingAnnotation) existingAnnotation;

			// if a new position can be calculated then update the position of
			// the annotation,
			// else the annotation needs to be deleted
			if (newPos != null && newPos.length > 0 && projectionAnnotationModel != null) {
				Position oldPos = projectionAnnotationModel.getPosition(foldingAnnotation);
				// only update the position if we have to
				if (!newPos.equals(oldPos)) {
					oldPos.setOffset(newPos.offset);
					oldPos.setLength(newPos.length);
					modifications.add(foldingAnnotation);
				}
			} else {
				deletions.add(foldingAnnotation);
			}
		}
	}

	/**
	 * <p>
	 * Searches the given {@link DirtyRegion} for annotations that now have a length
	 * of 0. This is caused when something that was being folded has been deleted.
	 * These {@link FoldingAnnotation}s are then added to the {@link List} of
	 * {@link FoldingAnnotation}s to be deleted
	 * </p>
	 * 
	 * @param dirtyRegion find the now invalid {@link FoldingAnnotation}s in this
	 *                    {@link DirtyRegion}
	 * @param deletions   the current list of {@link FoldingAnnotation}s marked for
	 *                    deletion that the newly found invalid
	 *                    {@link FoldingAnnotation}s will be added to
	 */
	protected void markInvalidAnnotationsForDeletion(DirtyRegion dirtyRegion, List<FoldingAnnotation> deletions,
			List<FoldingAnnotation> existing) {
		Iterator<Annotation> iter = getAnnotationIterator(dirtyRegion);
		if (iter != null) {
			while (iter.hasNext()) {
				Annotation anno = iter.next();
				if (anno instanceof FoldingAnnotation) {
					FoldingAnnotation folding = (FoldingAnnotation) anno;
					Position pos = projectionAnnotationModel.getPosition(anno);
					if (pos.length == 0) {
						deletions.add(folding);
					} else {
						existing.add(folding);
					}
				}
			}
		}
	}

	@Override
	public void reconcile(IRegion partition) {
		// not used, we use:
		// reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		// Do nothing
	}

	@Override
	public void initialReconcile() {
		reconcile(new DirtyRegion(0, document.getLength(), DirtyRegion.INSERT, document.get()), null);
	}
}
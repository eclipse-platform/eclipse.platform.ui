/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;

import org.eclipse.core.runtime.Assert;


/**
 * Describes the presentation styles for a section of an indexed text such as a
 * document or string. A text presentation defines a default style for the whole
 * section and in addition style differences for individual subsections. Text
 * presentations can be narrowed down to a particular result window. All methods
 * are result window aware, i.e. ranges outside the result window are always
 * ignored.
 * <p>
 * All iterators provided by a text presentation assume that they enumerate non
 * overlapping, consecutive ranges inside the default range. Thus, all these
 * iterators do not include the default range. The default style range must be
 * explicitly asked for using <code>getDefaultStyleRange</code>.
 */
public class TextPresentation {

	/**
	 * Applies the given presentation to the given text widget. Helper method.
	 *
	 * @param presentation the style information
	 * @param text the widget to which to apply the style information
	 * @since 2.0
	 */
	public static void applyTextPresentation(TextPresentation presentation, StyledText text) {

		StyleRange[] ranges= new StyleRange[presentation.getDenumerableRanges()];

		int i= 0;
		Iterator e= presentation.getAllStyleRangeIterator();
		while (e.hasNext())
			ranges[i++]= (StyleRange) e.next();

		text.setStyleRanges(ranges);
	}




	/**
	 * Enumerates all the <code>StyleRange</code>s included in the presentation.
	 */
	class FilterIterator implements Iterator {

		/** The index of the next style range to be enumerated */
		protected int fIndex;
		/** The upper bound of the indices of style ranges to be enumerated */
		protected int fLength;
		/** Indicates whether ranges similar to the default range should be enumerated */
		protected boolean fSkipDefaults;
		/** The result window */
		protected IRegion fWindow;

		/**
		 * <code>skipDefaults</code> tells the enumeration to skip all those style ranges
		 * which define the same style as the presentation's default style range.
		 *
		 * @param skipDefaults <code>false</code> if ranges similar to the default range should be enumerated
		 */
		protected FilterIterator(boolean skipDefaults) {

			fSkipDefaults= skipDefaults;

			fWindow= fResultWindow;
			fIndex= getFirstIndexInWindow(fWindow);
			fLength= getFirstIndexAfterWindow(fWindow);

			if (fSkipDefaults)
				computeIndex();
		}

		/*
		 * @see Iterator#next()
		 */
		public Object next() {
			try {
				StyleRange r= (StyleRange) fRanges.get(fIndex++);
				return createWindowRelativeRange(fWindow, r);
			} catch (ArrayIndexOutOfBoundsException x) {
				throw new NoSuchElementException();
			} finally {
				if (fSkipDefaults)
					computeIndex();
			}
		}

		/*
		 * @see Iterator#hasNext()
		 */
		public boolean hasNext() {
			return fIndex < fLength;
		}

		/*
		 * @see Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Returns whether the given object should be skipped.
		 *
		 * @param o the object to be checked
		 * @return <code>true</code> if the object should be skipped by the iterator
		 */
		protected boolean skip(Object o) {
			StyleRange r= (StyleRange) o;
			return r.similarTo(fDefaultRange);
		}

		/**
		 * Computes the index of the styled range that is the next to be enumerated.
		 */
		protected void computeIndex() {
			while (fIndex < fLength && skip(fRanges.get(fIndex)))
				++ fIndex;
		}
	}

	/** The style information for the range covered by the whole presentation */
	private StyleRange fDefaultRange;
	/** The member ranges of the presentation */
	private ArrayList fRanges;
	/** A clipping region against which the presentation can be clipped when asked for results */
	private IRegion fResultWindow;
	/**
	 * The optional extent for this presentation.
	 * @since 3.0
	 */
	private IRegion fExtent;


	/**
	 * Creates a new empty text presentation.
	 */
	public TextPresentation() {
		fRanges= new ArrayList(50);
	}

	/**
	 * Creates a new empty text presentation. <code>sizeHint</code> tells the expected size of this
	 * presentation.
	 * 
	 * @param sizeHint the expected size of this presentation, must be positive
	 */
	public TextPresentation(int sizeHint) {
		Assert.isTrue(sizeHint > 0);
		fRanges= new ArrayList(sizeHint);
	}

	/**
	 * Creates a new empty text presentation with the given extent. <code>sizeHint</code> tells the
	 * expected size of this presentation.
	 * 
	 * @param extent the extent of the created <code>TextPresentation</code>
	 * @param sizeHint the expected size of this presentation, must be positive
	 * @since 3.0
	 */
	public TextPresentation(IRegion extent, int sizeHint) {
		this(sizeHint);
		Assert.isNotNull(extent);
		fExtent= extent;
	}

	/**
	 * Sets the result window for this presentation. When dealing with
	 * this presentation all ranges which are outside the result window
	 * are ignored. For example, the size of the presentation is 0
	 * when there is no range inside the window even if there are ranges
	 * outside the window. All methods are aware of the result window.
	 *
	 * @param resultWindow the result window
	 */
	public void setResultWindow(IRegion resultWindow) {
		fResultWindow= resultWindow;
	}

	/**
	 * Set the default style range of this presentation.
	 * The default style range defines the overall area covered
	 * by this presentation and its style information.
	 *
	 * @param range the range describing the default region
	 */
	public void setDefaultStyleRange(StyleRange range) {
		fDefaultRange= range;
	}

	/**
	 * Returns this presentation's default style range. The returned <code>StyleRange</code>
	 * is relative to the start of the result window.
	 *
	 * @return this presentation's default style range
	 */
	public StyleRange getDefaultStyleRange() {
		StyleRange range= createWindowRelativeRange(fResultWindow, fDefaultRange);
		if (range == null)
			return null;
		return (StyleRange)range.clone();

	}

	/**
	 * Add the given range to the presentation. The range must be a
	 * subrange of the presentation's default range.
	 *
	 * @param range the range to be added
	 */
	public void addStyleRange(StyleRange range) {
		checkConsistency(range);
		fRanges.add(range);
	}

	/**
	 * Replaces the given range in this presentation. The range must be a
	 * subrange of the presentation's default range.
	 *
	 * @param range the range to be added
	 * @since 3.0
	 */
	public void replaceStyleRange(StyleRange range) {
		applyStyleRange(range, false);
	}

	/**
	 * Merges the given range into this presentation. The range must be a
	 * subrange of the presentation's default range.
	 *
	 * @param range the range to be added
	 * @since 3.0
	 */
	public void mergeStyleRange(StyleRange range) {
		applyStyleRange(range, true);
	}

	/**
	 * Applies the given range to this presentation. The range must be a
	 * subrange of the presentation's default range.
	 *
	 * @param range the range to be added
	 * @param merge <code>true</code> if the style should be merged instead of replaced
	 * @since 3.0
	 */
	private void applyStyleRange(StyleRange range, boolean merge) {
		if (range.length == 0)
			return;

		checkConsistency(range);

		int start= range.start;
		int length= range.length;
		int end= start + length;

		if (fRanges.size() == 0) {
			StyleRange defaultRange= getDefaultStyleRange();
			if (defaultRange == null)
				defaultRange= range;

			defaultRange.start= start;
			defaultRange.length= length;
			applyStyle(range, defaultRange, merge);
			fRanges.add(defaultRange);
		} else {
			IRegion rangeRegion= new Region(start, length);
			int first= getFirstIndexInWindow(rangeRegion);

			if (first == fRanges.size()) {
				StyleRange defaultRange= getDefaultStyleRange();
				if (defaultRange == null)
					defaultRange= range;
				defaultRange.start= start;
				defaultRange.length= length;
				applyStyle(range, defaultRange, merge);
				fRanges.add(defaultRange);
				return;
			}

			int last= getFirstIndexAfterWindow(rangeRegion);
			for (int i= first; i < last && length > 0; i++) {

				StyleRange current= (StyleRange)fRanges.get(i);
				int currentStart= current.start;
				int currentEnd= currentStart + current.length;

				if (end <= currentStart) {
					fRanges.add(i, range);
					return;
				}

				if (start >= currentEnd)
					continue;

				StyleRange currentCopy= null;
				if (end < currentEnd)
					currentCopy= (StyleRange)current.clone();

				if (start < currentStart) {
					// Apply style to new default range and add it
					StyleRange defaultRange= getDefaultStyleRange();
					if (defaultRange == null)
						defaultRange= new StyleRange();

					defaultRange.start= start;
					defaultRange.length= currentStart - start;
					applyStyle(range, defaultRange, merge);
					fRanges.add(i, defaultRange);
					i++; last++;


					// Apply style to first part of current range
					current.length= Math.min(end, currentEnd) - currentStart;
					applyStyle(range, current, merge);
				}

				if (start >= currentStart) {
					// Shorten the current range
					current.length= start - currentStart;

					// Apply the style to the rest of the current range and add it
					if (current.length > 0) {
						current= (StyleRange)current.clone();
						i++; last++;
						fRanges.add(i, current);
					}
					applyStyle(range, current, merge);
					current.start= start;
					current.length= Math.min(end, currentEnd) - start;
				}

				if (end < currentEnd) {
					// Add rest of current range
					currentCopy.start= end;
					currentCopy.length= currentEnd - end;
					i++; last++;
					fRanges.add(i,  currentCopy);
				}

				// Update range
				range.start=  currentEnd;
				range.length= Math.max(end - currentEnd, 0);
				start= range.start;
				length= range.length;
			}
			if (length > 0) {
				// Apply style to new default range and add it
				StyleRange defaultRange= getDefaultStyleRange();
				if (defaultRange == null)
					defaultRange= range;
				defaultRange.start= start;
				defaultRange.length= end - start;
				applyStyle(range, defaultRange, merge);
				fRanges.add(last, defaultRange);
			}
		}
	}

	/**
	 * Replaces the given ranges in this presentation. Each range must be a
	 * subrange of the presentation's default range. The ranges must be ordered
	 * by increasing offset and must not overlap (but may be adjacent).
	 *
	 * @param ranges the ranges to be added
	 * @since 3.0
	 */
	public void replaceStyleRanges(StyleRange[] ranges) {
		applyStyleRanges(ranges, false);
	}

	/**
	 * Merges the given ranges into this presentation. Each range must be a
	 * subrange of the presentation's default range. The ranges must be ordered
	 * by increasing offset and must not overlap (but may be adjacent).
	 *
	 * @param ranges the ranges to be added
	 * @since 3.0
	 */
	public void mergeStyleRanges(StyleRange[] ranges) {
		applyStyleRanges(ranges, true);
	}

	/**
	 * Applies the given ranges to this presentation. Each range must be a
	 * subrange of the presentation's default range. The ranges must be ordered
	 * by increasing offset and must not overlap (but may be adjacent).
	 *
	 * @param ranges the ranges to be added
	 * @param merge <code>true</code> if the style should be merged instead of replaced
	 * @since 3.0
	 */
	private void applyStyleRanges(StyleRange[] ranges, boolean merge) {
		int j= 0;
		ArrayList oldRanges= fRanges;
		ArrayList newRanges= new ArrayList(2*ranges.length + oldRanges.size());
		for (int i= 0, n= ranges.length; i < n; i++) {
			StyleRange range= ranges[i];
			fRanges= oldRanges; // for getFirstIndexAfterWindow(...)
			for (int m= getFirstIndexAfterWindow(new Region(range.start, range.length)); j < m; j++)
				newRanges.add(oldRanges.get(j));
			fRanges= newRanges; // for mergeStyleRange(...)
			applyStyleRange(range, merge);
		}
		for (int m= oldRanges.size(); j < m; j++)
			newRanges.add(oldRanges.get(j));
		fRanges= newRanges;
	}

	/**
	 * Applies the template's style to the target.
	 *
	 * @param template the style range to be used as template
	 * @param target the style range to which to apply the template
	 * @param merge <code>true</code> if the style should be merged instead of replaced
	 * @since 3.0
	 */
	private void applyStyle(StyleRange template, StyleRange target, boolean merge) {
		if (merge) {
			if (template.font != null)
				target.font= template.font;
			target.fontStyle|= template.fontStyle;

			if (template.metrics != null)
				target.metrics= template.metrics;

			if (template.foreground != null || template.underlineStyle == SWT.UNDERLINE_LINK)
				target.foreground= template.foreground;
			if (template.background != null)
				target.background= template.background;

			target.strikeout|= template.strikeout;
			if (template.strikeoutColor != null)
				target.strikeoutColor= template.strikeoutColor;

			target.underline|= template.underline;
			if (template.underlineStyle != SWT.NONE && target.underlineStyle != SWT.UNDERLINE_LINK)
				target.underlineStyle= template.underlineStyle;

			if (template.underlineColor != null)
				target.underlineColor= template.underlineColor;

			if (template.borderStyle != SWT.NONE)
				target.borderStyle= template.borderStyle;
			if (template.borderColor != null)
				target.borderColor= template.borderColor;

		} else {
			target.font= template.font;
			target.fontStyle= template.fontStyle;
			target.metrics= template.metrics;
			target.foreground= template.foreground;
			target.background= template.background;
			target.strikeout= template.strikeout;
			target.strikeoutColor= template.strikeoutColor;
			target.underline= template.underline;
			target.underlineStyle= template.underlineStyle;
			target.underlineColor= template.underlineColor;
			target.borderStyle= template.borderStyle;
			target.borderColor= template.borderColor;
		}
	}

	/**
	 * Checks whether the given range is a subrange of the presentation's
	 * default style range.
	 *
	 * @param range the range to be checked
	 * @exception IllegalArgumentException if range is not a subrange of the presentation's default range
	 */
	private void checkConsistency(StyleRange range) {

		if (range == null)
			throw new IllegalArgumentException();

		if (fDefaultRange != null) {

			if (range.start < fDefaultRange.start)
				range.start= fDefaultRange.start;

			int defaultEnd= fDefaultRange.start + fDefaultRange.length;
			int end= range.start + range.length;
			if (end > defaultEnd)
				range.length -= (end - defaultEnd);
		}
	}

	/**
	 * Returns the index of the first range which overlaps with the
	 * specified window.
	 *
	 * @param window the window to be used for searching
	 * @return the index of the first range overlapping with the window
	 */
	private int getFirstIndexInWindow(IRegion window) {
		if (window != null) {
			int start= window.getOffset();
			int i= -1, j= fRanges.size();
			while (j - i > 1) {
				int k= (i + j) >> 1;
				StyleRange r= (StyleRange) fRanges.get(k);
				if (r.start + r.length > start)
					j= k;
				else
					i= k;
			}
			return j;
		}
		return 0;
	}

	/**
	 * Returns the index of the first range which comes after the specified window and does
	 * not overlap with this window.
	 *
	 * @param window the window to be used for searching
	 * @return the index of the first range behind the window and not overlapping with the window
	 */
	private int getFirstIndexAfterWindow(IRegion window) {
		if (window != null) {
			int end= window.getOffset() + window.getLength();
			int i= -1, j= fRanges.size();
			while (j - i > 1) {
				int k= (i + j) >> 1;
				StyleRange r= (StyleRange) fRanges.get(k);
				if (r.start < end)
					i= k;
				else
					j= k;
			}
			return j;
		}
		return fRanges.size();
	}

	/**
	 * Returns a style range which is relative to the specified window and
	 * appropriately clipped if necessary. The original style range is not
	 * modified.
	 *
	 * @param window the reference window
	 * @param range the absolute range
	 * @return the window relative range based on the absolute range
	 */
	private StyleRange createWindowRelativeRange(IRegion window, StyleRange range) {
		if (window == null || range == null)
			return range;

		int start= range.start - window.getOffset();
		if (start < 0)
			start= 0;

		int rangeEnd= range.start + range.length;
		int windowEnd= window.getOffset() + window.getLength();
		int end= (rangeEnd > windowEnd ? windowEnd : rangeEnd);
		end -= window.getOffset();

		StyleRange newRange= (StyleRange) range.clone();
		newRange.start= start;
		newRange.length= end - start;
		return newRange;
	}

	/**
	 * Returns the region which is relative to the specified window and
	 * appropriately clipped if necessary.
	 *
	 * @param coverage the absolute coverage
	 * @return the window relative region based on the absolute coverage
	 * @since 3.0
	 */
	private IRegion createWindowRelativeRegion(IRegion coverage) {
		if (fResultWindow == null || coverage == null)
			return coverage;

		int start= coverage.getOffset() - fResultWindow.getOffset();
		if (start < 0)
			start= 0;

		int rangeEnd= coverage.getOffset() + coverage.getLength();
		int windowEnd= fResultWindow.getOffset() + fResultWindow.getLength();
		int end= (rangeEnd > windowEnd ? windowEnd : rangeEnd);
		end -= fResultWindow.getOffset();

		return new Region(start, end - start);
	}

	/**
	 * Returns an iterator which enumerates all style ranged which define a style
	 * different from the presentation's default style range. The default style range
	 * is not enumerated.
	 *
	 * @return a style range iterator
	 */
	public Iterator getNonDefaultStyleRangeIterator() {
		return new FilterIterator(fDefaultRange != null);
	}

	/**
	 * Returns an iterator which enumerates all style ranges of this presentation
	 * except the default style range. The returned <code>StyleRange</code>s
	 * are relative to the start of the presentation's result window.
	 *
	 * @return a style range iterator
	 */
	public Iterator getAllStyleRangeIterator() {
		return new FilterIterator(false);
	}

	/**
	 * Returns whether this collection contains any style range including
	 * the default style range.
	 *
	 * @return <code>true</code> if there is no style range in this presentation
	 */
	public boolean isEmpty() {
		return (fDefaultRange == null && getDenumerableRanges() == 0);
	}

	/**
	 * Returns the number of style ranges in the presentation not counting the default
	 * style range.
	 *
	 * @return the number of style ranges in the presentation excluding the default style range
	 */
	public int getDenumerableRanges() {
		int size= getFirstIndexAfterWindow(fResultWindow) - getFirstIndexInWindow(fResultWindow);
		return (size < 0 ? 0 : size);
	}

	/**
	 * Returns the style range with the smallest offset ignoring the default style range or null
	 * if the presentation is empty.
	 *
	 * @return the style range with the smallest offset different from the default style range
	 */
	public StyleRange getFirstStyleRange() {
		try {

			StyleRange range= (StyleRange) fRanges.get(getFirstIndexInWindow(fResultWindow));
			return createWindowRelativeRange(fResultWindow, range);

		} catch (NoSuchElementException x) {
		} catch (IndexOutOfBoundsException x) {
		}

		return null;
	}

	/**
	 * Returns the style range with the highest offset ignoring the default style range.
	 *
	 * @return the style range with the highest offset different from the default style range
	 */
	public StyleRange getLastStyleRange() {
		try {

			StyleRange range=  (StyleRange) fRanges.get(getFirstIndexAfterWindow(fResultWindow) - 1);
			return createWindowRelativeRange(fResultWindow, range);

		} catch (NoSuchElementException x) {
			return null;
		} catch (IndexOutOfBoundsException x) {
			return null;
		}
	}

	/**
	 * Returns the coverage of this presentation as clipped by the presentation's
	 * result window.
	 *
	 * @return the coverage of this presentation
	 */
	public IRegion getCoverage() {

		if (fDefaultRange != null) {
			StyleRange range= getDefaultStyleRange();
			return new Region(range.start, range.length);
		}

		StyleRange first= getFirstStyleRange();
		StyleRange last= getLastStyleRange();

		if (first == null || last == null)
			return null;

		return new Region(first.start, last.start - first. start + last.length);
	}

	/**
	 * Returns the extent of this presentation clipped by the
	 * presentation's result window.
	 *
	 * @return the clipped extent
	 * @since 3.0
	 */
	public IRegion getExtent() {
		if (fExtent != null)
			return createWindowRelativeRegion(fExtent);
		return getCoverage();
	}

	/**
	 * Clears this presentation by resetting all applied changes.
	 * @since 2.0
	 */
	public void clear() {
		fDefaultRange= null;
		fResultWindow= null;
		fRanges.clear();
	}


}

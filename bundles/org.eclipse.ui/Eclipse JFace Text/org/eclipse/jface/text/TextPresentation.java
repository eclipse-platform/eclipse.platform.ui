/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;

 
/**
 * Describes the presentation styles for a section of an indexed text 
 * such as a document or string. A text presentation defines a default style
 * for the whole section and in addition style differences for individual 
 * subsections. Text presentations can be narrowed down to a particular
 * result window. All methods are result window aware, i.e. ranges outside
 * the result window are always ignored.<p>
 * All iterators provided by a text presentation assume that they enumerate
 * non overlapping, consequtive ranges inside the default range. Thus, all
 * these iterators do not include the default range. The default style range
 * must be explicitly asked for using <code>getDefaultStyleRange</code>.
 */
public class TextPresentation {
	
	/**
	 * Applies the given presentation to the given text widget. Helper method.
	 *
	 * @param presentation the style information
	 * @param the widget to which to apply the style information
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
		
		protected int fIndex;
		protected int fLength;
		protected boolean fSkipDefaults;
		protected IRegion fWindow;
		
		/**
		 * <code>skipDefaults</code> tells the enumeration to skip all those style ranges
		 * which define the same style as the presentation's default style range.
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
		 * @see Iterator#next
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
		 * @see Iterator#hasNext
		 */
		public boolean hasNext() {
			return fIndex < fLength;
		}
		
		/*
		 * @see Iterator#remove
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		/**
		 * Returns whether the given object should be skipped.
		 * 
		 * @return <code>true</code> if teh object should be skipped by the iterator
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
	};
	
	/** The syle information for the range covered by the whole presentation */
	private StyleRange fDefaultRange;
	/** The member ranges of the presentation */
	private ArrayList fRanges= new ArrayList();
	/** A clipping region against which the presentation can be clipped when asked for results */
	private IRegion fResultWindow;
	
	
	/**
	 * Creates a new empty text presentation.
	 */
	public TextPresentation() {
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
	 * @param range the range decribing the default region
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
		return createWindowRelativeRange(fResultWindow, fDefaultRange);
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
	 * Checks whether the given range is a subrange of the presentation's
	 * default style range.
	 *
	 * @param range the range to be checked
	 * @exception IllegalArgumentAxception if range is not a subrange of the presentation's default range
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
				range.length -= (defaultEnd - end);
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
		int i= 0;
		if (window != null) {
			int start= window.getOffset();	
			while (i < fRanges.size()) {
				StyleRange r= (StyleRange) fRanges.get(i++);
				if (r.start + r.length > start) {
					-- i;
					break;
				}
			}
		}
		return i;
	}
	
	/**
	 * Returns the index of the first range which comes after the specified window and does
	 * not overlap with this window.
	 *
	 * @param window the window to be used for searching
	 * @return the index of the first range behind the window and not overlapping with the window
	 */
	private int getFirstIndexAfterWindow(IRegion window) {
		int i= fRanges.size();
		if (window != null) {
			int end= window.getOffset() + window.getLength();	
			while (i > 0) {
				StyleRange r= (StyleRange) fRanges.get(--i);
				if (r.start < end) {
					++ i;
					break;
				}
			}
		}
		return i;
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
	 * Returns an iterator which enumerates all style ranged which define a style 
	 * different from the presentation's default style range. The default style range
	 * is not enumerated.
	 *
	 * @return a style range interator
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
	 * @return the style range with the smalled offset different from the default style range
	 */
	public StyleRange getFirstStyleRange() {
		try {
			
			StyleRange range= (StyleRange) fRanges.get(getFirstIndexInWindow(fResultWindow));
			return createWindowRelativeRange(fResultWindow, range);
			
		} catch (NoSuchElementException x) {
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
		}
		
		return null;
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
	 * Clears this presentation by resetting all applied changes.
	 * @since 2.0
	 */
	public void clear() {
		fDefaultRange= null;
		fResultWindow= null;
		fRanges.clear();
	}
}
/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.misc.StringMatcher;

/**
 * A filter used in conjunction with <code>FilteredTree</code>.  This filter is 
 * inefficient - in order to see if a node should be filtered it must use the 
 * content provider of the tree to do pattern matching on its children.  This 
 * causes the entire tree structure to be realized.
 * 
 * @see org.eclipse.ui.internal.dialogs.FilteredTree  
 * @since 3.0
 */
public class PatternFilter extends ViewerFilter {
	
    private Map cache = new HashMap();
    
	/**
	 * Whether to include a leading wildcard for all provided patterns.  A
	 * trailing wildcard is always included.
	 */
	private boolean includeLeadingWildcard = false;

    private StringMatcher matcher;

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#filter(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object[])
     */
    public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
        if (matcher == null)
            return elements;

        Object[] filtered = (Object[]) cache.get(parent);
        if (filtered == null) {
            filtered = super.filter(viewer, parent, elements);
            cache.put(parent, filtered);
        }
        return filtered;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        return isElementMatch(viewer, element);
    }
    
    /**
	 * Sets whether a leading wildcard should be attached to each pattern
	 * string.
	 * 
	 * @param includeLeadingWildcard
	 *            Whether a leading wildcard should be added.
	 */
	public final void setIncludeLeadingWildcard(
			final boolean includeLeadingWildcard) {
		this.includeLeadingWildcard = includeLeadingWildcard;
	}

    /**
     * 
     * @param patternString
     */
    public void setPattern(String patternString) {
        cache.clear();
        if (patternString == null || patternString.equals("")) //$NON-NLS-1$
            matcher = null;
        else {
			String pattern = patternString + "*"; //$NON-NLS-1$
			if (includeLeadingWildcard) {
				pattern = "*" + pattern; //$NON-NLS-1$
			}
			matcher = new StringMatcher(pattern, true, false);
		}
    }

    /**
     * Answers whether the given String matches the pattern.
     * 
     * @param string the String to test
     * 
     * @return whether the string matches the pattern
     */
    protected boolean match(String string) {
        return matcher.match(string);
    }
    
    /**
     * Answers whether the given Object should be selected in 
     * the filtered control.
     * 
     * @param element
     * @return true if this element should be auto selected
     */
    protected boolean isElementSelectable(Object element){
    	return element != null;
    }
    
    /**
     * Answers whether the given element in the given viewer matches
     * the filter pattern.  This is a default implementation that will 
     * match any entry in the tree based on whether the provided filter 
     * text matches the text of the given element's text.  Subclasses 
     * should override this method.
     * 
     * @param viewer the tree viewer in which the element resides
     * @param element the element in the tree to check for a match
     * 
     * @return true if the element matches the filter pattern
     */
    protected boolean isElementMatch(Viewer viewer, Object element){
        Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
                .getContentProvider()).getChildren(element);
        if ((children != null) && (children.length > 0))
            return filter(viewer, element, children).length > 0;

        String labelText = ((ILabelProvider) ((StructuredViewer) viewer)
                .getLabelProvider()).getText(element);
        if(labelText == null)
        	return false;
        return match(labelText);   	
    }
}

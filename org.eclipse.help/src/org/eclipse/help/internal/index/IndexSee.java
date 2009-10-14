/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.index;

import org.eclipse.help.IIndexSee;
import org.eclipse.help.IIndexSubpath;
import org.eclipse.help.internal.UAElement;
import org.w3c.dom.Element;

import com.ibm.icu.text.Collator;

public class IndexSee extends UAElement implements IIndexSee, Comparable  {
	
	public static final String NAME = "see"; //$NON-NLS-1$
	public static final String ATTRIBUTE_KEYWORD = "keyword"; //$NON-NLS-1$
	
	public IndexSee(Element src) {
		super(src);
	}
	
	public IndexSee(IIndexSee src) {
		super(NAME, src);
		setKeyword(src.getKeyword());
		appendChildren(src.getChildren());
	}

	public String getKeyword() {
		return getAttribute(ATTRIBUTE_KEYWORD);
	}
	
	private String[] getPath() {
		IIndexSubpath[] subpaths = getSubpathElements();
		String[] result = new String[1 + subpaths.length];
		result[0] = getKeyword();
		for (int i = 0; i < subpaths.length; i++) {
			result[i + 1] = subpaths[i].getKeyword();
		}
		return result;
	}
	
	public void setKeyword(String keyword) {
		setAttribute(ATTRIBUTE_KEYWORD, keyword);
	}
	
	private int getCategory(String keyword) {
		if (keyword != null && keyword.length() > 0) {
			char c = keyword.charAt(0);
			if (Character.isDigit(c)) {
				return 2;
			}
			else if (Character.isLetter(c)) {
				return 3;
			}
			return 1;
		}
		return 4;
	}
	
	public int compareTo(Object arg0) {
		Collator collator = Collator.getInstance();
		if (arg0 instanceof IndexSee) {
			String[] path = getPath();
			String[] targetPath = ((IndexSee)arg0).getPath();
			for (int i = 0; i < path.length; i++) {
				if (i >= targetPath.length) {
					return 1;
				}
                if (getCategory(path[i]) != getCategory(targetPath[i])) {
                	return getCategory(path[i]) - getCategory(targetPath[i]);
                }
				int result = collator.compare(path[i], targetPath[i]);
				if (result != 0) {
					return result;
				}
			}
			return path.length - targetPath.length;
		}
		return 0;
	}
	
	public boolean equals(Object obj) {
		return obj instanceof IndexSee && compareTo(obj) == 0;
	}
	
	public int hashCode() {
		int result = getKeyword().hashCode();
		IIndexSubpath[] subpaths = getSubpathElements();
		for (int i = 0; i < subpaths.length; i++) {
			result += subpaths[i].getKeyword().hashCode();
		}
		return result;
    }

	public boolean isSeeAlso() {
		UAElement parentElement = getParentElement();
		return ! (parentElement.getChildren()[0] instanceof IIndexSee);
	}

	public IIndexSubpath[] getSubpathElements() {
		return (IIndexSubpath[])getChildren(IIndexSubpath.class);
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.resources;


import org.eclipse.team.internal.ccvs.core.CVSTag;

public class CVSEntryLineTag extends CVSTag {
	
	/*
	 * The parameter tag must not be null.
	 */
	public CVSEntryLineTag(CVSTag tag) {
		super(tag.getName(), tag.getType());
	}
	
	public CVSEntryLineTag(String entryLineTag) {
		switch (entryLineTag.charAt(0)) {
			case 'T' : type = BRANCH; break;
			case 'N' : type = VERSION; break;
			case 'D' : type = DATE; break;
			default: type = HEAD;
		}
		name = entryLineTag.substring(1);
	}
	/*
	 * Returns the tag name
	 */
	public String getName() {
		return name;
	}
	/*
	 * Returns the tag type
	 */
	public int getType() {
		return type;
	}
	
	public String toEntryLineFormat(boolean useSamePrefixForBranchAndTag) {
		if (type == BRANCH || (type == VERSION && useSamePrefixForBranchAndTag))
			return "T" + name;//$NON-NLS-1$
		else if (type == VERSION)
			return "N" + name;//$NON-NLS-1$
		else if (type == DATE)
			return "D" + name;//$NON-NLS-1$
		return "";//$NON-NLS-1$
	}
	
	/*
	 * For debugging purposes.
	 */
	public String toString() {
		return toEntryLineFormat(false);
	}
}


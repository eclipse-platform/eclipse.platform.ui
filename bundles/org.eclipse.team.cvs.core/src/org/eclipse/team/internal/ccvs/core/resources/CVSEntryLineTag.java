package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
	
	public boolean equals(Object obj) {
		// We assume, that the name and type can not be null
		if (obj == this) return true;
		if (!(obj instanceof CVSEntryLineTag)) return false;
		return (type == ((CVSEntryLineTag)obj).type) && name.equals(((CVSEntryLineTag)obj).name);
	}
	
	/*
	 * For debugging purposes.
	 */
	public String toString() {
		return toEntryLineFormat(false);
	}
}


package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;

/**
 * A tag in CVS gives a label  to a collection of revisions. The labels can represent a version, a branch, 
 * or a date.
 */
public class CVSTag {

	public final static int HEAD = 0;
	public final static int BRANCH = 1;
	public final static int VERSION = 2;
	public final static int DATE = 3;
	
	public static final CVSTag DEFAULT = new CVSTag();
	public static final CVSTag BASE = new CVSTag("BASE", VERSION); //$NON-NLS-1$
	
	protected String name;
	protected int type;
	
	public CVSTag() {
		this("HEAD", HEAD); //$NON-NLS-1$
	}

	public CVSTag(String name, int type) {
		this.name = name;
		this.type = type;
	}
	
	public boolean equals(Object other) {
		if(other == this) return true;
		if (!(other instanceof CVSTag)) return false;
			
		CVSTag tag = ((CVSTag)other);
		if (getType() != tag.getType()) return false;
		if (!getName().equals(tag.getName())) return false;
		return true;
	}
	
	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}
	
	public int hashCode() {
		return name.hashCode();
	}
	
	public int compareTo(CVSTag other) {
		return getName().compareTo(other.getName());
	}
	
	public static boolean equalTags(CVSTag tag1, CVSTag tag2) {
		if (tag1 == null) tag1 = CVSTag.DEFAULT;
		if (tag2 == null) tag2 = CVSTag.DEFAULT;
		return tag1.equals(tag2);
	}
	
	public static IStatus validateTagName(String tagName) {
		if (tagName == null)
			return new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTag.nullName")); //$NON-NLS-1$
		if (tagName.equals(""))  //$NON-NLS-1$
			return new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTag.emptyName")); //$NON-NLS-1$
		if (!Character. isLetter(tagName.charAt(0)))
			return new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTag.beginName")); //$NON-NLS-1$
		
		for (int i = 0; i < tagName.length(); i++) {
			char c = tagName.charAt(i);
			if ( Character.isSpaceChar(c) || c == '$' || c == ',' || c == '.' || c == ':' || c == ';' || c == '@' || c == '|')
				return new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTag.badCharName")); //$NON-NLS-1$
		}
		return new CVSStatus(CVSStatus.OK, Policy.bind("ok")); //$NON-NLS-1$
	}
}
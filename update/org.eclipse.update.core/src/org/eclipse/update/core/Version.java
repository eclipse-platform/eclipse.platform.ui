package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * <p>
 * Version identifier. In its string representation, 
 * it consists of up to 4 tokens separated by decimal point.
 * The first 3 tokens are integer numbers, the last token
 * is an uninterpreted string (no whitespace characters allowed).
 * For example, the following are valid version identifiers 
 * (as strings):
 * <ul>
 *   <li><code>0.0.0</code></li>
 *   <li><code>1.0.127564</code></li>
 *   <li><code>3.7.2.build-127J</code></li>
 *   <li><code>1.9</code> (interpreted as <code>1.9.0</code>)</li>
 *   <li><code>3</code> (interpreted as <code>3.0.0</code>)</li>
 * </ul>
 * </p>
 * <p>
 * The version identifier can be decomposed into a major, minor, 
 * service and qualifier components. A difference in the major 
 * component is interpreted as an incompatible version change. 
 * A difference in the minor (and not the major) component is 
 * interpreted as a compatible version change. The service
 * level component is interpreted as a cumulative 
 * and compatible service update of the minor version component.
 * The qualifier is not interpreted, other than in version
 * comparisons. The qualifiers are compared using lexicographical
 * string comparison.
 * @see java.lang.String#compareTo 
 * </p>
 * <p>
 * Clients may instantiate; not intended to be subclassed by clients.
 */	
public final class Version {
		
	private	int major = 0;
	private int minor = 0;
	private int service = 0;
	private String qualifier = null;
	
	private static final String	SEPARATOR = ".";
	
private Version () {}

/**
 * Creates a plug-in version identifier from its components.
 * 
 * @param major major component of the version identifier
 * @param minor minor component of the version identifier
 * @param service service update component of the version identifier
 * @since 2.0 
 */

public Version(int major, int minor, int service) {
	this(major, minor, service, null);
}
	
/**
 * Creates a plug-in version identifier from its components.
 * 
 * @param major major component of the version identifier
 * @param minor minor component of the version identifier
 * @param service service update component of the version identifier
 * @param qualoifier component of the version identifier
 * @since 2.0 
 */

public Version(int major, int minor, int service, String qualifier) {

	if(major<0) major=0;
	if(minor<0) minor=0;
	if(service<0) service=0;
	if(qualifier==null) qualifier="";
	
	this.major = major;
	this.minor = minor;
	this.service = service;
	this.qualifier = removeWhiteSpace(qualifier);
}

/**
 * Creates a plug-in version identifier from the given string.
 * The string represenation consists of up to 4 tokens 
 * separated by decimal point.
 * For example, the following are valid version identifiers 
 * (as strings):
 * <ul>
 *   <li><code>0.0.0</code></li>
 *   <li><code>1.0.127564</code></li>
 *   <li><code>3.7.2.build-127J</code></li>
 *   <li><code>1.9</code> (interpreted as <code>1.9.0</code>)</li>
 *   <li><code>3</code> (interpreted as <code>3.0.0</code>)</li>
 * </ul>
 * </p>
 * 
 * @param versionId string representation of the version identifier
 * @since 2.0 
 */

public Version(String versionId) {

	// set up default values
	this.major   = 0;
	this.minor   = 0;
	this.service = 0;
	this.qualifier = "";
		
	// parse string value
	try{
		if( versionId == null )
			versionId = "0.0.0";
			
		String s = versionId.trim();
	
		StringTokenizer st = new StringTokenizer(s, SEPARATOR);
		Integer token;
		Vector elements = new Vector(3);

		while(st.hasMoreTokens()) {
			elements.addElement(st.nextToken());
		}

		if (elements.size()>=1) this.major = (new Integer((String)elements.elementAt(0))).intValue();
		if (elements.size()>=2) this.minor = (new Integer((String)elements.elementAt(1))).intValue();
		if (elements.size()>=3) this.service = (new Integer((String)elements.elementAt(2))).intValue();
		if (elements.size()>=4) this.qualifier = removeWhiteSpace((String)elements.elementAt(3));
		
	} catch (Exception e) { // use default version 0.0.0
	}

}
/**
 * Compare version identifiers for equality. Identifiers are
 * equal if all of their components are equal.
 *
 * @param object an object to compare
 * @return whehter or not the two objects are equal
 * @since 2.0 
 */

public boolean equals(Object object) {
	if (!(object instanceof Version))
		return false;
	Version v = (Version) object;
	return 
		v.getMajorComponent() == major 
		&& v.getMinorComponent() == minor 
		&& v.getServiceComponent() == service
		&& v.getQualifierComponent().equals(qualifier);
}
/**
 * Returns the major (incompatible) component of this 
 * version identifier.
 *
 * @return the major version
 * @since 2.0 
 */

public int getMajorComponent() {
	return major;
}
/**
 * Returns the minor (compatible) component of this 
 * version identifier.
 *
 * @return the minor version
 * @since 2.0 
 */

public int getMinorComponent() {
	return minor;
}

/**
 * Returns the service level component of this 
 * version identifier.
 *
 * @return the service level
 * @since 2.0 
 */

public int getServiceComponent() {
	return service;
}

/**
 * Returns the qualifier component of this 
 * version identifier.
 *
 * @return the qualifier
 * @since 2.0 
 */

public String getQualifierComponent() {
	return qualifier;
}

/**
 * Compares two version identifiers for order using multi-decimal
 * comparison for the first 3 components (major, minor, service)
 * and lexicographic string comparison for the qualifier component
 * 
 * @see java.lang.String#compareTo 
 * @param versionId the other version identifier
 * @return -1 if the other version id is smaller than this
 * version, 0 if it is equal and 1 if it is greater than
 * this version.
 * @since 2.0 
 */

public int compare(Version id) {

	if (id == null) {
		if (major==0 && minor==0 && service==0) return -1;
		else return 1;
	}

	if (major > id.getMajorComponent()) return 1;
	if (major < id.getMajorComponent()) return -1;
	if (minor > id.getMinorComponent()) return 1;
	if (minor < id.getMinorComponent()) return -1;	
	if (service > id.getServiceComponent()) return 1;
	if (service < id.getServiceComponent()) return -1;
	return compareQualifiers(qualifier, id.getQualifierComponent());
}
/**
 * Returns the string representation of this version identifier. 
 * The result satisfies
 * <code>vi.equals(new PluginVersionIdentifier(vi.toString()))</code>.
 *
 * @return the string representation of this plug-in version identifier
 * @since 2.0 
 */

public String toString() {
	String s = major+SEPARATOR+minor+SEPARATOR+service;
	if (!qualifier.equals(""))
		s += SEPARATOR + qualifier;
	return s;
}

private String removeWhiteSpace(String s) {
	char[] chars = s.trim().toCharArray();
	boolean whitespace = false;
	for(int i=0; i<chars.length; i++) {
		if (Character.isWhitespace(chars[i])) {
			chars[i] = '_';
			whitespace = true;
		}
	}
	return whitespace ? new String(chars) : s;
}

private int compareQualifiers(String q1, String q2) {
	int result = q1.compareTo(q2);
	if (result<0)
		return -1;
	else if (result>0)
		return 1;
	else
		return 0;
}

}

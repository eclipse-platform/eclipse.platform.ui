/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import java.util.StringTokenizer;
import java.util.Vector;
import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.internal.runtime.Messages;
import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * Version identifier for a plug-in. In its string representation, 
 * it consists of up to 4 tokens separated by a decimal point.
 * The first 3 tokens are positive integer numbers, the last token
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
 * service level component and qualifier components. A difference
 * in the major component is interpreted as an incompatible version
 * change. A difference in the minor (and not the major) component
 * is interpreted as a compatible version change. The service
 * level component is interpreted as a cumulative and compatible
 * service update of the minor version component. The qualifier is
 * not interpreted, other than in version comparisons. The 
 * qualifiers are compared using lexicographical string comparison.
 * </p>
 * <p>
 * Version identifiers can be matched as perfectly equal, equivalent,
 * compatible or greaterOrEqual.
 * </p>
 * <p>
 * Clients may instantiate; not intended to be subclassed by clients.
 * </p>
 * @see java.lang.String#compareTo(java.lang.String) 
 */
public final class PluginVersionIdentifier {

	private int major = 0;
	private int minor = 0;
	private int service = 0;
	private String qualifier = ""; //$NON-NLS-1$

	private static final String SEPARATOR = "."; //$NON-NLS-1$

	/**
	 * Creates a plug-in version identifier from its components.
	 * 
	 * @param major major component of the version identifier
	 * @param minor minor component of the version identifier
	 * @param service service update component of the version identifier
	 */
	public PluginVersionIdentifier(int major, int minor, int service) {
		this(major, minor, service, null);
	}

	/**
	 * Creates a plug-in version identifier from its components.
	 * 
	 * @param major major component of the version identifier
	 * @param minor minor component of the version identifier
	 * @param service service update component of the version identifier
	 * @param qualifier qualifier component of the version identifier. 
	 * Qualifier characters that are not a letter or a digit are replaced.
	 */
	public PluginVersionIdentifier(int major, int minor, int service, String qualifier) {

		// Do the test outside of the assert so that they 'Policy.bind' 
		// will not be evaluated each time (including cases when we would
		// have passed by the assert).

		if (major < 0)
			Assert.isTrue(false, NLS.bind(Messages.parse_postiveMajor, major + SEPARATOR + minor + SEPARATOR + service + SEPARATOR + qualifier));
		if (minor < 0)
			Assert.isTrue(false, NLS.bind(Messages.parse_postiveMinor, major + SEPARATOR + minor + SEPARATOR + service + SEPARATOR + qualifier));
		if (service < 0)
			Assert.isTrue(false, NLS.bind(Messages.parse_postiveService, major + SEPARATOR + minor + SEPARATOR + service + SEPARATOR + qualifier));
		if (qualifier == null)
			qualifier = ""; //$NON-NLS-1$

		this.major = major;
		this.minor = minor;
		this.service = service;
		this.qualifier = verifyQualifier(qualifier);
	}

	/**
	 * Creates a plug-in version identifier from the given string.
	 * The string representation consists of up to 4 tokens 
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
	 * @param versionId string representation of the version identifier. 
	 * Qualifier characters that are not a letter or a digit are replaced.
	 */
	public PluginVersionIdentifier(String versionId) {
		Object[] parts = parseVersion(versionId);
		this.major = ((Integer) parts[0]).intValue();
		this.minor = ((Integer) parts[1]).intValue();
		this.service = ((Integer) parts[2]).intValue();
		this.qualifier = (String) parts[3];
	}

	/**
	 * Validates the given string as a plug-in version identifier.
	 * 
	 * @param version the string to validate
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given string is valid as a plug-in version identifier, otherwise a status
	 *		object indicating what is wrong with the string
	 * @since 2.0
	 */
	public static IStatus validateVersion(String version) {
		try {
			parseVersion(version);
		} catch (RuntimeException e) {
			return new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, e.getMessage(), e);
		}
		return Status.OK_STATUS;
	}

	private static Object[] parseVersion(String versionId) {

		// Do the test outside of the assert so that they 'Policy.bind' 
		// will not be evaluated each time (including cases when we would
		// have passed by the assert).
		if (versionId == null)
			Assert.isNotNull(null, Messages.parse_emptyPluginVersion);
		String s = versionId.trim();
		if (s.equals("")) //$NON-NLS-1$
			Assert.isTrue(false, Messages.parse_emptyPluginVersion);
		if (s.startsWith(SEPARATOR))
			Assert.isTrue(false, NLS.bind(Messages.parse_separatorStartVersion, s));
		if (s.endsWith(SEPARATOR))
			Assert.isTrue(false, NLS.bind(Messages.parse_separatorEndVersion, s));
		if (s.indexOf(SEPARATOR + SEPARATOR) != -1)
			Assert.isTrue(false, NLS.bind(Messages.parse_doubleSeparatorVersion, s));

		StringTokenizer st = new StringTokenizer(s, SEPARATOR);
		Vector elements = new Vector(4);

		while (st.hasMoreTokens())
			elements.addElement(st.nextToken());

		int elementSize = elements.size();

		if (elementSize <= 0)
			Assert.isTrue(false, NLS.bind(Messages.parse_oneElementPluginVersion, s));
		if (elementSize > 4)
			Assert.isTrue(false, NLS.bind(Messages.parse_fourElementPluginVersion, s));

		int[] numbers = new int[3];
		try {
			numbers[0] = Integer.parseInt((String) elements.elementAt(0));
			if (numbers[0] < 0)
				Assert.isTrue(false, NLS.bind(Messages.parse_postiveMajor, s));
		} catch (NumberFormatException nfe) {
			Assert.isTrue(false, NLS.bind(Messages.parse_numericMajorComponent, s));
		}

		try {
			if (elementSize >= 2) {
				numbers[1] = Integer.parseInt((String) elements.elementAt(1));
				if (numbers[1] < 0)
					Assert.isTrue(false, NLS.bind(Messages.parse_postiveMinor, s));
			} else
				numbers[1] = 0;
		} catch (NumberFormatException nfe) {
			Assert.isTrue(false, NLS.bind(Messages.parse_numericMinorComponent, s));
		}

		try {
			if (elementSize >= 3) {
				numbers[2] = Integer.parseInt((String) elements.elementAt(2));
				if (numbers[2] < 0)
					Assert.isTrue(false, NLS.bind(Messages.parse_postiveService, s));
			} else
				numbers[2] = 0;
		} catch (NumberFormatException nfe) {
			Assert.isTrue(false, NLS.bind(Messages.parse_numericServiceComponent, s));
		}

		// "result" is a 4-element array with the major, minor, service, and qualifier
		Object[] result = new Object[4];
		result[0] = new Integer(numbers[0]);
		result[1] = new Integer(numbers[1]);
		result[2] = new Integer(numbers[2]);
		if (elementSize >= 4)
			result[3] = verifyQualifier((String) elements.elementAt(3));
		else
			result[3] = ""; //$NON-NLS-1$
		return result;
	}

	/**
	 * Compare version identifiers for equality. Identifiers are
	 * equal if all of their components are equal.
	 *
	 * @param object an object to compare
	 * @return whether or not the two objects are equal
	 */
	public boolean equals(Object object) {
		if (!(object instanceof PluginVersionIdentifier))
			return false;
		PluginVersionIdentifier v = (PluginVersionIdentifier) object;
		return v.getMajorComponent() == major && v.getMinorComponent() == minor && v.getServiceComponent() == service && v.getQualifierComponent().equals(qualifier);
	}

	/**
	 * Returns a hash code value for the object. 
	 *
	 * @return an integer which is a hash code value for this object.
	 */
	public int hashCode() {
		int code = major + minor + service; // R1.0 result
		if (qualifier.equals("")) //$NON-NLS-1$
			return code;
		else
			return code + qualifier.hashCode();
	}

	/**
	 * Returns the major (incompatible) component of this 
	 * version identifier.
	 *
	 * @return the major version
	 */
	public int getMajorComponent() {
		return major;
	}

	/**
	 * Returns the minor (compatible) component of this 
	 * version identifier.
	 *
	 * @return the minor version
	 */
	public int getMinorComponent() {
		return minor;
	}

	/**
	 * Returns the service level component of this 
	 * version identifier.
	 *
	 * @return the service level
	 */
	public int getServiceComponent() {
		return service;
	}

	/**
	 * Returns the qualifier component of this 
	 * version identifier.
	 *
	 * @return the qualifier
	 */
	public String getQualifierComponent() {
		return qualifier;
	}

	/**
	 * Compares two version identifiers to see if this one is
	 * greater than or equal to the argument.
	 * <p>
	 * A version identifier is considered to be greater than or equal
	 * if its major component is greater than the argument major 
	 * component, or the major components are equal and its minor component
	 * is greater than the argument minor component, or the
	 * major and minor components are equal and its service component is
	 * greater than the argument service component, or the major, minor and
	 * service components are equal and the qualifier component is
	 * greater than the argument qualifier component (using lexicographic
	 * string comparison), or all components are equal.
	 * </p>
	 *
	 * @param id the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is compatible with the given version identifier, and
	 *    <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean isGreaterOrEqualTo(PluginVersionIdentifier id) {
		if (id == null)
			return false;
		if (major > id.getMajorComponent())
			return true;
		if ((major == id.getMajorComponent()) && (minor > id.getMinorComponent()))
			return true;
		if ((major == id.getMajorComponent()) && (minor == id.getMinorComponent()) && (service > id.getServiceComponent()))
			return true;
		if ((major == id.getMajorComponent()) && (minor == id.getMinorComponent()) && (service == id.getServiceComponent()) && (qualifier.compareTo(id.getQualifierComponent()) >= 0))
			return true;
		else
			return false;
	}

	/**
	 * Compares two version identifiers for compatibility.
	 * <p>
	 * A version identifier is considered to be compatible if its major 
	 * component equals to the argument major component, and its minor component
	 * is greater than or equal to the argument minor component.
	 * If the minor components are equal, than the service level of the
	 * version identifier must be greater than or equal to the service level
	 * of the argument identifier. If the service levels are equal, the two 
	 * version identifiers are considered to be equivalent if this qualifier is 
	 * greater or equal to the qualifier of the argument (using lexicographic
	 * string comparison).
	 * </p>
	 *
	 * @param id the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is compatible with the given version identifier, and
	 *    <code>false</code> otherwise
	 */
	public boolean isCompatibleWith(PluginVersionIdentifier id) {
		if (id == null)
			return false;
		if (major != id.getMajorComponent())
			return false;
		if (minor > id.getMinorComponent())
			return true;
		if (minor < id.getMinorComponent())
			return false;
		if (service > id.getServiceComponent())
			return true;
		if (service < id.getServiceComponent())
			return false;
		if (qualifier.compareTo(id.getQualifierComponent()) >= 0)
			return true;
		else
			return false;
	}

	/**
	 * Compares two version identifiers for equivalency.
	 * <p>
	 * Two version identifiers are considered to be equivalent if their major 
	 * and minor component equal and are at least at the same service level 
	 * as the argument. If the service levels are equal, the two version
	 * identifiers are considered to be equivalent if this qualifier is 
	 * greater or equal to the qualifier of the argument (using lexicographic
	 * string comparison).
	 * 
	 * </p>
	 *
	 * @param id the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is equivalent to the given version identifier, and
	 *    <code>false</code> otherwise
	 */
	public boolean isEquivalentTo(PluginVersionIdentifier id) {
		if (id == null)
			return false;
		if (major != id.getMajorComponent())
			return false;
		if (minor != id.getMinorComponent())
			return false;
		if (service > id.getServiceComponent())
			return true;
		if (service < id.getServiceComponent())
			return false;
		if (qualifier.compareTo(id.getQualifierComponent()) >= 0)
			return true;
		else
			return false;
	}

	/**
	 * Compares two version identifiers for perfect equality.
	 * <p>
	 * Two version identifiers are considered to be perfectly equal if their
	 * major, minor, service and qualifier components are equal
	 * </p>
	 *
	 * @param id the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is perfectly equal to the given version identifier, and
	 *    <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean isPerfect(PluginVersionIdentifier id) {
		if (id == null)
			return false;
		if ((major != id.getMajorComponent()) || (minor != id.getMinorComponent()) || (service != id.getServiceComponent()) || (!qualifier.equals(id.getQualifierComponent())))
			return false;
		else
			return true;
	}

	/**
	 * Compares two version identifiers for order using multi-decimal
	 * comparison. 
	 *
	 * @param id the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is greater than the given version identifier, and
	 *    <code>false</code> otherwise
	 */
	public boolean isGreaterThan(PluginVersionIdentifier id) {

		if (id == null) {
			if (major == 0 && minor == 0 && service == 0 && qualifier.equals("")) //$NON-NLS-1$
				return false;
			else
				return true;
		}

		if (major > id.getMajorComponent())
			return true;
		if (major < id.getMajorComponent())
			return false;
		if (minor > id.getMinorComponent())
			return true;
		if (minor < id.getMinorComponent())
			return false;
		if (service > id.getServiceComponent())
			return true;
		if (service < id.getServiceComponent())
			return false;
		if (qualifier.compareTo(id.getQualifierComponent()) > 0)
			return true;
		else
			return false;

	}

	/**
	 * Returns the string representation of this version identifier. 
	 * The result satisfies
	 * <code>vi.equals(new PluginVersionIdentifier(vi.toString()))</code>.
	 *
	 * @return the string representation of this plug-in version identifier
	 */
	public String toString() {
		String base = major + SEPARATOR + minor + SEPARATOR + service; // R1.0 result
		if (qualifier.equals("")) //$NON-NLS-1$
			return base;
		else
			return base + SEPARATOR + qualifier;
	}

	private static String verifyQualifier(String s) {
		char[] chars = s.trim().toCharArray();
		boolean whitespace = false;
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isLetterOrDigit(chars[i])) {
				chars[i] = '-';
				whitespace = true;
			}
		}
		return whitespace ? new String(chars) : s;
	}
}

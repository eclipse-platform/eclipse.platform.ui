package org.eclipse.ant.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.apache.tools.ant.util.regexp.RegexpMatcher;
import org.apache.xerces.utils.regex.RegularExpression;
import org.apache.xerces.utils.regex.Match;
import java.util.Vector;
import org.apache.tools.ant.BuildException;

/**
 * An Ant regular expression matcher adapted to use the Xerces XML parser.
 * This implementation is the standard one used when running Ant inside the
 * Eclipse platform using the <code>AntRunner</code>.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */

public class XercesRegexpMatcher implements RegexpMatcher {
	private RegularExpression regexp;
/**
 * Returns a Vector of matched groups found in the argument.
 *
 * <p>Group 0 will be the full match, the rest are the
 * parenthesized subexpressions</p>.
 */
public Vector getGroups(String argument) {
	Match match = new Match();
	if (!regexp.matches(argument, match))
		return null;
	int count = match.getNumberOfGroups();
	Vector result = new Vector(count);
	for (int i = 0; i < count; i++) {
		result.add(match.getCapturedText(i));
	}
	return result;
}
/**
 * Get a String representation of the regexp pattern
 */
public String getPattern() {
	return regexp.getPattern();
}
/**
 * Does the given argument match the pattern?
 */
public boolean matches(String argument) {
	return regexp.matches(argument);
}
/**
 * Set the regexp pattern from the String description.
 */
public void setPattern(String pattern) throws BuildException {
	if (regexp == null)
		regexp = new RegularExpression(pattern);
	else
		regexp.setPattern(pattern);
}
}

package org.eclipse.ant.core;

import org.apache.tools.ant.util.regexp.RegexpMatcher;
import org.apache.xerces.utils.regex.RegularExpression;
import org.apache.xerces.utils.regex.Match;
import java.util.Vector;
import org.apache.tools.ant.BuildException;

/**
 * 
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

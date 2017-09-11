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
package org.eclipse.ui.console;

/**
 * A pattern match listener is registered with a <code>TextConsole</code>,
 * and is notified when its pattern has been matched to contents in
 * that console. A pattern match listener can be registered with a console
 * programmatically or via the <code>consolePatternMatchListeners</code> extension
 * point.
 * <p>
 * Following is an example console pattern match listener extension definition.
 * </pre>
 * &lt;extension point="org.eclipse.ui.console.consolePatternMatchListeners"&gt;
 *   &lt;consolePatternMatchListener
 *      id="com.example.ConsolePatternMatcher"
 *      regex=".*foo.*"
 *      class="com.example.ConsolePatternMatcher"&gt;
 *   &lt;/consolePatternMatchListener&gt;
 * &lt;/extension&gt;
 * </pre>
 * Attributes are specified as follows:
 * <ul>
 * <li><code>id</code> - a unique identifier for the pattern match listener</li>
 * <li><code>regex</code> - regular expression to match</li>
 * <li><code>class</code> - fully qualified name of the Java class implementing
 *  <code>org.eclipse.ui.console.IPatternMatchListenerDelegate</code></li>
 * </ul>
 * </p> 
 * <p>
 * Optionally a <code>qualifier</code> attribute may be specified to improve performance
 * of regular expression matching. A qualifier specifies a simple regular expression used to
 * qualify lines for the search. Lines that do not contain the qualifier are not considered.
 * </p>
 * <p>
 * Optionally an <code>enablement</code> expression may be provided to specify
 * which console(s) a pattern matcher should be contributed to.
 * </p>
 * <p>
 * Clients may implement this interface directly if registering a pattern match listener with
 * a text console programmatically. Clients contributing a pattern match listener via an
 * extension implement <code>IPatternMatchListenerDelegate</code> instead.
 * </p>
 * @see org.eclipse.ui.console.TextConsole
 * @since 3.1
 */
public interface IPatternMatchListener extends IPatternMatchListenerDelegate {
    /**
     * Returns the pattern to be used for matching. The pattern is
     * a string representing a regular expression. 
     * 
     * @return the regular expression to be used for matching
     */
    public String getPattern();
    
    /**
     * Returns the flags to use when compiling this pattern match listener's
     * regular expression, as defined by by <code>Pattern.compile(String regex, int flags)</code>
     * 
     * @return the flags to use when compiling this pattern match listener's
     * regular expression
     * @see java.util.regex.Pattern#compile(java.lang.String, int)
     */
    public int getCompilerFlags();
    
    /**
     * Returns a simple regular expression used to identify lines that may
     * match this pattern matcher's complete pattern, or <code>null</code>.
     * Use of this attribute can improve performance by disqualifying lines
     * from the search. When a line is found containing a match for this expression,
     * the line is searched from the beginning for this pattern matcher's
     * complete pattern. Lines not containing this pattern are discarded.
     * 
     * @return a simple regular expression used to identify lines that may
     * match this pattern matcher's complete pattern, or <code>null</code>
     */
    public String getLineQualifier();
    
}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * An <code>IFilter</code> is a content node whose children may be filtered
 * if the filter expression is true.
 * 
 * @since 3.3
 */
public interface IFilter extends INode {

	/**
	 * <p>
	 * Returns the filter's expression. The expression evaluates to either
	 * true or false, and determines whether or not the children will be
	 * filtered.
	 * </p>
	 * <p>
	 * The general form of the expression is
	 * "<code>[name][operator][value]</code>" where <code>name</code> is the
	 * name of the property by which to filter, for example <code>os</code> for
	 * operating system (see table below for possible values). The
	 * <code>operator</code> is either <code>=</code> to denote a match (exact
	 * match, case sensitive), or <code>!=</code> to denote does not match. The
	 * <code>value</code> is what the property should (or shouldn't) match. For
	 * example, for <code>os</code>, one of the possible values is
	 * <code>win32</code> (Windows).
	 * </p>
	 * 
	 * <table border="1">
	 * <tr>
	 *    <td><b>Property</b></td>
	 *    <td><b>Meaning</b></td>
	 *    <td><b>Possible Values</b></td>
	 * </tr>
	 * <tr>
	 *    <td><code>os</code></td>
	 *    <td>operating system</td>
	 *    <td><code>win32, win32, linux, macosx, aix, solaris, hpux, qnx</code></td>
	 * </tr>
	 * <tr>
	 *    <td><code>ws</code></td>
	 *    <td>windowing system</td>
	 *    <td><code>win32, motif, gtk, photon, carbon</code></td>
	 * </tr>
	 * <tr>
	 *    <td><code>arch</code></td>
	 *    <td>processor architecture</td>
	 *    <td><code>x86, x86_64, ia64, ia64_32, ppc, PA_RISC, sparc</code></td>
	 * </tr>
	 * <tr>
	 *    <td><code>product</code></td>
	 *    <td>eclipse product identifier</td>
	 *    <td>Any product identifier (e.g., for SDK, <code>org.eclipse.sdk.ide</code>)</td>
	 * </tr>
	 * <tr>
	 *    <td><code>plugin</code></td>
	 *    <td>plug-in presence</td>
	 *    <td>Any plug-in identifier (e.g. <code>org.eclipse.help</code>)</td>
	 * </tr>
	 * <tr>
	 *    <td><code>category</code></td>
	 *    <td>category of activities</td>
	 *    <td>Any activity category identifier (e.g. for Team category, <code>org.eclipse.categories.teamCategory</code>)</td>
	 * </tr>
	 * <tr>
	 *    <td><code>activity</code></td>
	 *    <td>activity (capability)</td>
	 *    <td>Any activity identifier (e.g. for CVS Support activity, <code>org.eclipse.team.cvs</code>)</td>
	 * </tr>
	 * </table>
	 * 
	 * @return the filter expression
	 */
	public String getExpression();
}

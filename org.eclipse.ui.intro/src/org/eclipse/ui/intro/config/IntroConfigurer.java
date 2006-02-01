/***************************************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.ui.intro.config;


/**
 * Classes that extend this abstract class are used to configure <code>CustomizableIntroPart</code>.
 * Since it is possible for multiple products to use the same intro configuration, this class allows
 * them to customize some aspects of the intro content so that it looks different for different
 * products even though they all share the same intro implementation and content.
 * 
 * @since 3.2
 * 
 */

public abstract class IntroConfigurer {

	/**
	 * Returns the value of the variable defined by the configurer. This variable can appear in XML
	 * content files in attribute names and values of elements. Whenever $variable$ is encountered
	 * in the content, it is evaluated using this class by passing 'variable' to this method and
	 * substituting the result in the content.
	 * 
	 * @param variableName
	 *            the name of the substitution variable
	 * @return the value to substitute in place of a variable or <code>null</code> if the variable
	 *         cannot be resolved.
	 */
	public abstract String getVariable(String variableName);

	/**
	 * Returns the children of dynamic groups. Groups marked as dynamic will be completed at run
	 * time when the group is asked to provide children.
	 * 
	 * @param pageId
	 *            the identifier of the page in which this group appears
	 * @param groupId
	 *            the identifier of the group group within the page
	 * @return an array of intro elements for this group. Each intro element should contain only
	 *         legal elements and attributes according to the intro content schema. Returns an empty
	 *         array for no children.
	 */
	public abstract IntroElement[] getGroupChildren(String pageId, String groupId);
/**
 * Resolves an incomplete path in the form "page_id/@" where page_id
 * represents the identifier of the target page. The configurator
 * should complete the path according to its internal resolution
 * mechanism. The final path must point at an anchor in the 
 * referenced page.
 * @param extensionId the id specified for the config extension
 * @param path the incomplete path specified for the config extension
 * @return the complete path that points at the anchor element
 * in the referenced page, or <code>null</code> if the path
 * cannot be resolved or the extension should be hidden.
 */
	public abstract String resolvePath(String extensionId, String path);
}
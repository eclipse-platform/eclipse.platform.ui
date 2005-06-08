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

/**
 * A configuration element, with its attributes and children, 
 * directly reflects the content and structure of the extension section
 * within the declaring plug-in's manifest (<code>plugin.xml</code>) file.
 * <p>
 * This interface also provides a way to create executable extension
 * objects.
 * </p>
 * <p>
 * These registry objects are intended for relatively short-term use. Clients that 
 * deal with these objects must be aware that they may become invalid if the 
 * declaring plug-in is updated or uninstalled. If this happens, all methods except
 * {@link #isValid()} will throw {@link InvalidRegistryObjectException}.
 * For configuration element objects, the most common case is code in a plug-in dealing
 * with extensions contributed to one of the extension points it declares.
 * Code in a plug-in that has declared that it is not dynamic aware (or not
 * declared anything) can safely ignore this issue, since the registry
 * would not be modified while it is active. However, code in a plug-in that
 * declares that it is dynamic aware must be careful when accessing the extension
 * and configuration element objects because they become invalid if the contributing
 * plug-in is removed. Similarly, tools that analyze or display the extension registry
 * are vulnerable. Client code can pre-test for invalid objects by calling {@link #isValid()},
 * which never throws this exception. However, pre-tests are usually not sufficient
 * because of the possibility of the extension or configuration element object becoming
 * invalid as a result of a concurrent activity. At-risk clients must treat 
 * <code>InvalidRegistryObjectException</code> as if it were a checked exception.
 * Also, such clients should probably register a listener with the extension registry
 * so that they receive notification of any changes to the registry.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IConfigurationElement {
	/**
	 * Creates and returns a new instance of the executable extension 
	 * identified by the named attribute of this configuration element.
	 * The named attribute value must contain a fully qualified name
	 * of a Java class. The class can either refer to a class implementing 
     * the executable extension or to a factory capable of returning the 
     * executable extension.
	 * <p>
	 * The specified class is instantiated using its 0-argument public constructor.
	 * <p>
	 * Then the following checks are done:<br>
     * If the specified class implements the {@link IExecutableExtension} 
     * interface, the method {@link IExecutableExtension#setInitializationData(IConfigurationElement, String, Object)} 
     * is called, passing to the object the configuration information that was used to create it. 
	 * <p>
     * If the specified class implements {@link IExecutableExtensionFactory} 
     * interface, the method {@link IExecutableExtensionFactory#create()} 
     * is invoked.
	 * </p>
	 * <p>
	 * Unlike other methods on this object, invoking this method may activate 
	 * the plug-in.
	 * </p>
	 *
	 * @param propertyName the name of the property
	 * @return the executable instance
	 * @exception CoreException if an instance of the executable extension
	 *   could not be created for any reason
	 * @see IExecutableExtension#setInitializationData(IConfigurationElement, String, Object)
     * @see IExecutableExtensionFactory
	 */
	public Object createExecutableExtension(String propertyName) throws CoreException;
    
	/**
	 * Returns the named attribute of this configuration element, or
	 * <code>null</code> if none. 
	 * <p>
	 * The names of configuration element attributes
	 * are the same as the attribute names of the corresponding XML element.
	 * For example, the configuration markup 
	 * <pre>
	 * &lt;bg pattern="stripes"/&gt;
	 * </pre>
	 * corresponds to a configuration element named <code>"bg"</code>
	 * with an attribute named <code>"pattern"</code>
	 * with attribute value <code>"stripes"</code>.
	 * </p>
	 * <p> Note that any translation specified in the plug-in manifest
	 * file is automatically applied.
	 * </p>
	 *
	 * @param name the name of the attribute
	 * @return attribute value, or <code>null</code> if none
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 */
	public String getAttribute(String name) throws InvalidRegistryObjectException;

	/**
	 * Returns the named attribute of this configuration element, or
	 * <code>null</code> if none. 
	 * <p>
	 * The names of configuration element attributes
	 * are the same as the attribute names of the corresponding XML element.
	 * For example, the configuration markup 
	 * <pre>
	 * &lt;bg pattern="stripes"/&gt;
	 * </pre>
	 * corresponds to a configuration element named <code>"bg"</code>
	 * with an attribute named <code>"pattern"</code>
	 * with attribute value <code>"stripes"</code>.
	 * </p>
	 * <p>
	 * Note that any translation specified in the plug-in manifest
	 * file for this attribute is <b>not</b> automatically applied.
	 * </p>
	 *
	 * @param name the name of the attribute
	 * @return attribute value, or <code>null</code> if none
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 */
	public String getAttributeAsIs(String name) throws InvalidRegistryObjectException;

	/**
	 * Returns the names of the attributes of this configuration element.
	 * Returns an empty array if this configuration element has no attributes.
	 * <p>
	 * The names of configuration element attributes
	 * are the same as the attribute names of the corresponding XML element.
	 * For example, the configuration markup 
	 * <pre>
	 * &lt;bg color="blue" pattern="stripes"/&gt;
	 * </pre>
	 * corresponds to a configuration element named <code>"bg"</code>
	 * with attributes named <code>"color"</code>
	 * and <code>"pattern"</code>.
	 * </p>
	 *
	 * @return the names of the attributes 
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 */
	public String[] getAttributeNames() throws InvalidRegistryObjectException;

	/**
	 * Returns all configuration elements that are children of this
	 * configuration element. 
	 * Returns an empty array if this configuration element has no children.
	 * <p>
	 * Each child corresponds to a nested
	 * XML element in the configuration markup.
	 * For example, the configuration markup 
	 * <pre>
	 * &lt;view&gt;
	 * &nbsp&nbsp&nbsp&nbsp&lt;verticalHint&gt;top&lt;/verticalHint&gt;
	 * &nbsp&nbsp&nbsp&nbsp&lt;horizontalHint&gt;left&lt;/horizontalHint&gt;
	 * &lt;/view&gt;
	 * </pre>
	 * corresponds to a configuration element, named <code>"view"</code>,
	 * with two children.
	 * </p>
	 *
	 * @return the child configuration elements
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 * @see #getChildren(String)
	 */
	public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException;

	/**
	 * Returns all child configuration elements with the given name. 
	 * Returns an empty array if this configuration element has no children
	 * with the given name.
	 *
	 * @param name the name of the child configuration element
	 * @return the child configuration elements with that name
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 * @see #getChildren()
	 */
	public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException;

	/** 
	 * Returns the extension that declares this configuration element.
	 *
	 * @return the extension
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 */
	public IExtension getDeclaringExtension() throws InvalidRegistryObjectException;

	/**
	 * Returns the name of this configuration element. 
	 * The name of a configuration element is the same as
	 * the XML tag of the corresponding XML element. 
	 * For example, the configuration markup 
	 * <pre>
	 * &lt;wizard name="Create Project"/&gt; 
	 * </pre>
	 * corresponds to a configuration element named <code>"wizard"</code>.
	 *
	 * @return the name of this configuration element
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 */
	public String getName() throws InvalidRegistryObjectException;

	/**
	 * Returns the element which contains this element.  If this element
	 * is an immediate child of an extension, the
	 * returned value can be downcast to <code>IExtension</code>.
	 * Otherwise the returned value can be downcast to 
	 * <code>IConfigurationElement</code>.
	 *
	 * @return the parent of this configuration element
	 *  or <code>null</code>
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 * @since 3.0
	 */
	public Object getParent() throws InvalidRegistryObjectException;

	/**
	 * Returns the text value of this configuration element.
	 * For example, the configuration markup 
	 * <pre>
	 * &lt;script lang="javascript"&gt;.\scripts\cp.js&lt;/script&gt;
	 * </pre>
	 * corresponds to a configuration element <code>"script"</code>
	 * with value <code>".\scripts\cp.js"</code>.
	 * <p> Values may span multiple lines (i.e., contain carriage returns
	 * and/or line feeds).
	 * <p> Note that any translation specified in the plug-in manifest
	 * file is automatically applied.
	 * </p>
	 *
	 * @return the text value of this configuration element or <code>null</code>
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 */
	public String getValue() throws InvalidRegistryObjectException;

	/**
	 * Returns the untranslated text value of this configuration element.
	 * For example, the configuration markup 
	 * <pre>
	 * &lt;script lang="javascript"&gt;.\scripts\cp.js&lt;/script&gt;
	 * </pre>
	 * corresponds to a configuration element <code>"script"</code>
	 * with value <code>".\scripts\cp.js"</code>.
	 * <p> Values may span multiple lines (i.e., contain carriage returns
	 * and/or line feeds).
	 * <p>
	 * Note that translation specified in the plug-in manifest
	 * file is <b>not</b> automatically applied.
	 * For example, the configuration markup 
	 * <pre>
	 * &lt;tooltip&gt;#hattip&lt;/tooltip&gt;
	 * </pre>
	 * corresponds to a configuration element, named <code>"tooltip"</code>,
	 * with value <code>"#hattip"</code>.
	 * </p>
	 *
	 * @return the untranslated text value of this configuration element or <code>null</code>
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 */
	public String getValueAsIs() throws InvalidRegistryObjectException;
	
	/**
	 * Returns the namespace for this configuration element. This value can be used
	 * in various global facilities to discover this configuration element's provider.
	 * <p>
	 * 
	 * @return the namespace for this configuration element
	 * @throws InvalidRegistryObjectException if this configuration element is no longer valid
	 * @see Platform#getBundle(String)
	 * @see IExtensionRegistry
	 * @since 3.1
	 */
	public String getNamespace() throws InvalidRegistryObjectException;
	
	/* (non-javadoc) 
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o);
	
	/**
	 * Returns whether this configuration element object is valid.
	 * 
	 * @return <code>true</code> if the object is valid, and <code>false</code>
	 * if it is no longer valid
	 * @since 3.1
	 */
	public boolean isValid();
}

package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import java.util.*;
public interface INLResourceHelper {
/**
 * Returns this plug-in's resource bundle for the current locale. 
 * <p>
 * The bundle is stored as the <code>plugin.properties</code> file 
 * in the plug-in install directory, and contains any translatable
 * strings used in the plug-in manifest file (<code>plugin.xml</code>)
 * along with other resource strings used by the plug-in implementation.
 * </p>
 *
 * @return the resource bundle
 * @exception MissingResourceException if the resource bundle was not found
 */
public ResourceBundle getResourceBundle() throws MissingResourceException;
/**
 * Returns a resource string corresponding to the argument value.
 * If the argument value specifies a resource key, the string
 * is looked up in a resource bundle. If the argument does not
 * specify a valid key, the argument itself is returned as the
 * resource string. The key lookup is performed in the
 * plugin.properties resource bundle. If a resource string 
 * corresponding to the key is not found in the resource bundle
 * the key value, or any default text following the key in the
 * argument value is returned as the resource string.
 * A key is identified as a string begining with the "%" character.
 * Note, that the "%" character is stripped off prior to lookup
 * in the resource bundle
 *
 * For example, assume resource bundle plugin.properties contains
 * name = Project Name
 *
 * getResourceString("Hello World") returns "Hello World"
 * getResourceString("%name") returns "Project Name"
 * getResourceString("%name Hello World") returns "Project Name"
 * getResourceString("%abcd Hello World") returns "Hello World"
 * getResourceString("%abcd") returns "%abcd"
 * getResourceString("%%abcd") returns "%abcd"
 *
 * @param value the value
 * @return the resource string
 * @see #getResourceBundle
 */
public String getResourceString(String value) ;
/**
 * Returns a resource string corresponding to the argument value.
 * If the argument value specifies a resource key, the string
 * is looked up in a resource bundle. If the argument does not
 * specify a valid key, the argument itself is returned as the
 * resource string. The key lookup is performed against the
 * specified resource bundle. If a resource string 
 * corresponding to the key is not found in the resource bundle
 * the key value, or any default text following the key in the
 * argument value is returned as the resource string.
 * A key is identified as a string begining with the "%" character.
 * Note, that the "%" character is stripped off prior to lookup
 * in the resource bundle
 *
 * For example, assume the specified resource bundle contains
 * name = Project Name
 *
 * getResourceString("Hello World",b) returns "Hello World"
 * getResourceString("%name",b) returns "Project Name"
 * getResourceString("%name Hello World",b) returns "Project Name"
 * getResourceString("%abcd Hello World",b) returns "Hello World"
 * getResourceString("%abcd",b) returns "%abcd"
 * getResourceString("%%abcd") returns "%abcd"
 *
 * @param value the value
 * @param bundle the resource bundle
 * @return the resource string
 * @see #getResourceBundle
 */
public String getResourceString(String value, ResourceBundle bundle) ;
}

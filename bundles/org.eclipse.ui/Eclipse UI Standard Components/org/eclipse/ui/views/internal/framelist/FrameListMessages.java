package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class which helps managing messages
 * 
 * @deprecated This has been promoted to API and will be removed for 2.0.  
 *   Use the corresponding class in package org.eclipse.ui.views.framelist instead.
 */
class FrameListMessages {
	private static final String RESOURCE_BUNDLE= "org.eclipse.ui.views.internal.framelist.messages";//$NON-NLS-1$
	private static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
	
private FrameListMessages(){
	// prevent instantiation of class
}
/**
 * Returns the formatted message for the given key in
 * the resource bundle. 
 *
 * @param key the resource name
 * @param args the message arguments
 * @return the string
 */	
public static String format(String key, Object[] args) {
	return MessageFormat.format(getString(key),args);
}
/**
 * Returns the resource object with the given key in
 * the resource bundle. If there isn't any value under
 * the given key, the key is returned.
 *
 * @param key the resource name
 * @return the string
 */	
public static String getString(String key) {
	try {
		return bundle.getString(key);
	} catch (MissingResourceException e) {
		return key;
	}
}
}

package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.update.internal.core.Assert;

/**
 * An object which has the general characteristics of all elements
 * in the install/ update support.
 * <p>
 * This class cannot be instantiated and must be subclassed.
 * </p>
 */

public abstract class ModelObject {
	
	private boolean readOnly = false;
	
	private static final String KEY_PREFIX = "%";
	private static final String KEY_DOUBLE_PREFIX = KEY_PREFIX+KEY_PREFIX;
		
	/**
	 * Creates a a base model object.
	 * 
	 * @since 2.0
	 */
	protected ModelObject() {
	}
	
	/**
	 * Checks that this model object is writeable.  A runtime exception
	 * is thrown if it is not.
	 * 
	 * @since 2.0
	 */
	protected final void assertIsWriteable() {
		Assert.isTrue(!isReadOnly(), "Model is read-only");
	}
	
	/**
	 * Sets this model object and all of its descendents to be read-only.
	 * Subclasses may extend this implementation.
	 * 
	 * @since 2.0
	 * @see #isReadOnly
	 */
	public void markReadOnly() {
		readOnly = true;
	}
	
	/**
	 * Returns whether or not this model object is read-only.
	 * 
	 * @since 2.0
	 * @return <code>true</code> if this model object is read-only,
	 *		<code>false</code> otherwise
	 * @see #markReadOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
		
	/**
	 * Delegate setting of read-only
	 *
	 * @since 2.0
	 * @param o object to delegate to. Must be of type ModelObject.
	 * @see #isReadOnly
	 */
	protected void markReferenceReadOnly(ModelObject o) {
		if (o==null)
			return;
		o.markReadOnly();	
	}
		
	/**
	 * Delegate setting of read-only
	 *
	 * @since 2.0
	 * @param o object array to delegate to. Each element must be of type ModelObject.
	 * @see #isReadOnly
	 */
	protected void markListReferenceReadOnly(ModelObject[] o) {
		if (o==null)
			return;
		for (int i=0; i<o.length; i++) {
			o[i].markReadOnly();
		}
	}
	
	/**
	 * Resolve the model element. This method allows any relative URL strings
	 * to be resolved to actual URL. It also allows any translatable strings
	 * to be localized.
	 * 
	 * Subclasses need to override this method to perform the actual resolution.
	 * @param base base URL.
	 * @param bundle resource bundle.
	 * 
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle) throws MalformedURLException {
		return;
	}
		
	/**
	 * Delegate resolution to referenced model
	 *
	 * @since 2.0
	 * @param o object to delegate to. Must be of type ModelObject.
	 * @param base base URL.
	 * @param bundle resource bundle.
	 */
	protected void resolveReference(ModelObject o, URL url, ResourceBundle bundle) throws MalformedURLException {
		if (o==null)
			return;
		o.resolve(url,bundle);	
	}
		
	/**
	 * Delegate resolution to list of referenced models
	 *
	 * @since 2.0
	 * @param o object array to delegate to. Each element must be of type ModelObject.
	 * @param base base URL.
	 * @param bundle resource bundle.
	 */
	protected void resolveListReference(ModelObject[] o, URL url, ResourceBundle bundle) throws MalformedURLException {
		if (o==null)
			return;
		for (int i=0; i<o.length; i++) {
			o[i].resolve(url, bundle);
		}
	}
			
	/**
	 * Resolve a URL based on context
	 *
	 * @since 2.0
	 * @param base base URL.
	 * @param bundle resource bundle.
	 * @param urlString url string from model.
	 * @return URL, or <code>null</code>.
	 */
	protected URL resolveURL(URL context, ResourceBundle bundle, String urlString) throws MalformedURLException {
		
		// URL string was not specified
		if (urlString == null || urlString.trim().equals(""))
			return null;
		
		// check to see if we have NL-sensitive URL
		String resolvedUrlString = resolveNLString(bundle, urlString);
		
		// if we don't have a base url, use only the supplied string
		if (context == null)
			return new URL(resolvedUrlString);
			
		// otherwise return new URL in context of base URL
		return new URL(context, resolvedUrlString);
	}
			
	/**
	 * Returns a resource string corresponding to the given argument 
	 * value and bundle.
	 * If the argument value specifies a resource key, the string
	 * is looked up in the given resource bundle. If the argument does not
	 * specify a valid key, the argument itself is returned as the
	 * resource string. The key lookup is performed against the
	 * specified resource bundle. If a resource string 
	 * corresponding to the key is not found in the resource bundle
	 * the key value, or any default text following the key in the
	 * argument value is returned as the resource string.
	 * A key is identified as a string begining with the "%" character.
	 * Note that the "%" character is stripped off prior to lookup
	 * in the resource bundle.
	 * <p>
	 * For example, assume resource bundle plugin.properties contains
	 * name = Project Name
	 * <pre>
	 *     resolveNLString(b,"Hello World") returns "Hello World"</li>
	 *     resolveNLString(b,"%name") returns "Project Name"</li>
	 *     resolveNLString(b,"%name Hello World") returns "Project Name"</li>
	 *     resolveNLString(b,"%abcd Hello World") returns "Hello World"</li>
	 *     resolveNLString(b,"%abcd") returns "%abcd"</li>
	 *     resolveNLString(b,"%%name") returns "%name"</li>
	 * </pre>
	 * </p>
	 * 
	 * @since 2.0
	 * @param bundle resource bundle.
	 * @param s translatable string from model
	 * @return string, or <code>null</code>
	 */
	protected String resolveNLString(ResourceBundle b, String string) {		

		if (string == null)
			return null;

		String s = string.trim();
		
		if (s.equals(""))
			return s;
	
		if (!s.startsWith(KEY_PREFIX)) 
			return s;

		if (s.startsWith(KEY_DOUBLE_PREFIX)) 
			return s.substring(1);

		int ix = s.indexOf(" ");
		String key = ix == -1 ? s : s.substring(0,ix);
		String dflt = ix == -1 ? s : s.substring(ix+1);
	
		if (b==null) 
			return dflt;
	
		try { 
			return b.getString(key.substring(1));
		} catch(MissingResourceException e) { 
			return dflt; 
		}
	}
	
	/**
	 * @since 2.0
	 */
	protected Object[] arrayTypeFor(List l) {
		if (l == null || l.size()==0)
			return null;
		return (Object[])Array.newInstance(l.get(0).getClass(),0);
	}
	
	/**
	 * @since 2.0
	 */
	protected Object[] arrayTypeFor(Set s) {
		if (s == null || s.size()==0)
			return null;
		Iterator i = s.iterator();
		return (Object[])Array.newInstance(i.next().getClass(),0);
	}
}

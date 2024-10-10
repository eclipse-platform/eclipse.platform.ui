package org.eclipse.e4.emf.xpath;

import org.eclipse.e4.emf.xpath.internal.java.JavaXPathFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * @since 0.5
 */
public abstract class JavaXPathFactory<T> {
	/**
	 * Creates a new XPathContext with the specified object as the root node.
	 *
	 * @param contextBean Object
	 * @return XPathContext
	 */
	public abstract XPathContext newContext(T contextBean);

	/**
	 * Creates a new XPathContext with the specified bean as the root node and the
	 * specified parent context. Variables defined in a parent context can be
	 * referenced in XPaths passed to the child context.
	 *
	 * @param parentContext parent context
	 * @param contextBean   Object
	 * @return XPathContext
	 */
	public abstract XPathContext newContext(XPathContext parentContext, T contextBean);

	/**
	 * @param <Type> the object type the xpath is created for
	 * @return Create a new XPath-Factory
	 */
	public static XPathContextFactory<EObject> newInstance() {
		return new JavaXPathFactoryImpl<>();
	}



}

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.topics;
/**
 * IDescriptor is a descriptor of ITopic or ITopics
 * that can be useful when refering or displaying
 * these objects.
 */
public interface IDescriptor {

	public final static String HREF = "href";
	public final static String LABEL = "label";
	
	/**
	 * Obtains href
	 */
	public String getHref();
	/**
	 * Obtains label
	 */
	public String getLabel();
}
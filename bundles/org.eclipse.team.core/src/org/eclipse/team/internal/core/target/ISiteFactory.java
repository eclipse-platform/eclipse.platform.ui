/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.core.target;

import java.io.ObjectInputStream;
import java.util.Properties;

/**
 * The <code>ISiteFactory</code> interface must be implemented by any plug-in
 * that is providing target management. It provides mechanisms for creating
 * concrete <code>Site</code> instances for it's target type.
 * 
 * @see Site
 */
public interface ISiteFactory {
	/**
	 * Responsible for reading from the stream and restoring the classes fields
	 * then returning a new <code>Site</code> instance. The <code>Site</code>
	 * instances are written using the <code>Site#writeObject</code> method.
	 * 
	 * @param is the input stream that contains the output of Site#writeObject
	 * @return a new target site
	 */	
	public Site newSite(ObjectInputStream is);
	
	/**
	 * Returns a new target site for the given target specific properties. This
	 * is mainly used for testing purposes.
	 * 
	 * @param properties the target specific location encoded in properties
	 * @return a new target site
	 */	
	public Site newSite(Properties properties);
}
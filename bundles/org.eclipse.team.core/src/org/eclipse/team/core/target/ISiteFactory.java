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
package org.eclipse.team.core.target;

import java.util.Properties;

public interface ISiteFactory {
	
	/**
	 * Returns a new target site for the given target specific 
	 * description.
	 * 
	 * @param description the target specific description encoded
	 * as a string
	 * @return a new target site
	 */	
	public Site newSite(String description);
	
	/**
	 * Returns a new target site for the given target specific 
	 * properties.
	 * 
	 * @param properties the target specific location encoded
	 * in properties
	 * @return a new target site
	 */	
	public Site newSite(Properties properties);
}

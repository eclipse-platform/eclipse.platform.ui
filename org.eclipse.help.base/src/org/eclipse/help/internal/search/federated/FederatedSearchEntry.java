/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search.federated;

import org.eclipse.help.internal.search.*;

/**
 * A participant in the federated search.
 */
public class FederatedSearchEntry {
	private String engineId;
	private ISearchScope scope;
	private ISearchEngine engine;

	/**
	 * 
	 */
	public FederatedSearchEntry(String engineId, ISearchScope scope, ISearchEngine engine) {
		this.engineId = engineId;
		this.scope = scope;
		this.engine = engine;
	}
	
	public String getEngineId() {
		return engineId;
	}

	public ISearchEngine getEngine() {
		return engine;
	}
	public ISearchScope getScope() {
		return scope;
	}
}

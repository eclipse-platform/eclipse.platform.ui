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
package org.eclipse.help.internal.search.federated;

import org.eclipse.help.search.*;

/**
 * A participant in the federated search.
 */
public class FederatedSearchEntry {
	private String engineId;
	private String engineName;
	private ISearchScope scope;
	private ISearchEngine engine;
	private ISearchEngineResultCollector collector;

	/**
	 * 
	 */
	public FederatedSearchEntry(String engineId, String engineName, ISearchScope scope, ISearchEngine engine, ISearchEngineResultCollector collector) {
		this.engineId = engineId;
		this.engineName = engineName;
		this.scope = scope;
		this.engine = engine;
		this.collector = collector;
	}
	
	public String getEngineId() {
		return engineId;
	}
	
	public String getEngineName() {
		return engineName;
	}	

	public ISearchEngine getEngine() {
		return engine;
	}
	public ISearchScope getScope() {
		return scope;
	}
	public ISearchEngineResultCollector getResultCollector() {
		return collector;
	}
}

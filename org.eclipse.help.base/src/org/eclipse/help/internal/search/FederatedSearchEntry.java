/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.internal.search;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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

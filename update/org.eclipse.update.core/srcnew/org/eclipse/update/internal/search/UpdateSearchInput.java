/*
 * Created on May 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.search;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UpdateSearchInput {
	private IUpdateSearchCategory category;
	private UpdateSearchScope scope;

	public UpdateSearchInput(IUpdateSearchCategory category, UpdateSearchScope scope) {
		this.category = category;
		this.scope = scope;
	}

	public IUpdateSearchCategory getSearchCategory() {
		return category;
	}

	public UpdateSearchScope getSearchScope() {
		return scope;
	}
}
/*
 * Created on Jan 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FederatedSearchResultData {
	String expression;
	boolean showDescription;
	boolean showCategories;

	/**
	 * 
	 */
	public FederatedSearchResultData(String expression, boolean desc, boolean cat) {
		this.expression = expression;
		this.showDescription = desc;
		this.showCategories = cat;
	}
}

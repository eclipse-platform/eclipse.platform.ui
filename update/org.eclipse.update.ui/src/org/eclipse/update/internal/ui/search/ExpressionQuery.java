package org.eclipse.update.internal.ui.search;

import org.eclipse.update.core.IFeature;
import org.w3c.dom.Node;
import java.io.PrintWriter;

public class ExpressionQuery implements ISearchQuery {
	public static final int NAME=1;
	public static final int PROVIDER = 2;
	public static final int DESCRIPTION = 4;
	
	private String expression="";
	private boolean caseSensitive=false;
	private int flags=NAME;
	
	public ExpressionQuery() {
	}
	
	public ExpressionQuery(String expression, int flags, boolean caseSensitive) {
		this.expression = expression;
		this.flags= flags;
		this.caseSensitive = caseSensitive;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	public void setCaseSensitive(boolean value) {
		this.caseSensitive = value;
	}

	/**
	 * @see ISearchQuery#matches(IFeature)
	 */
	public boolean matches(IFeature feature) {
		return false;
	}
	public void parse(Node node) {
	}
	public void write(String indent, PrintWriter writer) {
	}
}
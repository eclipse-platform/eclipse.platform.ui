package org.eclipse.ua.tests.help.other;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;

public class UserToc implements IToc {
	
	private List children = new ArrayList();
	private boolean isEnabled;
	private String href;
	private String label;
	
	public UserToc(String label, String href, boolean isEnabled) {
		this.label = label;
		this.href = href;
		this.isEnabled = isEnabled;
	}

	/*
	 * Not exercised by any test so return of null is OK for now
	 */
	public ITopic getTopic(String href) {
		return null;
	}

	public ITopic[] getTopics() {
		return (ITopic[])children.toArray(new ITopic[0]);
	}

	public IUAElement[] getChildren() {
		return getTopics();
	}

	public void addTopic(ITopic child) {
		children.add(child);
	}

	public boolean isEnabled(IEvaluationContext context) {
		return isEnabled;
	}

	public String getHref() {
		return href;
	}

	public String getLabel() {
		return label;
	}

}

package org.eclipse.ua.tests.help.other;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ICriteria;
import org.eclipse.help.IUAElement;

public class UserCriteria implements ICriteria {
	
	private String name;
	private String value;
	private boolean enabled;

	public UserCriteria(String name, String value, boolean enabled) {
		this.name = name;
		this.value = value;
		this.enabled = enabled;
	}

	public boolean isEnabled(IEvaluationContext context) {
		return enabled;
	}

	public IUAElement[] getChildren() {
		return null;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}

package org.eclipse.ui.externaltools.variable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class DefaultVariableExpander implements IVariableExpander {

	private static DefaultVariableExpander instance;

	public static DefaultVariableExpander getDefault() {
		if (instance == null) {
			instance= new DefaultVariableExpander();
		}
		return instance;
	}

	public IPath getPath(String varTag, String varValue, ExpandVariableContext context) {
		return null;
	}

	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) {
		return null;
	}

	public String getText(String varTag, String varValue, ExpandVariableContext context) {
		return null;
	}

}

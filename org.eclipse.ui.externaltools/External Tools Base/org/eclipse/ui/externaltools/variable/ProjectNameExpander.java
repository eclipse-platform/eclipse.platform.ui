package org.eclipse.ui.externaltools.variable;

import org.eclipse.core.resources.IProject;

/**
 * Extracts the project name from a variable context */
public class ProjectNameExpander implements IVariableTextExpander {

	/**
	 * Returns the name of the project in the given context or
	 * <code>null</code> if there is no project in the context.
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) {
		IProject project= context.getProject();
		if (project != null) {
			return project.getName();
		}
		return null;
	}

}

/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ant.core.toolscripts;

import java.util.Map;

import org.eclipse.ant.core.IAntRunnerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An ant tool script consists of an Ant file (either in the
 * workspace or in the filesystem), parameters, and refresh options.
 */
public class AntToolScript extends ToolScript {
	protected IFile antFile;
	protected String[] parameters;
	
public AntToolScript(IFile file) {
	this.antFile = file;
}
/**
 * Factory method for creating a tool script based on builder arguments.
 * Returns null if no corresponding tool script could be created.
 */
public static ToolScript createFromArguments(Map arguments) {
	return null;
}
/**
 * @see ToolScript#execute(IProgressMonitor)
 */
protected void execute(IAntRunnerListener listener, IProgressMonitor monitor) throws CoreException {
}
protected void fillBuilderArguments(Map arguments) {
}
public int getKind() {
	return KIND_ANT_SCRIPT;
}
/**
 * Sets the parameters.
 * @param parameters The parameters to set
 */
public void setParameters(String[] parameters) {
	this.parameters = parameters;
}
/**
 * Returns an NL-enabled string describing this command.
 */
public String toString() {
	return "Ant script";
}
}

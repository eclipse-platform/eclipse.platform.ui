package org.eclipse.ant.core.toolscripts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.core.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Class for holding information about a tool script.  A tool script
 * consists of a command line, plus options for refreshing some
 * working set after the command is executed.
 */
public abstract class ToolScript {
	//the known kinds of tool scripts
	public static final int KIND_EXTERNAL_COMMAND = 1;
	public static final int KIND_ANT_SCRIPT = 2;
	
	//the builder argument that represents the kind of tool script
	protected static final String ARG_SCRIPT_KIND = "_Script_Kind";
	protected static final String ARG_REFRESH_CONTAINER = "_Refresh_Container";
	
	//the container to refresh after running the script, or null
	protected IContainer refreshContainer;
	
public ToolScript() {
}
/**
 * Returns the map of arguments for when this script is run as 
 * an incremental builder.  All argument keys and values must be strings.
 */
public Map createBuilderArguments() {
	HashMap args = new HashMap(20);
	args.put(ARG_SCRIPT_KIND, Integer.toString(getKind()));
	String container = refreshContainer == null ? "" : refreshContainer.getFullPath().toString();
	args.put(ARG_REFRESH_CONTAINER, container);
	fillBuilderArguments(args);
	return args;
}
/**
 * Factory method for creating a tool script based on builder arguments.
 * Returns null if no corresponding tool script could be created.
 */
public static ToolScript createFromArguments(Map arguments) {
	String kindString = (String)arguments.get(ARG_SCRIPT_KIND);
	if (kindString == null) {
		return null;
	}
	int kind = -1;
	try {
		kind = Integer.parseInt(kindString);
	} catch (NumberFormatException e) {
		return null;
	}
	ToolScript script = null;
	switch (kind) {
		case KIND_ANT_SCRIPT:
			script = AntToolScript.createFromArguments(arguments);
			break;
		case KIND_EXTERNAL_COMMAND:
			script = ExternalToolScript.createFromArguments(arguments);
			break;
	}
	if (script == null) {
		return null;
	}
	String refresh = (String)arguments.get(ARG_REFRESH_CONTAINER);
	if (refresh != null && refresh.length() > 0) {
		IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(refresh));
		if (container instanceof IContainer) {
			script.setRefreshContainer((IContainer)container);
		}
	}
	return script;
}
/**
 * Returns the kind of tool script.
 */
public abstract int getKind();
/**
 * Runs the main tool script, supplying progress to the given progress monitor.
 */
protected abstract void execute(IProgressMonitor monitor) throws CoreException;
/**
 * Adds builder arguments that are specific to this type of tool script
 */
protected abstract void fillBuilderArguments(Map arguments);
/**
 * Runs the tool script, including local refresh at the end.
 */
public void run(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		if (refreshContainer == null) {
			execute(monitor);
			return;
		}
		//need to divide into run and refresh segments
		monitor.beginTask("Running tool script...", 100);
		execute(new SubProgressMonitor(monitor, 70));
		refreshContainer.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 30));
	} finally {
		monitor.done();
	}
}
public void setRefreshContainer(IContainer container) {
	this.refreshContainer = container;
}
/**
 * Returns an NL-enabled string describing this command.
 */
public abstract String toString();
}

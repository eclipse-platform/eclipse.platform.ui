package org.eclipse.ant.core.toolscripts;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.ant.core.AntPlugin;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.*;

/**
 * The ant builder can be added to the build spec of a project to add 
 * ant scripts to the incremental build process.  Note that there is only
 * ever one instance of AntBuilder per project, and the script to run is 
 * specified in the builder arguments.
 */
public class AntBuilder extends IncrementalProjectBuilder {
	public static final String ID = AntPlugin.PI_ANT + ".antBuilder";
/*
 * @see IncrementalProjectBuilder#build(int, Map, IProgressMonitor)
 */
protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
	ToolScript script = ToolScript.createFromArguments(args);
	if (script != null) {
		script.run(null, monitor);
	}
	return null;
}
}

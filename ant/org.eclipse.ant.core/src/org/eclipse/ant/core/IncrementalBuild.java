package org.eclipse.ant.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.util.HashMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant task which runs the platform's incremental build facilities.
 */

public class IncrementalBuild extends Task {
	private String builder;
	private String project;
	private int kind= IncrementalProjectBuilder.INCREMENTAL_BUILD;
	private HashMap args= new HashMap(5);

	public class Argument {
		private String name;

		public void setName(String value) {
			name= value;
		}
		public void setValue(String value) {
			if (value == null)
				args.remove(name);
			else
				args.put(name, value);
		}
	}

	public IncrementalBuild() {
		super();
	}
	public Argument createArgument() {
		return new Argument();
	}
	public void execute() throws BuildException {
		try {
			if (project == null) {
				ResourcesPlugin.getWorkspace().build(kind, null);
			} else {
				IProject target= ResourcesPlugin.getWorkspace().getRoot().getProject(project);
				if (builder == null)
					target.build(kind, null);
				else
					target.build(kind, builder, args, null);
			}
		} catch (CoreException e) {
			throw new BuildException(e);
		}
	}
	public void setBuilder(String value) {
		builder= value;
	}
	public void setKind(String value) {
		if ("full".equalsIgnoreCase(value))
			kind= IncrementalProjectBuilder.FULL_BUILD;
		else if ("auto".equalsIgnoreCase(value))
			kind= IncrementalProjectBuilder.AUTO_BUILD;
		else if ("incr".equalsIgnoreCase(value))
			kind= IncrementalProjectBuilder.INCREMENTAL_BUILD;
	}
	public void setProject(String value) {
		project= value;
	}
}

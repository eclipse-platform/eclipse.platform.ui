/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
{
 initargs:[],
 init:function() {
   this.counter = 0;
 },
 executeargs:["org.eclipse.core.resources.IWorkspace", "org.eclipse.core.runtime.IProgressMonitor"],
 execute:function(workspace, monitor) {
// 	var projectName = "Blue Note";
 	var projectName = "Boris Snaps";
//    var projectName = "Album" + (this.counter++);
	var project = workspace.getRoot().getProject(projectName);
	var projectDescription = workspace.newProjectDescription(projectName);
	workspace.run(function(monitor) {
	  project.create(projectDescription, monitor);
	  project.open(monitor);
	}, monitor);
 }
}


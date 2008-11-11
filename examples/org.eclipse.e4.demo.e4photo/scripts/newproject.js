{
 initargs:[],
 init:function() {
   this.counter = 0;
 },
 runargs:["org.eclipse.core.resources.IWorkspace", "org.eclipse.core.runtime.IProgressMonitor"],
 run:function(workspace, monitor) {
// 	var projectName = "Blue Note";
// 	var projectName = "Boris Snaps";
    var projectName = "Album" + (this.counter++);
	var project = workspace.getRoot().getProject(projectName);
	var projectDescription = workspace.newProjectDescription(projectName);
	workspace.run(function(monitor) {
	  project.create(projectDescription, monitor);
	  project.open(monitor);
	}, monitor);
 }
}


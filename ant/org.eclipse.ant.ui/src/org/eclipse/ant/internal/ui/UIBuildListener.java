package org.eclipse.ant.internal.ui;

import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

// TBD
// * Marker mechanism doesn't work for Locations other than
//   the original build file. This could pose problems for
//   ant tasks.
// * incremental task shows minimal feedback

public class UIBuildListener implements BuildListener {
	IProgressMonitor fMonitor;
	Target fTarget;
	Task fTask;
	IFile fBuildFile;
	
	public UIBuildListener(IProgressMonitor monitor, IFile file) {
		fMonitor= monitor;
		fBuildFile= file;
	}
	public void buildFinished(BuildEvent be){
		fMonitor.done();
		if (be.getException() != null) {
			handleBuildException(be.getException());
		}
	}
	public void buildStarted(BuildEvent be) {
		fMonitor.subTask("Build started...");
		removeMarkers();
	}
	private void checkCanceled() {
		if (fMonitor.isCanceled())
			throw new BuildCanceledException();
	}
	private void createMarker(IFile file, BuildException be) {
		try {
			int lineNumber= getLineFromLocation(be.getLocation());
			IMarker marker= file.createMarker(IMarker.PROBLEM);
			Map map= new HashMap();
			map.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
			map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
			map.put(IMarker.MESSAGE, be.getMessage());
			map.put(IMarker.LOCATION, Integer.toString(lineNumber));
			marker.setAttributes(map);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	private int getLineFromLocation(Location l) {
		String locstr= l.toString();
		int end= locstr.lastIndexOf(':');
		int start= locstr.lastIndexOf(':', end-1);
		String lstr= locstr.substring(start+1, end);
		try {
			return Integer.parseInt(lstr);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	private void handleBuildException(Throwable t) {
		System.out.println("BuildException: "+t);
		if (t instanceof BuildException) {
			BuildException bex= (BuildException)t;
			// the build exception has a location that
			// refers to a build file
			if (bex.getLocation() != Location.UNKNOWN_LOCATION) {
				createMarker(fBuildFile, bex);					
				System.out.println(bex.getLocation());
			}
		}
	}
	public void messageLogged(BuildEvent event) {
		checkCanceled();
        if (event.getPriority() <= Project.MSG_INFO) 
			System.out.println("> "+event.getMessage());
			
	}


	private void removeMarkers() {
		try {
			fBuildFile.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	public void targetFinished(BuildEvent be) {
		checkCanceled();
		if (be.getException() != null)
			handleBuildException(be.getException());
	}
	public void targetStarted(BuildEvent be) {
		checkCanceled();
		fTarget= be.getTarget();
		fMonitor.subTask("Target: \""+fTarget.getName()+"\" starting...");
	}
	public void taskFinished(BuildEvent be) {
		checkCanceled();
	}
	public void taskStarted(BuildEvent be) {
		checkCanceled();
		fTask= be.getTask();
		fMonitor.subTask("Target: \""+fTarget.getName()+"\" - "+fTask.getTaskName());
		if (be.getException() != null)
			handleBuildException(be.getException());
	}
}

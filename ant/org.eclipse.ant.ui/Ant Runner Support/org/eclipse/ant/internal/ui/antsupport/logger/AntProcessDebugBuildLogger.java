/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.antsupport.logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.eclipse.ant.internal.ui.antsupport.logger.debug.DebugMessageIds;
import org.eclipse.ant.internal.ui.antsupport.logger.debug.RemoteAntBreakpoint;
import org.eclipse.ant.internal.ui.debug.IAntDebugController;
import org.eclipse.ant.internal.ui.debug.model.AntDebugTarget;
import org.eclipse.ant.internal.ui.debug.model.AntThread;
import org.eclipse.ant.internal.ui.launchConfigurations.AntProcess;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IProcess;

public class AntProcessDebugBuildLogger extends AntProcessBuildLogger implements IAntDebugController {
	
	private boolean fStepOverSuspend= false;
	private boolean fStepIntoSuspend= false;
	private boolean fClientSuspend= false;
	private boolean fShouldSuspend= false;
	
	private Stack fTasks= new Stack();
	private Task fCurrentTask;
	private Task fStepOverTask;
	private Task fLastTaskFinished;
	
	private List fBreakpoints= null;
	
	private Map fProperties= null;
	
	private AntDebugTarget fTarget;
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		// TODO Auto-generated method stub
		super.buildFinished(event);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		super.buildStarted(event);
		IProcess process= getAntProcess(event.getProject().getUserProperty(AntProcess.ATTR_ANT_PROCESS_ID));
		ILaunch launch= process.getLaunch();
		//fTarget= new AntDebugTarget(launch, process, this);
		launch.addDebugTarget(fTarget);
		fTarget.buildStarted();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		// TODO Auto-generated method stub
		super.targetStarted(event);
	}
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		// TODO Auto-generated method stub
		super.targetFinished(event);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		super.taskFinished(event);
		fLastTaskFinished= (Task)fTasks.pop();
		fCurrentTask= null;
		waitIfSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
		super.taskStarted(event);
		fCurrentTask= event.getTask();
		fTasks.push(fCurrentTask);
		waitIfSuspended();
	}
	
	private void waitIfSuspended() {
		synchronized (this) {
			if (fCurrentTask != null) {
				String detail= null;
				boolean shouldSuspend= true;
				RemoteAntBreakpoint breakpoint= null;//breakpointAtLineNumber(fCurrentTask.getLocation());
				if (breakpoint != null) {
					detail= breakpoint.toMarshallString();
				} else if (fStepIntoSuspend) {
					detail= DebugMessageIds.STEP;
					fStepIntoSuspend= false;
				} else if (fStepOverSuspend) {
				    if (fLastTaskFinished == fStepOverTask) {
				        detail= DebugMessageIds.STEP;
				        fStepOverSuspend= false;
				        fStepOverTask= null;
				    } else {
				        shouldSuspend= false;
				    }
				} else if (fClientSuspend) {
					detail= DebugMessageIds.CLIENT_REQUEST;
					fClientSuspend= false;
				} else {
					shouldSuspend= false;
				}
				if (shouldSuspend) {
					//set suspended
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			} else if (fShouldSuspend) {
				try {
					fShouldSuspend= false;
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#resume()
	 */
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#suspend()
	 */
	public synchronized void suspend() {
		fClientSuspend= true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#stepInto()
	 */
	public synchronized void stepInto() {
		fStepIntoSuspend= true;
		this.notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#stepOver()
	 */
	public synchronized void stepOver() {
		fStepOverSuspend= true;
		fStepOverTask= fCurrentTask;
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#handleBreakpoint(org.eclipse.debug.core.model.IBreakpoint, boolean)
	 */
	public void handleBreakpoint(IBreakpoint breakpoint, boolean added) {
		if (added) {
			if (fBreakpoints == null) {
				fBreakpoints= new ArrayList();
			}
			fBreakpoints.add(breakpoint);
		} else {
			fBreakpoints.remove(breakpoint);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#getProperties()
	 */
	public void getProperties() {
		  StringBuffer propertiesRepresentation= new StringBuffer();
		  propertiesRepresentation.append(DebugMessageIds.PROPERTIES);
		  propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		  Map currentProperties= null;
		  if (!fTasks.isEmpty()) {
		  	currentProperties= ((Task)fTasks.peek()).getProject().getProperties();
		  	
		  	if (fProperties != null && currentProperties.size() == fProperties.size()) {
		  		//no new properties
		  		((AntThread) fTarget.getThreads()[0]).newProperties("no"); //$NON-NLS-1$
		  		return;
		  	}
		  	
		  	Iterator iter= currentProperties.keySet().iterator();
		  	String propertyName;
		  	String propertyValue;
		  	while (iter.hasNext()) {
		  		propertyName = (String) iter.next();
		  		if (propertyName.equals("line.separator")) { //$NON-NLS-1$
		  			continue;
		  		}
		  		if (fProperties == null || fProperties.get(propertyName) == null) { //new property
		  			propertiesRepresentation.append(propertyName.length());
		  			propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		  			propertiesRepresentation.append(propertyName);
		  			propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		  			propertyValue= (String) currentProperties.get(propertyName);
		  			propertiesRepresentation.append(propertyValue.length());
		  			propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		  			propertiesRepresentation.append(propertyValue);
		  			propertiesRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		  		}
		  	}
		  }
		  fProperties= currentProperties;
		  ((AntThread) fTarget.getThreads()[0]).newProperties(propertiesRepresentation.toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.debug.IAntDebugController#getStackFrames()
	 */
	public void getStackFrames() {
		StringBuffer stackRepresentation= new StringBuffer();
		stackRepresentation.append(DebugMessageIds.STACK);
		stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		
		for (int i = fTasks.size() - 1; i >= 0 ; i--) {
			Task task = (Task) fTasks.get(i);
			stackRepresentation.append(task.getOwningTarget().getName());
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			stackRepresentation.append(task.getTaskName());
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			
			Location location= task.getLocation();
			stackRepresentation.append(location.getFileName());
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
			stackRepresentation.append(location.getLineNumber());
			stackRepresentation.append(DebugMessageIds.MESSAGE_DELIMITER);
		}	
		 
		 ((AntThread) fTarget.getThreads()[0]).buildStack(stackRepresentation.toString());
	}
}

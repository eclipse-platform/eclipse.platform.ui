package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.debug.internal.core.DebugCoreMessages;

/**
 * A launch is the result of launching a debug session
 * and/or one or more system processes. This class provides
 * a public implementation of <code>ILaunch</code> for client
 * use.
 * <p>
 * Clients may instantiate this class. Clients may subclass this class.
 * All of the methods in this class that are part of the launcher interface are
 * final. Clients that subclass this class are not intended to change the behavior
 * or implementation of the provided methods. Subclassing is only intended
 * to add additional information to a specific launch. For example, a client that
 * implements a launch object representing a Java launch might store a classpath
 * with the launch.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see ILaunch
 * @see ILaunchManager
 */

public class Launch extends PlatformObject implements ILaunch {
	
	/**
	 * The target being debugged, or <code>null</code> if
	 * there is no debug target.
	 */
	private IDebugTarget fTarget= null;

	/**
	 * The element that was launched.
	 */
	private Object fElement= null;

	/**
	 * The launcher that was used.
	 */
	private ILauncher fLauncher= null;

	/**
	 * The system processes resulting from the launch.
	 */
	private IProcess[] fProcesses= null;

	/**
	 * The source locator to use in the debug session
	 * or <code>null</code> if not supported.
	 */
	private ISourceLocator fLocator= null;

	/**
	 * The mode this launch was launched in.
	 */
	private String fMode;
	 
	/**
	 * Collection of children.
	 */
	private List fChildren= null;
	
	/**
	 * Constructs a launch with the specified attributes. A launch must
	 * have at least one of a process or debug target.
	 *
	 * @param launcher the launcher that created this launch
	 * @param mode the mode of this launch - run or debug (constants
	 *  defined by <code>ILaunchManager</code>)
	 * @param launchedElement the element that was launched
	 * @param locator the source locator to use for this debug session, or
	 * 	<code>null</code> if not supported
	 * @param processes the processes created by this launch, empty
	 *    or <code>null</code> if none
	 * @param target the debug target created by this launch, or <code>null</code>
	 *	if none 
	 */
	public Launch(ILauncher launcher, String mode, Object launchedElement, ISourceLocator locator, IProcess[] processes, IDebugTarget target) {
		setLauncher(launcher);			
		setElement(launchedElement);
		setSourceLocator(locator);
		setProcesses(processes);
		setDebugTarget(target);
		setLaunchMode(mode);
		initialize();
	}
	
	/**
	 * Constructs a launch with the specified attributes. A launch must
	 * have at least one of a process or debug target.
	 *
	 * @param launchConfiguration the configuration that was launched
	 * @param mode the mode of this launch - run or debug (constants
	 *  defined by <code>ILaunchManager</code>)
	 * @param launchedElement the element that was launched
	 * @param locator the source locator to use for this debug session, or
	 * 	<code>null</code> if not supported
	 * @param processes the processes created by this launch, empty
	 *    or <code>null</code> if none
	 * @param target the debug target created by this launch, or <code>null</code>
	 *	if none 
	 */
	public Launch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator, IProcess[] processes, IDebugTarget target) {
		setLauncher(null);			
		setElement(launchConfiguration);
		setSourceLocator(locator);
		setProcesses(processes);
		setDebugTarget(target);
		setLaunchMode(mode);
		initialize();
	}	
	
	/**
	 * @see ITerminate@canTerminate()
	 */
	public final boolean canTerminate() {
		return !isTerminated();
	}

	/**
	 * @see ILaunch#getChildren()
	 */
	public final Object[] getChildren() {
		return fChildren.toArray();
	}

	/**
	 * @see ILaunch#getDebugTarget()
	 */
	public final IDebugTarget getDebugTarget() {
		return fTarget;
	}

	/**
	 * Sets the debug target associated with this
	 * launch.
	 * 
	 * @param debugTarget the debug target associated
	 *  with this launch, or <code>null</code> if none.
	 */
	private void setDebugTarget(IDebugTarget debugTarget) {
		fTarget = debugTarget;
	}
	
	/**
	 * @see ILaunch#getElement()
	 */
	public final Object getElement() {
		return fElement;
	}
	
	/**
	 * Sets the object that was launched
	 * 
	 * @param element the object that was launched
	 */
	private void setElement(Object element) {
		fElement = element;
	}	

	/**
	 * @see ILaunch#getLauncher()
	 */
	public final ILauncher getLauncher() {
		return fLauncher;
	}
	
	/**
	 * Sets the launcher that created
	 * this launch.
	 * 
	 * @param launcher the launcher that created
	 *  this launch
	 */
	private void setLauncher(ILauncher launcher) {
		fLauncher = launcher;
	}	

	/**
	 * @see ILaunch#getProcesses()
	 */
	public final IProcess[] getProcesses() {
		return fProcesses;
	}

	/**
	 * Sets the processes associated with this launch.
	 * 
	 * @param processes the processes associated with
	 *  this launch - <code>null</code> or empty if none.
	 */
	private void setProcesses(IProcess[] processes) {
		if (processes == null) {
			fProcesses= new IProcess[0];
		} else {
			fProcesses= processes;
		}
	}
	
	/**
	 * @see ILaunch#getSourceLocator()
	 */
	public final ISourceLocator getSourceLocator() {
		return fLocator;
	}
	
	/**
	 * Sets the source locator to use when locating
	 * source for the debug target associated with this
	 * launch.
	 * 
	 * @param sourceLocator the source locator for
	 *  this launch, or <code>null</code> if none.
	 */
	private void setSourceLocator(ISourceLocator sourceLocator) {
		fLocator = sourceLocator;
	}	

	/**
	 * Build the children collection for this launch -
	 * a collection of the processes and debug target
	 * associated with this launch.
	 */
	private final void initialize() {

		IProcess[] ps= getProcesses();
		if (ps != null) {
			fChildren= new ArrayList(ps.length + 1);
			for (int i= 0; i < ps.length; i++) {
				fChildren.add(ps[i]);
			}
		}
		if (getDebugTarget() != null) {
			if (fChildren == null) {
				fChildren= new ArrayList(1);
			}
			fChildren.add(getDebugTarget());
		}
		if (fChildren == null) {
			fChildren= Collections.EMPTY_LIST;
		}
	}

	/**
	 * @see ITerminate#isTerminated()
	 */
	public final boolean isTerminated() {
		IProcess[] ps = getProcesses();
		
		for (int i = 0; i < ps.length; i++) {
			if (!ps[i].isTerminated()) {
				return false;
			}
		}
		
		IDebugTarget target = getDebugTarget();
		if (target != null) {
			return target.isTerminated() || target.isDisconnected();
		}
		
		return true;
	}

	/**
	 * @see ITerminate#terminate()
	 */
	public final void terminate() throws DebugException {
		MultiStatus status= 
			new MultiStatus(DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), DebugException.REQUEST_FAILED, DebugCoreMessages.getString("Launch.terminate_failed"), null); //$NON-NLS-1$
		IProcess[] ps= getProcesses();
		
		// terminate the system processes
		if (ps != null) {
			for (int i = 0; i < ps.length; i++) {
				IProcess process = ps[i];
				if (process.canTerminate()) {
					try {
						process.terminate();
					} catch (DebugException e) {
						status.merge(e.getStatus());
					}
				}
			}
		}
		
		// disconnect debug target if it is still connected
		IDebugTarget target= getDebugTarget();
		if (target != null) {
			if (target.canTerminate()) {
				try {
					target.terminate();
				} catch (DebugException e) {
					status.merge(e.getStatus());
				}
			} else {
				if (target.canDisconnect()) {
					try {
						target.disconnect();
					} catch (DebugException de) {
						status.merge(de.getStatus());
					}
				}
			}
		}
		if (status.isOK())
			return;
		IStatus[] children= status.getChildren();
		if (children.length == 1) {
			throw new DebugException(children[0]);
		} else {
			throw new DebugException(status);
		}
	}

	/**
	 * @see ILaunch#getLaunchMode()
	 */
	public final String getLaunchMode() {
		return fMode;
	}
	
	/**
	 * Sets the mode in which this launch was 
	 * launched.
	 * 
	 * @param mode the mode in which this launch
	 *  was launched - one of the constants defined
	 *  by <code>ILaunchManager</code>.
	 */
	private void setLaunchMode(String mode) {
		fMode = mode;
	}
	
	/**
	 * @see ILaunch#getLaunchConfiguration()
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		if (getElement() instanceof ILaunchConfiguration) {
			return (ILaunchConfiguration)getElement();
		}
		return null;
	}

}



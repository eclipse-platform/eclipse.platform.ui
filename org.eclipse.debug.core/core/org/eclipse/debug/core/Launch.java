package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.PlatformObject;
import java.util.*;

/**
 * An implementation of <code>ILaunch</code>.
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
	 * Shared, immutable empty collection used for efficiency.
	 */
	private static List fgEmptyChildren= Collections.EMPTY_LIST;

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
	 * or <code>null</code> if not available.
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
	 * Constructs a launch based on the specified attributes. A launch must
	 * have at least one of a process or debug target.
	 *
	 * @param launcher the launcher that created this launch
	 * @param mode the mode of this launch - run or debug
	 * @param locator the source locator to use for this debug session, or
	 * 	<code>null</code> if not supported
	 * @param processes the processes created by this launch, empty
	 *    or <code>null</code> if none
	 * @param target the debug target created by this launch, or <code>null</code>
	 *	if none 
	 */
	public Launch(ILauncher launcher, String mode, Object launchedElement, ISourceLocator locator, IProcess[] processes, IDebugTarget target) {
		super();
		fLauncher= launcher;			
		fElement= launchedElement;
		fLocator= locator;
		if (processes == null) {
			fProcesses= new IProcess[0];
		} else {
			fProcesses= processes;
		}
		fTarget= target;
		fMode= mode;
		initialize();
	}
	
	/**
	 * @see ITerminate
	 */
	public final boolean canTerminate() {
		return !isTerminated();
	}

	/**
	 * @see ILaunch
	 */
	public final Object[] getChildren() {
		return fChildren.toArray();
	}

	/**
	 * @see ILaunch
	 */
	public final IDebugTarget getDebugTarget() {
		return fTarget;
	}

	/**
	 * @see ILaunch
	 */
	public final Object getElement() {
		return fElement;
	}

	/**
	 * @see ILaunch
	 */
	public final ILauncher getLauncher() {
		return fLauncher;
	}

	/**
	 * @see ILaunch
	 */
	public final IProcess[] getProcesses() {
		return fProcesses;
	}

	/**
	 * @see ILaunch
	 */
	public final ISourceLocator getSourceLocator() {
		return fLocator;
	}

	/**
	 * Build my children collection.
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
			fChildren= fgEmptyChildren;
		}
	}

	/**
	 * @see ITerminate
	 */
	public final boolean isTerminated() {
		Iterator children= fChildren.iterator();
		while (children.hasNext()) {
			ITerminate t= (ITerminate) children.next();
			if (!t.isTerminated()) {
				if (t instanceof IDisconnect) {
					IDisconnect d= (IDisconnect)t;
					if (!d.isDisconnected()) {
						return false;
					}
				} else {
					return false;
				}
			}

		}
		return true;
	}

	/**
	 * @see ITerminate
	 */
	public final void terminate() throws DebugException {
		MultiStatus status= new MultiStatus(DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), IDebugStatusConstants.REQUEST_FAILED, "Terminate failed", null);
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
	 * @see ILaunch
	 */
	public final String getLaunchMode() {
		return fMode;
	}
}



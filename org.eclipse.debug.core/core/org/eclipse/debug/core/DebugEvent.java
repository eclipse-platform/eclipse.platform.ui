package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.EventObject;

/**
 * A debug event describes an event in a program being debugged.
 * Debug model implementations are required to generate debug events as
 * specified by this class. Debug events also describe the creation and
 * termination of system processes.
 * <p>
 * The following list defines the events that are required to 
 * be generated for each element type, and when the event should be created.
 * The object that an event is associated
 * with is available from the event's <code>getSource</code> method. Creation
 * events are guaranteed to occur in a top down order - that is, parents are created
 * before children. Termination events are guaranteed to occur in a bottom up order -
 * that is, children before parents. However, termination events are not guaranteed
 * for all  elements that are created. That is, terminate events can be coalesced - a 
 * terminate event for a parent signals that all children have been terminated.
 * </p>
 * <p>
 * The generic <code>CHANGE</code> event can be fired at any time by any element.
 * Generally, a client of a debug model, such as as a UI, can get sufficient
 * information to update by listening/responding to the other event kinds. However,
 * if a debug model needs to inform clients of a change that is not specified
 * by create/terminate/suspend/resume, the <code>CHANGE</code> event may be used.
 * For example, generally, the only way a thread or any of its children can change
 * state between a suspend and resume operation, is if the thread or owning debug
 * target is termianted. However, if a debug model supports some other operation
 * that would allow a debug element to change state while suspended, the debug model
 * would fire a change event for that element. 
 * </p>
 * <ul>
 * <li><code>IDebugTarget</code>
 *	<ul>
 *	<li><code>CREATE</code> - a debug target has been created and is ready
 *		to begin a debug session.</li>
 *	<li><code>TERMINATE</code> - a debug target has terminated and the debug
 *		session has ended.</li>
 *	</ul>
 * </li>
 * <li><code>IThread</code>
 *	<ul>
 *	<li><code>CREATE</code> - a thread has been created in a debug target.</li>
 *	<li><code>TERMINATE</code> - a thread has ended.</li>
 *	<li><code>SUSPEND</code> - a thread has suspended. Event detail provides
 *		the reason for the suspension, or -1 if unknown:<ul>
 *		<li>STEP_END - a request to step has completed</li>
 *		<li>BREAKPOINT - a breakpoint has been hit</li>
 *		<li>CLIENT_REQUEST - a client request has caused the thread to suspend</li>
 *		</ul>
 *	</li>
 *	<li><code>RESUME</code> - a thread has resumed. Event detail provides
 *		the reason for the resume, or -1 if unknown:<ul>
 *		<li>STEP_START - a thread is being resumed because of a request to step</li>
 *		<li>CLIENT_REQUEST - a client request has caused the thread to be resumed</li>
 *		</ul>
 *	</li>
 *    </ul>
 * </li>
 * <li><code>IStackFrame</code> - no events are specified for stack frames.
 *	When a thread is suspended, it has children (stack frames). When a thread resumes,
 *	stack frames are unavailable.
 * </li>
 * <li><code>IVariable</code> - no events are specified for variables.
 *	When a thread is suspended, stack frames have variables. When a thread resumes,
 *	variables are unavailable.
 * </li>
 * <li><code>IValue</code> - no events are specified for values.
 * </li>
 * <li><code>IProcess</code>
 *	<ul>
 *	<li><code>CREATE</code> - a system process has been created.</li>
 *	<li><code>TERMINATE</code> - a system process has terminated.</li>
 *	</ul>
 * </li>
 * </ul>
 * <p>
 * Clients may instantiate this class. Clients are not intended to subclass this class.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public final class DebugEvent extends EventObject {
	
	/**
	 * Resume event.
	 */
	public static final int RESUME= 0x0001;

	/**
	 * Suspend event.
	 */
	public static final int SUSPEND= 0x0002;

	/**
	 * Create event.
	 */
	public static final int CREATE= 0x0004;

	/**
	 * Terminate event.
	 */
	public static final int TERMINATE= 0x0008;
	
	/**
	 * Change event.
	 */
	public static final int CHANGE= 0x0010;

	/**
	 * Step start detail. Indicates a thread was resumed by a step action.
	 */
	public static final int STEP_START= 0x0001;

	/**
	 * Step end detail. Indicates a thread was suspended due
	 * to the completion of a step action.
	 */
	public static final int STEP_END= 0x0002;
	
	/**
	 * Breakpoint detail. Indicates a thread was suspended by
	 * a breakpoint.
	 */
	public static final int BREAKPOINT= 0x0004;
	
	/**
	 * Client request detail. Indicates a thread was suspended due
	 * to a client request.
	 */
	public static final int CLIENT_REQUEST= 0x0008;
	
	/**
	 * The kind of event - one of the kind constants defined by
	 * this class.
	 */
	private int fKind= 0;

	/**
	 * The detail of the event - one of the detail constants defined by
	 * this class.
	 */
	private int fDetail= -1;
	/**
	 * Constructs a new debug event of the given kind with a detail code of -1.
	 *
	 * @param eventSource the object that generated the event
	 * @param kind the kind of debug envent (one of the
	 *	constants in <code>IDebugEventConstants</code>)
	 */
	public DebugEvent(Object eventSource, int kind) {
		this(eventSource, kind, -1);
	}

	/**
	 * Constructs a new debug event of the given kind with the given detail.
	 *
	 * @param eventSource the object that generated the event
	 * @param kind the kind of debug envent (one of the
	 *	kind constants defined by this class)
	 * @param detail extra information about the event (one of the
	 *	detail constants defined by this class), or -1 if
	 *    unspecified
	 */
	public DebugEvent(Object eventSource, int kind, int detail) {
		super(eventSource);
		if ((kind & (RESUME | SUSPEND | CREATE | TERMINATE | CHANGE)) == 0)
			throw new IllegalArgumentException("kind is not one of the allowed constants, see IDebugEventConstants");
		if (detail != -1 && (detail & (STEP_END | STEP_START | BREAKPOINT | CLIENT_REQUEST)) == 0)
			throw new IllegalArgumentException("detail is not one of the allowed constants, see IDebugEventConstants");
		fKind= kind;
		fDetail= detail;
	}

	/**
	 * Returns a constant describing extra detail about the event - one
	 * of the detail constants defined by this class, or -1 if unspecified.
	 *
	 * @return the detail code
	 */
	public int getDetail() {
		return fDetail;
	}

	/**
	 * Returns this event's kind - one of the kind constants defined by this class.
	 */
	public int getKind() {
		return fKind;
	}

}


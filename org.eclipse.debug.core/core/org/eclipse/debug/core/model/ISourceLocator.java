package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IStackFrame;

/**
 * A source locator locates source elements for stack frames. Each
 * debug session launch (optionally) specifies a source locator which is
 * used to locate source for that debug session. If a launch does not
 * provide a source locator, source cannot be displayed.
 * Abstraction of source lookup allows clients to hide implementation
 * details of source location and representation.
 * <p>
 * Generally, an implementor of a debug model will also implement launchers
 * and source locators that work together as a whole. That is, the implementation
 * if a source locator will have knowledge of how to locate a source element
 * for a stack frame. For example, a Java stack frame could define API which
 * specifies a source file name. A Java source locator would use this information
 * to locate the associated file in the workspace.
 * </p>
 * <p>
 * Source is displayed by the debug UI plug-in. The debug UI uses a source locator
 * to resolve an object representing the source for a stack frame, and then uses
 * a debug model presentation to determine the editor and editor input to use to
 * display the actual source in an editor.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.ILaunch
 * @see IStackFrame 
 * @see org.eclipse.debug.ui.IDebugModelPresentation
 */
public interface ISourceLocator {
	
	/**
	 * Returns a source element that corresponds to the given stack frame, or
	 * <code>null</code> if a source element could not be located. The object returned
	 * by this method will be used by the debug UI plug-in to display source.
	 * The debug UI uses the debug model presentation associated
	 * with the given stack frame's debug model to translate a source object into an
	 * {editor input, editor id} pair in which to display source.
	 * <p>
	 * For example, a java source locator could return an object representing a
	 * compilation unit or class file. The java debug model presentation would
	 * then be responsible for providing an editor input and editor id for each
	 * compilation unit and class file such that the debug UI could display source.
	 * </p>
	 *
	 * @param stackFrame the stack frame for which to locate source
	 * @return an object representing a source element. 
	 */
	 Object getSourceElement(IStackFrame stackFrame);

}



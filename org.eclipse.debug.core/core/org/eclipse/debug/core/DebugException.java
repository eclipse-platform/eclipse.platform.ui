package org.eclipse.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * A checked exception representing a failure.
 * <p>
 * Clients may instantiate this class. Clients are not intended to subclass this class.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IStatus
 */
public class DebugException extends CoreException {	
	
	/**
	 * Constructs a new debug exception with the given status object.
	 *
	 * @param status the status object to be associated with this exception
	 * @see IStatus
	 */
	public DebugException(IStatus status) {
		super(status);
	}

}

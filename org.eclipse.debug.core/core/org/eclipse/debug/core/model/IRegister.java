package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
 
/**
 * A register is a special kind of variable that is contained
 * in a register group. Each register has a name and a value.
 * Not all debug architectures provide access to registers.
 * 
 * @since 2.0
 */
public interface IRegister extends IVariable {
	
	/**
	 * Returns the register group this register is contained in.
	 * 
	 * @return the register group this register is contained in
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public IRegisterGroup getRegisterGroup() throws DebugException; 

}


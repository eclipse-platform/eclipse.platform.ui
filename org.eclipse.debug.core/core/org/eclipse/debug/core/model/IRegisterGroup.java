package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;
 
/**
 * A register group is a group of registers that are
 * assigned to a stack frame. Some debug architectures
 * provide access to registers, and registers are often
 * grouped logically. For example, a floating point
 * register group. 
 * 
 * @since 2.0
 */
public interface IRegisterGroup extends IDebugElement {
	

	/**
	 * Returns the name of this register group.
	 * 
	 * @return this register group's name
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public String getName() throws DebugException;
	
	/**
	 * Returns the registers in this register group.
	 * 
	 * @return the registers in this register group
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public IRegister[] getRegisters() throws DebugException;
	
	/**
	 * Returns whether this register group currently contains any registers.
	 * 
	 * @return whether this register group currently contains any registers
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public boolean hasRegisters() throws DebugException;	

}


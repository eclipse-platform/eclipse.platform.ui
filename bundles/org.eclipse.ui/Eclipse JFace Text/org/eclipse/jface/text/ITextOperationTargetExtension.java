package org.eclipse.jface.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Extension interface to <code>ITextOperationTarget</code>.
 */
public interface ITextOperationTargetExtension {
	
	/**
	 * Enables/Disabled the given text operation.
	 * 
	 * @param operation the operation to enable/disable
	 * @param enable <code>true</code> to enable the operation otherwise <code>false</code>
	 */
	void enableOperation(int operation, boolean enable);
}


/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.ui;

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * @since 2.0
 */
public interface IWorkingSetRegistry {
	public static final String CHANGE_WORKING_SET_ADD = "workingSetAdd";	//$NON-NLS-1$
	public static final String CHANGE_WORKING_SET_REMOVE = "workingSetRemove";	//$NON-NLS-1$
	 
	public void addPropertyChangeListener(IPropertyChangeListener listener);
	public IWorkingSetDialog getWorkingSetDialog(Class workingSetClass);
	public IWorkingSet getWorkingSet(String name);
	public IWorkingSet[] getWorkingSets();
	public void removePropertyChangeListener(IPropertyChangeListener listener);
}

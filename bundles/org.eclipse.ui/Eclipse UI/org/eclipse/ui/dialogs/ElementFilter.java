package org.eclipse.ui.dialogs;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The ElementFilter is a interface that defines 
 * the api for filtering the current selection of 
 * a ResourceTreeAndListGroup in order to find a 
 * subset to update as the result of a type filtering
 */

public interface ElementFilter {

	public void filterElements(Collection elements, IProgressMonitor monitor)
		throws InterruptedException;
		
	public void filterElements(Object[] elements, IProgressMonitor monitor)
		throws InterruptedException;

}
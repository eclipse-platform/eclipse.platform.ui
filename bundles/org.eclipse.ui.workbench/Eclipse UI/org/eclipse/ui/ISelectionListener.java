package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.ISelection;

/**
 * Interface for listening to selection changes.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see ISelectionService#addSelectionListener
 */
public interface ISelectionListener {
/**
 * Notifies this listener that the selection has changed.
 * <p>
 * This method is called when the selection changes from one to a 
 * <code>non-null</code> value, but not when the selection changes to 
 * <code>null</code>.  If there is a requirement to be notified in the latter 
 * scenario, implement <code>INullSelectionListener</code>.  The event will
 * be posted through this method.
 * </p>
 *
 * @param part the workbench part containing the selection
 * @param selection the current selection.  This may be <code>null</code> 
 * 		if <code>INullSelectionListener</code> is implemented.
 */
public void selectionChanged(IWorkbenchPart part, ISelection selection);
}

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
 *
 * @param part the workbench part containing the selection
 * @param selection the new selection, or <code>null</code> if none
 */
public void selectionChanged(IWorkbenchPart part, ISelection selection);
}

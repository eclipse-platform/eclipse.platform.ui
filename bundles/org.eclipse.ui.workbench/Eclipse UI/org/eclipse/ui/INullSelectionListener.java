package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Interface for listening to null selection changes.
 * <p>
 * This interface should be implemented by <code>ISelectionListener</code>
 * objects which wish to be notified when the selection becomes 
 * <code>null</code>.  It has no methods.  It simply indicates the 
 * desire to receive null selection events through the existing 
 * <code>selectionChanged</code> method.
 * Either the part or the selection may be <code>null</code>.
 * </p>
 *
 * @see ISelectionListener#selectionChanged
 * @see IActionDelegate#selectionChanged
 * 
 * @since 2.0
 */
public interface INullSelectionListener {
}

package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.ISelection;

/**
 * Interface for views which support reveal and select.
 * <p>
 * This interface may be implemented by subclasses of <code>ViewPart</code>.
 * This interface is commonly used by a New wizard to reveal and select a
 * resource in a workbench part which it has just created.
 * </p>
 *
 * @see IView
 * @see ViewPart
 */
public interface ISetSelectionTarget {
/**
 * Reveals and selects the given element within this target view.
 *
 * @param selection the new element to select
 */
public void selectReveal(ISelection selection);
}

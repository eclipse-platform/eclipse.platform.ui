package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

/**
 * Print out selection listener events.
 */
public class TestSelectionListener implements org.eclipse.ui.ISelectionListener {
/**
 * TestSelectionListener constructor comment.
 */
public TestSelectionListener() {
	super();
}
/**
 * Notifies this listener that the selection has changed.
 *
 * @param part the workbench part containing the selection
 * @param selection the new selection, or <code>null</code> if none
 */
public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	System.out.println("selectionChanged(" + selection + ")");//$NON-NLS-2$//$NON-NLS-1$
}
}

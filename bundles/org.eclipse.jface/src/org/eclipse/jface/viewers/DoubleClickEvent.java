package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.EventObject;

import org.eclipse.jface.util.Assert;

/**
 * Event object describing a double-click. The source of these
 * events is a viewer.
 *
 * @see IDoubleClickListener
 */
public class DoubleClickEvent extends EventObject {

	/**
	 * The selection.
	 */
	protected ISelection selection;
/**
 * Creates a new event for the given source and selection.
 *
 * @param source the viewer
 * @param selection the selection
 */
public DoubleClickEvent(Viewer source, ISelection selection) {
	super(source);
	Assert.isNotNull(selection);
	this.selection = selection;
}
/**
 * Returns the selection.
 *
 * @return the selection
 */
public ISelection getSelection() {
	return selection;
}
/**
 * Returns the viewer that is the source of this event.
 *
 * @return the originating viewer
 */
public Viewer getViewer() {
	return (Viewer) getSource();
}
}

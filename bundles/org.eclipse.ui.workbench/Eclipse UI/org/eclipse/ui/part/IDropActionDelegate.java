package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Interface for actions supplied by extensions to the
 * org.eclipse.ui.dropActions extension point.
 */
public interface IDropActionDelegate {
/**
 * Runs the drop action on the given source and target.
 * @param source The object that is being dragged.
 * @param target The object that the drop is occurring over.
 * @return boolean True if the drop was successful, and false otherwise.
 */
public boolean run(Object source, Object target);
}

package org.eclipse.ui.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
/**
 * For validating selections in some selection dialogs.
 * <p>
 * Clients should implement this interface to define specialized selection
 * validators. 
 * </p>
 *
 * @see ContainerSelectionDialog
 */
public interface ISelectionValidator {
/**
 * Returns a string indicating whether the given selection is valid. If the
 * result is <code>null</code>, the selection is considered to be valid; if the result is
 * non-empty, it contains the error message to be displayed to the user.
 *
 * @param selection the selection to be validated
 * @return the error message, or <code>null</code> indicating
 *	that the value is valid
 */
public String isValid(Object selection);
}

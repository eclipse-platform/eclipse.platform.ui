package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
public interface IInstallable {
/**
 * Returns the name of the directory of this component in install/components
 * The format is compid_version
 *
 *
 * @return the component's directory name in install/components
 */
public String getDirName();
/**
 * Returns a displayable label (name) for this Product or component.
 * Returns the empty string if no label for this Product or component
 * is specified in its install manifest file.
 * <p> Note that any translation specified in the install manifest
 * file is automatically applied. 
 * </p>
 *
 * @see #getResourceString 
 *
 * @return a displayable string label for this Product or component,
 *    possibly the empty string
 */
public String getLabel();
}

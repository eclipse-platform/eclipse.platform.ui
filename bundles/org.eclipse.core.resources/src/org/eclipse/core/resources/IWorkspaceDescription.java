package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A workspace description represents the workspace preferences. It can be
 * used to query the current preferences and set new ones.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkspace#getDescription
 * @see IWorkspace#setDescription
 */
public interface IWorkspaceDescription {
/**
 * Returns the order in which projects in the workspace should be built.
 * The returned value is <code>null</code> if the workspace's default build
 * order is being used.
 *
 * @return the names of projects in the order they will be built, 
 *   or <code>null</code> if the default build order should be used
 * @see #setBuildOrder
 */
public String[] getBuildOrder();
/**
 * Returns the maximum length of time, in milliseconds, a file state should be 
 * kept in the local history.  
 *
 * @return the maximum time a file state should be kept in the local history
 *   represented in milliseconds
 * @see #setFileStateLongevity
 */
public long getFileStateLongevity();
/**
 * Returns the maximum number of states per file that can be stored in the local history.
 *
 * @return the maximum number of states per file that can be stored in the local history
 * @see #setMaxFileStates
 */
public int getMaxFileStates();
/**
 * Returns the maximum permited size of a file, in bytes, to be stored in the
 * local history.
 *
 * @return the maximum permited size of a file to be stored in the local history
 * @see #setMaxFileStateSize
 */
public long getMaxFileStateSize();
/**
 * Returns whether this workspace performs auto-builds.
 *
 * @return <code>true</code> if auto-building is on, otherwise
 *		<code>false</code>
 * @see #setAutoBuilding
 */
public boolean isAutoBuilding();
/**
 * Records whether this workspace performs auto-builds.
 * <p>
 * When auto-build is on, any changes made to a project and its
 * resources automatically triggers an incremental build of that
 * project. If resources in several projects are changed within the
 * scope of a workspace runnable, the affected projects are auto-built
 * in no particular order.
 * </p>
 * <p>
 * Users must call <code>IWorkspace.setDescription</code> before changes 
 * made to this description take effect.
 * </p>
 *
 * @param value <code>true</code> to turn on auto-building,
 *  and <code>false</code> to turn it off
 * @see IWorkspace#setDescription
 * @see #isAutoBuilding
 */
public void setAutoBuilding(boolean value);
/**
 * Sets the order in which projects in the workspace should be built.
 * Projects not named in this list are built in a default order defined
 * by the workspace.  Set this value to <code>null</code> to use the
 * default ordering for all projects.  Projects not named in the list are 
 * built in unspecified order after all ordered projects.
 * <p>
 * Users must call <code>IWorkspace.setDescription</code> before changes 
 * made to this description take effect.
 * </p>
 *
 * @param value the names of projects in the order in which they are built,
 *   or <code>null</code> to use the workspace's default order for all projects
 * @see IWorkspace#setDescription
 * @see #getBuildOrder
 */
public void setBuildOrder(String[] value);
/**
 * Sets the maximum time, in milliseconds, a file state should be kept in the
 * local history.
 * <p>
 * Users must call <code>IWorkspace.setDescription</code> before changes 
 * made to this description take effect.
 * </p>
 *
 * @param time the maximum number of milliseconds a file state should be 
 * 		kept in the local history
 * @see IWorkspace#setDescription
 * @see #getFileStateLongevity
 */
public void setFileStateLongevity(long time);
/**
 * Sets the maximum number of states per file that can be stored in the local history.
 * If the maximum number is reached, older states are removed in favor of
 * new ones.
 * <p>
 * Users must call <code>IWorkspace.setDescription</code> before changes 
 * made to this description take effect.
 * </p>
 *
 * @param number the maximum number of states per file that can be stored in the local history
 * @see IWorkspace#setDescription
 * @see #getMaxFileStates
 */
public void setMaxFileStates(int number);
/**
 * Sets the maximum permited size of a file, in bytes,  to be stored in the
 * local history.
 * <p>
 * Users must call <code>IWorkspace.setDescription</code> before changes 
 * made to this description take effect.
 * </p>
 *
 * @param size the maximum permited size of a file to be stored in the local history
 * @see IWorkspace#setDescription
 * @see #getMaxFileStateSize
 */
public void setMaxFileStateSize(long size);
}

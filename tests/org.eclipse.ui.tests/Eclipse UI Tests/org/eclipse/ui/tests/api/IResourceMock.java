package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class IResourceMock implements IResource {

	/**
	 * @see IResource#accept(IResourceVisitor)
	 */
	public void accept(IResourceVisitor arg0) throws CoreException {
	}

	/**
	 * @see IResource#accept(IResourceVisitor, int, boolean)
	 */
	public void accept(IResourceVisitor arg0, int arg1, boolean arg2)
		throws CoreException {
	}

	/**
	 * @see IResource#clearHistory(IProgressMonitor)
	 */
	public void clearHistory(IProgressMonitor arg0) throws CoreException {
	}

	/**
	 * @see IResource#copy(IProjectDescription, boolean, IProgressMonitor)
	 */
	public void copy(IProjectDescription arg0, boolean arg1, IProgressMonitor arg2)
		throws CoreException {
	}

	/**
	 * @see IResource#copy(IPath, boolean, IProgressMonitor)
	 */
	public void copy(IPath arg0, boolean arg1, IProgressMonitor arg2)
		throws CoreException {
	}

	/**
	 * @see IResource#createMarker(String)
	 */
	public IMarker createMarker(String arg0) throws CoreException {
		return null;
	}

	/**
	 * @see IResource#delete(boolean, IProgressMonitor)
	 */
	public void delete(boolean arg0, IProgressMonitor arg1) throws CoreException {
	}

	/**
	 * @see IResource#deleteMarkers(String, boolean, int)
	 */
	public void deleteMarkers(String arg0, boolean arg1, int arg2)
		throws CoreException {
	}

	/**
	 * @see IResource#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * @see IResource#findMarker(long)
	 */
	public IMarker findMarker(long arg0) throws CoreException {
		return null;
	}

	/**
	 * @see IResource#findMarkers(String, boolean, int)
	 */
	public IMarker[] findMarkers(String arg0, boolean arg1, int arg2)
		throws CoreException {
		return null;
	}

	/**
	 * @see IResource#getFileExtension()
	 */
	public String getFileExtension() {
		return null;
	}

	/**
	 * @see IResource#getFullPath()
	 */
	public IPath getFullPath() {
		return null;
	}

	/**
	 * @see IResource#getLocation()
	 */
	public IPath getLocation() {
		return null;
	}

	/**
	 * @see IResource#getMarker(long)
	 */
	public IMarker getMarker(long arg0) {
		return null;
	}

	/**
	 * @see IResource#getModificationStamp()
	 */
	public long getModificationStamp() {
		return 0;
	}

	/**
	 * @see IResource#getName()
	 */
	public String getName() {
		return null;
	}

	/**
	 * @see IResource#getParent()
	 */
	public IContainer getParent() {
		return null;
	}

	/**
	 * @see IResource#getPersistentProperty(QualifiedName)
	 */
	public String getPersistentProperty(QualifiedName arg0) throws CoreException {
		return null;
	}

	/**
	 * @see IResource#getProject()
	 */
	public IProject getProject() {
		return null;
	}

	/**
	 * @see IResource#getProjectRelativePath()
	 */
	public IPath getProjectRelativePath() {
		return null;
	}

	/**
	 * @see IResource#getSessionProperty(QualifiedName)
	 */
	public Object getSessionProperty(QualifiedName arg0) throws CoreException {
		return null;
	}

	/**
	 * @see IResource#getType()
	 */
	public int getType() {
		return 0;
	}

	/**
	 * @see IResource#getWorkspace()
	 */
	public IWorkspace getWorkspace() {
		return null;
	}

	/**
	 * @see IResource#isAccessible()
	 */
	public boolean isAccessible() {
		return false;
	}

	/**
	 * @see IResource#isLocal(int)
	 */
	public boolean isLocal(int arg0) {
		return false;
	}

	/**
	 * @see IResource#isPhantom()
	 */
	public boolean isPhantom() {
		return false;
	}

	/**
	 * @see IResource#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * @see IResource#move(IProjectDescription, boolean, boolean, IProgressMonitor)
	 */
	public void move(
		IProjectDescription arg0,
		boolean arg1,
		boolean arg2,
		IProgressMonitor arg3)
		throws CoreException {
	}

	/**
	 * @see IResource#move(IPath, boolean, IProgressMonitor)
	 */
	public void move(IPath arg0, boolean arg1, IProgressMonitor arg2)
		throws CoreException {
	}

	/**
	 * @see IResource#refreshLocal(int, IProgressMonitor)
	 */
	public void refreshLocal(int arg0, IProgressMonitor arg1)
		throws CoreException {
	}

	/**
	 * @see IResource#setLocal(boolean, int, IProgressMonitor)
	 */
	public void setLocal(boolean arg0, int arg1, IProgressMonitor arg2)
		throws CoreException {
	}

	/**
	 * @see IResource#setPersistentProperty(QualifiedName, String)
	 */
	public void setPersistentProperty(QualifiedName arg0, String arg1)
		throws CoreException {
	}

	/**
	 * @see IResource#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean arg0) {
	}

	/**
	 * @see IResource#setSessionProperty(QualifiedName, Object)
	 */
	public void setSessionProperty(QualifiedName arg0, Object arg1)
		throws CoreException {
	}

	/**
	 * @see IResource#touch(IProgressMonitor)
	 */
	public void touch(IProgressMonitor arg0) throws CoreException {
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return null;
	}

}


package org.eclipse.ui.tests.api;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class IFileMock extends IResourceMock implements IFile {

	/**
	 * @see IFile#appendContents(InputStream, boolean, boolean, IProgressMonitor)
	 */
	public void appendContents(	InputStream arg0, boolean arg1, boolean arg2, IProgressMonitor arg3) throws CoreException 
	{
	}

	/**
	 * @see IFile#create(InputStream, boolean, IProgressMonitor)
	 */
	public void create(InputStream arg0, boolean arg1, IProgressMonitor arg2) throws CoreException 
	{
	}

	/**
	 * @see IFile#delete(boolean, boolean, IProgressMonitor)
	 */
	public void delete(boolean arg0, boolean arg1, IProgressMonitor arg2) throws CoreException 
	{
	}

	/**
	 * @see IFile#getContents()
	 */
	public InputStream getContents() throws CoreException {
		return null;
	}

	/**
	 * @see IFile#getContents(boolean)
	 */
	public InputStream getContents(boolean arg0) throws CoreException 
	{
		return null;
	}

	/**
	 * @see IFile#getHistory(IProgressMonitor)
	 */
	public IFileState[] getHistory(IProgressMonitor arg0) throws CoreException 
	{
		return null;
	}

	/**
	 * @see IFile#move(IPath, boolean, boolean, IProgressMonitor)
	 */
	public void move(IPath arg0, boolean arg1, boolean arg2, IProgressMonitor arg3)	throws CoreException 
	{
	}

	/**
	 * @see IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)
	 */
	public void setContents( InputStream arg0, boolean arg1, boolean arg2, IProgressMonitor arg3 ) throws CoreException 
	{
	}

	/**
	 * @see IFile#setContents(IFileState, boolean, boolean, IProgressMonitor)
	 */
	public void setContents( IFileState arg0, boolean arg1,	boolean arg2, IProgressMonitor arg3 ) throws CoreException 
	{
	}
}


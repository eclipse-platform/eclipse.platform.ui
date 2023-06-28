/*******************************************************************************
 * Copyright (c) 2013, 2019 Pivotal Software, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0s
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.tests;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class MockResource implements IResource {

	IPath fullPath;

	public MockResource(String pathStr) {
		this.fullPath = IPath.fromOSString(pathStr);
	}

	@Override
	public String toString() {
		return fullPath.toString();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		throw new Error("Not implemented");
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		throw new Error("Not implemented");
	}

	@Override
	public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void accept(IResourceVisitor visitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void accept(IResourceVisitor visitor, int depth,
			boolean includePhantoms) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void accept(IResourceVisitor visitor, int depth, int memberFlags)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void clearHistory(IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void copy(IPath destination, boolean force, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void copy(IPath destination, int updateFlags,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void copy(IProjectDescription description, boolean force,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void copy(IProjectDescription description, int updateFlags,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public IMarker createMarker(String type) throws CoreException {
		// TODO Auto-generated method stub
		throw new Error("Not implemented");
	}

	@Override
	public IResourceProxy createProxy() {
		// TODO Auto-generated method stub
		throw new Error("Not implemented");
	}

	@Override
	public void delete(boolean force, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void delete(int updateFlags, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void deleteMarkers(String type, boolean includeSubtypes, int depth)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public IMarker findMarker(long id) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public int findMaxProblemSeverity(String type, boolean includeSubtypes,
			int depth) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public String getFileExtension() {
		return fullPath.getFileExtension();
	}

	@Override
	public IPath getFullPath() {
		return fullPath;
	}

	@Override
	public long getLocalTimeStamp() {
		throw new Error("Not implemented");
	}

	@Override
	public IPath getLocation() {
		throw new Error("Not implemented");
	}

	@Override
	public URI getLocationURI() {
		throw new Error("Not implemented");
	}

	@Override
	public IMarker getMarker(long id) {
		throw new Error("Not implemented");
	}

	@Override
	public long getModificationStamp() {
		throw new Error("Not implemented");
	}

	@Override
	public String getName() {
		String name = fullPath.lastSegment();
		if (name!=null) {
			return name;
		}
		return "";
	}

	@Override
	public IPathVariableManager getPathVariableManager() {
		throw new Error("Not implemented");
	}

	@Override
	public IContainer getParent() {
		throw new Error("Not implemented");
	}

	@Override
	public Map<QualifiedName, String> getPersistentProperties()
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPersistentProperty(QualifiedName key) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public IProject getProject() {
		throw new Error("Not implemented");
	}

	@Override
	public IPath getProjectRelativePath() {
		throw new Error("Not implemented");
	}

	@Override
	public IPath getRawLocation() {
		throw new Error("Not implemented");
	}

	@Override
	public URI getRawLocationURI() {
		throw new Error("Not implemented");
	}

	@Override
	public ResourceAttributes getResourceAttributes() {
		throw new Error("Not implemented");
	}

	@Override
	public Map<QualifiedName, Object> getSessionProperties()
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public Object getSessionProperty(QualifiedName key) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public int getType() {
		throw new Error("Not implemented");
	}

	@Override
	public IWorkspace getWorkspace() {
		throw new Error("Not implemented");
	}

	@Override
	public boolean isAccessible() {
		return true;
	}

	@Override
	public boolean isDerived() {
		return false;
	}

	@Override
	public boolean isDerived(int options) {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isHidden(int options) {
		return false;
	}

	@Override
	public boolean isLinked() {
		return false;
	}

	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public boolean isLinked(int options) {
		return false;
	}

	@Override
	public boolean isLocal(int depth) {
		return false;
	}

	@Override
	public boolean isPhantom() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public boolean isSynchronized(int depth) {
		throw new Error("Not implemented");
	}

	@Override
	public boolean isTeamPrivateMember() {
		throw new Error("Not implemented");
	}

	@Override
	public boolean isTeamPrivateMember(int options) {
		throw new Error("Not implemented");
	}

	@Override
	public void move(IPath destination, boolean force, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void move(IPath destination, int updateFlags,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void move(IProjectDescription description, boolean force,
			boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void move(IProjectDescription description, int updateFlags,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void refreshLocal(int depth, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void revertModificationStamp(long value) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void setDerived(boolean isDerived) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void setDerived(boolean isDerived, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void setHidden(boolean isHidden) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void setLocal(boolean flag, int depth, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public long setLocalTimeStamp(long value) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void setPersistentProperty(QualifiedName key, String value)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		throw new Error("Not implemented");
	}

	@Override
	public void setResourceAttributes(ResourceAttributes attributes)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void setSessionProperty(QualifiedName key, Object value)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void setTeamPrivateMember(boolean isTeamPrivate)
			throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	@Override
	public IMarker createMarker(String type, Map<String, ? extends Object> attributes) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}

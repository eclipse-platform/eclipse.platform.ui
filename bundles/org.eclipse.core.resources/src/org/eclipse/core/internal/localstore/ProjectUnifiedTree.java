package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.internal.resources.Project;
import java.io.File;
import java.util.*;
/**
 * This tree just visits the project members and not the project itself. It creates one
 * UnifiedTree for each of the project existing (in workspace) or inexisting (in file system)
 * members. If you call the accept method with D_ZERO as the depth nothing happens.
 */
public class ProjectUnifiedTree {
	protected IProject project;
public ProjectUnifiedTree(IProject project) {
	this.project = project;
}
public void accept(IUnifiedTreeVisitor visitor) throws CoreException {
	accept(visitor, IResource.DEPTH_INFINITE);
}
public void accept(IUnifiedTreeVisitor visitor, int depth) throws CoreException {
	int newDepth = depth;
	switch (depth) {
		case IResource.DEPTH_ZERO :
			return;
		case IResource.DEPTH_ONE :
			newDepth = IResource.DEPTH_ZERO;
	}
	HashMap children = new HashMap(20);
	/* Find children. The order of these method calls should not be changed. */
	findElementsInWorkspace(children);
	findElementsInFileSystem(children);
	UnifiedTree tree = new UnifiedTree();
	/* create one UnifiedTree for each element */
	for (Iterator i = children.values().iterator(); i.hasNext();) {
		IResource target = (IResource) i.next();
		tree.setRoot(target);
		tree.accept(visitor, newDepth);
	}
}
protected void findElementsInFileSystem(HashMap children) throws CoreException {
	IPath location = project.getLocation();
	File file = location.toFile();
	String[] list = file.list();
	if (list == null)
		return;
	for (int i = 0; i < list.length; i++) {
		String name = list[i];
		/* skip existing child */
		if (children.containsKey(name))
			continue;
		/* add child */
		File target = new File(file, name);
		IResource child;
		if (target.isFile())
			child = project.getFile(name);
		else
			child = project.getFolder(name);
		children.put(name, child);
	}
}
protected void findElementsInWorkspace(HashMap children) throws CoreException {
	IResource[] members = project.members();
	for (int i = 0; i < members.length; i++) {
		IResource child = members[i];
		children.put(child.getName(), child);
	}
}
}

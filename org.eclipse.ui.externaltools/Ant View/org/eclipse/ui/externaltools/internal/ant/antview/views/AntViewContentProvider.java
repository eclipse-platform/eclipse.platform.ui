/**********************************************************************
Copyright (c) 2002 Roscoe Rush, IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
	Roscoe Rush - Initial implementation
    IBM Corporation - Maintenance
    Kevin Bedell - restoreTargetVector() patch
*********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.antview.views;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.externaltools.internal.ant.antview.core.IAntViewConstants;
import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;
import org.eclipse.ui.externaltools.internal.ant.antview.preferences.Preferences;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.ElementNode;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.ErrorNode;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.ProjectErrorNode;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.TargetNode;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.TreeNode;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;

public class AntViewContentProvider implements IStructuredContentProvider, ITreeContentProvider, IAntViewConstants, IResourceChangeListener {

	public static final String SEP_KEYVAL = "|";
	public static final String SEP_REC = ";";

	private TreeNode treeRoot = null;

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this);
		saveTargetVector();
	}
	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object parent) {
		if (parent.equals(ResourcesPlugin.getWorkspace())) {
			if (getTreeRoot() == null)
				initialize();
			return getChildren(getTreeRoot());
		}
		if (parent instanceof TreeNode)
			return getChildren(parent);
		return new Object[0];
	}
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */

	public Object getParent(Object child) {
		if (child instanceof TreeNode) {
			return ((TreeNode) child).getParent();
		}
		return null;
	}
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof TreeNode) {
			if (((TreeNode) parent).hasChildren()) {
				return ((TreeNode) parent).getChildren();
			}
		}
		return new Object[0];
	}
	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object parent) {
		if (parent instanceof TreeNode)
			return ((TreeNode) parent).hasChildren();
		return false;
	}

	public void removeNode(TreeNode node) {
		node.getParent().removeChild(node);
	}

	/**
	 * Method reset.
	 */
	public void reset() {
		saveTargetVector();
		clear();
		setTreeRoot(null);
	}
	/**
	 * Method clear.
	 */
	public void clear() {
		Vector targetVector = (Vector) getTreeRoot().getProperty("TargetVector");
		if (null == targetVector)
			return;
		Enumeration targets = targetVector.elements();
		while (targets.hasMoreElements()) {
			TreeNode item = (TreeNode) targets.nextElement();
			item.setSelected(false);
		}
		targetVector.removeAllElements();
	}
	/**
	 * Method getTargetVector.
	 * @return Vector
	 */
	public Vector getTargetVector() {
		return (Vector) getTreeRoot().getProperty("TargetVector");
	}
	/**
	 * Method initialize.
	 */
	private void initialize() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this);

		setTreeRoot(new TreeNode(""));
		getTreeRoot().setProperty("TargetVector", new Vector());

		final ArrayList buildFileList = new ArrayList();
		final String buildFileName = Preferences.getString(IAntViewConstants.PREF_ANT_BUILD_FILE);
		IResourceVisitor buildVisitor = new IResourceVisitor() {
			public boolean visit(IResource res) throws CoreException {
				if (res instanceof File) {
					if (res.getName().equalsIgnoreCase(buildFileName)) {
						IPath file = (IPath) ((IFile) res).getLocation();
						buildFileList.add(file);
					}
				}
				return true;
			}
		};

		try {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			workspaceRoot.accept(buildVisitor);
		} catch (CoreException e) {
			workspace.addResourceChangeListener(this);
			return;
		}

		if (0 == buildFileList.size()) {
			getTreeRoot().addChild(new ErrorNode(ResourceMgr.getString("Tree.NoProjects") + " " + "(" + Preferences.getString(PREF_ANT_BUILD_FILE) + ")"));
			workspace.addResourceChangeListener(this);
			return;
		}

		Iterator buildFiles = buildFileList.iterator();
		while (buildFiles.hasNext()) {
			IPath file = (IPath) buildFiles.next();
			getTreeRoot().addChild(parseAntBuildFile(file.toString()));
		}
		restoreTargetVector();
		workspace.addResourceChangeListener(this);
	}
	/**
	 * Method parseAntBuildFile.
	 * @param filename
	 * @return TreeNode
	 */
	private TreeNode parseAntBuildFile(String filename) {
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(filename);
		TargetInfo[] infos = null;
		try {
			infos = runner.getAvailableTargets();
		} catch (CoreException e) {
			return new ProjectErrorNode(filename, "An exception occurred retrieving targets: " + e.getMessage());
		}
		if (infos.length < 1) {
			return new ProjectErrorNode(filename, "No targets found");
		}
		Project project = new Project();
		if (infos[0].getProject() != null) {
			project.setName(infos[0].getProject());
		}
		for (int i = 0; i < infos.length; i++) {
			TargetInfo info = infos[i];
			if (info.isDefault()) {
				project.setDefault(info.getName());
			}
			Target target = new Target();
			target.setName(info.getName());
			String[] dependencies = info.getDependencies();
			StringBuffer depends = new StringBuffer();
			int numDependencies = dependencies.length;
			if (numDependencies > 0) {
				// Onroll the loop to avoid trailing comma
				depends.append(dependencies[0]);
			}
			for (int j = 1; j < numDependencies; j++) {
				depends.append(',').append(dependencies[j]);
			}
			target.setDepends(depends.toString());
			target.setDescription(info.getDescription());
			project.addTarget(target);
		}
		if (project.getDefaultTarget() == null) {
			return new ProjectErrorNode(filename, ResourceMgr.getString("Tree.NoProjectElement"));
		}

		String projectName = project.getName();
		if (projectName == null) {
			projectName = "(unnamed)";
		}
		TreeNode projectNode = new ProjectNode(filename, projectName);
		Enumeration projTargets = project.getTargets().elements();
		while (projTargets.hasMoreElements()) {
			Target target = (Target) projTargets.nextElement();
			// Target Node -----------------			
			TreeNode targetNode = new TargetNode(filename, target);
			projectNode.addChild(targetNode);
			// Dependency Sub-Node ---------
			TreeNode dependencyNode = new ElementNode(ResourceMgr.getString("Tree.Dependencies"));
			targetNode.addChild(dependencyNode);
			Enumeration dependency = target.getDependencies();
			while (dependency.hasMoreElements()) {
				dependencyNode.addChild(new ElementNode((String) dependency.nextElement()));
			}
			if (!dependencyNode.hasChildren()) {
				dependencyNode.addChild(new ElementNode(ResourceMgr.getString("Tree.None")));
			}
			// Execution Path Sub-Node -------
			TreeNode topoNode = new ElementNode(ResourceMgr.getString("Tree.ExecuteOrder"));
			targetNode.addChild(topoNode);
			Vector topoSort = project.topoSort(target.getName(), project.getTargets());
			int n = topoSort.indexOf(target) + 1;
			while (topoSort.size() > n)
				topoSort.remove(topoSort.size() - 1);
			topoSort.trimToSize();
			ListIterator topoElements = topoSort.listIterator();
			while (topoElements.hasNext()) {
				int i = topoElements.nextIndex();
				Target topoTask = (Target) topoElements.next();
				topoNode.addChild(new ElementNode((i + 1) + ":" + topoTask.getName()));
			}
		}
		return projectNode;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;

		IResourceDelta delta = event.getDelta();
		if (delta == null)
			return;

		final ArrayList deltaResources = new ArrayList();
		final String buildFileName = Preferences.getString(IAntViewConstants.PREF_ANT_BUILD_FILE);
		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) {
				if (delta.getKind() != IResourceDelta.CHANGED)
					return true;
				if (0 == (delta.getFlags() & IResourceDelta.CONTENT))
					return true;
				IResource resource = delta.getResource();

				if (resource.getType() == IResource.FILE && buildFileName.equalsIgnoreCase(resource.getName())) {
					deltaResources.add(resource);
				}
				return true;
			}
		};

		try {
			delta.accept(visitor);
		} catch (CoreException e) {
			return;
		}

		if (0 == deltaResources.size())
			return;

		saveTargetVector();
		clear();
		Iterator changedResources = deltaResources.iterator();
		while (changedResources.hasNext()) {
			IResource fileResource = (IResource) changedResources.next();
			String buildFile = fileResource.getLocation().toString();
			TreeNode rootChild[] = getTreeRoot().getChildren();
			for (int i = 0; i < rootChild.length; i++) {
				String nodeBuildFile = (String) rootChild[i].getProperty("BuildFile");
				if (null == nodeBuildFile)
					continue;
				if (buildFile.equals(nodeBuildFile)) {
					getTreeRoot().removeChild(rootChild[i]);
					break;
				}
			}
			getTreeRoot().addChild(parseAntBuildFile(buildFile));
		}
		restoreTargetVector();
		AntView antView = AntUtil.getAntView();
		if (antView != null) {
			antView.refresh();
		}
	}

	private void saveTargetVector() {
		Vector targetVector = (Vector) getTreeRoot().getProperty("TargetVector");
		if (null == targetVector) {
			return;
		}
		String targets = "";
		ListIterator targetList = targetVector.listIterator();
		while (targetList.hasNext()) {
			TreeNode target = (TreeNode) targetList.next();
			targets += target.getProperty("BuildFile") + SEP_KEYVAL + target.getText() + SEP_REC;
		}
		Preferences.setString(PREF_TARGET_VECTOR, targets);
	}

	private void restoreTargetVector() {
		HashMap targetMap = new HashMap();
		TreeNode rootChildren[] = getTreeRoot().getChildren();
		for (int i = 0; i < rootChildren.length; i++) {
			TreeNode targetNodes[] = rootChildren[i].getChildren();
			TreeNode node;
			for (int j = 0; j < targetNodes.length; j++) {
				node= targetNodes[j];
				targetMap.put(node.getProperty("BuildFile") + SEP_KEYVAL + node.getText(), node);
			}
		}

		String targetsPref = Preferences.getString(PREF_TARGET_VECTOR);
		if (null == targetsPref || targetsPref.equals("")) {
			return;
		}
		StringTokenizer st = new StringTokenizer(targetsPref, SEP_REC);
		String targets[] = new String[st.countTokens()];

		for (int tokenCounter = 0; st.hasMoreTokens(); tokenCounter++) {
			targets[tokenCounter] = st.nextToken();
		}

		for (int i = 0; i < targets.length; i++) {
			if (null == targets[i] || targets[i].equals(""))
				continue;
			TreeNode targetNode = (TargetNode) targetMap.get(targets[i]);
			if (null == targetNode || targetNode.isSelected())
				continue;
			targetNode.setSelected(true);
			//-------------------
			//  	      TreeViewer viewer = AntviewPlugin.getDefault().getAntView().getTreeViewer();
			//  	      viewer.expandToLevel(targetNode, 2);
			//--------------------
		}
		AntView view = AntUtil.getAntView();
		if (view != null) {
			view.refresh();
		}
	}

	private void setTreeRoot(TreeNode treeRoot) {
		this.treeRoot = treeRoot;
	}

	public TreeNode getTreeRoot() {
		return treeRoot;
	}
}

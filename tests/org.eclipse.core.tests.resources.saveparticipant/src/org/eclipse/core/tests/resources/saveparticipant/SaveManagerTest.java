package org.eclipse.core.tests.resources.saveparticipant;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public abstract class SaveManagerTest extends WorkspaceSessionTest {

	/** project names */
	static final String PROJECT_1 = "MyProject";
	static final String PROJECT_2 = "Project2";

	/** activities */
	static final String COMMENT_1 = "COMMENT ONE";
	static final String COMMENT_2 = "COMMENT TWO";

	/** plugin ids */
	static final String PI_SAVE_PARTICIPANT_1 = "org.eclipse.core.tests.resources.saveparticipant1";
	static final String PI_SAVE_PARTICIPANT_2 = "org.eclipse.core.tests.resources.saveparticipant2";
	static final String PI_SAVE_PARTICIPANT_3 = "org.eclipse.core.tests.resources.saveparticipant3";
public SaveManagerTest() {
}
public SaveManagerTest(String name) {
	super(name);
}
protected String[] defineHierarchy(String project) {
	if (project.equals(PROJECT_1))
		return defineHierarchy1();
	if (project.equals(PROJECT_2))
		return defineHierarchy2();
	return new String[0];
}
protected String[] defineHierarchy1() {
	return new String[] {
		"/folder110/",
		"/folder110/folder120/",
		"/folder110/folder120/folder130/",
		"/folder110/folder120/folder130/folder140/",
		"/folder110/folder120/folder130/folder140/folder150/",
		"/folder110/folder120/folder130/folder140/folder150/file160",
		"/folder110/folder120/folder130/folder140/file150",
		"/folder110/folder121/",
		"/folder110/folder121/folder131/",
		"/folder110/folder120/folder130/folder141/"
	};
}
protected String[] defineHierarchy2() {
	return new String[] {
		"/file110",
		"/folder110/",
		"/folder110/file120",
		"/folder111/",
		"/folder111/folder120/",
		"/folder111/file121"
	};
}
/**
 * Sets the workspace autobuilding to the desired value.
 */
protected void setAutoBuilding(boolean value) throws CoreException {
	IWorkspace workspace = getWorkspace();
	if (workspace.isAutoBuilding() == value)
		return;
	IWorkspaceDescription desc = workspace.getDescription();
	desc.setAutoBuilding(value);
	workspace.setDescription(desc);
}
protected void touch(IProject project) throws CoreException {
	project.accept(new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			if (resource.getType() == IResource.FILE) {
				resource.touch(null);
				return false;
			}
			return true;
		}
	});
}
}

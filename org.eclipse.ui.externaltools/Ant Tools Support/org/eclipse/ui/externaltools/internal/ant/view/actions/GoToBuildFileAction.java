package org.eclipse.ui.externaltools.internal.ant.view.actions;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Actions that opens the build file for a project in the Ant view.
 */
public class GoToBuildFileAction extends Action implements IUpdate {

	private AntView view;
	
	public GoToBuildFileAction(AntView view) {
		super("Go to File", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_GO_TO_FILE));
		setDescription("Open the selected build file");
		setToolTipText("Open the selected build file");
		this.view= view;
	}
	
	/**
	 * Opens an editor on the file of the currently selected project in the Ant
	 * view project viewer
	 */
	public void run() {
		ProjectNode project= getSelectedProject();
		if (project == null) {
			return;		
		}
		Path buildFilePath= new Path(project.getBuildFileName());
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		int matchingSegments= root.getLocation().matchingFirstSegments(buildFilePath);
		IFile file= root.getFile(buildFilePath.removeFirstSegments(matchingSegments));
		if (file.exists()) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(file);
			} catch (PartInitException e) {
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Exception opening file", MessageFormat.format("An exception occurred opening file {0}:\n{1}", new String[] {buildFilePath.toString(), e.getMessage()}));
			}
		}
	}
	
	/**
	 * Returns the currently selected project in the Ant view's project viewer
	 * or <code>null</code> if none.
	 */
	private ProjectNode getSelectedProject() {
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		Iterator iter= selection.iterator();
		Object data= iter.next();
		if (iter.hasNext()) {
			return null;
		}
		if (data instanceof ProjectNode) {
			return (ProjectNode) data;
		}
		return null;
	}
	
	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		setEnabled(getSelectedProject() != null);
	}

}

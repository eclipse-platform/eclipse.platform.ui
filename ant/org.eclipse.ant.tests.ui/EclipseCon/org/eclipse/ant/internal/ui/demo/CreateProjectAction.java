package org.eclipse.ant.internal.ui.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Javac;
import org.eclipse.ant.internal.ui.editor.model.AntElementNode;
import org.eclipse.ant.internal.ui.editor.model.AntProjectNode;
import org.eclipse.ant.internal.ui.editor.model.AntTargetNode;
import org.eclipse.ant.internal.ui.editor.model.AntTaskNode;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.editor.outline.ILocationProvider;
import org.eclipse.ant.internal.ui.editor.outline.XMLCore;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class CreateProjectAction implements IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow fWindow;
	
	private IDocument fCurrentDocument;
	
	private AntModel fAntModel;
	
	public CreateProjectAction() {
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		
		File buildFile= getBuildFile();
		if (buildFile == null) {
			return;
		}
		
		fAntModel= getAntModel(buildFile);
		AntProjectNode projectNode= getProjectNode();
		String projectName= projectNode.getLabel();
		if (projectName == null) {
			projectName= "Ant Project";
		}
		List javacNodes= new ArrayList();
		getJavacNodes(javacNodes, projectNode);
		
		if (javacNodes.size() > 1) {
			MessageDialog.openInformation(
				fWindow.getShell(),
				"Ant Demo",
				"Currently only support creating a project from a single javac declaration");
		}
		
		List javacTasks= resolveJavacTasks(javacNodes);
		ProjectCreator creator= new ProjectCreator();
		Iterator iter= javacTasks.iterator();
		while (iter.hasNext()) {
			Javac javacTask = (Javac) iter.next();
			try {
				creator.createJavaProjectFromJavacNode(projectName, javacTask);
			} catch (CoreException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	private List resolveJavacTasks(List javacNodes) {
		List resolvedJavacTasks= new ArrayList(javacNodes.size());
		Iterator nodes= javacNodes.iterator();
		while (nodes.hasNext()) {
			AntTaskNode taskNode = (AntTaskNode) nodes.next();
			Task javacTask= taskNode.getTask();
			if (javacTask instanceof UnknownElement) {
				if (((UnknownElement)javacTask).getRealThing() == null) {
					javacTask.maybeConfigure();
				}
				
				resolvedJavacTasks.add(((UnknownElement)javacTask).getRealThing());
			} else {
				resolvedJavacTasks.add(javacTask);
			}
			
		}
		return resolvedJavacTasks;
	}


	private void getJavacNodes(List javacNodes, AntElementNode parent) {
		List children= parent.getChildNodes();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			AntElementNode node = (AntElementNode) iter.next();
			if (node instanceof AntTargetNode) {
				getJavacNodes(javacNodes, node);
			} else if (node instanceof AntTaskNode) {
				AntTaskNode task= (AntTaskNode)node;
				if (task.getName() == "javac") {
					javacNodes.add(task);
				}
			}
		}
	}

	private AntProjectNode getProjectNode() {
		AntElementNode[] nodes= fAntModel.getRootElements();
		if (nodes.length == 0) {
			return null;
		}
		AntProjectNode projectNode= (AntProjectNode)nodes[0];
		return projectNode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.fWindow = window;
	}
	
	/**
	 * Determine the buildfile the user wishes to operate from
	 */
	private File getBuildFile() {
		String lastUsedPath= null;
		//lastUsedPath= dialogSettings.get(IAntUIConstants.DIALOGSTORE_LASTEXTFILE);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(fWindow.getShell(), SWT.SINGLE);
		dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$;
		dialog.setFilterPath(lastUsedPath);

		String result = dialog.open();
		if (result == null) {
			return null;
		}
		IPath filterPath= new Path(dialog.getFilterPath());
		String buildFileName= dialog.getFileName();
		IPath path= filterPath.append(buildFileName).makeAbsolute();	
		
		return path.toFile();
	}
	
	protected AntModel getAntModel(final File buildFile) {
		fCurrentDocument= getDocument(buildFile);
		AntModel model= new AntModel(XMLCore.getDefault(), fCurrentDocument, null, new ILocationProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.ant.internal.ui.editor.outline.ILocationProvider#getLocation()
			 */
			public IPath getLocation() {
				return new Path(buildFile.getAbsolutePath());
			}
		});
		model.reconcile();
		return model;
	}
	
	protected IDocument getDocument(File buildFile) {
		InputStream in;
		try {
			in = new FileInputStream(buildFile);
		} catch (FileNotFoundException e) {
			return null;
		}
		String initialContent= getStreamContentAsString(in);
		return new Document(initialContent);
	}
	
	protected String getStreamContentAsString(InputStream inputStream) {
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(inputStream, ResourcesPlugin.getEncoding());
		} catch (UnsupportedEncodingException e) {
			AntUIPlugin.log(e);
			return ""; //$NON-NLS-1$
		}

		return getReaderContentAsString( new BufferedReader(reader));
	}
	
	protected String getReaderContentAsString(BufferedReader bufferedReader) {
		StringBuffer result = new StringBuffer();
		try {
			String line= bufferedReader.readLine();

			while(line != null) {
				if(result.length() != 0) {
					result.append("\n"); //$NON-NLS-1$
				}
				result.append(line);
				line = bufferedReader.readLine();
			}
		} catch (IOException e) {
			AntUIPlugin.log(e);
			return null;
		}

		return result.toString();
	}
	
	
}
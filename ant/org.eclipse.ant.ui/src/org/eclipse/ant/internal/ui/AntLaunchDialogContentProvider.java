package org.eclipse.ant.internal.ui;

//import javax.xml.parsers.SAXParserFactory;
import java.io.File;import org.apache.tools.ant.ProjectHelper;import org.eclipse.ant.core.EclipseProject;import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.*;

/**
 * Content provider for the Ant dialog: it provides the ListViewer with the targets defined in the xml file.
 */

public class AntLaunchDialogContentProvider implements IStructuredContentProvider {
	
	private static AntLaunchDialog parent;
	private static AntLaunchDialogContentProvider instance;
	
	static {
		instance = new AntLaunchDialogContentProvider();
	}
	
	// private to ensure that it remains a singleton
	private AntLaunchDialogContentProvider() {
		super();
	}
	
	public static AntLaunchDialogContentProvider getInstance(AntLaunchDialog theParent) {
		parent = theParent;
		return instance;
	}
	
	
	/**
	 * Returns the targets found in the xml file after parsing.
	 * 
	 * @param groupName the name of the group
	 * @return the array of the targets found
	 */
	public Object[] getElements(Object inputElement) {
		IFile sourceFile = (IFile)inputElement;

		// create a project and initialize it
		EclipseProject antProject = new EclipseProject();
		antProject.init();
		antProject.setProperty("ant.file",sourceFile.getLocation().toOSString());
		
		try {
//			Class.forName("javax.xml.parsers.SAXParserFactory");
			ProjectHelper.configureProject(antProject,new File(sourceFile.getLocation().toOSString()));
//		} catch (ClassNotFoundException e) {
			// should not happen
//			e.printStackTrace();
//		}
		} catch (Exception e) {
			// If the document is not well-formated for example
			e.printStackTrace();
			System.out.println(e.getMessage());
			// should do something here: stop the process
		}

		// give the reference to the AntLaunchDialog
		parent.setProject(antProject);
		
		return antProject.getTargets().values().toArray() ;
	}
	
	public void dispose() {
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}

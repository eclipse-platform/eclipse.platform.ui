package org.eclipse.ant.internal.ui;

//import javax.xml.parsers.SAXParserFactory;
import java.io.File;import org.apache.tools.ant.ProjectHelper;import org.eclipse.ant.core.EclipseProject;import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.*;

public class AntLaunchDialogContentProvider implements IStructuredContentProvider {
	
	private static AntLaunchDialogContentProvider instance;
	
	static {
		instance = new AntLaunchDialogContentProvider();
	}
	
	// private to ensure that it remains a singleton
	private AntLaunchDialogContentProvider() {
		super();
	}
	
	public static AntLaunchDialogContentProvider getInstance() {
		return instance;
	}
	
	public Object[] getElements(Object inputElement) {
		IFile sourceFile = (IFile)inputElement;

		EclipseProject antProject = new EclipseProject();
		antProject.init();
		antProject.setProperty("ant.file",sourceFile.getLocation().toOSString());
		
//		try {
//			Class.forName("javax.xml.parsers.SAXParserFactory");
			ProjectHelper.configureProject(antProject,new File(sourceFile.getLocation().toOSString()));
//		} catch (ClassNotFoundException e) {
			// should not happen
//			e.printStackTrace();
//		}

		return new Object[1];
	}
	
	public void dispose() {
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}

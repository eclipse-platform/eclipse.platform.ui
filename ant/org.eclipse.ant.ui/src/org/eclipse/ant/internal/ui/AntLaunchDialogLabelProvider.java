package org.eclipse.ant.internal.ui;import org.apache.tools.ant.Target;import org.eclipse.jface.viewers.LabelProvider;

public class AntLaunchDialogLabelProvider extends LabelProvider {
	
	private static AntLaunchDialogLabelProvider instance;
	
	static {
		instance = new AntLaunchDialogLabelProvider();
	}
	
	// private to ensure that it remains a singleton
	private AntLaunchDialogLabelProvider() {
		super();
	}
	
	public static AntLaunchDialogLabelProvider getInstance() {
		return instance;
	}

	/**
	 * Takes an object and returns a string which will stand for this object.
	 * 
	 * @param model the object that has to be displayed
	 * @return the string representing the object
	 */	
	public String getText(Object model) {
		Target targetToDisplay = (Target) model;
		// Could it be null ?
		if (targetToDisplay != null )
			return targetToDisplay.getName();
		else
			return "";
	}
	
//	public Image getImage(Object model) {
//		return null;
//	}

}

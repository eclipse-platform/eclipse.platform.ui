package org.eclipse.ant.internal.ui;import org.eclipse.jface.viewers.LabelProvider;
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
	
	public String getText(Object model) {
		
		return "";
	}
	
//	public Image getImage(Object model) {
//		return null;
//	}
}

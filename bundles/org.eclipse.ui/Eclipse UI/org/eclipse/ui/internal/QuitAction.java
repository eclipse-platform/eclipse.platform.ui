package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;

/**
 * Try to quit the application.
 */
public class QuitAction extends Action {
	private IWorkbench workbench;
/**
 * Creates a new <code>QuitAction</code>. The action is
 * initialized from the <code>JFaceResources</code> bundle.
 */
public QuitAction(IWorkbench workbench) {
	super("Exit");
	setText("E&xit");
	setToolTipText("Exit the workbench");
	setId(IWorkbenchActionConstants.QUIT);
	this.workbench = workbench;
}
/**
 * Perform the action: quit the application.
 */
public void run() {
	workbench.close();
}
}

package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.help.ITopic;
/**
 * PrintTopicTree action
 */
public class PrintTopicTreeAction extends Action {
	private ITopic rootTopic;
	/**
	 * @param selection - IStructuredSelection containing
	 * ITopic, which is a root of the tree to be printed
	 */
	public PrintTopicTreeAction(IStructuredSelection selection) {
		super(WorkbenchResources.getString("Print_Topic_Tree"));
		if (TopicTreePrinter.busy) {
			setEnabled(false);
			return;
		}
		this.rootTopic = (ITopic) selection.getFirstElement();
		setEnabled(true);
	}
	public void run() {
		TopicTreePrinter navPrinter = new TopicTreePrinter(rootTopic);
		navPrinter.print();
	}
}
package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.internal.ui.win32.*;
import org.eclipse.ui.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.viewers.*;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.topics.*;
import org.eclipse.jface.action.Action;

/**
 * PrintTopicTree action
 * To be used on win32 OS only.
 */
public class PrintTopicTreeAction extends Action {
	private ITopic rootTopic;
	/**
	 * @param selection - IStructuredSelection containing
	 * ITopic, which is a root of the tree to be printed
	 */
	public PrintTopicTreeAction(IStructuredSelection selection) {
		super();
		if (!(selection.getFirstElement() instanceof ITopic)) {
			// some other tree element
			setEnabled(false);
			return;
		}
		rootTopic = (ITopic) selection.getFirstElement();
		if (((ITopic) rootTopic).getChildTopics().isEmpty()) {
			// no children
			setEnabled(false);
			return;
		}
		this.rootTopic=rootTopic;
		setText(WorkbenchResources.getString("Print_Topic_Tree"));
		setEnabled(true);
	}
	public void run() {
		TopicTreePrinter navPrinter=new TopicTreePrinter(rootTopic);
		navPrinter.print();
	}
}
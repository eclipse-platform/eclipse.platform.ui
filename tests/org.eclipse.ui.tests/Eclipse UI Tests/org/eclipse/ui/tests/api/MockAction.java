package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.tests.TestPlugin;

public class MockAction extends Action {
	
	private boolean hasRun = false;

	/**
	 * Constructor for MockAction
	 */
	protected MockAction(String text) {
		super(text);
		TestPlugin plugin = TestPlugin.getDefault();
		setImageDescriptor(plugin.getImageDescriptor("anything.gif"));
		setToolTipText(text);
	}

	/**
	 * Constructor for MockAction
	 */
	protected MockAction(String text, ImageDescriptor image) {
		super(text, image);
		setToolTipText(text);
	}
	
	public void run() {
		hasRun = true;
	}
	
	public void clearRun() {
		hasRun = false;
	}
	
	public boolean getRun() {
		return hasRun;
	}
	
}


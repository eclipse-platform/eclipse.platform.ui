package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import java.util.*;

/**
 * A dynamic contribution item which supports to switch to other Contexts.
 */
public class SwitchToPerspMenu extends PerspectiveMenu {
	private IMenuManager parentMenuManager;
	private boolean enabled = true;
	private IWorkbenchWindow window;
	private IPerspectiveRegistry reg;
/**
 * Constructs a new menu.
 */
public SwitchToPerspMenu(IMenuManager menuManager, IWorkbenchWindow window) {
	this(window);
	this.parentMenuManager = menuManager;
}
/**
 * Constructs a new menu.
 */
public SwitchToPerspMenu(IWorkbenchWindow window) {
	super(window, "SwitchToPerspectiveMenu");
	showActive(true);
}
/* (non-Javadoc)
 * Fills the menu with perspective items.
 */
public void fill(Menu menu, int index) {
	if (enabled)
		super.fill(menu, index);
}
/**
 * Run the action.
 */
public void run(IPerspectiveDescriptor desc) {
	IWorkbenchPage persp = getWindow().getActivePage();
	if (persp != null) {
		persp.setPerspective(desc);
	}
}
/**
 * Set whether this menu item is enabled
 * within its parent menu.
 */
public void setEnabled(boolean isEnabled) {
	if (enabled != isEnabled) {
		enabled = isEnabled;
		if (parentMenuManager != null)
			parentMenuManager.update(true);
	}
}
}

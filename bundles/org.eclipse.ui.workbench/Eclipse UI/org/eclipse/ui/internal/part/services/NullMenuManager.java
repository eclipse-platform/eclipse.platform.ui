/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @since 3.1
 */
public class NullMenuManager extends NullContributionManager implements IMenuManager {

	public void addMenuListener(IMenuListener listener) {}
	public IMenuManager findMenuUsingPath(String path) {return null;}
	public IContributionItem findUsingPath(String path) {return null;}
	public boolean getRemoveAllWhenShown() {return false;}
	public boolean isEnabled() {return false;}
	public void removeMenuListener(IMenuListener listener) {}
	public void setRemoveAllWhenShown(boolean removeAll) {}
	public void updateAll(boolean force) {}
	public void dispose() {}
	public void fill(Composite parent) {}
	public void fill(Menu parent, int index) {}
	public void fill(ToolBar parent, int index) {}
	public void fill(CoolBar parent, int index) {}
	public String getId() {return null;}
	public boolean isDynamic() {return false;}
	public boolean isGroupMarker() {return false;}
	public boolean isSeparator() {return false;}
	public boolean isVisible() {return false;}
	public void saveWidgetState() {}
	public void setParent(IContributionManager parent) {}
	public void setVisible(boolean visible) {}
	public void update() {}
	public void update(String id) {}
}

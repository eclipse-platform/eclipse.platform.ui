/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.presentations;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.RectangleAnimation;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.model.LaunchBarElement;
import org.eclipse.ui.internal.intro.impl.model.LaunchBarShortcutElement;
import org.eclipse.ui.internal.intro.impl.swt.SharedStyleManager;
import org.eclipse.ui.internal.intro.impl.util.ImageUtil;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.config.CustomizableIntroPart;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;


/**
 * This class is responsible for creating the intro launch bar
 * in the provided parent. It creates 'restore' and 'close'
 * actions, as well as actions for each shortcut element
 * contributed in the extension point.
 * @author dejan
 *
 */
public class IntroLaunchBar {
	private Composite container;
	private ToolBarManager toolBarManager;

	private int orientation;

	private String lastPageId;

	private Action closeAction;

	private LaunchBarElement element;
	private Color fg;
	private Color bg;

	public IntroLaunchBar(int orientation, String lastPageId,
			LaunchBarElement element) {
		this.orientation = orientation;
		this.lastPageId = lastPageId;
		this.element = element;
	}

	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);		
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		toolBarManager = new ToolBarManager(SWT.FLAT | orientation);
		fillToolBar();
		toolBarManager.createControl(container);
		ToolBar toolBar = toolBarManager.getControl();
		computeColors(parent.getDisplay());
		//new ToolItem(toolBar, SWT.SEPARATOR);
		if (bg!=null)
			toolBar.setBackground(bg);
		toolBar.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				Color color = fg;
				if (color==null)
					color = e.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
				gc.setForeground(color);
				Point size = container.getSize();
				gc.drawRectangle(0, 0, size.x-1, size.y-1);
			}
		});
		MenuManager manager = new MenuManager();
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		};
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(listener);
		Menu contextMenu = manager.createContextMenu(toolBarManager
				.getControl());
		toolBarManager.getControl().setMenu(contextMenu);
		IntroPlugin.getDefault().setLaunchBar(this);		
	}

	private void computeColors(Display display) {
		if (element.getBackground()!=null) {
			RGB r = SharedStyleManager.parseRGB(element.getBackground());
			if (r!=null)
				bg = new Color(display, r);
		}
		if (element.getForeground()!=null) {
			RGB r = SharedStyleManager.parseRGB(element.getForeground());
			if (r!=null)
				fg = new Color(display, r);
		}
	}

	public Control getControl() {
		return container;
	}

	public void dispose() {
		container.dispose();		
		toolBarManager.dispose();
		toolBarManager.removeAll();
		toolBarManager = null;
		container = null;
		if (bg!=null)
			bg.dispose();
		if (fg!=null)
			fg.dispose();
	}

	private void fillToolBar() {
		Action action;

		closeAction = new Action("close") { //$NON-NLS-1$
			public void run() {
				closeLaunchBar(false);
			}
		};
		closeAction.setText(IntroPlugin.getString("IntroLaunchBar.close.label")); //$NON-NLS-1$
		closeAction.setToolTipText(IntroPlugin.getString("IntroLaunchBar.close.tooltip")); //$NON-NLS-1$
		closeAction.setImageDescriptor(ImageUtil.
				createImageDescriptor("full/elcl16/close_view.gif")); //$NON-NLS-1$

		action = new Action("restore") { //$NON-NLS-1$
			public void run() {
				openPage(lastPageId);
			}
		};
		action.setToolTipText(IntroPlugin.getString("IntroLaunchBar.restore.tooltip")); //$NON-NLS-1$
		action.setImageDescriptor(ImageUtil
				.createImageDescriptor("full/etool16/restore_welcome.gif")); //$NON-NLS-1$
		toolBarManager.add(closeAction);
		toolBarManager.add(action);
		toolBarManager.add(new Separator());
		if (element == null)
			return;
		LaunchBarShortcutElement[] shortcuts = element.getShortcuts();
		for (int i = 0; i < shortcuts.length; i++) {
			LaunchBarShortcutElement shortcut = shortcuts[i];
			addShortcut(shortcut, toolBarManager);
		}
	}

	private void addShortcut(final LaunchBarShortcutElement shortcut,
			IToolBarManager toolBarManager) {
		Action action = new Action(shortcut.getToolTip()) {
			public void run() {
				executeShortcut(shortcut.getURL());
			}
		};
		action.setImageDescriptor(shortcut.getImageDescriptor());
		action.setToolTipText(shortcut.getToolTip());
		toolBarManager.add(action);
	}
	
	public void close() {
		closeLaunchBar(false);
	}

	private IIntroPart closeLaunchBar(boolean restore) {
		IntroPlugin.getDefault().setLaunchBar(null);		
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IIntroPart intro = null;
		if (restore) {
			intro = PlatformUI.getWorkbench().getIntroManager().showIntro(
					window, false);
		}
		if (restore) {
			CustomizableIntroPart cpart = (CustomizableIntroPart) intro;
			Rectangle startBounds = Geometry.toDisplay(
					getControl().getParent(), getControl().getBounds());
			Rectangle endBounds = Geometry.toDisplay(cpart.getControl()
					.getParent(), cpart.getControl().getBounds());

			RectangleAnimation animation = new RectangleAnimation(window
					.getShell(), startBounds, endBounds);
			animation.schedule();
		}
		dispose();
		window.getShell().layout();
		return intro;
	}

	private void executeShortcut(String url) {
		IIntroURL introURL = IntroURLFactory.createIntroURL(url);
		if (introURL != null) {
			IIntroPart intro = closeLaunchBar(true);
			if (intro == null)
				return;
			introURL.execute();
		}
	}

	private void openPage(String id) {
		IIntroPart intro = closeLaunchBar(true);
		if (intro == null)
			return;
		StringBuffer url = new StringBuffer();
		url.append(IntroPlugin.getString("IntroLaunchBar.6")); //$NON-NLS-1$
		url.append(id);
		IIntroURL introURL = IntroURLFactory.createIntroURL(url.toString());
		if (introURL != null)
			introURL.execute();
	}

	private void contextMenuAboutToShow(IMenuManager manager) {
		manager.add(closeAction);
	}
}

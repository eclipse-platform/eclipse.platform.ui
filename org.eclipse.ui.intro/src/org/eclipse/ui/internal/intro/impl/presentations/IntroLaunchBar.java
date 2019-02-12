/***************************************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
 *
 * This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 * Lars Vogel <Lars.Vogel@vogella.com> - Bug 440136
 **************************************************************************************************/
package org.eclipse.ui.internal.intro.impl.presentations;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.AnimationEngine;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.Messages;
import org.eclipse.ui.internal.intro.impl.model.IntroLaunchBarElement;
import org.eclipse.ui.internal.intro.impl.model.IntroLaunchBarShortcut;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.IntroTheme;
import org.eclipse.ui.internal.intro.impl.swt.SharedStyleManager;
import org.eclipse.ui.internal.intro.impl.util.ImageUtil;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.config.CustomizableIntroPart;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;

/**
 * This class is responsible for creating the intro launch bar in the provided parent. It creates
 * 'restore' and 'close' actions, as well as actions for each shortcut element contributed in the
 * extension point.
 *
 * Reimplemented as an E4 MToolControl for 4.x. Ideally the intro configuration information would be
 * available from the application IEclipseContext.
 *
 * @since 3.1
 */
public class IntroLaunchBar {

	private static final String LAUNCHBAR_ID = "org.eclipse.ui.internal.intro.impl.presentations.IntroLaunchBar"; //$NON-NLS-1$
	private static final String BUNDLECLASS_URI = "bundleclass://org.eclipse.ui.intro/" //$NON-NLS-1$
			+ IntroLaunchBar.class.getName();

	/* Information to persist so as to be able to reload the intro state */
	private static final String LAST_PAGE_ID = "lastPageId"; //$NON-NLS-1$
	private static final String INTRO_CONFIG_ID = "introConfigId"; //$NON-NLS-1$

	private Composite container;

	protected ToolBarManager toolBarManager;

	protected String lastPageId;

	protected Action closeAction = null;

	@Inject
	@Optional
	private IntroLaunchBarElement element;

	protected boolean simple;

	@Inject
	@Optional
	private IntroTheme theme;

	static final int[] TOP_LEFT_CORNER = new int[] { 0, 6, 1, 5, 1, 4, 4, 1, 5, 1, 6, 0 };

	static final int[] TOP_RIGHT_CORNER = new int[] { -6, 0, -5, 1, -4, 1, -1, 4, -1, 5, 0, 6 };

	static final int[] BOTTOM_LEFT_CORNER = new int[] { 0, -6, 1, -5, 1, -4, 4, -1, 5, -1, 6, 0 };

	static final int[] BOTTOM_RIGHT_CORNER = new int[] { -6, 0, -5, -1, -4, -1, -1, -4, -1, -5, 0, -6 };

	static final int[] SIMPLE_TOP_LEFT_CORNER = new int[] { 0, 2, 1, 1, 2, 0 };

	static final int[] SIMPLE_TOP_RIGHT_CORNER = new int[] { -2, 0, -1, 1, 0, 2 };

	static final int[] SIMPLE_BOTTOM_LEFT_CORNER = new int[] { 0, -2, 1, -1, 2, 0 };

	static final int[] SIMPLE_BOTTOM_RIGHT_CORNER = new int[] { -2, 0, -1, -1, 0, -2 };

	static final String S_STORED_LOCATION = "introLaunchBar.location"; //$NON-NLS-1$

	private final static String LAUNCH_COMMAND_BASE = "http://org.eclipse.ui.intro/showPage?id="; //$NON-NLS-1$

	private Color fg;

	private Color bg;

	private MToolControl trimControl;

	class BarLayout extends Layout {

		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean changed) {
			boolean vertical = (getOrientation() & SWT.VERTICAL) != 0;
			int marginWidth = vertical | isPlain() ? 1 : simple ? 3 : 7;
			int marginHeight = !vertical | isPlain() ? 1 : simple ? 3 : 7;
			int width = 0;
			int height = 0;

			Point tsize = toolBarManager.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);

			if (vertical) {
				width = tsize.x;
				height = tsize.y;
			} else {
				height = tsize.y;
				width = tsize.x;
			}
			if (vertical) {
				width += marginWidth;
				height += marginHeight + marginHeight;
			} else {
				width += marginWidth + marginWidth;
				height += marginHeight;
			}
			return new Point(width, height);
		}

		@Override
		protected void layout(Composite composite, boolean changed) {
			boolean vertical = (getOrientation() & SWT.VERTICAL) != 0;
			int marginWidth = vertical | isPlain() ? 1 : simple ? 4 : 7;
			int marginHeight = !vertical | isPlain() ? 1 : simple ? 4 : 7;

			Point tsize = toolBarManager.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
			Rectangle carea = composite.getClientArea();
			int x = carea.x + (getLocation() == SideValue.LEFT ? 0 : marginWidth);
			int y = carea.y + marginHeight;

			if (vertical) {
				toolBarManager.getControl().setBounds(x, y, carea.width - marginWidth, tsize.y);
			} else {
				toolBarManager.getControl().setBounds(x, y, tsize.x, carea.height - marginHeight);
			}
		}
	}

	/**
	 * Install the Intro Launch Bar into the provided window.
	 *
	 * @param window
	 *            the window to host the launch bar
	 * @param modelRoot
	 *            the current intro configuration, with pages
	 * @param element
	 *            the launch bar configuration
	 * @return the launch bar instance
	 */
	public static IntroLaunchBar create(IWorkbenchWindow window, IntroModelRoot modelRoot,
			IntroLaunchBarElement element) {
		EModelService modelService = window.getService(EModelService.class);
		MTrimmedWindow trimmedWindow = window.getService(MTrimmedWindow.class);

		MToolControl trimControl = modelService.createModelElement(MToolControl.class);
		trimControl.setElementId(LAUNCHBAR_ID);
		trimControl.setContributionURI(BUNDLECLASS_URI);
		// Must record sufficient information so as to be able to obtain the
		// launch configuration on workspace restart
		trimControl.getPersistedState().put(INTRO_CONFIG_ID, modelRoot.getId());
		trimControl.getPersistedState().put(LAST_PAGE_ID, modelRoot.getCurrentPageId());
		trimControl.getTags().add(IPresentationEngine.DRAGGABLE);

		MTrimBar bar = modelService.getTrim(trimmedWindow, determineLocation(element));
		bar.getChildren().add(trimControl);

		// should now be rendered
		return (IntroLaunchBar) trimControl.getObject();
	}

	/**
	 * Remove all traces of any launch bars found in the model. Required on startup as the
	 * {@linkplain IntroLaunchBar} instances may not be have been rendered yet and so are
	 * disconnected from the {@link IntroPlugin}'s state.
	 *
	 * @param workbench
	 *            the workbench to process
	 */
	public static void destroyAll(IWorkbench workbench) {
		EModelService modelService = workbench.getService(EModelService.class);
		MApplication application = workbench.getService(MApplication.class);
		List<MToolControl> candidates = modelService.findElements(application, MToolControl.class,
				EModelService.IN_TRIM, toolControl -> LAUNCHBAR_ID.equals(toolControl.getElementId())
						&& BUNDLECLASS_URI.equals(((MToolControl) toolControl).getContributionURI()));
		for (MToolControl trimControl : candidates) {
			if (trimControl.getParent() != null) {
				trimControl.getParent().getChildren().remove(trimControl);
			}
		}
	}

	private static SideValue determineLocation(IntroLaunchBarElement element) {
		// Try restoring to the same location if moved previously
		IDialogSettings settings = IntroPlugin.getDefault().getDialogSettings();
		try {
			int storedLocation = settings.getInt(S_STORED_LOCATION);
			if (storedLocation > 0)
				return toSideValue(storedLocation);
		} catch (NumberFormatException e) {
			// The stored value either does not exist or
			// is corrupted - just pick the default silently.
		}
		return toSideValue(element.getLocation());
	}

	private static SideValue toSideValue(int location) {
		switch (location) {
		case SWT.LEFT:
			return SideValue.LEFT;
		case SWT.RIGHT:
			return SideValue.RIGHT;
		case SWT.TOP:
			return SideValue.TOP;
		case SWT.BOTTOM:
			return SideValue.BOTTOM;
		}
		return SideValue.BOTTOM;
	}

	private static int toSWT(SideValue sv) {
		switch (sv) {
		case LEFT:
			return SWT.LEFT;
		case RIGHT:
			return SWT.RIGHT;
		case TOP:
			return SWT.TOP;
		case BOTTOM:
			return SWT.BOTTOM;
		}
		return SWT.BOTTOM;
	}

	@PostConstruct
	void init(Composite parent, MToolControl trimControl) {
		simple = true;
		this.trimControl = trimControl;
		this.lastPageId = trimControl.getPersistedState().get(LAST_PAGE_ID);

		// Handle situation where intro information is not available from the
		// the application's IEclipseContext
		if (element == null || theme == null) {
			String configId = trimControl.getPersistedState().get(INTRO_CONFIG_ID);
			IntroModelRoot modelRoot = IntroPlugin.getDefault().getExtensionPointManager().getModel(configId);
			element = modelRoot.getPresentation().getLaunchBarElement();
			theme = modelRoot.getTheme();
		}

		createControl(parent);
		storeLocation(); // since we may have been dragged elsewhere
	}

	private void storeLocation() {
		IDialogSettings settings = IntroPlugin.getDefault().getDialogSettings();
		settings.put(S_STORED_LOCATION, toSWT(getLocation()));
	}

	/**
	 * Not supported anymore as of the removal of the presentation API
	 * TODO remove usage, see Bug 446171
	 *
	 * @return
	 */
	protected boolean isPlain() {
		return true;
	}

	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		computeColors(parent.getDisplay());
		container.setLayout(new BarLayout());
		// boolean vertical = (orientation & SWT.VERTICAL) != 0;
		toolBarManager = new ToolBarManager(SWT.FLAT | getOrientation());


		fillToolBar();
		// coolBar = new CoolBar(container, SWT.NULL);
		// CoolItem coolItem = new CoolItem(coolBar, SWT.NULL);
		// toolBarManager.createControl(coolBar);
		toolBarManager.createControl(container);
		ToolBar toolBar = toolBarManager.getControl();

		// coolItem.setControl(toolBar);
		// Point toolBarSize = toolBar.computeSize(SWT.DEFAULT,
		// SWT.DEFAULT);
		// Set the preffered size to the size of the toolbar plus trim
		// Point preferredSize = coolItem.computeSize(toolBarSize.x,
		// toolBarSize.y);
		// coolItem.setPreferredSize(preferredSize);

		if (bg != null) {
			toolBar.setBackground(bg);
			// coolBar.setBackground(bg);
		}
		container.addPaintListener(e -> onPaint(e));
		MenuManager manager = new MenuManager();
		IMenuListener listener = manager1 -> contextMenuAboutToShow(manager1);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(listener);
		Menu contextMenu = manager.createContextMenu(toolBarManager.getControl());
		toolBarManager.getControl().setMenu(contextMenu);
		IntroPlugin.getDefault().setLaunchBar(this);
	}

	private SideValue getLocation() {
		MElementContainer<?> parent = trimControl.getParent();
		while (parent != null) {
			if (parent instanceof MTrimBar) {
				return ((MTrimBar) parent).getSide();
			}
			parent = parent.getParent();
		}
		return SideValue.BOTTOM;
	}


	protected void onPaint(PaintEvent e) {
		GC gc = e.gc;
		Color color = fg;
		if (color == null) {
			color = e.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		}
		gc.setForeground(color);
		if (bg != null)
			gc.setBackground(bg);
		if (isPlain()) {
			Point size = container.getSize();
			gc.fillRectangle(0, 0, size.x, size.y);
			gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
		} else {
			switch (getLocation()) {
			case LEFT:
				paintLeft(gc);
				break;
			case RIGHT:
				paintRight(gc);
				break;
			case BOTTOM:
				paintBottom(gc);
				break;
			case TOP:
				break;
			}
		}
	}

	private void paintLeft(GC gc) {
		int[] top = simple ? SIMPLE_TOP_RIGHT_CORNER : TOP_RIGHT_CORNER;
		int[] bot = simple ? SIMPLE_BOTTOM_RIGHT_CORNER : BOTTOM_RIGHT_CORNER;
		int[] shape = new int[top.length + bot.length + 4];
		int index = 0;
		Point size = container.getSize();
		int x = size.x - 1;
		int y = 0;
		index = fillShape(shape, top, index, x, y, false);
		y = size.y - 1;
		index = fillShape(shape, bot, index, x, y, true);
		shape[index++] = -1;
		shape[index++] = size.y - 1;
		shape[index++] = -1;
		shape[index++] = 0;
		gc.fillPolygon(shape);
		gc.drawPolygon(shape);
	}

	private void paintBottom(GC gc) {
		int[] left = simple ? SIMPLE_TOP_LEFT_CORNER : TOP_LEFT_CORNER;
		int[] right = simple ? SIMPLE_TOP_RIGHT_CORNER : TOP_RIGHT_CORNER;
		int[] shape = new int[left.length + right.length + 4];
		int index = 0;
		Point size = container.getSize();
		int x = 0;
		int y = 0;
		index = fillShape(shape, left, index, x, y, false);
		x = size.x - 1;
		index = fillShape(shape, right, index, x, y, false);
		shape[index++] = size.x - 1;
		shape[index++] = size.y;
		shape[index++] = 0;
		shape[index++] = size.y;
		gc.fillPolygon(shape);
		gc.drawPolygon(shape);
	}

	private void paintRight(GC gc) {
		int[] top = simple ? SIMPLE_TOP_LEFT_CORNER : TOP_LEFT_CORNER;
		int[] bot = simple ? SIMPLE_BOTTOM_LEFT_CORNER : BOTTOM_LEFT_CORNER;
		int[] shape = new int[top.length + bot.length + 4];
		int index = 0;
		Point size = container.getSize();
		int x = 0;
		int y = 0;
		index = fillShape(shape, top, index, x, y, false);
		shape[index++] = size.x;
		shape[index++] = 0;
		shape[index++] = size.x;
		shape[index++] = size.y - 1;
		x = 0;
		y = size.y - 1;
		fillShape(shape, bot, index, x, y, true);
		gc.fillPolygon(shape);
		gc.drawPolygon(shape);
	}


	private int fillShape(int[] shape, int[] points, int index, int x, int y, boolean reverse) {
		int fill = points.length;
		for (int i = 0; i < points.length / 2; i++) {
			if (!reverse) {
				shape[index++] = x + points[2 * i];
				shape[index++] = y + points[2 * i + 1];
			} else {
				shape[index + fill - 2 - 2 * i] = x + points[2 * i];
				shape[index + fill - 1 - 2 * i] = y + points[2 * i + 1];
			}
		}
		if (reverse) {
			index += fill;
		}
		return index;
	}

	private void computeColors(Display display) {
		if (element.getBackground() != null) {
			String value = resolveColor(element.getBackground());
			if (value!=null) {
				RGB r = SharedStyleManager.parseRGB(value);
				if (r != null)
					bg = new Color(display, r);
			}
		}
		if (element.getForeground() != null) {
			String value = resolveColor(element.getForeground());
			if (value!=null) {
				RGB r = SharedStyleManager.parseRGB(value);
				if (r != null)
					fg = new Color(display, r);
			}
		}
	}

	private String resolveColor(String value) {
		if (value.indexOf('$')== -1)
			return value;
		if (value.charAt(0)=='$' && value.charAt(value.length()-1)=='$' && theme!=null) {
			Map properties = theme.getProperties();
			if (properties!=null) {
				String key = value.substring(1, value.length()-1);
				return (String)properties.get(key);
			}
		}
		return value;
	}

	public Control getControl() {
		return container;
	}

	@PreDestroy
	public void dispose() {
		if (container != null) {
			container.dispose();
		}
		if (toolBarManager != null) {
			toolBarManager.dispose();
			toolBarManager.removeAll();
		}

		toolBarManager = null;
		container = null;

		if (bg != null)
			bg.dispose();
		if (fg != null)
			fg.dispose();
	}

	private void fillToolBar() {
		Action action;

		closeAction = new Action("close") { //$NON-NLS-1$

			@Override
			public void run() {
				closeLaunchBar(false);
			}
		};
		closeAction.setText(Messages.IntroLaunchBar_close_label);
		closeAction.setToolTipText(Messages.IntroLaunchBar_close_tooltip);
		/*
		 * closeAction.setImageDescriptor(ImageUtil
		 * .createImageDescriptor("full/elcl16/close_view.gif")); //$NON-NLS-1$
		 */

		action = new Action("restore") { //$NON-NLS-1$

			@Override
			public void run() {
				openPage(lastPageId);
			}
		};
		action.setToolTipText(Messages.IntroLaunchBar_restore_tooltip);
		action.setImageDescriptor(ImageUtil.createImageDescriptor("full/etool16/restore_welcome.gif")); //$NON-NLS-1$
		// toolBarManager.add(closeAction);
		toolBarManager.add(action);
		toolBarManager.add(new Separator());
		if (element == null)
			return;
		IntroLaunchBarShortcut[] shortcuts = element.getShortcuts();
		for (int i = 0; i < shortcuts.length; i++) {
			IntroLaunchBarShortcut shortcut = shortcuts[i];
			addShortcut(shortcut, toolBarManager);
		}
	}

	private void addShortcut(final IntroLaunchBarShortcut shortcut, IToolBarManager toolBarManager) {
		Action action = new Action(shortcut.getToolTip()) {

			@Override
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

	protected IIntroPart closeLaunchBar(boolean restore) {

		IntroPlugin.getDefault().setLaunchBar(null);

		// if we've already been removed, this won't hurt us
		if (trimControl.getParent() != null) {
			trimControl.getParent().getChildren().remove(trimControl);
		}

		IIntroPart intro = null;
		if (restore) {
			IWorkbenchWindow window = getWorkbenchWindow();
			intro = PlatformUI.getWorkbench().getIntroManager().showIntro(window, false);
			CustomizableIntroPart cpart = (CustomizableIntroPart) intro;
			Rectangle startBounds = Geometry.toDisplay(getControl().getParent(), getControl().getBounds());
			Rectangle endBounds = Geometry.toDisplay(cpart.getControl().getParent(), cpart.getControl()
					.getBounds());

			AnimationEngine.createTweakedAnimation(window.getShell(), 400, startBounds, endBounds);
		}
		dispose();
		return intro;
	}

	private IWorkbenchWindow getWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	protected void executeShortcut(String url) {
		IIntroURL introURL = IntroURLFactory.createIntroURL(url);
		if (introURL != null) {
			IIntroPart intro = closeLaunchBar(true);
			if (intro == null)
				return;
			introURL.execute();
		}
	}

	protected void openPage(String id) {
		IIntroPart intro = closeLaunchBar(true);
		if (intro == null)
			return;
		StringBuilder url = new StringBuilder();
		url.append(LAUNCH_COMMAND_BASE);
		url.append(id);
		IIntroURL introURL = IntroURLFactory.createIntroURL(url.toString());
		if (introURL != null)
			introURL.execute();
	}

	protected void contextMenuAboutToShow(IMenuManager manager) {
		manager.add(closeAction);
	}

	public void dock(int side) {
		// dispose();
		// setLocation(side);
		// storeLocation();
	}

	private int getOrientation() {
		switch (getLocation()) {
		case LEFT:
		case RIGHT:
			return SWT.VERTICAL;
		default:
			return SWT.HORIZONTAL;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.IWindowTrim#getId()
	 */
	public String getId() {
		return LAUNCHBAR_ID;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.IWindowTrim#getDisplayName()
	 */
	public String getDisplayName() {
		return Messages.IntroLaunchBar_welcome;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.IWindowTrim#isCloseable()
	 */
	public boolean isCloseable() {
		return element.getClose();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.IWindowTrim#handleClose()
	 */
	public void handleClose() {
		closeLaunchBar(false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWindowTrim#getWidthHint()
	 */
	public int getWidthHint() {
		return SWT.DEFAULT;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWindowTrim#getHeightHint()
	 */
	public int getHeightHint() {
		return SWT.DEFAULT;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWindowTrim#isResizeable()
	 */
	public boolean isResizeable() {
		return false;
	}

}

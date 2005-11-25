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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.AbstractHyperlink;
import org.eclipse.ui.internal.IWindowTrim;
import org.eclipse.ui.internal.RectangleAnimation;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.Messages;
import org.eclipse.ui.internal.intro.impl.model.IntroLaunchBarElement;
import org.eclipse.ui.internal.intro.impl.model.IntroLaunchBarShortcut;
import org.eclipse.ui.internal.intro.impl.swt.SharedStyleManager;
import org.eclipse.ui.internal.intro.impl.util.ImageUtil;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.config.CustomizableIntroPart;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;

/**
 * This class is responsible for creating the intro launch bar in the provided
 * parent. It creates 'restore' and 'close' actions, as well as actions for each
 * shortcut element contributed in the extension point.
 * 
 * @since 3.1
 */
public class IntroLaunchBar implements IWindowTrim {
    private Composite container;

    protected Composite handle;

    protected CloseButton closeButton;

    protected Image handleImage;
    
    private static final int HANDLE_MARGIN = 2;
    private static final int HANDLE_GAP = 5;

    // private CoolBar coolBar;

    protected ToolBarManager toolBarManager;

    protected int orientation;

    protected int location;

    protected String lastPageId;

    protected Action closeAction;

    private IntroLaunchBarElement element;

    protected boolean simple;

    private String presentationId;

    static final int[] TOP_LEFT_CORNER = new int[] { 0, 6, 1, 5, 1, 4, 4, 1, 5,
            1, 6, 0 };

    static final int[] TOP_RIGHT_CORNER = new int[] { -6, 0, -5, 1, -4, 1, -1,
            4, -1, 5, 0, 6 };

    static final int[] BOTTOM_LEFT_CORNER = new int[] { 0, -6, 1, -5, 1, -4, 4,
            -1, 5, -1, 6, 0 };

    static final int[] BOTTOM_RIGHT_CORNER = new int[] { -6, 0, -5, -1, -4, -1,
            -1, -4, -1, -5, 0, -6 };

    static final int[] SIMPLE_TOP_LEFT_CORNER = new int[] { 0, 2, 1, 1, 2, 0 };

    static final int[] SIMPLE_TOP_RIGHT_CORNER = new int[] { -2, 0, -1, 1, 0, 2 };

    static final int[] SIMPLE_BOTTOM_LEFT_CORNER = new int[] { 0, -2, 1, -1, 2,
            0 };

    static final int[] SIMPLE_BOTTOM_RIGHT_CORNER = new int[] { -2, 0, -1, -1,
            0, -2 };

    static final int[] CLOSE_POINTS = new int[] { 2, 0, 4, 2, 5, 2, 7, 0, 9, 2,
            7, 4, 7, 5, 9, 7, 7, 9, 5, 7, 4, 7, 2, 9, 0, 7, 2, 5, 2, 4, 0, 2 };

    static final int CLOSE_FILL = SWT.COLOR_LIST_BACKGROUND;

    static final RGB CLOSE_HOT_FILL = new RGB(252, 160, 160);

    static final int BUTTON_FG = SWT.COLOR_WIDGET_DARK_SHADOW;

    static final String S_STORED_LOCATION = "introLaunchBar.location"; //$NON-NLS-1$

    private final static String LAUNCH_COMMAND_BASE = "http://org.eclipse.ui.intro/showPage?id="; //$NON-NLS-1$

    private Color fg;

    private Color bg;

    class CloseButton extends AbstractHyperlink {
        protected Color closeHotBg;

        private int state;

        private static final int HOVER = 1 << 1;

        private static final int ACTIVE = 1 << 2;

        public CloseButton(Composite parent, int style) {
            super(parent, style);
            closeHotBg = new Color(parent.getDisplay(), CLOSE_HOT_FILL);
            addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    closeHotBg.dispose();
                }
            });
            setCursor(null);
        }

        public Point computeSize(int wHint, int hHint, boolean changed) {
            return new Point(10, 12);
        }

        protected void handleEnter(Event e) {
            state = HOVER;
            super.handleEnter(e);
        }

        protected void handleExit(Event e) {
            state = 0;
            super.handleExit(e);
        }

        protected void handleActivate(Event e) {
            state &= ACTIVE;
            redraw();
            super.handleActivate(e);
            state &= ~ACTIVE;
            if (!isDisposed())
                redraw();
            closeAction.run();
        }

        protected void paintHyperlink(GC gc) {
            boolean hover = (state & HOVER) != 0;
            gc.setBackground(hover ? closeHotBg : getDisplay().getSystemColor(
                CLOSE_FILL));
            gc.fillPolygon(CLOSE_POINTS);
            gc.setForeground(getDisplay().getSystemColor(BUTTON_FG));
            gc.drawPolygon(CLOSE_POINTS);
        }
    }

    class BarLayout extends Layout {
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean changed) {
            boolean vertical = (orientation & SWT.VERTICAL) != 0;
            int marginWidth = vertical | isPlain() ? 1 : simple ? 3 : 7;
            int marginHeight = !vertical | isPlain() ? 1 : simple ? 3 : 7;
            int width = 0;
            int height = 0;
            Point csize = null;

            Rectangle ibounds = handleImage != null ? handleImage.getBounds()
                    : null;

            if (closeButton != null)
                csize = closeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    changed);
            Point tsize = toolBarManager.getControl().computeSize(SWT.DEFAULT,
                SWT.DEFAULT, changed);
            // Point tsize = coolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT,
            // changed);

            if (vertical) {
                width = tsize.x;
                if (csize != null)
                    width = Math.max(width, csize.x);
                height = tsize.y;
                if (ibounds != null) {
                    height += ibounds.height + 2*HANDLE_MARGIN;
                }
                if (csize != null) {
                    height += csize.y + CLOSE_SPACING;
                    if (ibounds!=null)
                        height += HANDLE_GAP;
                }
                
            } else {
                height = tsize.y;
                if (csize != null)
                    height = Math.max(height, csize.y);
                width = tsize.x;
                if (ibounds != null)
                    width += ibounds.width + 2*HANDLE_MARGIN;
                if (csize != null) {
                    width += csize.x + CLOSE_SPACING;
                    if (ibounds!=null)
                        width += HANDLE_GAP;
                }
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

        protected void layout(Composite composite, boolean changed) {
            boolean vertical = (orientation & SWT.VERTICAL) != 0;
            int marginWidth = vertical | isPlain() ? 1 : simple ? 4 : 7;
            int marginHeight = !vertical | isPlain() ? 1 : simple ? 4 : 7;
            Point csize = null;
            Rectangle ibounds = handleImage != null ? handleImage.getBounds()
                    : null;
            if (closeButton != null)
                csize = closeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    changed);
            Point tsize = toolBarManager.getControl().computeSize(SWT.DEFAULT,
                SWT.DEFAULT, changed);
            // Point tsize = coolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT,
            // changed);
            Rectangle carea = composite.getClientArea();
            int x = carea.x + (location == SWT.LEFT ? 0 : marginWidth);
            int y = carea.y + marginHeight;

            if (vertical) {
                if (handle != null && ibounds != null) {
                    y += HANDLE_MARGIN;
                    handle.setBounds(x, y, carea.width - marginWidth,
                        ibounds.height+2*HANDLE_MARGIN);
                    y += ibounds.height;
                }
                if (csize != null) {
                    if (handle!=null && ibounds != null)
                        y+= HANDLE_GAP;
                    int cx = carea.x+ carea.width/2-csize.x/2 +1;
                    closeButton.setBounds(cx, y, csize.x, csize.y);
                    y += csize.y + CLOSE_SPACING;
                }
                toolBarManager.getControl().setBounds(x, y,
                // coolBar.setBounds(x, y,
                    carea.width - marginWidth, tsize.y);
            } else {
                if (handle != null && ibounds != null) {
                    x += HANDLE_MARGIN;
                    handle.setBounds(x, y, ibounds.width+2*HANDLE_MARGIN, 
                            carea.height-marginHeight);
                    x += ibounds.width;
                }
                if (csize != null) {
                    if (handle!=null && ibounds!=null)
                        x+= HANDLE_GAP;
                    int cy = carea.y+carea.height/2 - csize.y/2 +1;
                    closeButton.setBounds(x, cy, csize.x,
                        csize.y);
                    x += csize.x + CLOSE_SPACING;
                }
                 toolBarManager.getControl().setBounds(x, y, tsize.x,
                // coolBar.setBounds(x, y, tsize.x,
                    carea.height - marginHeight);
            }
        }
    }

    private static final int CLOSE_SPACING = 1;

    public IntroLaunchBar(int orientation, String lastPageId,
            IntroLaunchBarElement element) {
        this.orientation = orientation;
        this.location = element.getLocation();
        this.lastPageId = lastPageId;
        this.element = element;

        /*
         * simple = PlatformUI.getPreferenceStore().getBoolean(
         * IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
         */
        simple = true;
        presentationId = PlatformUI.getPreferenceStore().getString(
            IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID);
        loadStoredLocation();
    }

    private void loadStoredLocation() {
        IDialogSettings settings = IntroPlugin.getDefault().getDialogSettings();
        try {
            int storedLocation = settings.getInt(S_STORED_LOCATION);
            if (storedLocation > 0)
                setLocation(storedLocation);
        } catch (NumberFormatException e) {
            // The stored value either does not exist or
            // is corrupted - just pick the default silently.
        }
    }

    private void storeLocation() {
        IDialogSettings settings = IntroPlugin.getDefault().getDialogSettings();
        settings.put(S_STORED_LOCATION, this.location);
    }

    public void createInActiveWindow() {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow();
        WorkbenchWindow wwindow = (WorkbenchWindow) window;
        createControl(window.getShell());
        wwindow.addToTrim(this, location);
        window.getShell().layout();
    }

    protected boolean isPlain() {
        return !"org.eclipse.ui.presentations.default".equals(presentationId); //$NON-NLS-1$
    }

    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NULL);
        computeColors(parent.getDisplay());
        container.setLayout(new BarLayout());
        // boolean vertical = (orientation & SWT.VERTICAL) != 0;
        toolBarManager = new ToolBarManager(SWT.FLAT | orientation);
        Listener dragListener = new Listener() {
            public void handleEvent(Event event) {
                Point position = DragUtil.getEventLoc(event);

                startDragging(position, false);
            }
        };
        if (element.getCreateHandle()) {
            if (element.getClose()) {
                closeButton = new CloseButton(container, SWT.NULL);
                closeButton.setBackground(bg);
            }
            handle = new Composite(container, SWT.NULL);
            final Cursor dragCursor = new Cursor(parent.getDisplay(),
                SWT.CURSOR_SIZEALL);
            handle.setCursor(dragCursor);
            handle.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    dragCursor.dispose();
                }
            });
            ImageDescriptor desc = element.getHandleImageDescriptor();
            if (desc != null)
                handleImage = desc.createImage();
            handle.setBackground(bg);
            handle.addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent e) {
                    onHandlePaint(e);
                }
            });
            handle.addListener(SWT.DragDetect, dragListener);
            handle.setCursor(dragCursor);
            closeButton.addListener(SWT.DragDetect, dragListener);
        }
        fillToolBar();
        // coolBar = new CoolBar(container, SWT.NULL);
        // CoolItem coolItem = new CoolItem(coolBar, SWT.NULL);
        // toolBarManager.createControl(coolBar);
        toolBarManager.createControl(container);
        ToolBar toolBar = toolBarManager.getControl();
        if (closeButton != null)
            container.setTabList(new Control[] { closeButton, toolBar });

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
        container.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                onPaint(e);
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

    protected void startDragging(Point position, boolean usingKeyboard) {
        Rectangle dragRect = DragUtil.getDisplayBounds(getControl());
        startDrag(this, dragRect, position, usingKeyboard);
    }

    private void startDrag(Object toDrag, Rectangle dragRect, Point position,
            boolean usingKeyboard) {

        DragUtil.performDrag(toDrag, dragRect, position, !usingKeyboard);
    }

    protected void onPaint(PaintEvent e) {
        GC gc = e.gc;
        Color color = fg;
        if (color == null) {
            color = e.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        }
        gc.setForeground(color);
        if (bg!=null)
            gc.setBackground(bg);
        if (isPlain()) {
            Point size = container.getSize();
            gc.fillRectangle(0, 0, size.x, size.y);
            gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
        } else {
            switch (location) {
            case SWT.LEFT:
                paintLeft(gc);
                break;
            case SWT.RIGHT:
                paintRight(gc);
                break;
            case SWT.BOTTOM:
                paintBottom(gc);
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

    protected void onHandlePaint(PaintEvent e) {
        Point size = handle.getSize();

        if (handleImage != null) {
            Rectangle ibounds = handleImage.getBounds();
            int x = ((size.x - 2*HANDLE_MARGIN) % ibounds.width) / 2 + HANDLE_MARGIN;
            int y = ((size.y - 2*HANDLE_MARGIN) % ibounds.height)/ 2 + HANDLE_MARGIN;

            for (;;) {
                e.gc.drawImage(handleImage, x, y);
                /*
                if (location == SWT.LEFT) {
                    x += ibounds.width;
                    if (x + ibounds.width >= size.x)
                        break;
                } else if (location == SWT.RIGHT) {
                    x -= ibounds.width;
                    if (x <= 0)
                        break;
                } else if (location == SWT.BOTTOM) {
                    y -= ibounds.height;
                    if (y <= 0)
                        break;
                }
                */
                if (orientation==SWT.VERTICAL) {
                    x+= ibounds.width;
                if (x+ibounds.width>size.x-HANDLE_MARGIN)
                    break;
                }
                else {
                    y+= ibounds.height;
                    if (y + ibounds.height > size.y - HANDLE_MARGIN)
                        break;
                }
            }
        }
    }

    private int fillShape(int[] shape, int[] points, int index, int x, int y,
            boolean reverse) {
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
            RGB r = SharedStyleManager.parseRGB(element.getBackground());
            if (r != null)
                bg = new Color(display, r);
        }
        if (element.getForeground() != null) {
            RGB r = SharedStyleManager.parseRGB(element.getForeground());
            if (r != null)
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
        if (bg != null)
            bg.dispose();
        if (fg != null)
            fg.dispose();
        if (handleImage != null) {
            handleImage.dispose();
            handleImage = null;
        }
    }

    private void fillToolBar() {
        Action action;

        closeAction = new Action("close") { //$NON-NLS-1$
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
        if (closeButton != null)
            closeButton.setToolTipText(closeAction.getToolTipText());

        action = new Action("restore") { //$NON-NLS-1$
            public void run() {
                openPage(lastPageId);
            }
        };
        action.setToolTipText(Messages.IntroLaunchBar_restore_tooltip);
        action.setImageDescriptor(ImageUtil
            .createImageDescriptor("full/etool16/restore_welcome.gif")); //$NON-NLS-1$
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

    private void addShortcut(final IntroLaunchBarShortcut shortcut,
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

    protected IIntroPart closeLaunchBar(boolean restore) {
        IntroPlugin.getDefault().setLaunchBar(null);
        IWorkbenchWindow window = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow();
        IIntroPart intro = null;
        if (restore) {
            intro = PlatformUI.getWorkbench().getIntroManager().showIntro(
                window, false);
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
        StringBuffer url = new StringBuffer();
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
        dispose();
        setLocation(side);
        storeLocation();
        createInActiveWindow();
    }

    private void setLocation(int location) {
        this.orientation = (location == SWT.LEFT || location == SWT.RIGHT) ? SWT.VERTICAL
                : SWT.HORIZONTAL;
        this.location = location;
    }

    public int getValidSides() {
        return SWT.LEFT | SWT.RIGHT | SWT.BOTTOM;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.IWindowTrim#getId()
     */
    public String getId() {
        return "org.eclipse.ui.internal.intro.impl.presentations.IntroLaunchBar";
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IWindowTrim#getDisplayName()
	 */
	public String getDisplayName() {
		return WorkbenchMessages.TrimCommon_IntroBar_TrimName;
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.IWindowTrim#isCloseable()
     */
    public boolean isCloseable() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.IWindowTrim#handleClose()
     */
    public void handleClose() {
        closeLaunchBar(false);
    }
    
}

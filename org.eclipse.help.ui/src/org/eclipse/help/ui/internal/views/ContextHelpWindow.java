/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 93374
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IContext;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ContextHelpWindow extends Window implements IPageChangedListener {
	private ReusableHelpPart helpPart;

	private static final int DOCK_MARGIN = 10;

	private static final int CLIP_ALLOWANCE = 5;

	private FormToolkit toolkit;

	private Listener listener;

	private ControlListener parentListener;

	private Rectangle savedPbounds;

	private Rectangle savedBounds;

	private boolean parentResizeBlocked = false;

	public ContextHelpWindow(Shell parent) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
		if (!Platform.getWS().equals(Platform.WS_GTK)) {
			parentListener = new ControlListener() {
				public void controlMoved(ControlEvent e) {
					maintainRelativePosition();
				}

				public void controlResized(ControlEvent e) {
					onParentWindowResize();
				}
			};
			listener = new Listener() {
				public void handleEvent(Event e) {
					switch (e.type) {
					case SWT.FocusIn:
					case SWT.Selection:
						update((Control) e.widget);
						break;
					case SWT.Move:
						if (onWindowMove())
							e.doit = false;
						break;
					case SWT.Resize:
						onWindowResize();
						break;
					}
				}
			};
		}
	}
	
	public void showSearch() {
		helpPart.showPage(IHelpUIConstants.HV_FSEARCH_PAGE, true);
	}

	private void maintainRelativePosition() {
		if (savedPbounds == null || isDocked())
			dock(true);
		else {
			Rectangle pbounds = getShell().getParent().getBounds();
			Rectangle bounds = getShell().getBounds();
			int deltaX = pbounds.x - savedPbounds.x;
			int deltaY = pbounds.y - savedPbounds.y;
			int newX = bounds.x + deltaX;
			int newY = bounds.y + deltaY;
			boolean doDock = false;
			Rectangle dbounds = getShell().getDisplay().getBounds();
			if (newX > dbounds.width - bounds.width) {
				newX = dbounds.width - bounds.width;
				if (pbounds.x + pbounds.width > newX)
					doDock = true;
			} else if (newX < 0)
				doDock = true;
			if (newY > dbounds.height - bounds.height) {
				newY = dbounds.height - bounds.height;
			} else if (newY < 0)
				newY = 0;
			if (doDock) {
				dock(true);
				return;
			}
			getShell().setLocation(newX, newY);
			savedPbounds = pbounds;
			savedBounds = getShell().getBounds();
		}
	}

	protected Control createContents(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
				HyperlinkGroup.UNDERLINE_HOVER);
		toolkit.getColors().initializeSectionToolBarColors();
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);

		GridData gd;
		ToolBarManager tbm = new ToolBarManager(SWT.FLAT);
		tbm.createControl(container);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.grabExcessHorizontalSpace = true;
		tbm.getControl().setLayoutData(gd);
		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.heightHint = 1;
		separator.setLayoutData(gd);
		helpPart = new ReusableHelpPart(PlatformUI.getWorkbench()
				.getProgressService());
		helpPart.init(null, tbm, null, null);
		helpPart.setDefaultContextHelpText(Messages.HelpView_defaultText); //		
		helpPart.createControl(container, toolkit);
		helpPart.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		if (!Platform.getWS().equals(Platform.WS_GTK)) 
			hookListeners();
		helpPart.showPage(IHelpUIConstants.HV_CONTEXT_HELP_PAGE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		return container;
	}

	private void hookListeners() {
		Shell shell = getShell();
		shell.addListener(SWT.Move, listener);
		shell.addListener(SWT.Resize, listener);
		hookPageChangeListener(shell.getParent(), listener);
		shell.getParent().addControlListener(parentListener);
	}

	private void unhookListeners() {
		Shell shell = getShell();
		shell.getParent().removeControlListener(parentListener);
		unhookPageChangeListener(shell.getParent(), listener);
		shell.removeListener(SWT.Move, listener);
		shell.removeListener(SWT.Resize, listener);
	}

	private void hookPageChangeListener(Composite parent, Listener listener) {
		Object data = parent.getData();
		if (data instanceof IPageChangeProvider) {
			((IPageChangeProvider) data).addPageChangedListener(this);
		}
	}

	private void unhookPageChangeListener(Composite parent, Listener listener) {
		Object data = parent.getData();
		if (data instanceof IPageChangeProvider) {
			((IPageChangeProvider) data).removePageChangedListener(this);
		}
	}

	public void dock(boolean changeSides) {
		getShell().setBounds(computeDockedBounds(changeSides));
	}

	public Rectangle computeDockedBounds(boolean changeSides) {
		Display d = getShell().getDisplay();
		Rectangle dbounds = d.getBounds();
		Rectangle pbounds = getShell().getParent().getBounds();

		int leftMargin = pbounds.x;
		int rightMargin = dbounds.width - pbounds.x - pbounds.width;
		int centeredLeftMargin = dbounds.width / 2 - pbounds.width / 2;
		boolean rightParent = leftMargin > centeredLeftMargin;
		int currentX = getShell().getLocation().x;
		int newSize = getShell().getSize().x;
		boolean leftOK = newSize <= leftMargin + CLIP_ALLOWANCE;
		boolean rightOK = newSize <= rightMargin + CLIP_ALLOWANCE;
		int x;
		// first try to keep the same side
		if (currentX < pbounds.x && leftOK && (!changeSides || !rightParent)) {
			x = pbounds.x - newSize;
		} else if (currentX > pbounds.x && rightOK
				&& (!changeSides || rightParent)) {
			x = pbounds.x + pbounds.width;
		}
		// must switch side
		else if (changeSides) {
			if (rightOK)
				x = pbounds.x + pbounds.width;
			else if (leftOK)
				x = pbounds.x - newSize;
			else {
				// pick the margin that has more space, reduce size
				if (leftMargin > rightMargin) {
					newSize = leftMargin;
					x = pbounds.x - newSize;
				} else {
					newSize = rightMargin;
					x = dbounds.width - newSize;
				}
			}
		} else {
			if (currentX < pbounds.x) {
				newSize = leftMargin;
				x = pbounds.x - newSize;
			} else {
				newSize = rightMargin;
				x = dbounds.width - newSize;
			}
		}
		savedPbounds = pbounds;
		savedBounds = getShell().getBounds();
		return new Rectangle(x, pbounds.y, newSize, pbounds.height);
	}

	private boolean onWindowMove() {
		if (savedBounds == null) {
			savedBounds = getShell().getBounds();
			savedPbounds = getShell().getParent().getBounds();
			return false;
		}
		Rectangle bounds = getShell().getBounds();
		Rectangle pbounds = getShell().getParent().getBounds();
		if (bounds.y != savedBounds.y) {
			// vertical move
			if (bounds.y + bounds.height == savedBounds.y + savedBounds.height) {
				// upper edge resize
				if (isDocked()) {
					savedBounds = bounds;
					savedPbounds = pbounds;
					return false;
				}
			}
		}
		boolean doDock = false;

		if (bounds.x < pbounds.x) {
			// left
			int deltaX = bounds.x - savedBounds.x;
			if (deltaX > 0 || bounds.x + bounds.width > pbounds.x) {
				// moving closer - check for dock snap
				int distance = pbounds.x - bounds.x - bounds.width;
				if (Math.abs(distance) <= DOCK_MARGIN)
					doDock = true;
			}
		} else {
			// right
			int deltaX = bounds.x - savedBounds.x;
			if (deltaX < 0 || bounds.x < pbounds.x + pbounds.width) {
				// moving closer - check for dock snap
				int distance = bounds.x - pbounds.x - pbounds.width;
				if (Math.abs(distance) <= DOCK_MARGIN)
					doDock = true;
			}
		}
		if (bounds.y + bounds.height < pbounds.y) // above
			doDock = false;
		if (pbounds.y + pbounds.height < bounds.y) // below
			doDock = false;
		if (doDock)
			dock(false);
		savedBounds = getShell().getBounds();
		savedPbounds = getShell().getParent().getBounds();
		return doDock;
	}

	private void onWindowResize() {
		if (isDocked()) {
			Rectangle bounds = getShell().getBounds();
			Rectangle pbounds = getShell().getParent().getBounds();
			if (bounds.height != savedBounds.height) {
				Shell parent = (Shell) getShell().getParent();
				if ((parent.getStyle() & SWT.RESIZE) != 0) {
					parentResizeBlocked = true;
					parent.setBounds(pbounds.x, bounds.y, pbounds.width,
							bounds.height);
					parentResizeBlocked = false;
				}
			}
		}
		savedBounds = getShell().getBounds();
	}

	private void onParentWindowResize() {
		if (!parentResizeBlocked && isDocked()) {
			Rectangle bounds = getShell().getBounds();
			Rectangle pbounds = getShell().getParent().getBounds();
			if (bounds.x == savedPbounds.x + savedPbounds.width) {
				// right
				if (savedPbounds.x + savedPbounds.width != pbounds.x
						+ pbounds.width)
					// right edge moved
					dock(false);
			} else {
			}
			getShell().setSize(getShell().getSize().x,
					getShell().getParent().getSize().y);
		}
		savedPbounds = getShell().getParent().getBounds();
	}

	public void update(Control c) {
		helpPart.update(null, c);
	}

	public void update(IContext context, Control c) {
		helpPart.showPage(IHelpUIConstants.HV_CONTEXT_HELP_PAGE);
		helpPart.update(context, null, c);
	}

	public boolean close() {
		if (!Platform.getWS().equals(Platform.WS_GTK)) 
			unhookListeners();
		if (super.close()) {
			if (toolkit != null) {
				toolkit.dispose();
				toolkit = null;
			}
			if (helpPart != null) {
				helpPart.dispose();
				helpPart = null;
			}
			return true;
		}
		return false;
	}

	private boolean isDocked() {
		if (savedPbounds == null)
			return false;
		return isDocked(savedBounds, savedPbounds);
	}

	private boolean isDocked(Rectangle bounds, Rectangle pbounds) {
		if (pbounds.height != bounds.height)
			return false;
		if (bounds.y + bounds.height < pbounds.y) // above
			return false;
		if (pbounds.y + pbounds.height < bounds.y) // below
			return false;
		return bounds.x == pbounds.x + pbounds.width
				|| bounds.x == pbounds.x - bounds.width;
	}

	public void pageChanged(PageChangedEvent event) {
		Object page = event.getSelectedPage();
		Control c = null;
		if (page instanceof IDialogPage) {
			c = ((IDialogPage) page).getControl();
		} else {
			c = getShell().getDisplay().getFocusControl();
			if (c instanceof TabFolder) {
				TabFolder folder = (TabFolder) c;
				TabItem[] selection = folder.getSelection();
				if (selection.length == 1) {
					c = selection[0].getControl();
				}
			}
		}
		update(null, c);
	}
}
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.IContext;
import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ContextHelpWindow extends Window {
	private ReusableHelpPart helpPart;
	private static final int DOCK_MARGIN = 10;

	private FormToolkit toolkit;

	private Listener listener;
	private ControlListener parentListener;
	private Rectangle savedPbounds;
	private Rectangle savedBounds;
	private boolean parentResizeBlocked=false;

	public ContextHelpWindow(Shell parent) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
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
				case SWT.Activate:
					update(e.display.getFocusControl());
					break;
				case SWT.Move:
					if (onWindowMove())
						e.doit=false;
					break;
				case SWT.Resize:
					onWindowResize();
					break;
				}
			}
		};
	}
	
	

	public void setPartFocus() {
		if (helpPart != null)
			helpPart.setFocus();
	}
	
	public void maintainRelativePosition() {
		if (savedPbounds==null || isDocked())
			dock(true);
		else {
			Rectangle pbounds = getShell().getParent().getBounds();
			Rectangle bounds = getShell().getBounds();
			int deltaX = pbounds.x - savedPbounds.x;
			int deltaY = pbounds.y - savedPbounds.y;
			int newX = bounds.x+deltaX;
			int newY = bounds.y+deltaY;
			boolean doDock=false;
			Rectangle dbounds = getShell().getDisplay().getBounds();
			if (newX > dbounds.width-bounds.width) {
				newX = dbounds.width-bounds.width;
				if (pbounds.x+pbounds.width>newX)
					doDock=true;
			}
			else if (newX < 0)
				doDock=true;
			if (newY > dbounds.height-bounds.height) {
				newY = dbounds.height - bounds.height;
			}
			else if (newY < 0)
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
		// toolkit.setBackground(toolkit.getColors().createNoContentBackground());
		toolkit.getColors().initializeSectionToolBarColors();
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);

		GridData gd;
		ToolBarManager tbm = new ToolBarManager();
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
		helpPart.init(null, tbm, null);
		helpPart.setDefaultContextHelpText(HelpUIResources
				.getString("HelpView.defaultText")); //$NON-NLS-1$		
		helpPart.createControl(container, toolkit);
		helpPart.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		hookListeners();
		helpPart.showPage(IHelpUIConstants.HV_CONTEXT_HELP_PAGE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		return container;
	}

	private void hookListeners() {
		Shell shell = getShell();
		shell.addListener(SWT.Move, listener);
		shell.addListener(SWT.Resize, listener);
		shell.getParent().addListener(SWT.Activate, listener);
		shell.getParent().addControlListener(parentListener);
	}

	private void unhookListeners() {
		Shell shell = getShell();
		shell.getParent().removeListener(SWT.Activate, listener);
		shell.getParent().removeControlListener(parentListener);
		shell.removeListener(SWT.Move, listener);
		shell.removeListener(SWT.Resize, listener);
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
		int currentX = getShell().getLocation().x;
		// try right
		int newSize = getShell().getSize().x;
		boolean leftOK = newSize<=leftMargin;
		boolean rightOK = newSize<=rightMargin;
		int x;
		// first try to keep the same side
		if (currentX<pbounds.x && leftOK) {
			x = pbounds.x-newSize;
		}
		else if (currentX>pbounds.x && rightOK) {
			x = pbounds.x+pbounds.width;
		}
		//must switch side
		else if (changeSides) {
			if (rightOK)
				x = pbounds.x+pbounds.width;
			else if (leftOK)
				x = pbounds.x-newSize;
			else {
				// pick the margin that has more space, reduce size
				if (leftMargin>rightMargin) {
					newSize = leftMargin;
					x = pbounds.x-newSize;
				}
				else {
					newSize = rightMargin;
					x = dbounds.width - newSize;
				}
			}
		}
		else {
			if (currentX<pbounds.x) {
				newSize = leftMargin;
				x = pbounds.x-newSize;
			}
			else {
				newSize = rightMargin;
				x = dbounds.width-newSize;
			}
		}
		savedPbounds = pbounds;
		savedBounds = getShell().getBounds();
		return new Rectangle(x, pbounds.y, newSize, pbounds.height);
	}	
	
	private boolean onWindowMove() {
		if (savedBounds==null) {
			savedBounds = getShell().getBounds();
			savedPbounds = getShell().getParent().getBounds();
			return false;
		}
		Rectangle bounds = getShell().getBounds();
		Rectangle pbounds = getShell().getParent().getBounds();
		if (bounds.y!=savedBounds.y) {
			// vertical move
			if (bounds.y+bounds.height == savedBounds.y+savedBounds.height) {
				// upper edge resize
				if (isDocked()) {
					savedBounds = bounds;
					savedPbounds = pbounds;
					return false;
				}
			}
		}
		boolean doDock=false;

		if (bounds.x<pbounds.x) {
			// left
			int deltaX = bounds.x-savedBounds.x;
			if (deltaX >0 || bounds.x+bounds.width>pbounds.x) {
				// moving closer - check for dock snap
				int distance = pbounds.x - bounds.x-bounds.width;
				if (Math.abs(distance) <=DOCK_MARGIN)
					doDock=true;
			}
		}
		else {
			// right
			int deltaX = bounds.x-savedBounds.x;
			if (deltaX<0 || bounds.x<pbounds.x+pbounds.width) {
				//moving closer - check for dock snap
				int distance = bounds.x-pbounds.x-pbounds.width;
				if (Math.abs(distance) <=DOCK_MARGIN)
					doDock=true;
			}
		}
		if (bounds.y + bounds.height < pbounds.y) // above
			doDock=false;
		if (pbounds.y + pbounds.height < bounds.y) // below
			doDock=false;
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
				Shell parent = (Shell)getShell().getParent();
				parentResizeBlocked=true;
				parent.setBounds(pbounds.x, bounds.y, 
						pbounds.width, bounds.height);
				parentResizeBlocked=false;
			}
		}
		savedBounds = getShell().getBounds();
	}
	
	private void onParentWindowResize() {
		if (!parentResizeBlocked && isDocked()) {
			Rectangle bounds = getShell().getBounds();
			Rectangle pbounds = getShell().getParent().getBounds();
			if (bounds.x==savedPbounds.x+savedPbounds.width) {
				// right
				if (savedPbounds.x+savedPbounds.width!=pbounds.x+pbounds.width)
					//right edge moved
					dock(false);
			}
			else {
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
		if (savedPbounds==null)
			return false;
		return isDocked(savedBounds, savedPbounds);
	}
	private boolean isDocked(Rectangle bounds, Rectangle pbounds) {
		if (pbounds.height!=bounds.height)
			return false;
		if (bounds.y + bounds.height < pbounds.y) // above
			return false;
		if (pbounds.y + pbounds.height < bounds.y) // below
			return false;
		return bounds.x==pbounds.x+pbounds.width ||
			bounds.x==pbounds.x-bounds.width;
	}
}
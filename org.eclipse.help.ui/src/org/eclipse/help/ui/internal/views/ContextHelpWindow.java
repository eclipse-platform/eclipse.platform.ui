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
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ContextHelpWindow extends Window {
	private ReusableHelpPart helpPart;
	private static final int DOCK_MARGIN = 10;

	private FormToolkit toolkit;

	private Listener listener;
	private Rectangle savedPbounds;
	private Rectangle savedBounds;

	public ContextHelpWindow(Shell parent) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				maintainRelativePosition();
			}

			public void controlResized(ControlEvent e) {
				onParentWindowResize();
			}
		});
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
		if (savedPbounds==null || isDocked(savedPbounds))
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
		helpPart.createControl(container, toolkit);
		helpPart.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		hookListeners(getShell().getParent());
		helpPart.showPage(IHelpUIConstants.HV_CONTEXT_HELP_PAGE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		return container;
	}

	private void hookListeners(Control c) {
		getShell().addListener(SWT.Move, listener);
		getShell().addListener(SWT.Resize, listener);	
		c.addListener(SWT.Activate, listener);
	}

	private void unhookListeners(Control c) {
		c.removeListener(SWT.Activate, listener);
		getShell().removeListener(SWT.Move, listener);
		getShell().removeListener(SWT.Resize, listener);
	}
	
	public void dock(boolean changeSides) {
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
		getShell().setLocation(x, pbounds.y);
		getShell().setSize(newSize, pbounds.height);
		savedPbounds = pbounds;
		savedBounds = getShell().getBounds();
	}
	
	private boolean onWindowMove() {
		if (savedBounds==null) {
			savedBounds = getShell().getBounds();
			savedPbounds = getShell().getParent().getBounds();
			return false;
		}
		boolean doDock=false;
		Rectangle bounds = getShell().getBounds();
		Rectangle pbounds = getShell().getParent().getBounds();
		if (bounds.x<pbounds.x) {
			// left
			int deltaX = bounds.x-savedBounds.x;
			if (deltaX<0) {
				// moving away - ignore
			}
			else {
				// moving closer - check for dock snap
				int distance = pbounds.x - bounds.x-bounds.width;
				if (distance <=DOCK_MARGIN)
					doDock=true;
			}
		}
		else {
			// right
			int deltaX = bounds.x-savedBounds.x;
			if (deltaX>0) {
				// moving away - ignore
			}
			else {
				//moving closer - check for dock snap
				int distance = bounds.x-pbounds.x-pbounds.width;
				if (distance <=DOCK_MARGIN)
					doDock=true;
			}
		}
		if (doDock)
			dock(true);
		savedBounds = getShell().getBounds();
		savedPbounds = getShell().getParent().getBounds();
		return doDock;
	}
	
	private void onWindowResize() {
		savedBounds = getShell().getBounds();
	}
	
	private void onParentWindowResize() {
		if (isDocked())
			getShell().setSize(getShell().getSize().x,
					getShell().getParent().getSize().y);
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
		unhookListeners(getShell().getParent());
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
		return isDocked(savedPbounds);
	}
	private boolean isDocked(Rectangle pbounds) {
		Rectangle bounds = getShell().getBounds();
		if (pbounds.height!=bounds.height)
			return false;
		return bounds.x==pbounds.x+pbounds.width ||
			bounds.x==pbounds.x-bounds.width;
	}
}
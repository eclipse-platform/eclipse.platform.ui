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
import org.eclipse.jface.action.*;
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

	private FormToolkit toolkit;

	private Listener listener;

	public ContextHelpWindow(Shell parent) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				syncHelpBounds();
			}

			public void controlResized(ControlEvent e) {
				syncHelpBounds();
			}
		});
		listener = new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Activate:
					update(e.display.getFocusControl());
					break;
				case SWT.Move:
					syncHelpBounds();
					break;
				case SWT.Resize:
					syncHelpBounds();
					break;
				}
			}
		};
	}

	public void setPartFocus() {
		if (helpPart != null)
			helpPart.setFocus();
	}

	public void syncHelpBounds() {
		Display d = getShell().getDisplay();
		Rectangle dbounds = d.getBounds();
		Rectangle pbounds = getShell().getParent().getBounds();
		
		int leftMargin = pbounds.x;
		int rightMargin = dbounds.width - pbounds.x - pbounds.width;
		// try right
		int newSize = getShell().getSize().x;
		int x;
		if (newSize<=rightMargin)
			x = pbounds.x+pbounds.width;
		else if (newSize<=leftMargin)
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
		getShell().setLocation(x, pbounds.y);
		getShell().setSize(newSize, pbounds.height);
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
		helpPart.init(null, tbm, new StatusLineManager());
		helpPart.createControl(container, toolkit);
		helpPart.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		hookListeners(getShell().getParent());
		helpPart.showPage(IHelpUIConstants.HV_CONTEXT_HELP_PAGE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		return container;
	}

	private void hookListeners(Control c) {
		c.addListener(SWT.Move, listener);
		c.addListener(SWT.Resize, listener);	
		c.addListener(SWT.Activate, listener);
	}

	private void unhookListeners(Control c) {
		c.removeListener(SWT.Activate, listener);
		c.removeListener(SWT.Move, listener);
		c.removeListener(SWT.Resize, listener);
	}

	public void update(Control c) {
		helpPart.update(null, c);
	}
	
	public void update(IContext context, Control c) {
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
}
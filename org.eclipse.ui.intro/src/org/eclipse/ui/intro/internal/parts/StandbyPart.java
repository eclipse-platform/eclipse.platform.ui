/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro.internal.parts;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.cheatsheets.registry.*;
import org.eclipse.ui.internal.cheatsheets.views.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.util.*;

public class StandbyPart {
	private FormToolkit toolkit;
	private IntroModelRoot model;
	private ImageHyperlink returnLink;
	private Composite container;
	private Composite content;
	private ContextHelpPart helpPart;
	private CheatSheetView cheatSheet;
	private int VGAP = 10;
	private int VMARGIN = 5;
	private int HMARGIN = 5;
	private IIntroSite site;

	class ViewSiteAdapter implements IViewSite {
		public IActionBars getActionBars() {
			return site.getActionBars();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchPartSite#getId()
		 */
		public String getId() {
			return site.getId();
		}
		public String getSecondaryId() {
			return null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
		 */
		public IKeyBindingService getKeyBindingService() {
			return site.getKeyBindingService();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchPartSite#getPluginId()
		 */
		public String getPluginId() {
			return site.getPluginId();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchPartSite#getRegisteredName()
		 */
		public String getRegisteredName() {
			return site.getRegisteredName();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(org.eclipse.jface.action.MenuManager,
		 *      org.eclipse.jface.viewers.ISelectionProvider)
		 */
		public void registerContextMenu(
			MenuManager menuManager,
			ISelectionProvider selectionProvider) {
			site.registerContextMenu(menuManager, selectionProvider);
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(java.lang.String,
		 *      org.eclipse.jface.action.MenuManager,
		 *      org.eclipse.jface.viewers.ISelectionProvider)
		 */
		public void registerContextMenu(
			String menuId,
			MenuManager menuManager,
			ISelectionProvider selectionProvider) {
			registerContextMenu(menuId, menuManager, selectionProvider);
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchSite#getPage()
		 */
		public IWorkbenchPage getPage() {
			return site.getPage();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchSite#getSelectionProvider()
		 */
		public ISelectionProvider getSelectionProvider() {
			return getSelectionProvider();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchSite#getShell()
		 */
		public Shell getShell() {
			return site.getShell();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchSite#getWorkbenchWindow()
		 */
		public IWorkbenchWindow getWorkbenchWindow() {
			return site.getWorkbenchWindow();
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IWorkbenchSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
		 */
		public void setSelectionProvider(ISelectionProvider provider) {
			site.setSelectionProvider(provider);
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			return site.getAdapter(adapter);
		}
	}

	class StandbyLayout extends Layout {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
		 *      int, int, boolean)
		 */
		protected Point computeSize(
			Composite composite,
			int wHint,
			int hHint,
			boolean flushCache) {
			Point lsize =
				returnLink.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			Point csize =
				content.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			int width = Math.max(lsize.x + 2 * HMARGIN, csize.x);
			int height = HMARGIN + lsize.y + VGAP + csize.y;
			return new Point(width, height);
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
		 *      boolean)
		 */
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle carea = composite.getClientArea();
			int lwidth = carea.width - HMARGIN * 2;
			Point lsize =
				returnLink.computeSize(lwidth, SWT.DEFAULT, flushCache);
			int x = HMARGIN;
			int y = VMARGIN;
			returnLink.setBounds(x, y, carea.width, lsize.y);
			x = 0;
			y += lsize.y + VGAP;
			content.setBounds(
				x,
				y,
				carea.width,
				carea.height - VMARGIN - lsize.y - VGAP);
		}
	}

	/**
	 * @param parent
	 */
	public StandbyPart(IntroModelRoot model, IIntroSite site) {
		this.model = model;
		this.site = site;
	}

	public IIntroSite getSite() {
		return site;
	}

	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		container = toolkit.createComposite(parent);
		container.setLayout(new StandbyLayout());
		ImageUtil.registerImage(ImageUtil.BACK, "home_nav.gif");
		returnLink = toolkit.createImageHyperlink(container, SWT.WRAP);
		returnLink.setImage(ImageUtil.getImage(ImageUtil.BACK));
		returnLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doReturn();
			}
		});
		content = toolkit.createComposite(container);
		StackLayout slayout = new StackLayout();
		slayout.marginWidth = slayout.marginHeight = 0;
		content.setLayout(slayout);
		Util.highlight(content, SWT.COLOR_GREEN);
		helpPart = new ContextHelpPart(this);
		Util.highlight(container, SWT.COLOR_CYAN);
	}

	public void setInput(Object input) {
		if (input == null) {
			// show context help
			showContextPart();
		} else if (input instanceof String) {
			CheatSheetElement element = findCheatSheet((String) input);
			showCheatSheet(element);
		}
		updateReturnLinkLabel();
		container.layout();
	}

	private CheatSheetElement findCheatSheet(String id) {
		CheatSheetRegistryReader reader =
			CheatSheetRegistryReader.getInstance();
		return reader.findCheatSheet(id);
	}
	private void showContextPart() {
		Control c = helpPart.getControl();
		if (c == null) {
			c = helpPart.createPartControl(content, toolkit);
		}
		setTopControl(c);
	}

	private void showCheatSheet(CheatSheetElement element) {
		if (cheatSheet == null) {
			cheatSheet = new CheatSheetView();
			try {
				cheatSheet.init(new ViewSiteAdapter());
				cheatSheet.createPartControl(content);
			} catch (PartInitException e) {
				return;
			}
		}
		cheatSheet.setContent(element);
		Control[] controls = content.getChildren();
		Control c;
		if (controls.length == 0)
			c = controls[0];
		else {
			if (controls[0] == helpPart.getControl())
				c = controls[1];
			else
				c = controls[0];
		}
		setTopControl(c);
	}

	private void setTopControl(Control c) {
		StackLayout layout = (StackLayout) content.getLayout();
		layout.topControl = c;
		if (c instanceof Composite)
			 ((Composite) c).layout();
		content.layout();
		container.layout();
	}

	private void updateReturnLinkLabel() {
		AbstractIntroPage page = model.getCurrentPage();
		String linkText = "Return to Introduction";
		if (page instanceof IntroPage) {
			linkText = "Return to " + page.getTitle();
		}
		returnLink.setText(linkText);
		returnLink.setToolTipText(returnLink.getText());
	}

	private void doReturn() {
		IIntroPart part = PlatformUI.getWorkbench().findIntro();
		PlatformUI.getWorkbench().setIntroStandby(part, false);
	}

	public void dispose() {
		if (cheatSheet != null)
			cheatSheet.dispose();
		if (helpPart != null)
			helpPart.dispose();
		toolkit.dispose();
	}
}
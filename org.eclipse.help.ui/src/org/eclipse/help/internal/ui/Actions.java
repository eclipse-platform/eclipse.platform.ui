package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.File;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.help.internal.ui.util.WorkbenchResources;

/**
 * Actions
 */
public class Actions {
	// Images
	static ImageDescriptor home;
	static ImageDescriptor forward;
	static ImageDescriptor back;
	static ImageDescriptor print;
	static ImageDescriptor copy;
	static ImageDescriptor synchronize;

	static ImageDescriptor actionsImage;
	static ImageDescriptor hidenav;
	static {
		synchronize =
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("resynch_icon"));
		home =
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("home_icon"));
		back =
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("back_icon"));
		forward =
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("forward_icon"));
		print =
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("printer_icon"));
		copy =
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("copy_icon"));
		hidenav =
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("hidenav_icon"));
		actionsImage =
			ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("actions_icon"));
	}

	/**
	 */
	public static abstract class WebAction extends Action {
		protected IBrowser web;
		public WebAction(IBrowser web, String name) {
			super(name);
			this.web = web;
		}
	}

	/**
	 */
	public static class CopyAction extends WebAction {
		public CopyAction(IBrowser web) {
			super(web, WorkbenchResources.getString("copy"));
			setText("&Copy@Ctrl+C");
			setImageDescriptor(copy);
			setToolTipText(WorkbenchResources.getString("Copy_1"));
			WorkbenchHelp.setHelp(
				this,
				new String[] {
					IHelpUIConstants.COPY_ACTION,
					IHelpUIConstants.EMBEDDED_HELP_VIEW});
		}
		public void run() {
			web.copy();
		}
	}

	/**
	 */
	public static class HomeAction extends WebAction {
		protected String fHome;
		public HomeAction(IBrowser web, String homeURL) {
			super(web, WorkbenchResources.getString("home"));
			this.fHome = homeURL;
			setToolTipText(WorkbenchResources.getString("Home_page"));
			setImageDescriptor(home);
			setText("&Home@Ctrl+H");
			WorkbenchHelp.setHelp(
				this,
				new String[] {
					IHelpUIConstants.HOME_ACTION,
					IHelpUIConstants.EMBEDDED_HELP_VIEW});
		}

		public HomeAction(IBrowser web) {
			this(web, null);
		}

		public void run() {
			web.navigate(this.fHome);
		}

		public void setHome(String homeURL) {
			this.fHome = homeURL;
		}
	}

	/**
	 */
	public static class BackAction extends WebAction {
		public BackAction(IBrowser web) {
			super(web, WorkbenchResources.getString("back"));
			setToolTipText(WorkbenchResources.getString("Previous_page"));
			setImageDescriptor(back);
			setText("&Back@BACKSPACE");
			WorkbenchHelp.setHelp(
				this,
				new String[] {
					IHelpUIConstants.BACK_ACTION,
					IHelpUIConstants.EMBEDDED_HELP_VIEW });
		}
		public void run() {
			web.back();
		}

		public void update() {

		}
	}

	/**
	 */
	public static class ForwardAction extends WebAction {
		public ForwardAction(IBrowser web) {
			super(web, WorkbenchResources.getString("forward"));
			setToolTipText(WorkbenchResources.getString("Next_page"));
			setImageDescriptor(forward);
			setText("Forward@Alt+RIGHT_ARROW");
			WorkbenchHelp.setHelp(
				this,
				new String[] {
					IHelpUIConstants.FORWARD_ACTION,
					IHelpUIConstants.EMBEDDED_HELP_VIEW });
		}
		public void run() {
			web.forward();
		}
	}

	/**
	 */
	public static class PrintAction extends WebAction {
		public PrintAction(IBrowser web) {
			super(web, WorkbenchResources.getString("print"));
			setToolTipText(WorkbenchResources.getString("Print_page"));
			setImageDescriptor(print);
			setText("&Print@Ctrl+P");
			WorkbenchHelp.setHelp(
				this,
				new String[] {
					IHelpUIConstants.PRINT_ACTION,
					IHelpUIConstants.EMBEDDED_HELP_VIEW});
		}
		public void run() {
			web.print();
		}
	}

	/**
	 * Action that synchronizes document viewed in the browser with TOC
	 */
	public static class SynchronizeAction extends WebAction {
		ISelectionProvider selectionProvider;
		ShowHideAction showHideAction;
		
		public SynchronizeAction(IBrowser web, ISelectionProvider selectionProvider, ShowHideAction showHideAction) {
			super(web, WorkbenchResources.getString("synchronize"));
			this.showHideAction = showHideAction;
			setText("&Synchronize@Ctrl+S");
			setImageDescriptor(synchronize);
			setToolTipText(WorkbenchResources.getString("Synchronize_with_TOC"));
			this.selectionProvider = selectionProvider;
			WorkbenchHelp.setHelp(
				this,
				new String[] {
					IHelpUIConstants.SYNCH_ACTION,
					IHelpUIConstants.EMBEDDED_HELP_VIEW});
		}
		public void run() {
			String currentURL = web.getLocationURL();
			if (currentURL == null)
				return;
			if (this.selectionProvider != null)
			{
				selectionProvider.setSelection(new StructuredSelection(currentURL));
				showHideAction.showNavigation();
			}
		}
	}

	/**
	 * Action that shows/hides the TOC   */
	public static class ShowHideAction extends WebAction {
		EmbeddedHelpView view;
		
		public ShowHideAction(EmbeddedHelpView view) {
			super(null, WorkbenchResources.getString("toggle"));
			setText("&Hide navigation@Ctrl+H");
			setImageDescriptor(hidenav);
			setToolTipText(WorkbenchResources.getString("Hide_TOC"));
			this.view = view;
			WorkbenchHelp.setHelp(
				this,
				new String[] {
					IHelpUIConstants.SHOW_HIDE_ACTION,
					IHelpUIConstants.EMBEDDED_HELP_VIEW});
		}
		public void run() {
			boolean hidden = view.toggleNavigation();
			if (hidden) {
				setText("&Show navigation@Ctrl+H");
				setToolTipText(WorkbenchResources.getString("Show_TOC"));
			} else {
				setText("&Hide navigation@Ctrl+H");
				setToolTipText(WorkbenchResources.getString("Hide_TOC"));
			}
		}
		public void showNavigation()
		{
			if (isChecked()) // i.e. is navigation hidden
			{
				view.toggleNavigation();
				setText("&Hide navigation@Ctrl+H");
				setToolTipText(WorkbenchResources.getString("Hide_TOC"));
				setChecked(false);
			}
		}
	}
	/**
	 */
	private Actions() {
		super();
	}
}

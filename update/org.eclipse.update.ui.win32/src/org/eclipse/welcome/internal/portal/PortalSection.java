/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import java.io.*;
import java.net.*;
import java.util.Hashtable;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.*;
import org.eclipse.welcome.internal.WelcomePortal;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PortalSection extends FormSection  {
	private SectionDescriptor desc;
	private WelcomePortalForm form;
	private HyperlinkAction eclipseAction;
	private HTTPAction httpAction;
	private HyperlinkAction helpAction;
	private Composite formContainer;
	private Hashtable eclipseActions;

	class ActionWrapper extends Action {
		IActionDelegate delegate;

		public ActionWrapper(String arg, IActionDelegate delegate) {
			super(arg);
			this.delegate = delegate;
			if (delegate instanceof IWorkbenchWindowActionDelegate)
				((IWorkbenchWindowActionDelegate) delegate).init(
					WelcomePortal.getActiveWorkbenchWindow());
		}
		public void run() {
			delegate.run(this);
		}
	}

	public PortalSection(SectionDescriptor desc, WelcomePortalForm form) {
		this.desc = desc;
		this.form = form;
		setDescriptionPainted(false);
		setCollapsable(true);
		setHeaderText(desc.getName());
		makeHyperlinkActions();
	}

	private void makeHyperlinkActions() {
		IActionBars bars =
			form.getPortal().getEditor().getEditorSite().getActionBars();
		httpAction = new HTTPAction() {
			public void linkActivated(IHyperlinkSegment link) {
				String url = link.getArg();
				if (url != null)
					WelcomePortal.showURL(link.getText(), url);
			}
		};
		httpAction.setStatusLineManager(bars.getStatusLineManager());
		eclipseAction = new HyperlinkAction() {
			public void linkActivated(IHyperlinkSegment link) {
				String arg = link.getArg();
				if (arg != null)
					runEclipseAction(arg);
			}
		};
		eclipseAction.setStatusLineManager(bars.getStatusLineManager());
		helpAction = new HyperlinkAction() {
			public void linkActivated(IHyperlinkSegment link) {
				String arg = link.getArg();
				if (arg != null)
					openEclipseHelp(arg);
			}
		};
		helpAction.setStatusLineManager(bars.getStatusLineManager());
	}
	public Composite createClient(
		Composite parent,
		FormWidgetFactory factory) {
		IConfigurationElement config = desc.getConfig();
		formContainer = factory.createComposite(parent);
		HTMLTableLayout layout = new HTMLTableLayout();
		formContainer.setLayout(layout);
		layout.topMargin = layout.bottomMargin = 0;
		layout.leftMargin = layout.rightMargin = 0;
		IConfigurationElement[] children = config.getChildren();
		for (int i = 0; i < children.length; i++) {
			IConfigurationElement child = children[i];
			if (child.getName().equals("form")) {
				createForm(formContainer, child, factory);
			}
		}
		return formContainer;
	}

	private void createForm(
		final Composite parent,
		IConfigurationElement config,
		FormWidgetFactory factory) {
		String type = config.getAttribute("type");
		if (type != null && type.equals("custom")) {
			createCustomForm(parent, config, factory);
			return;
		}
		final FormEngine engine = factory.createFormEngine(parent);
		//engine.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		engine.setHyperlinkSettings(factory.getHyperlinkHandler());
		TableData td = new TableData(TableData.FILL, TableData.TOP);
		td.grabHorizontal = true;
		engine.setLayoutData(td);
		engine.registerTextObject("url", httpAction);
		engine.registerTextObject("action", eclipseAction);
		engine.registerTextObject("help", helpAction);
		if (type == null || type.equals("local")) {
			String value = config.getValue();
			engine.load("<form>" + value + "</form>", true, false);
		} else if (type.equals("dynamic")) {
			IFormContentProvider provider = createContentProvider(config);
			if (provider!=null) {
				engine.load(provider.getContent(), true, false);
				provider.setContentObserver(new IFormContentObserver() {
					public void contentChanged(IFormContentProvider provider) {
						engine.load(provider.getContent(), true, false);
						reflow();
					}
				});
			}
		} else if (type.equals("remote")) {
			String urlName = config.getAttribute("url");
			try {
				final URL url = new URL(urlName);
				Runnable loader = new Runnable() {
					public void run() {
						InputStream is = null;
						try {
							is = url.openStream();
							engine.load(is, false);
							parent.getDisplay().syncExec(new Runnable() {
								public void run() {
									reflow();
								}
							});
						} catch (final IOException e) {
							parent.getDisplay().syncExec(new Runnable() {
								public void run() {
									WelcomePortal.logException(e);
								}
							});

						} finally {
							try {
								if (is != null)
									is.close();
							} catch (IOException e) {
							}
						}
					}
				};
				Thread t = new Thread(loader);
				t.start();
			} catch (MalformedURLException e) {
				WelcomePortal.logException(e);
			}
		}
	}

	private void createCustomForm(
		Composite parent,
		IConfigurationElement config,
		FormWidgetFactory factory) {
		try {
			Object obj = config.createExecutableExtension("class");
			if (obj instanceof IPortalSectionForm) {
				IPortalSectionForm psection = (IPortalSectionForm) obj;
				//psection.init(form.getPortal());
				Control control = psection.createControl(parent, factory);
				TableData td = new TableData(TableData.FILL, TableData.TOP);
				td.grabHorizontal = true;
				control.setLayoutData(td);
			}
		} catch (CoreException e) {
			WelcomePortal.logException(e);
		}
	}
	
	private IFormContentProvider createContentProvider(IConfigurationElement config) {
		try {
			Object obj = config.createExecutableExtension("class");
			if (obj instanceof IFormContentProvider) {
				return (IFormContentProvider) obj;
			}
		} catch (CoreException e) {
			WelcomePortal.logException(e);
		}
		return null;
	}
	protected void reflow() {
		formContainer.layout();
		super.reflow();
		form.updateSize();
	}

	private void runEclipseAction(final String arg) {
		BusyIndicator
			.showWhile(form.getControl().getDisplay(), new Runnable() {
			public void run() {
				final IAction action = getEclipseAction(arg);
				if (action != null)
					action.run();
			}
		});
	}

	private IAction getEclipseAction(String arg) {
		if (eclipseActions == null) {
			eclipseActions = new Hashtable();
		}
		IAction action = (IAction) eclipseActions.get(arg);
		if (action != null)
			return action;
		// load
		int col = arg.indexOf(':');
		String pluginId = arg.substring(0, col);
		String className = arg.substring(col + 1);
		IPluginDescriptor desc =
			Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		if (desc == null) {
			logActionLinkError(pluginId, className);
			return null;
		}
		Class actionClass;
		try {
			actionClass = desc.getPluginClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			logActionLinkError(pluginId, className);
			return null;
		}
		try {
			Object obj = actionClass.newInstance();
			if (obj instanceof IAction)
				action = (IAction) obj;
			else
				action = new ActionWrapper(arg, (IActionDelegate) obj);
		} catch (InstantiationException e) {
			logActionLinkError(pluginId, className);
			return null;
		} catch (IllegalAccessException e) {
			logActionLinkError(pluginId, className);
			return null;
		} catch (ClassCastException e) {
			logActionLinkError(pluginId, className);
			return null;
		}
		eclipseActions.put(arg, action);
		return action;
	}

	private void logActionLinkError(String pluginId, String className) {
		IStatus status =
			new Status(
				IStatus.ERROR,
				WelcomePortal.getPluginId(),
				IStatus.OK,
				"Unable to load class "
					+ className
					+ " from plug-in "
					+ pluginId
					+ ".",
				null);
		WelcomePortal.log(status, true);
	}
	
	private void openEclipseHelp(String arg) {
		WorkbenchHelp.getHelpSupport().displayHelpResource(arg);
	}
}

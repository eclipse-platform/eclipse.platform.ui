/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.context.IStyledContext;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.*;

public class ContextHelpPart extends SectionPart implements IHelpPart {
	private ReusableHelpPart parent;

	private static final String HELP_KEY = "org.eclipse.ui.help"; //$NON-NLS-1$	

	private FormText text;
	private Control lastControl;
	private IContextProvider lastProvider;
	private IContext lastContext;
	private IWorkbenchPart lastPart;

	private String defaultText = ""; //$NON-NLS-1$

	private String id;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public ContextHelpPart(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.EXPANDED | Section.TWISTIE
				| Section.TITLE_BAR);
		Section section = getSection();
		section.marginWidth = 5;
		section.setText(HelpUIResources.getString("ContextHelpPart.about")); //$NON-NLS-1$
		Composite container = toolkit.createComposite(section);
		section.setClient(container);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				if (e.getState() && (lastProvider!=null || lastControl!=null)) {
					String helpText = createContextHelp(lastProvider, lastControl);
					updateText(helpText);
				}
			}
		});
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = layout.bottomMargin = 0;
		layout.leftMargin = layout.rightMargin = 0;
		layout.verticalSpacing = 10;
		container.setLayout(layout);
		text = toolkit.createFormText(container, true);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		text.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		String key = IHelpUIConstants.IMAGE_FILE_F1TOPIC;
		text.setImage(key, HelpUIResources.getImage(key));
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doOpenLink(e.getHref());
			}
		});
		text.setText(defaultText, false, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return getSection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		getSection().setVisible(visible);
	}

	/**
	 * @return Returns the defaultText.
	 */
	public String getDefaultText() {
		return defaultText;
	}

	/**
	 * @param defaultText
	 *            The defaultText to set.
	 */
	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
		if (text != null)
			text.setText(defaultText, false, false);
	}

	private void doOpenLink(Object href) {
		parent.showURL((String) href);
	}

	public void handleActivation(Control c, IWorkbenchPart part) {
		if (text.isDisposed())
			return;
		lastControl = c;
		lastPart = part;
		lastProvider = null;
		String helpText = createContextHelp(c);
		if (getSection().isExpanded())
			updateText(helpText);
		updateDynamicHelp();
	}

	private void updateDynamicHelp() {
		if (lastProvider!=null || lastControl!=null)
			updateDynamicHelp(lastProvider!=null?lastProvider.getSearchExpression(lastControl):null, lastControl);		
	}
	
	public void handleActivation(IContext context, Control c, IWorkbenchPart part) {
		if (text.isDisposed())
			return;
		lastControl = c;
		lastContext = context;
		lastPart = part;
		lastProvider = null;
		String helpText = formatHelpContext(context);
		updateTitle();
		if (getSection().isExpanded())
			updateText(helpText);
		updateDynamicHelp();
	}

	public void handleActivation(IContextProvider provider, Control c, IWorkbenchPart part) {
		if (text.isDisposed())
			return;
		lastControl = c;
		lastProvider = provider;
		lastPart = part;
		String helpText = createContextHelp(provider, c);
		updateTitle();
		if (getSection().isExpanded())
			updateText(helpText);
		updateDynamicHelp();
	}
	
	private void updateTitle() {
		if (lastPart!=null)
			getSection().setText(HelpUIResources.getString("ContextHelpPart.aboutP", lastPart.getSite().getRegisteredName())); //$NON-NLS-1$
		else
			getSection().setText(HelpUIResources.getString("ContextHelpPart.about")); //$NON-NLS-1$
	}

	private void updateText(String helpText) {
		text.setText(helpText != null ? helpText : defaultText,
				helpText != null, //$NON-NLS-1$
				false);
		getSection().layout();
		parent.reflow();
	}

	private void updateDynamicHelp(String expression, Control c) {
		if (expression == null) {
			expression = computeDefaultSearchExpression(c);
		}
		DynamicHelpPart part = (DynamicHelpPart) parent
				.findPart(IHelpUIConstants.HV_SEARCH_RESULT);
		if (part != null) {
			if (expression != null)
				part.startSearch(expression, lastContext);
		}
	}

	private String computeDefaultSearchExpression(Control c) {
		StringBuffer buff = new StringBuffer();
		Composite parent = c.getParent();

		while (parent != null) {
			Object data = parent.getData();
			if (data instanceof IWizardContainer) {
				WizardDialog wd = (WizardDialog) data;
				buff.append("\""); //$NON-NLS-1$
				buff.append(wd.getCurrentPage().getName());
				buff.append("\" OR \""); //$NON-NLS-1$
				buff.append(wd.getCurrentPage().getWizard().getWindowTitle());
				buff.append("\""); //$NON-NLS-1$
				break;
			} else if (data instanceof IWorkbenchWindow) {
				IWorkbenchWindow window = (IWorkbenchWindow) data;
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IWorkbenchPart part = lastPart;
					if (part != null) {
						buff.append("\""); //$NON-NLS-1$
						if (part instanceof IViewPart)
							buff.append(HelpUIResources.getString("ContextHelpPart.query.view", part.getSite().getRegisteredName())); //$NON-NLS-1$
						buff.append("\" "); //$NON-NLS-1$
					}
					IPerspectiveDescriptor persp = page.getPerspective();
					if (persp != null) {
						if (buff.length() > 0)
							buff.append("OR "); //$NON-NLS-1$
						buff.append("\""); //$NON-NLS-1$
						buff.append(HelpUIResources.getString("ContextHelpPart.query.perspective", persp.getLabel())); //$NON-NLS-1$
						buff.append("\""); //$NON-NLS-1$
					}
				}
				break;
			}
			parent = parent.getParent();
		}
		return buff.length() > 0 ? buff.toString().trim() : null;
	}

	private String createContextHelp(IContextProvider provider, Control c) {
		if (provider==null) return createContextHelp(c);
		lastContext = provider.getContext(c);
		if (lastContext != null) {
			return formatHelpContext(lastContext);
		}
		return null;
	}

	private String createContextHelp(Control page) {
		String text = null;
		lastContext = null;		
		if (page != null) {
			if (page != null /* && page.isVisible() */&& !page.isDisposed()) {
				IContext helpContext = findHelpContext(page);
				if (helpContext != null) {
					text = formatHelpContext(helpContext);
					lastContext = helpContext;
				}
			}
		}
		return text;
	}

	private IContext findHelpContext(Control c) {
		String contextId = null;
		Control node = c;
		do {
			contextId = (String) node.getData(HELP_KEY);
			if (contextId != null)
				break;
			node = node.getParent();
		} while (node != null);
		if (contextId != null) {
			return HelpSystem.getContext(contextId);
		}
		return null;
	}

	private String formatHelpContext(IContext context) {
		String locale = Platform.getNL();
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<form>"); //$NON-NLS-1$
		sbuf.append("<p>"); //$NON-NLS-1$
		sbuf.append(decodeContextBoldTags(context));
		sbuf.append("</p>"); //$NON-NLS-1$
		IHelpResource[] links = context.getRelatedTopics();
		if (links != null && links.length > 0) {
			sbuf.append("<p><span color=\""); //$NON-NLS-1$
			sbuf.append(FormColors.TITLE);
			sbuf.append("\">"); //$NON-NLS-1$
			sbuf.append(HelpUIResources.getString("ContextHelpPart.seeAlso")); //$NON-NLS-1$
			sbuf.append("</span></p>"); //$NON-NLS-1$
			for (int i = 0; i < links.length; i++) {
				IHelpResource link = links[i];
				sbuf.append("<li style=\"image\" value=\""); //$NON-NLS-1$
				sbuf.append(IHelpUIConstants.IMAGE_FILE_F1TOPIC);
				sbuf.append("\" indent=\"21\">"); //$NON-NLS-1$
				sbuf.append("<a href=\""); //$NON-NLS-1$
				sbuf.append(link.getHref());
				sbuf.append("\" alt=\""); //$NON-NLS-1$
				sbuf.append(getTopicCategory(link.getHref(), locale));
				sbuf.append("\">"); //$NON-NLS-1$
				sbuf.append(link.getLabel());
				sbuf.append("</a>"); //$NON-NLS-1$
				sbuf.append("</li>"); //$NON-NLS-1$
			}
		}
		sbuf.append("</form>"); //$NON-NLS-1$
		return sbuf.toString();
	}

	private String getTopicCategory(String href, String locale) {
		IToc[] tocs = HelpPlugin.getTocManager().getTocs(locale);
		for (int i = 0; i < tocs.length; i++) {
			ITopic topic = tocs[i].getTopic(href);
			if (topic != null)
				return tocs[i].getLabel();
		}
		return null;
	}

	/**
	 * Make sure to support the Help system bold tag. Help systen returns a
	 * regular string for getText(). Use internal apis for now to get bold.
	 * 
	 * @param context
	 * @return
	 */
	private String decodeContextBoldTags(IContext context) {
		String styledText;
		if (context instanceof IStyledContext) {
			styledText = ((IStyledContext) context).getStyledText();
		} else {
			styledText = context.getText();
		}
		String decodedString = styledText.replaceAll("<@#\\$b>", "<b>"); //$NON-NLS-1$ //$NON-NLS-2$
		decodedString = decodedString.replaceAll("</@#\\$b>", "</b>"); //$NON-NLS-1$ //$NON-NLS-2$
		return decodedString;
	}

	public boolean setFormInput(Object input) {
		if (input instanceof ContextHelpProviderInput) {
			ContextHelpProviderInput chinput = (ContextHelpProviderInput) input;
			if (chinput.getContext()!=null)
				handleActivation(chinput.getContext(), chinput.getControl(), chinput.getPart());
			else
				handleActivation(chinput.getProvider(), chinput.getControl(), chinput.getPart());
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return parent.fillFormContextMenu(text, manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return text.equals(control);
	}

	public IAction getGlobalAction(String id) {
		if (id.equals(ActionFactory.COPY.getId()))
			return parent.getCopyAction();
		return null;
	}

	public void stop() {
	}
}
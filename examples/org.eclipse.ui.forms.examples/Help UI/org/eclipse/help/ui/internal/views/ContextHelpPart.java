/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.*;
import org.eclipse.help.internal.context.IStyledContext;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContextHelpPart extends SectionPart implements IHelpPart {
	private ReusableHelpPart parent;
	private static final String HELP_KEY = "org.eclipse.ui.help"; //$NON-NLS-1$	
	private FormText text;
	private String defaultText="";
	private String id;
	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public ContextHelpPart(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.EXPANDED|Section.TWISTIE|Section.TITLE_BAR);
		Section section = getSection();
		section.setText("About");
		text = toolkit.createFormText(section, true);
		section.setClient(text);
		text.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		text.setImage(ExamplesPlugin.IMG_HELP_TOPIC, ExamplesPlugin
				.getDefault().getImage(ExamplesPlugin.IMG_HELP_TOPIC));
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doOpenLink(e.getHref());
			}
		});
		text.setText(defaultText, false, false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return getSection();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}
	public String getId() {
		return id;
	}
	/* (non-Javadoc)
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
	 * @param defaultText The defaultText to set.
	 */
	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
		if (text!=null)
			text.setText(defaultText, false, false);
	}
	private void doOpenLink(Object href) {
		parent.showURL((String)href);
	}
	public void handleActivation(Control page) {
		if (text.isDisposed())
			return;
		String helpText = createContextHelp(page);
		updateText(helpText);
	}
	
	public void handleActivation(IContextHelpProvider provider, Control c) {
		if (text.isDisposed())
			return;
		String helpText = createContextHelp(provider, c);
		updateText(helpText);
	}
	
	private void updateText(String helpText) {
		text.setText(helpText != null ? helpText : defaultText, helpText != null, //$NON-NLS-1$
						false);
		getSection().layout();
		parent.reflow();		
	}
	
	private String createContextHelp(IContextHelpProvider provider, Control c) {
		IContext helpContext = provider.getHelpContext(c);
		if (helpContext!=null) {
			return formatHelpContext(helpContext);
		}
		return null;
	}

	private String createContextHelp(Control page) {
		String text = null;
		if (page != null) {
			if (page != null /* && page.isVisible() */&& !page.isDisposed()) {
				IContext helpContext = findHelpContext(page);
				if (helpContext != null) {
					text = formatHelpContext(helpContext);
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
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<form>"); //$NON-NLS-1$
		sbuf.append("<p>"); //$NON-NLS-1$
		sbuf.append(decodeContextBoldTags(context));
		sbuf.append("</p>"); //$NON-NLS-1$
		IHelpResource[] links = context.getRelatedTopics();
		if (links != null && links.length > 0) {
			sbuf.append("<p><span color=\"");
			sbuf.append(FormColors.TITLE);
			sbuf.append("\">See also:</span></p>");
			for (int i = 0; i < links.length; i++) {
				IHelpResource link = links[i];
				sbuf.append("<li style=\"image\" value=\"");
				sbuf.append(ExamplesPlugin.IMG_HELP_TOPIC);
				sbuf.append("\" indent=\"15\">"); //$NON-NLS-1$
				sbuf.append("<a href=\""); //$NON-NLS-1$
				sbuf.append(link.getHref());
				sbuf.append("\">"); //$NON-NLS-1$
				sbuf.append(link.getLabel());
				sbuf.append("</a>"); //$NON-NLS-1$
				sbuf.append("</li>"); //$NON-NLS-1$
			}
		}
		sbuf.append("</form>"); //$NON-NLS-1$
		return sbuf.toString();
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
		if (input instanceof Control) {
			handleActivation((Control)input);
			return true;
		}
		if (input instanceof ContextHelpProviderInput) {
			ContextHelpProviderInput chinput = (ContextHelpProviderInput)input;
			handleActivation(chinput.getProvider(), chinput.getControl());
			return true;
		}
		return false;
	}
}
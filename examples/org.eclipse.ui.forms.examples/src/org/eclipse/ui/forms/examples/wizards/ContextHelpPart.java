/*
 * Created on Oct 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.*;
import org.eclipse.help.internal.context.IStyledContext;
import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.help.ui.internal.search.*;
import org.eclipse.jface.operation.*;
import org.eclipse.search.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ContextHelpPart {
	private ScrolledForm form;

	//private Label title;

	private Text phraseText;

	private FormText text;

	private FormText searchResults;

	private String defaultText;

	private IRunnableContext runnableContext;

	private SorterByScore resultSorter;

	private static final String HELP_KEY = "org.eclipse.ui.help"; //$NON-NLS-1$

	public ContextHelpPart(IRunnableContext runnableContext) {
		this.runnableContext = runnableContext;
		resultSorter = new SorterByScore();
	}

	public void createControl(Composite parent, FormToolkit toolkit) {
		// parent form
		form = toolkit.createScrolledForm(parent);
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		form.setText("");
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		section.setText("About");
		text = toolkit.createFormText(section, true);
		text.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		text.setImage(ExamplesPlugin.IMG_HELP_TOPIC, ExamplesPlugin
				.getDefault().getImage(ExamplesPlugin.IMG_HELP_TOPIC));
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				openLink(e.getHref());
			}
		});
		section.setClient(text);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL,
				TableWrapData.FILL));
		text.setText(defaultText, false, false);

		toolkit.createLabel(form.getBody(), null);
		section = toolkit.createSection(form.getBody(), Section.TITLE_BAR
				| Section.TWISTIE | Section.EXPANDED);
		section.setText("Search");
		Composite helpContainer = toolkit.createComposite(section);
		section.setClient(helpContainer);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL));
		GridLayout glayout = new GridLayout();
		glayout.numColumns = 3;
		glayout.marginWidth = glayout.marginHeight = 1;
		helpContainer.setLayout(glayout);
		helpContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		toolkit.paintBordersFor(helpContainer);
		Label label = toolkit.createLabel(helpContainer, "Search");
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		phraseText = toolkit.createText(helpContainer, ""); //$NON-NLS-1$
		phraseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Button button = toolkit.createButton(helpContainer,
				"Go", SWT.PUSH); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				doSearch(phraseText.getText());
			}
		});
		button.setEnabled(false);
		phraseText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String text = phraseText.getText();
				button.setEnabled(text.length() > 0);
			}
		});
		phraseText.addKeyListener(new KeyAdapter() {

			public void keyReleased(KeyEvent e) {
				if (e.character == '\r') {
					if (button.isEnabled())
						doSearch(phraseText.getText());
				}
			}
		});
		toolkit.paintBordersFor(form.getBody());

		searchResults = toolkit.createFormText(form.getBody(), true);
		//searchResults.setBackground(helpContainer.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		searchResults.setImage(ExamplesPlugin.IMG_HELP_TOPIC, ExamplesPlugin
				.getDefault().getImage(ExamplesPlugin.IMG_HELP_TOPIC));
		searchResults.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if (href.toString().equals("_more"))
					doExternalSearch(phraseText.getText());
				else
					openLink(e.getHref());
			}
		});
		searchResults.setText("", false, false);
	}

	public Control getControl() {
		return form;
	}
	
	public ScrolledForm getForm() {
		return form;
	}

	private void doExternalSearch(String phrase) {
		try {
			String ephrase = URLEncoder.encode(phrase, "UTF-8"); //$NON-NLS-1$
			String query = "tab=search&searchWord=" + ephrase; //$NON-NLS-1$
			WorkbenchHelp.displayHelpResource(query);
		} catch (UnsupportedEncodingException e) {
			System.out.println(e);
		}
	}

	private void doSearch(String phrase) {
		SearchQueryData data = new SearchQueryData();
		data.setMaxHits(5);
		data.setSearchWord(phrase);
		final HelpSearchQuery query = new HelpSearchQuery(data);
		final ISearchResult result = query.getSearchResult();
		final StringBuffer resultBuffer = new StringBuffer();
		result.addListener(new ISearchResultListener() {
			public void searchResultChanged(SearchResultEvent e) {
				final HelpSearchResult hresult = (HelpSearchResult) result;
				getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateResults(resultBuffer, hresult);
					}
				});
			}
		});
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				query.run(monitor);
			}
		};
		try {
			runnableContext.run(true, true, op);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}

	private void updateResults(StringBuffer buff, HelpSearchResult hresult) {
		buff.delete(0, buff.length());
		if (hresult.getMatchCount() > 0) {
			buff.append("<form>");
			buff.append("<p><span color=\"");
			buff.append(FormColors.TITLE);
			buff.append("\">Search results:</span></p>");
			Object[] elements = hresult.getElements();
			resultSorter.sort(null, elements);

			for (int i = 0; i < elements.length; i++) {
				SearchHit hit = (SearchHit) elements[i];
				buff.append("<li indent=\"21\" style=\"image\" value=\"");
				buff.append(ExamplesPlugin.IMG_HELP_TOPIC);
				buff.append("\">");
				buff.append("<a href=\"");
				buff.append(hit.getHref());
				buff.append("\">");
				buff.append(hit.getLabel());
				buff.append("</a>");
				buff.append("</li>");
			}
			if (elements.length > 0) {
				buff.append("<p><a href=\"_more\">More results...</a></p>");
			}
			buff.append("</form>");
			searchResults.setText(buff.toString(), true, false);
		}
		else
			searchResults.setText("", false, false);
		form.reflow(true);
	}

	private void handlePageActivation(Control page) {
		if (text.isDisposed())
			return;
		//title.setText("What is" + " \"" + part.getSite().getRegisteredName()
		// + "\"?"); //$NON-NLS-1$ //$NON-NLS-2$
		String helpText = createContextHelp(page);
		text.setText(helpText != null ? helpText : "", helpText != null, //$NON-NLS-1$
				false);
		//form.getBody().layout();
		form.reflow(true);
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
				sbuf.append("<li style=\"text\" indent=\"2\">"); //$NON-NLS-1$
				sbuf.append("<img href=\""); //$NON-NLS-1$
				sbuf.append(ExamplesPlugin.IMG_HELP_TOPIC);
				sbuf.append("\"/> "); //$NON-NLS-1$
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

	private void openLink(Object href) {
		String url = (String) href;
		if (url != null)
			WorkbenchHelp.displayHelpResource(url);
	}

	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setFocus()
	 */
	public void setFocus() {
		phraseText.setFocus();
	}

	public void update(Control control) {
		if (form != null)
			handlePageActivation(control);
	}
}
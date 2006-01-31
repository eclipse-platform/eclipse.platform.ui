package org.eclipse.ui.internal.intro.impl.model;

import java.io.StringReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.search.ISearchIndex;
import org.eclipse.help.search.LuceneSearchParticipant;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;
import org.osgi.framework.Bundle;

/**
 * An implementation of the Lucene search participant that adds Welcome content into the local help
 * index so that it can be searched.
 * 
 */

public class IntroSearchParticipant extends LuceneSearchParticipant {

	private IntroModelRoot model;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.search.LuceneSearchParticipant#getContributingPlugins()
	 */
	public Set getContributingPlugins() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"org.eclipse.ui.intro.config"); //$NON-NLS-1$
		HashSet set = new HashSet();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getName().equals("config")) //$NON-NLS-1$
				continue;
			set.add(element.getNamespace());
		}
		elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"org.eclipse.ui.intro.configExtension"); //$NON-NLS-1$
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getName().equals("configExtension")) //$NON-NLS-1$
				continue;
			set.add(element.getNamespace());
		}
		return set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.search.LuceneSearchParticipant#getAllDocuments(java.lang.String)
	 */
	public Set getAllDocuments(String locale) {
		HashSet set = new HashSet();
		IProduct product = Platform.getProduct();
		if (product == null) {
			return set;
		}
		String productId = product.getId();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"org.eclipse.ui.intro"); //$NON-NLS-1$
		String targetIntroId = null;
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("introProductBinding")) { //$NON-NLS-1$
				String pid = element.getAttribute("productId"); //$NON-NLS-1$
				String iid = element.getAttribute("introId"); //$NON-NLS-1$
				if (productId.equals(pid)) {
					targetIntroId = iid;
					break;
				}
			}
		}
		if (targetIntroId == null)
			return set;
		elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.intro.config"); //$NON-NLS-1$
		IConfigurationElement config = null;
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("config")) { //$NON-NLS-1$
				String iid = element.getAttribute("introId"); //$NON-NLS-1$
				if (targetIntroId.equals(iid)) {
					config = element;
					break;
				}
			}
		}
		if (config == null)
			return set;
		String configId = config.getAttribute("id"); //$NON-NLS-1$
		ExtensionPointManager extensionPointManager = IntroPlugin.getDefault().getExtensionPointManager();
		model = extensionPointManager.getModel(configId);
		if (model != null && model.hasValidConfig())
			loadFromModel(model, set, locale);
		return set;
	}

	private void loadFromModel(IntroModelRoot model, HashSet set, String locale) {
		IntroPage[] pages = model.getPages();
		for (int i = 0; i < pages.length; i++) {
			IntroPage page = pages[i];
			Bundle bundle = page.getBundle();
			String bundleId = bundle.getSymbolicName();
			String content = page.getRawContent();
			String pageId = page.getId();
			String href;
			if (content != null)
				href = resolveVariables(bundleId, content, locale);
			else
				href = pageId;
			set.add("/" + bundleId + "/" + href + "?id=" + pageId); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.search.LuceneSearchParticipant#addDocument(java.lang.String,
	 *      java.lang.String, java.net.URL, java.lang.String, java.lang.String,
	 *      org.apache.lucene.document.Document)
	 */
	public IStatus addDocument(ISearchIndex index, String pluginId, String name, URL url, String id,
			Document doc) {
		if (model == null)
			return Status.CANCEL_STATUS;
		IntroPage page = getPage(id);
		if (page == null)
			return Status.CANCEL_STATUS;
		return addPage(index, pluginId, name, url, page, doc);
	}

	private IntroPage getPage(String id) {
		IntroPage[] pages = model.getPages();
		for (int i = 0; i < pages.length; i++) {
			if (pages[i].getId().equals(id))
				return pages[i];
		}
		return null;
	}

	private IStatus addPage(ISearchIndex index, String pluginId, String name, URL url, IntroPage page,
			Document doc) {
		AbstractIntroElement[] children = page.getChildren();
		if (children.length > 0) {
			StringBuffer buf = new StringBuffer();
			addChildren(children, buf, doc);
			doc.add(Field.Text("contents", new StringReader(buf.toString()))); //$NON-NLS-1$
			doc.add(Field.Text("exact_contents", new StringReader(buf.toString()))); //$NON-NLS-1$
			return Status.OK_STATUS;
		}
		// delegate to the help system
		return index.addDocument(pluginId, name, url, page.getId(), doc);
	}

	private void addChildren(AbstractIntroElement[] children, StringBuffer buf, Document doc) {
		for (int i = 0; i < children.length; i++) {
			AbstractIntroElement child = children[i];
			if (child instanceof AbstractTextElement) {
				String text = ((AbstractTextElement) child).getText();
				appendNewText(buf, text);
			} else if (child instanceof IntroText) {
				appendNewText(buf, ((IntroText) child).getText());
				if (child instanceof IntroPageTitle)
					addTitle(((IntroPageTitle) child).getTitle(), doc);
			} else if (child instanceof AbstractIntroContainer) {
				AbstractIntroContainer container = (AbstractIntroContainer) child;
				AbstractIntroElement[] cc = container.getChildren();
				addChildren(cc, buf, doc);
			}
		}
	}

	private void appendNewText(StringBuffer buf, String text) {
		if (buf.length() > 0)
			buf.append(" "); //$NON-NLS-1$
		buf.append(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.search.LuceneSearchParticipant#clear()
	 */
	public void clear() {
		model = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.search.LuceneSearchParticipant#open(java.lang.String)
	 */
	public boolean open(String id) {
		IIntroManager introManager = PlatformUI.getWorkbench().getIntroManager();
		IIntroPart intro = introManager
				.showIntro(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), false);
		if (intro == null)
			return false;
		IIntroURL url = IntroURLFactory.createIntroURL("http://org.eclipse.ui.intro/showPage?id=" + id); //$NON-NLS-1$
		return url.execute();
	}
}
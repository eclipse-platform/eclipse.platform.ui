package org.eclipse.ui.internal.cheatsheets;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.search.XMLSearchParticipant;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.xml.sax.Attributes;

public class CheatsheetSearchParticipant extends XMLSearchParticipant {
	private static final String EL_CHEATSHEET = "cheatsheet";

	private static final String EL_ITEM = "item";

	private static final String INTRO_DESC = "cheatsheet/intro/description";

	private static final String ITEM_DESC = "cheatsheet/item/description";

	private static final String ATT_TITLE = "title";

	/**
	 * Returns all the documents that this participant knows about. This method
	 * is only used for participants that handle documents outside of the help
	 * system's TOC.
	 * 
	 * @return a set of hrefs for documents managed by this participant.
	 */
	public Set getAllDocuments(String locale) {
		HashSet set = new HashSet();
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"org.eclipse.ui.cheatsheets.cheatSheetContent");
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getName().equals("cheatsheet"))
				continue;
			String fileName = element.getAttribute("contentFile");
			String id = element.getAttribute("id");
			String pluginId = element.getNamespace();
			fileName = resolveVariables(pluginId, fileName, locale);
			set.add("/" + pluginId + "/" + fileName + "?id=" + id);
		}
		return set;
	}

	public Set getContributingPlugins() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"org.eclipse.ui.cheatsheets.cheatSheetContent");
		HashSet set = new HashSet();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getName().equals("cheatsheet"))
				continue;
			set.add(element.getNamespace());
		}
		return set;
	}

	protected void handleStartElement(String name, Attributes attributes,
			IParsedXMLContent data) {
		if (name.equals(EL_CHEATSHEET))
			data.setTitle(attributes.getValue(ATT_TITLE));
		else if (name.equals(EL_ITEM))
			data.addText(attributes.getValue(ATT_TITLE));
	}

	protected void handleEndElement(String name, IParsedXMLContent data) {
	}

	protected void handleText(String text, IParsedXMLContent data) {
		String stackPath = getElementStackPath();
		if (stackPath.equalsIgnoreCase(INTRO_DESC)) {
			data.addText(text);
			data.addToSummary(text);
			return;
		}
		if (stackPath.equalsIgnoreCase(ITEM_DESC)) {
			data.addText(text);
			return;
		}
	}

	public boolean open(String id) {
		Action openAction = new OpenCheatSheetAction(id);
		openAction.run();
		return true;
	}
}
package org.eclipse.update.internal.ui.search;

import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;

public class DefaultUpdatesSearchObject extends SearchObject {
	private static final String KEY_NAME = "AvailableUpdates.name";
	private static final String SECTION_ID = "defaultUpdateSearch";

	public DefaultUpdatesSearchObject() {
		super(
			UpdateUIPlugin.getResourceString(KEY_NAME),
			SearchCategoryRegistryReader.getDefault().getDescriptor(
				"org.eclipse.update.ui.updates"),
			true);
		setModel(UpdateUIPlugin.getDefault().getUpdateModel());
	}
	private IDialogSettings getSection() {
		IDialogSettings master = UpdateUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = master.getSection(SECTION_ID);
		if (section == null) {
			section = master.addNewSection(SECTION_ID);
		}
		return section;
	}
	public boolean getSearchMyComputer() {
		return getSection().getBoolean(S_MY_COMPUTER);
	}

	public void setSearchMyComputer(boolean value) {
		getSection().put(S_MY_COMPUTER, value);
	}

	public void setSearchBookmarks(boolean value) {
		getSection().put(S_BOOKMARKS, value);
	}
	public boolean getSearchBookmarks() {
		return getSection().getBoolean(S_BOOKMARKS);
	}

	public void setSearchDiscovery(boolean value) {
		getSection().put(S_DISCOVERY, value);
	}
	public boolean getSearchDiscovery() {
		return getSection().getBoolean(S_DISCOVERY);
	}
	public String getDriveSettings() {
		return getSection().get(S_DRIVES);
	}
	public void setDriveSettings(String drives) {
		getSection().put(S_DRIVES, drives);
	}
}
package org.eclipse.update.internal.ui.search;

import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class DefaultUpdatesSearchObject extends SearchObject {
	private static final String KEY_NAME = "AvailableUpdates.name";

	public DefaultUpdatesSearchObject() {
		super(
			UpdateUIPlugin.getResourceString(KEY_NAME),
			SearchCategoryRegistryReader.getDefault().getDescriptor(
				"org.eclipse.update.ui.updates"),
			true);
		setModel(UpdateUIPlugin.getDefault().getUpdateModel());
	}
}
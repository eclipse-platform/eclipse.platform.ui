/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import java.io.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.jface.preference.*;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ScopeSet {
	public static final String SCOPE_DIR_NAME = "scope_sets";
	private String name;
	private PreferenceStore preferenceStore;
	private boolean needsSaving;
	
	public ScopeSet(String name) {
		this.needsSaving = true;
		this.name = name;
	}

	public ScopeSet(ScopeSet set) {
		this(set.getName()+"_new");
		copy((PreferenceStore)set.getPreferenceStore());
	}
	
	public void dispose() {
		File file = new File(getFileName(name));
		if (file.exists())
			file.delete();
	}

	public IPreferenceStore getPreferenceStore() {
		if (preferenceStore==null) {
			preferenceStore = new PreferenceStore(getFileName(this.name));
			try {
				File file = new File(getFileName(this.name));
				if (file.exists())
					preferenceStore.load();
			}
			catch (IOException e) {
				//TODO need to handle this
			}
		}
		return preferenceStore;
	}

	private String getFileName(String name) {
		IPath location = HelpUIPlugin.getDefault().getStateLocation();
		location = location.append(SCOPE_DIR_NAME);
		location = location.append(name+".pref");
		return location.toOSString();
	}

	private void copy(PreferenceStore store) {
		try {
			File file = File.createTempFile("sset", null);
			FileOutputStream fos = new FileOutputStream(file);
			store.save(fos, "");
			fos.close();
			FileInputStream fis = new FileInputStream(file);
			getPreferenceStore();
			preferenceStore.load(fis);
			fis.close();
		}
		catch (IOException e) {
		}
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		String oldFileName = getFileName(this.name);
		File oldFile = new File(oldFileName);
		if (oldFile.exists()) {
			// store under the old name already exists
			if (preferenceStore==null) {
				// just rename the file
				oldFile.renameTo(new File(getFileName(name)));
			}
			else {
				// remove the old file, set the new file name,
				// then save to create the new file
				oldFile.delete();
				preferenceStore.setFilename(getFileName(name));
				try {
					preferenceStore.save();
				}
				catch (IOException e) {
					//TODO handle this
				}
			}
		}
		this.name = name;
	}

	public void save() {
		if (preferenceStore!=null && (preferenceStore.needsSaving() || needsSaving)) {
			try {
				preferenceStore.save();
				needsSaving = false;
			}
			catch (IOException e) {
				//TODO handle this
			}
		}
	}
	
	public boolean getEngineEnabled(EngineDescriptor desc) {
		IPreferenceStore store = getPreferenceStore();
		String key = getMasterKey(desc.getId());
		if (store.contains(key))
			return store.getBoolean(key);
		return desc.isEnabled();
	}
	public void setEngineEnabled(EngineDescriptor desc, boolean value) {
		IPreferenceStore store = getPreferenceStore();
		String key = getMasterKey(desc.getId());
		store.setValue(key, value);
	}
	public static String getMasterKey(String id) {
		return id + ".master";
	}
}
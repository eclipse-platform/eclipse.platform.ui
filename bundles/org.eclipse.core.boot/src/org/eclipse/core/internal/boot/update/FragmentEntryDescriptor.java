package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.net.*;

public class FragmentEntryDescriptor extends FragmentEntryDescriptorModel implements IFragmentEntryDescriptor {


	private NLResourceHelper fNLHelper = null;
	private boolean				fHasNewer=false;			// newer version found
	private boolean				fSelected=false;			// selected for installation


/**
 * PluginEntryDescriptor constructor comment.
 */
public FragmentEntryDescriptor() {
	super();

}
public URL getCompInstallURL() {
	try {
		return UMEclipseTree.appendTrailingSlash(_getCompInstallURL());
		
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
public String getComponentId() {
	
	return _getComponentId();
}
public String getDirName() {

	String dir = getUniqueIdentifier() + "_" + getVersion();
	return dir;
}
public String[] getFiles() {
	int size = _getSizeOfFilesRel();	
	if(size == 0) return new String[0];
	
	String[] list = new String[size];
	_copyFilesRelInto(list);
	return list;
	
}
/**
 * Returns the URL of this plug-in's install directory. 
 * This is the ..../plugins/plugin-dir directory where plug-in
 * files are stored.
 *
 * @return the URL of this plug-in's install directory
 */
public java.net.URL getInstallURL() {

	try {
		return new URL(_getInstallURL());
	} catch (MalformedURLException e) {
		throw new IllegalStateException(); // unchecked
	}
}
public String getLabel() {
	String s = _getName();
	if (fNLHelper == null)
		fNLHelper = new NLResourceHelper( DEFAULT_BUNDLE_NAME,getCompInstallURL());
	return s==null ? "" : fNLHelper.getResourceString(s);
}
/**
 * Returns the unique identifier of this plug-in.
 * This identifier is a non-empty string and is unique 
 * within the plug-in registry.
 *
 * @return the unique identifier of the plug-in (e.g. <code>"com.example.myplugin"</code>)
 */
public String getUniqueIdentifier() {
	
	return _getId();
}
public String getVersion() {

	return _getVersion();

}
public VersionIdentifier getVersionIdentifier() {

	try {
		return new VersionIdentifier(getVersion());
	} catch (Throwable e) {
		return new VersionIdentifier(0,0,0);
	}
}
/**
 * Returns the component or configuration version string.
 *
 * @return the component or configuration version string
 */
public java.lang.String getVersionStr() {
	return _getVersion();
}
public boolean isSelected() {
	
	return fSelected;
}
public void isSelected(boolean sel) {
	
	fSelected = sel;
}
}

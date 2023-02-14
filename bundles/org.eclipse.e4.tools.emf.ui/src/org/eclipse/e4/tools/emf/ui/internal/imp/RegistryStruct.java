package org.eclipse.e4.tools.emf.ui.internal.imp;

public class RegistryStruct {

	String bundle;
	String extensionPoint;
	String extensionPointName;
	String mappingName;

	public RegistryStruct(String bundle, String extensionPoint, String extensionPointName, String mappingName) {
		super();
		this.bundle = bundle;
		this.extensionPoint = extensionPoint;
		this.extensionPointName = extensionPointName;
		this.mappingName = mappingName;
	}

	public String getBundle() {
		return bundle;
	}

	public String getExtensionPoint() {
		return extensionPoint;
	}

	public String getExtensionPointName() {
		return extensionPointName;
	}

	public String getMappingName() {
		return mappingName;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
}

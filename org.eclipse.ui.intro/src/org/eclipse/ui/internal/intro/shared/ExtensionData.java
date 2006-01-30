package org.eclipse.ui.internal.intro.shared;


public class ExtensionData {
	private String id;
	private String importance;
	
	public ExtensionData(String id) {
		this(id, ISharedIntroConstants.LOW);
	}
	
	public ExtensionData(String id, String importance) {
		this.id = id;
		this.importance = importance;
	}
	
	public String getId() {
		return id;
	}
	
	public String getImportance() {
		return importance;
	}
	
	public boolean isHidden() {
		return importance.equals(ISharedIntroConstants.HIDDEN);
	}
}
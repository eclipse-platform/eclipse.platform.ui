package org.eclipse.ui.internal.intro.shared;


public class ExtensionData {
	public static final int HIDDEN = -1;
	public static final int CALLOUT = 0;
	public static final int LOW = 1;
	public static final int MEDIUM = 2;
	public static final int HIGH = 3;

	private String id;
	private int fImportance = LOW;
	
	public ExtensionData(String id) {
		this(id, ISharedIntroConstants.LOW);
	}
	
	public ExtensionData(String id, String importance) {
		this.id = id;
		if (importance!=null) {
			if (importance.equals(ISharedIntroConstants.HIGH))
				fImportance = HIGH;
			else if (importance.equals(ISharedIntroConstants.MEDIUM))
				fImportance = MEDIUM;
			else if (importance.equals(ISharedIntroConstants.LOW))
				fImportance = LOW;
			else if (importance.equals(ISharedIntroConstants.CALLOUT))
				fImportance = CALLOUT;
			else if (importance.equals(ISharedIntroConstants.HIDDEN))
				fImportance = HIDDEN;
		}
	}
	
	public String getId() {
		return id;
	}
	
	public int getImportance() {
		return fImportance;
	}
	
	public boolean isHidden() {
		return fImportance==HIDDEN;
	}
}
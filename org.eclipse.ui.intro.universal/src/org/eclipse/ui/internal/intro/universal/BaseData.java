package org.eclipse.ui.internal.intro.universal;

import java.io.PrintWriter;

public abstract class BaseData {

	private GroupData parent;
	protected String id;

	protected void setParent(GroupData gd) {
		this.parent = gd;
	}

	public GroupData getParent() {
		return parent;
	}
	
	public abstract void write(PrintWriter writer, String indent);

	public String getId() {
		return id;
	}
}

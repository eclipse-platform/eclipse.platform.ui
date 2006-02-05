package org.eclipse.ui.internal.intro.shared;

import java.io.PrintWriter;

import org.eclipse.ui.internal.intro.impl.Messages;


public class ExtensionData {

	public static final int HIDDEN = -1;
	public static final int CALLOUT = 0;
	public static final int LOW = 1;
	public static final int MEDIUM = 2;
	public static final int HIGH = 3;
	public static final int NEW = 4;

	private String id;
	private String name;
	private int fImportance = LOW;
	private boolean implicit = false;

	public static final String[] IMPORTANCE_TABLE = { ISharedIntroConstants.CALLOUT,
			ISharedIntroConstants.LOW, ISharedIntroConstants.MEDIUM, ISharedIntroConstants.HIGH,
			ISharedIntroConstants.NEW };

	public ExtensionData(String id, String name) {
		this(id, name, ISharedIntroConstants.LOW, false);
	}

	public boolean isImplicit() {
		return implicit;
	}

	public ExtensionData(String id, String name, String importance, boolean implicit) {
		this.id = id;
		this.name = name;
		this.implicit = implicit;
		if (importance != null) {
			if (importance.equals(ISharedIntroConstants.HIGH))
				fImportance = HIGH;
			else if (importance.equals(ISharedIntroConstants.MEDIUM))
				fImportance = MEDIUM;
			else if (importance.equals(ISharedIntroConstants.LOW))
				fImportance = LOW;
			else if (importance.equals(ISharedIntroConstants.CALLOUT))
				fImportance = CALLOUT;
			else if (importance.equals(ISharedIntroConstants.NEW))
				fImportance = NEW;
			else if (importance.equals(ISharedIntroConstants.HIDDEN))
				fImportance = HIDDEN;
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getImportance() {
		return fImportance;
	}

	public boolean isHidden() {
		return fImportance == HIDDEN;
	}

	public void write(PrintWriter writer, String indent) {
		writer.print(indent);
		writer.print("<extension id=\"" + id + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		if (!isHidden())
			writer.println(" importance=\"" + getImportanceAttributeValue() + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
		else
			writer.println("/>"); //$NON-NLS-1$
	}

	private String getImportanceAttributeValue() {
		return IMPORTANCE_TABLE[fImportance];
	}
}
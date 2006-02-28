package org.eclipse.ui.internal.intro.universal;

import java.io.PrintWriter;

public class SeparatorData extends BaseData {
	
	public SeparatorData() {
	}
	
	public SeparatorData(String id) {
		this.id = id;
	}

	public void write(PrintWriter writer, String indent) {
		writer.print(indent);
		writer.print("<separator id=\""); //$NON-NLS-1$
		writer.print(id);
		writer.println("\"/>"); //$NON-NLS-1$
	}
}
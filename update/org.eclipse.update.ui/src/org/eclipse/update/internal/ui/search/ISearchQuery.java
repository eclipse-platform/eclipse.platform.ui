package org.eclipse.update.internal.ui.search;

import org.eclipse.update.core.IFeature;
import org.w3c.dom.Node;
import java.io.PrintWriter;

public interface ISearchQuery {
	public boolean matches(IFeature feature);
	public void parse(Node node);
	public void write(String indent, PrintWriter writer);

}

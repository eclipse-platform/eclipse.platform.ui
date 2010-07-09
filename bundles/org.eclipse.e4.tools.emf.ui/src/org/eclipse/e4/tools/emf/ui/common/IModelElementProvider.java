package org.eclipse.e4.tools.emf.ui.common;

import java.util.regex.Pattern;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public interface IModelElementProvider {
	public class Filter {
		public final EClass eClass;
		public final String elementId;
		public final Pattern elementIdPattern;

		public Filter(EClass eClass, String elementId) {
			this.eClass = eClass;
			this.elementId = elementId;
			this.elementIdPattern = Pattern.compile(".*" + elementId.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*") + ".*");
		}
	}

	public interface ModelResultHandler {
		public void result(EObject data);
	}

	public void getModelElements(Filter filter, ModelResultHandler handler);

	public void clearCache();
}

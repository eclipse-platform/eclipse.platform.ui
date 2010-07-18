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
			this.elementIdPattern = Pattern.compile(".*" + elementId.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*") + ".*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
	}

	public interface ModelResultHandler {
		public void result(EObject data);
	}

	public void getModelElements(Filter filter, ModelResultHandler handler);

	public void clearCache();
}

package org.eclipse.e4.workbench.modeling;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;

public class ICEAdapter extends ModelHandlerBase implements IAdapterFactory {

	public static final String CLASS_IMPL = "classImpl";
	
	public ICEAdapter() {
		super();
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		return this;
	}

	public Class[] getAdapterList() {
		return new Class[] {ModelHandlerBase.class};
	}

	@Override
	public Object[] getChildren(Object element, String id) {
		IConfigurationElement ice = (IConfigurationElement) element;
		IConfigurationElement[] kids = ice.getChildren(id);
		return kids;
	}

	@Override
	public Object getProperty(Object element, String id) {
		IConfigurationElement ice = (IConfigurationElement) element;
		
		// Construct a meaningful 'label'
		if ("label".equals(id)) {
			String idVal = ice.getAttribute("id");
			String nameVal = ice.getAttribute("name");
			
			String constructedName = "";
			if (nameVal != null) {
				constructedName = nameVal;
				if (idVal != null)
					constructedName += " [" + idVal + "]";
			}
			else if (idVal != null) {
				constructedName = idVal;
			}
			else
				constructedName = ice.getName();
			
			return constructedName;
		}
		else if (CLASS_IMPL.equals(id)) {
			try {
				return ice.createExecutableExtension("class");
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		return ice.getAttribute(id);
	}

	@Override
	public String[] getPropIds(Object element) {
		IConfigurationElement ice = (IConfigurationElement) element;
		return ice.getAttributeNames();
	}

	@Override
	public void setProperty(Object element, String id, Object value) {
		IConfigurationElement ice = (IConfigurationElement) element;
		super.setProperty(element, id, value);
	}
}

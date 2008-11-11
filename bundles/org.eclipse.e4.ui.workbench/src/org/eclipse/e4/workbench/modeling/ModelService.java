package org.eclipse.e4.workbench.modeling;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.e4.core.services.IDisposable;

public class ModelService extends ModelHandlerBase implements IDisposable {
	private IAdapterManager manager;

	public ModelService(IAdapterManager manager) {
		this.manager = manager;
	}
	
	public void dispose() {
	}

	private Object loadAdapterLocal(Object element) {
		ModelHandlerBase handler = (ModelHandlerBase) manager.getAdapter(element, ModelHandlerBase.class);
		if (handler == null) {
			handler = (ModelHandlerBase) manager.loadAdapter(element, ModelHandlerBase.class.getName());
		}
		
		return handler;
	}
	
	@Override
	public Object[] getChildren(Object element, String id) {
		ModelHandlerBase handler = (ModelHandlerBase)loadAdapterLocal(element);
		if (handler != null) {
			return handler.getChildren(element, id);
		}
		
		return new Object[0];
	}

	@Override
	public Object getProperty(Object element, String id) {
		ModelHandlerBase handler = (ModelHandlerBase)loadAdapterLocal(element);
		if (handler != null) {
			return handler.getProperty(element, id);
		}
		
		return null;
	}

	@Override
	public String[] getPropIds(Object element) {
		ModelHandlerBase handler = (ModelHandlerBase)loadAdapterLocal(element);
		if (handler != null) {
			return handler.getPropIds(element);
		}
		
		return new String[0];
	}

	@Override
	public void setProperty(Object element, String id, Object value) {
		ModelHandlerBase handler = (ModelHandlerBase)loadAdapterLocal(element);
		if (handler != null) {
			handler.setProperty(element, id, value);
		}
	}
}

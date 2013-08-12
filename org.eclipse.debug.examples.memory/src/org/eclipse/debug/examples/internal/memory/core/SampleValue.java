package org.eclipse.debug.examples.internal.memory.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class SampleValue extends DebugElement implements IValue {

	private SampleVariable fVariable;

	public SampleValue(SampleVariable variable) {
		super(variable.getDebugTarget());
		fVariable = variable;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getValueString() throws DebugException {
		return String.valueOf(System.currentTimeMillis());
	}

	@Override
	public boolean isAllocated() throws DebugException {
		return false;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return new IVariable[0];
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return false;
	}

	@Override
	public String getModelIdentifier() {
		return fVariable.getModelIdentifier();
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return fVariable.getDebugTarget();
	}

	@Override
	public ILaunch getLaunch() {
		return fVariable.getLaunch();
	}

}

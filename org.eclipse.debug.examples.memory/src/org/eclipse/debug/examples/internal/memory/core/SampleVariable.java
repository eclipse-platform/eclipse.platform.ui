package org.eclipse.debug.examples.internal.memory.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class SampleVariable extends DebugElement implements IVariable {

	private final SampleStackFrame fFrame;
	private String fName;

	SampleVariable(SampleStackFrame frame, String name) {
		super(frame.getDebugTarget());
		fFrame = frame;
		fName = name;
	}

	@Override
	public IValue getValue() throws DebugException {
		return new SampleValue(this);
	}

	@Override
	public String getName() throws DebugException {
		return fName;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return ""; //$NON-NLS-1$
	}

	@Override
	public boolean hasValueChanged() throws DebugException {
		return false;
	}

	@Override
	public String getModelIdentifier() {
		return fFrame.getModelIdentifier();
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return fFrame.getDebugTarget();
	}

	@Override
	public ILaunch getLaunch() {
		return fFrame.getLaunch();
	}

	@Override
	public void setValue(IValue value) throws DebugException {

	}

	@Override
	public boolean supportsValueModification() {
		return false;
	}

	@Override
	public boolean verifyValue(IValue value) throws DebugException {
		return false;
	}

	@Override
	public void setValue(String expression) throws DebugException {

	}

	@Override
	public boolean verifyValue(String expression) throws DebugException {
		return false;
	}
}
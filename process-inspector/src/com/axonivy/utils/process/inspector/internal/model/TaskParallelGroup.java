package com.axonivy.utils.process.inspector.internal.model;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.List;
import java.util.Objects;

import ch.ivyteam.ivy.process.model.BaseElement;
import ch.ivyteam.ivy.process.model.value.PID;

public class TaskParallelGroup implements ProcessElement {
	private BaseElement element;
	private List<AnalysisPath> internalPaths;

	public TaskParallelGroup(BaseElement element, List<AnalysisPath> internalPaths) {
		this.element = element;
		this.internalPaths = internalPaths;
	}
	
	public List<AnalysisPath> getInternalPaths() {
		return internalPaths;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.element, this.internalPaths);
	}

	@Override
	public String toString() {
		return String.format("%s : %s ", Objects.toString(element, EMPTY), Objects.toString(internalPaths, EMPTY));
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof TaskParallelGroup)) {
			return false;
		}
		TaskParallelGroup task = (TaskParallelGroup) other;
		return Objects.equals(task.element, this.element) && Objects.equals(task.internalPaths, this.internalPaths);
	}

	@Override
	public PID getPid() {
		return element.getPid();
	}

	@Override
	public BaseElement getElement() {
		return element;
	}
}

package com.axonivy.utils.estimator.demo.model;

import static java.util.Collections.emptyList;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import com.axonivy.utils.estimator.demo.constant.FindType;
import com.axonivy.utils.estimator.model.EstimatedTask;

import ch.ivyteam.ivy.process.model.Process;
import ch.ivyteam.ivy.process.model.element.SingleTaskCreator;

public class Estimator {
	private String id;
	private String flowName;
	private Process process;
	private String processPath;
	private List<SingleTaskCreator> elements;
	private FindType findType;
	private SingleTaskCreator startElement;
	private List<EstimatedTask> tasks;
	private Duration totalDuration;

	public Estimator() {
		this.id = UUID.randomUUID().toString();
		this.flowName = null;
		this.process = null;
		this.processPath = null;
		this.elements = emptyList();
		this.findType = null;
		this.startElement = null;
		this.tasks = emptyList();
		this.totalDuration = Duration.ZERO;
	}

	public String getId() {
		return id;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public String getProcessPath() {
		return processPath;
	}

	public void setProcessPath(String processPath) {
		this.processPath = processPath;
	}

	public List<SingleTaskCreator> getElements() {
		return elements;
	}

	public void setElements(List<SingleTaskCreator> elements) {
		this.elements = elements;
	}

	public SingleTaskCreator getStartElement() {
		return startElement;
	}

	public void setStartElement(SingleTaskCreator startElement) {
		this.startElement = startElement;
	}

	public FindType getFindType() {
		return findType;
	}

	public void setFindType(FindType findType) {
		this.findType = findType;
	}

	public List<EstimatedTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<EstimatedTask> tasks) {
		this.tasks = tasks;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public Duration getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(Duration toDuration) {
		this.totalDuration = toDuration;
	}
	
	public String getDisplayTotalDuration() {
		if(totalDuration.isZero()) {
			return StringUtils.EMPTY;
		}
		return  DurationFormatUtils.formatDuration(totalDuration.toMillis(), "H 'hours', m 'minutes', s 'seconds'");
	}

	public String getElementNames() {
		return this.tasks.stream().map(EstimatedTask::getElementName).collect(Collectors.joining(" -> "));
	}
}

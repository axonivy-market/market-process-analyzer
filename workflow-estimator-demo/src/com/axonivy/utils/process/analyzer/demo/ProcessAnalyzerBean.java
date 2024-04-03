package com.axonivy.utils.process.analyzer.demo;

import static java.util.Collections.emptyList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.component.selectoneradio.SelectOneRadio;

import com.axonivy.utils.process.analyzer.AdvancedProcessAnalyzer;
import com.axonivy.utils.process.analyzer.constant.UseCase;
import com.axonivy.utils.process.analyzer.demo.constant.FindType;
import com.axonivy.utils.process.analyzer.demo.helper.DateTimeHelper;
import com.axonivy.utils.process.analyzer.demo.model.Analyzer;
import com.axonivy.utils.process.analyzer.model.DetectedElement;

import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.model.BaseElement;
import ch.ivyteam.ivy.process.model.EmbeddedProcess;
import ch.ivyteam.ivy.process.model.Process;
import ch.ivyteam.ivy.process.model.connector.SequenceFlow;
import ch.ivyteam.ivy.process.model.element.EmbeddedProcessElement;
import ch.ivyteam.ivy.process.model.element.ProcessElement;
import ch.ivyteam.ivy.process.model.element.SingleTaskCreator;
import ch.ivyteam.ivy.process.model.element.gateway.Alternative;
import ch.ivyteam.ivy.process.rdm.IProcessManager;
import ch.ivyteam.ivy.process.viewer.api.ProcessViewer;
import ch.ivyteam.ivy.workflow.start.IProcessWebStartable;
import ch.ivyteam.ivy.workflow.start.IWebStartable;

@SuppressWarnings("restriction")
public class ProcessAnalyzerBean {
	private static final List<String> PROCESS_FOLDERS = Arrays.asList("Bussiness Processes");

	private List<Analyzer> analyzers = new ArrayList<>();
	
	private List<Process> processes = emptyList();
	
	private Analyzer selectedAnalyzer = null;

	public ProcessAnalyzerBean() {
		processes = getAllProcesses();
	}
	
	public List<Analyzer> getAnalyzers() {
		return analyzers;
	}

	public void setAnalyzers(List<Analyzer> analyzers) {
		this.analyzers = analyzers;
	}

	public List<Process> getProcesses() {
		return processes;
	}
	
	public Analyzer getSelectedAnalyzer() {
		return selectedAnalyzer;
	}

	public void setSelectedAnalyzer(Analyzer selectedAnalyzer) {
		this.selectedAnalyzer = selectedAnalyzer;
	}

	public List<SingleTaskCreator> getAllTaskModifier() {
		return  getElementOfProcess(this.selectedAnalyzer.getProcess()).stream()
			.filter(item -> item instanceof SingleTaskCreator)
			.map(SingleTaskCreator.class::cast)
			.toList();
	}
	
	public List<FindType> getAllFindType(){
		return  Arrays.stream(FindType.values()).toList();
	}
	
	public List<UseCase> getAllUseCases(){
		return  Arrays.stream(UseCase.values()).toList();
	}
	
	public List<Alternative> getALternativeWithMoreThanOneOutgoing(){
		List<Alternative> alternatives = getElementOfProcess(this.selectedAnalyzer.getProcess()).stream()
				.filter(item -> item instanceof Alternative)
				.map(Alternative.class::cast)
				.filter(item -> item.getOutgoing().size() > 1)
				.toList();
		return alternatives;	
	}
	
	public void onSelectSequenceFlow(AjaxBehaviorEvent event) {
		if (event.getSource() != null) {
			SequenceFlow newSequenceFlow = (SequenceFlow) ((SelectOneRadio) event.getSource()).getValue();
			if (newSequenceFlow != null) {
				if (newSequenceFlow.getSource() instanceof Alternative) {
					Alternative alternative = (Alternative) newSequenceFlow.getSource();
					selectedAnalyzer.getAlternativeFlows().put(alternative, newSequenceFlow);
				}
			}
		}
	}
	
	public List<DetectedElement> getDetectedTask() throws Exception {
		AdvancedProcessAnalyzer processAnalyzer = createprocessAnalyzer(selectedAnalyzer);

		long startTime = System.currentTimeMillis();
		List<DetectedElement> detectedElements = null;
		if (FindType.ALL_TASK.equals(selectedAnalyzer.getFindType())) {
			detectedElements = processAnalyzer.findAllTasks(selectedAnalyzer.getStartElement()).stream()
					.map(DetectedElement.class::cast).toList();
		} else {
			detectedElements = processAnalyzer.findTasksOnPath(selectedAnalyzer.getStartElement()).stream()
					.map(DetectedElement.class::cast).toList();
		}

		long executionTime = System.currentTimeMillis() - startTime;
		selectedAnalyzer.setExecutionTime(executionTime);

		return detectedElements;
	}

	public Duration getDetectedTaskCalculate() throws Exception{
		AdvancedProcessAnalyzer processAnalyzer = createprocessAnalyzer(selectedAnalyzer);
		
		Duration total = Duration.ZERO;
		if(FindType.ALL_TASK.equals(selectedAnalyzer.getFindType())) {
			total = processAnalyzer.calculateEstimatedDuration(selectedAnalyzer.getStartElement());
		} else {
			total = processAnalyzer.calculateEstimatedDuration(selectedAnalyzer.getStartElement());
		}
		return total;
	}

	private AdvancedProcessAnalyzer createprocessAnalyzer(Analyzer analyzer) {
		AdvancedProcessAnalyzer processAnalyzer = new AdvancedProcessAnalyzer(selectedAnalyzer.getProcess(), selectedAnalyzer.getUseCase(), selectedAnalyzer.getFlowName());

		HashMap<String, String> flowOverrides = getProcessFlowOverride(selectedAnalyzer);
		processAnalyzer.setProcessFlowOverrides(flowOverrides);
		
		return processAnalyzer;
	}

	private HashMap<String, String> getProcessFlowOverride(Analyzer analyzer) {		
		Map<Alternative, SequenceFlow> alternativeFlows = selectedAnalyzer.getAlternativeFlows();
		HashMap<String, String> processFlowOverride = new HashMap<String, String>();

		for (Alternative item : alternativeFlows.keySet()) {
			if (alternativeFlows.get(item) != null) {
				processFlowOverride.put(item.getPid().getRawPid(), alternativeFlows.get(item).getPid().getRawPid());
			}
		}
		return processFlowOverride;
	}
	
	private List<Process> getAllProcesses() {
		var manager = IProcessManager.instance().getProjectDataModelFor(IProcessModelVersion.current());
		List<Process> processes = manager.search().find().stream()
				.map(start -> start.getRootProcess())
				.filter(process -> isAcceptedProcess(PROCESS_FOLDERS, process.getFullQualifiedName().getName()))
				.distinct()
				.collect(Collectors.toList());
		return processes;
	}
	
	public String getProcessWebLink() {
		String guid = this.selectedAnalyzer.getProcess().getPid().getProcessGuid();
		IWebStartable webStartable = Ivy.session().getStartables().stream().filter(it -> it.getLink().toRelativeUri().getPath().contains(guid)).findFirst().orElse(null);
		
		if(webStartable != null) {
			return ProcessViewer.of((IProcessWebStartable) webStartable).url().toWebLink().getRelative();	
		}	
		return null;
	}
	
	private boolean isAcceptedProcess(List<String>folders, String fullQualifiedName) {
		return folders.stream().anyMatch(folder -> fullQualifiedName.contains(folder));
	}
	
	private static List<BaseElement> getElementOfProcess (Process process) {
		var processElements = process.getProcessElements();
		var childElments = getElementOfProcesses(processElements);
		var elements = process.getElements();
		elements.addAll(childElments);
		
		return elements;
	}
	
	private static List<BaseElement> getElementOfProcesses (List<ProcessElement> processElements) {
		if(processElements.isEmpty()) {
			return emptyList();
		}
		var embeddedProcess = processElements.stream()
				.filter(it -> it instanceof EmbeddedProcessElement == true)
				.map(EmbeddedProcessElement.class::cast)
				.map(it -> it.getEmbeddedProcess())
				.collect(Collectors.toList());

		var elememets = embeddedProcess.stream()
				.map(EmbeddedProcess::getElements)
				.flatMap(List::stream)
				.collect(Collectors.toList());
		
		var childProcessElements = embeddedProcess.stream()
				.map(EmbeddedProcess::getProcessElements)
				.flatMap(List::stream)
				.collect(Collectors.toList());
		
		var childElememts = getElementOfProcesses(childProcessElements);
		
		elememets.addAll(childElememts);
		
		return elememets;
	}
	
	public String getDisplayDuration(Duration duration) {
		return DateTimeHelper.getDisplayDuration(duration);
	}
}
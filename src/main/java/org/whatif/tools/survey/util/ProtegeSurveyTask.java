package org.whatif.tools.survey.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ProtegeSurveyTask {

	final String taskid;
	final String description;
	final boolean runreasoner;
	final boolean manual;
	final boolean confirm;
	private final Queue<ProtegeSurveyQuestion> questions = new LinkedList<ProtegeSurveyQuestion>();
	private ProtegeSurveyQuestion currentQuestion;
	private Map<String,Map<String,Set<String>>> additions;
	private Map<String,Map<String,Set<String>>> removals;
	
	public ProtegeSurveyTask(String taskid, List<ProtegeSurveyQuestion> questions, String description, Map<String,Map<String,Set<String>>> additions, Map<String,Map<String,Set<String>>> removals, boolean manual, boolean runreasoner, boolean confirm) {
		this.taskid = taskid;
		this.description = description;
		this.questions.addAll(questions);
		this.additions = additions;
		this.removals = removals;
		this.runreasoner = runreasoner;
		this.manual = manual;
		this.confirm = confirm;
	}

	public String getDescription() {
		return description;
	}

	public void nextQuestion() {
		this.currentQuestion = questions.remove();
	}
	
	public ProtegeSurveyQuestion getCurrentQuestion() {
		return currentQuestion;
	}

	public boolean hasNextQuestion() {
		return !questions.isEmpty();
	}

	public Map<String,Map<String,Set<String>>> getAdditions() {
		return additions;
	}
	
	public Map<String,Map<String,Set<String>>> getRemovals() {
		return removals;
	}

	public String getId() {
		return taskid;
	}
	
	public boolean isManual() {
		return manual;
	}
	
	public boolean isRunReasoner() {
		return runreasoner;
	}

	public int getRemainingQuestionsCount() {
		return questions.size();
	}

	public int getTotalQuestionCount() {
		return questions.size();
	}

	public boolean requiresConfirm() {
		return confirm;
	}
}

package org.whatif.tools.survey.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProtegeSurveyQuestion {

	final String id;
	final int timeout;
	final String phrase;
	final String optionstype;
	final List<String> options;
	final Set<String> correctoptions;
	final String tabid;
	final boolean questionratedifficulty;
	final boolean questionexpectation;
	
	public ProtegeSurveyQuestion(String id, String phrase, String optionstype, List<String> options, Set<String> correctoptions, String tabid, boolean questionratedifficulty, boolean questionexpectation, int to_i) {
		this.id = id;
		this.phrase = phrase;
		this.optionstype = optionstype;
		this.options = options;
		this.correctoptions = correctoptions;
		this.tabid = tabid;
		this.questionratedifficulty = questionratedifficulty;
		this.questionexpectation = questionexpectation;
		this.timeout = to_i;
	}
	
	public String getQuestionPhrase() {
		return phrase;
	}

	public String getOptionsType() {
		return optionstype;
	}

	public List<String> getOptions() {
		return options;
	}

	public Set<String> getCorrectOptions() {
		return correctoptions;
	}

	public String getId() {
		return id;
	}

	public String getTab() {
		return tabid;
	}
	
	public boolean isRate() {
		return questionratedifficulty;
	}

	public boolean isTimed() {
		return timeout>0;
	}

	public int getTimeout() {
		return timeout;
	}
}

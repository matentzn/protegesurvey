package org.whatif.tools.survey.util;

import java.awt.event.MouseListener;
import java.util.Set;

public interface SurveyRenderer {
	Object renderQuestion(ProtegeSurveyQuestion protegeSurveyQuestion);
	Set<String> parseResults(Object object);
}

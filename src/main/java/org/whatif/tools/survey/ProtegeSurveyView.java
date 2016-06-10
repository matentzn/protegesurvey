package org.whatif.tools.survey;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;
import org.protege.editor.core.ui.workspace.TabbedWorkspace;
import org.protege.editor.core.ui.workspace.WorkspaceTab;
import org.protege.editor.core.ui.workspace.WorkspaceTabPlugin;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.whatif.tools.survey.util.ProtegeSurvey;
import org.whatif.tools.survey.util.QuestionMetadata;
import org.whatif.tools.survey.util.SurveyRenderer;
import org.whatif.tools.survey.util.SurveyState;
import org.whatif.tools.survey.util.SwingSurveyRenderer;
import org.whatif.tools.util.WhatifUtils;

public class ProtegeSurveyView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = -4515710047558710080L;

	private static final Logger log = Logger.getLogger(ProtegeSurveyView.class);

	// JPanel surveypanel = null;
	JPanel sidebarpanel = null;
	JPanel viewpanel = null;
	JPanel scenariopanel = null;
	JPanel scenariodescriptionpanel = null;
	JPanel taskpanel = null;
	JPanel taskdescriptionpanel = null;
	JPanel questionpanel = null;
	JPanel questiondescriptionpanel = null;
	Component questionoptions = null;

	// HashSet<String> openTabs = new HashSet<String>();

	// private OWLSelectionModelListener listener;

	JButton button_startsurvey = new JButton("Start Survey");
	JTextField namefield = new JTextField();
	JButton button_scenario = new JButton("Confirm scenario");
	JButton button_task = new JButton("Ok");
	JButton button_question = new JButton("Done");
	JButton button_expection = new JButton("Done");
	JButton button_rate = new JButton("Done");
	JButton button_skip = new JButton("Skip");
	JButton button_restart_survey = new JButton("Restart");

	Map<String, WorkspaceTab> tabs = new HashMap<String, WorkspaceTab>();
	WorkspaceTab current = null;

	static Toolkit tk = Toolkit.getDefaultToolkit();
	static long eventMask = AWTEvent.MOUSE_WHEEL_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK;

	ProtegeSurvey survey = null;
	QuestionMetadata qm = null;
	int clickCount = 0;
	int keystrokeCount = 0;
	double scrollAmount = 0;

	@Override
	protected void initialiseOWLView() throws Exception {
		startSurvey();
		log.info("Protege Survey initialized");
	}

	private void initialiseSurvey() {
		File file = new File("protege_survey.xml");
		WhatifUtils.p(new File("").getAbsolutePath());
		SurveyRenderer factory = new SwingSurveyRenderer();
		survey = new ProtegeSurvey(file, factory);
	}

	private void setupSidebarPanel() {
		// Set up Sidebar panel
		sidebarpanel = new JPanel();
		sidebarpanel.setLayout(new GridBagLayout());
		sidebarpanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		sidebarpanel.setPreferredSize(new Dimension(400, 600));
		sidebarpanel.setMaximumSize(new Dimension(400, 100000));
		sidebarpanel.setMinimumSize(new Dimension(400, 600));
		scenariopanel = new JPanel();
		scenariopanel.setLayout(new BoxLayout(scenariopanel, BoxLayout.PAGE_AXIS));
		JPanel startsurvey = new JPanel(new BorderLayout());
		startsurvey.add(button_startsurvey, BorderLayout.SOUTH);
		startsurvey.add(namefield, BorderLayout.CENTER);
		scenariopanel.add(startsurvey);

		taskpanel = new JPanel();
		taskpanel.setLayout(new BoxLayout(taskpanel, BoxLayout.PAGE_AXIS));

		questionpanel = new JPanel();
		questionpanel.setLayout(new BoxLayout(questionpanel, BoxLayout.PAGE_AXIS));
		// questionpanel.setPreferredSize(new Dimension(300,0));
		// questionpanel.setBackground(Color.BLUE);

		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 1;
		c.weightx = 1;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		sidebarpanel.add(scenariopanel, c);
		c.gridx = 0;
		c.gridy = 1;
		sidebarpanel.add(taskpanel, c);
		c.gridx = 0;
		c.gridy = 2;
		sidebarpanel.add(questionpanel, c);

		// Register listeners
		registerSidebarButtonActionListener();
	}

	private void registerSidebarButtonActionListener() {
		removeAllListeners(button_startsurvey);
		button_startsurvey.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean started = survey.start(getParticipantName());
				if (started) {
					updateScenario();
				}
			}
		});

		removeAllListeners(button_scenario);
		button_scenario.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				survey.validateScenario(getOWLModelManager().getActiveOntology());
				updateScenario();
			}
		});

		removeAllListeners(button_task);
		button_task.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				survey.applyTaskChanges(getOWLModelManager().getActiveOntology(), false);
				if (survey.validateTask(getOWLModelManager().getActiveOntology(), getOWLModelManager()
						.getOWLReasonerManager().getReasonerStatus().equals(ReasonerStatus.INITIALIZED))) {
					updateScenario();
				} else {
					showIncompleteTaskDialog();
				}
			}
		});

		removeAllListeners(button_question);
		button_question.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setQuestionMetadata();
				survey.validateQuestion(questionoptions, qm);
				updateScenario();
			}
		});

		removeAllListeners(button_expection);
		button_expection.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setQuestionMetadata();
				survey.validateQuestion(questiondescriptionpanel, qm);
				updateScenario();
			}
		});

		removeAllListeners(button_skip);
		button_skip.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setQuestionMetadata();
				qm.setSkipped(true);
				survey.validateQuestion(questiondescriptionpanel, qm);
				updateScenario();
			}
		});

		removeAllListeners(button_restart_survey);
		button_restart_survey.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startSurvey();
			}
		});

		removeAllListeners(button_rate);
		button_rate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				survey.validateRating("rating");
				updateScenario();
			}
		});
	}

	private void removeAllListeners(AbstractButton bt) {
		for (ActionListener l : bt.getActionListeners()) {
			bt.removeActionListener(l);
		}
	}

	protected void startSurvey() {
		setLayout(new BorderLayout());

		// Set up survey (parse survey file)
		initialiseSurvey();

		// This is where the tabs will be displayed
		viewpanel = new JPanel(new GridLayout(1, 1));

		// to collect clicks and so on globally
		tk.addAWTEventListener(createMouseAndKeyListener(), eventMask);

		setupSidebarPanel();

		// surveypanel.add(new JPanel(), c);
		removeAll();
		add(sidebarpanel, BorderLayout.WEST);
		add(viewpanel, BorderLayout.CENTER);

		updateScenario();

	}

	private AWTEventListener createMouseAndKeyListener() {
		return new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent e) {
				if (e instanceof MouseWheelEvent) {
					if (((MouseWheelEvent) e).getID() == MouseWheelEvent.MOUSE_WHEEL) {
						scrollAmount = scrollAmount + Math.abs(((MouseWheelEvent) e).getPreciseWheelRotation());
					}
				} else if (e instanceof KeyEvent) {
					if (((KeyEvent) e).getID() == KeyEvent.KEY_PRESSED) {
						keystrokeCount++;
					}
				} else if (e instanceof MouseEvent) {
					if (((MouseEvent) e).getID() == MouseEvent.MOUSE_PRESSED) {
						clickCount++;
					}
				}
			}
		};
	}

	protected void showIncompleteTaskDialog() {
		JOptionPane.showMessageDialog(this,
				"You have not completed the task yet! Please read the instructions and try again before you can continue.");
	}

	protected String getParticipantName() {
		return namefield.getText().isEmpty() ? System.currentTimeMillis() + "" : namefield.getText();
	}

	private void updateScenario() {
		WhatifUtils.p("updateScenario(): " + survey.getState());
		// GridBagConstraints c = new GridBagConstraints();
		// c.weightx = 1;
		// c.fill = GridBagConstraints.BOTH;
		//
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setBold(set, true);
		StyleConstants.setAlignment(set, StyleConstants.ALIGN_CENTER);
		Font f = new Font(Font.SANS_SERIF, 3, 15);

		SurveyState state = survey.getState();
		switch (state) {
		case READY:
			break;
		case SCENARIO:
			taskpanel.removeAll();
			questionpanel.removeAll();
			scenariopanel.removeAll();
			viewpanel.removeAll();
			resetTabs();
			prepareScenarioPanel(set, f);
			if (survey.currentScenarioInvolvesOntology()) {
				loadNewOntology();
			}
			break;
		case SCENARIOVALIDATE:
			survey.nextState();
			updateScenario();
			break;
		case TASK:
			taskpanel.removeAll();
			viewpanel.removeAll();
			questionpanel.removeAll();
			prepareTaskPanel(set, f);
			break;
		case TASKVALIDATE:
			survey.nextState();
			updateScenario();
			break;
		case EXPECT:
			survey.nextState();
			updateScenario();
			break;
		case EXPECTVALIDATE:
			survey.nextState();
			updateScenario();
			break;
		case QUESTION:
			prepareQuestionPanel(set, f);
			updateSurveyViews();
			resetQuestionMetadata();
			break;
		case QUESTIONVALIDATE:
			survey.nextState();
			updateScenario();
			break;
		case RATEQUESTION:
			survey.nextState();
			updateScenario();
			break;
		case FINISHED:
			taskpanel.removeAll();
			questionpanel.removeAll();
			scenariopanel.removeAll();
			viewpanel.removeAll();
			prepareFinishedPanel(set, f);
			resetTabs();
			createEmptyOntology();
			break;
		default:
			System.err.println("Illegal state for Protege Survey: " + state);
			break;
		}

		// repaint();
		viewpanel.revalidate();
		viewpanel.repaint();
		sidebarpanel.revalidate();
		sidebarpanel.repaint();

		taskpanel.revalidate();
		questionpanel.revalidate();
		scenariopanel.revalidate();
		viewpanel.revalidate();

		taskpanel.repaint();
		questionpanel.repaint();
		scenariopanel.repaint();
		viewpanel.repaint();

		revalidate();
		repaint();

	}

	private void createEmptyOntology() {
		try {
			OWLOntology a = getOWLEditorKit().getModelManager().getActiveOntology();

			for (OWLOntology o : getOWLEditorKit().getModelManager().getOntologies()) {
				getOWLEditorKit().getModelManager().removeOntology(o);
			}

			/*
			 * getOWLEditorKit().handleNewRequest();
			 * 
			 * getOWLEditorKit().getModelManager().removeOntology(a);
			 * 
			 * OWLOntology b =
			 * getOWLEditorKit().getModelManager().getActiveOntology(); OWLAxiom
			 * ax = b.getOWLOntologyManager().getOWLDataFactory().
			 * getOWLSubClassOfAxiom(
			 * b.getOWLOntologyManager().getOWLDataFactory().getOWLThing(),
			 * b.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
			 * OWLOntology b = getOWLModelManager().createNewOntology(new
			 * OWLOntologyID(),null); //
			 * getOWLModelManager().getOWLReasonerManager()..getCurrentReasoner(
			 * ).flush();//.setCurrentReasonerFactoryId(
			 * "org.protege.editor.owl.NoOpReasoner");
			 */
			TabbedWorkspace workspace = (TabbedWorkspace) getWorkspace();
			workspace.setSelectedTab(workspace.getWorkspaceTab("protege.survey.ProtegeSurveyTab"));

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void resetTabs() {
		for (String t : tabs.keySet()) {
			try {
				tabs.get(t).dispose();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		tabs.clear();
		current = null;
	}

	private void resetQuestionMetadata() {
		clickCount = 0;
		keystrokeCount = 0;
		scrollAmount = 0;
		qm = new QuestionMetadata();
		qm.setStart(System.currentTimeMillis());
	}

	private void prepareFinishedPanel(SimpleAttributeSet set, Font f) {
		scenariodescriptionpanel = new JPanel(new BorderLayout());
		JTextPane finisheddescription = new JTextPane();
		finisheddescription.setFont(f);
		// Set the attributes before adding text
		finisheddescription.setCharacterAttributes(set, true);
		finisheddescription.setText("Survey Finished! Thank you for you participation.");
		scenariodescriptionpanel.add(finisheddescription);

		scenariopanel.add(getMainLabel("Thank you!"));
		scenariopanel.add(scenariodescriptionpanel);
		/// Add confirm button
		JPanel p = new JPanel(new GridLayout(1, 1));
		p.add(button_restart_survey);

		scenariopanel.add(p);
		scenariopanel.add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private void prepareTaskPanel(SimpleAttributeSet set, Font f) {
		taskdescriptionpanel = new JPanel(new BorderLayout());
		taskpanel.add(getMainLabel("Task"));

		JTextPane taskdescription = new JTextPane();
		taskdescription.setFont(f);
		taskdescription.setContentType("text/html");
		taskdescription.setCharacterAttributes(set, true);
		taskdescription.setText(survey.getTaskDescription());

		JScrollPane scrollPane = new JScrollPane(taskdescription);
		taskdescriptionpanel.add(scrollPane);

		taskpanel.add(taskdescriptionpanel);

		/// Add confirm button
		JPanel p = new JPanel(new GridLayout(1, 1));
		p.add(button_task);

		taskpanel.add(p);
		taskpanel.add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private void prepareScenarioPanel(SimpleAttributeSet set, Font f) {
		scenariodescriptionpanel = new JPanel(new BorderLayout());

		// c.weighty = 0.5;

		// c.gridx = 0;
		// c.gridy = 0;
		scenariopanel.add(getMainLabel("Scenario"));

		JTextPane scenariodescription = new JTextPane();
		scenariodescription.setContentType("text/html");
		scenariodescription.setFont(f);
		// Set the attributes before adding text
		scenariodescription.setCharacterAttributes(set, true);
		scenariodescription.setText(survey.getScenarioDescription());

		JScrollPane scrollPane = new JScrollPane(scenariodescription);
		scenariodescriptionpanel.add(scrollPane);
		scenariopanel.add(scenariodescriptionpanel);

		JPanel p = new JPanel(new GridLayout(1, 1));
		p.add(button_scenario);

		scenariopanel.add(p);
		scenariopanel.add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private void loadNewOntology() {
		try {
			OWLOntology a = getOWLEditorKit().getModelManager().getActiveOntology();

			for (OWLOntology o : getOWLEditorKit().getModelManager().getOntologies()) {
				if (o != a) {
					getOWLEditorKit().getModelManager().removeOntology(o);
				}
			}
			// getOWLEditorKit().handleNewRequest();
			// getOWLModelManager().getOWLReasonerManager().getCurrentReasoner().dispose();
			getOWLEditorKit().handleLoadFrom(URI.create(survey.getURI()));
			getOWLEditorKit().getModelManager().removeOntology(a);
			// getOWLModelManager().getOWLReasonerManager()..getCurrentReasoner().flush();//.setCurrentReasonerFactoryId("org.protege.editor.owl.NoOpReasoner");
			TabbedWorkspace workspace = (TabbedWorkspace) getWorkspace();
			workspace.setSelectedTab(workspace.getWorkspaceTab("protege.survey.ProtegeSurveyTab"));

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void prepareQuestionPanel(SimpleAttributeSet set, Font f) {
		JScrollPane scrollPane;
		JPanel p;
		questionpanel.removeAll();
		questiondescriptionpanel = new JPanel(new BorderLayout());
		questionpanel.add(getMainLabel("Question"));

		JTextPane questiondescription = new JTextPane();
		questiondescription.setFont(f);
		questiondescription.setContentType("text/html");

		// Set the attributes before adding text
		questiondescription.setCharacterAttributes(set, true);
		questiondescription.setText(survey.getQuestionDescription());
		scrollPane = new JScrollPane(questiondescription);
		questiondescriptionpanel.add(scrollPane);
		questionpanel.add(questiondescriptionpanel);
		questionoptions = (Component) survey.getQuestionOptions();
		questionpanel.add(questionoptions);

		/// Add confirm button
		p = new JPanel(new GridLayout(1, 2));
		p.add(button_question);
		p.add(button_skip);
		questionpanel.add(p);
		questionpanel.add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private WorkspaceTab getWorkspaceTab(String s) throws Exception {

		if (tabs.containsKey(s)) {
			return tabs.get(s);
		}

		TabbedWorkspace workspace = (TabbedWorkspace) getWorkspace();
		// openTabs.clear();
		for (WorkspaceTabPlugin plugin : workspace.getOrderedPlugins()) {
			if (s.equals(plugin.getId())) {
				System.out.println("CL: " + plugin.getClass());
				WorkspaceTab wt = plugin.newInstance();
				wt.initialise();
				tabs.put(s, wt);
				return wt;
			}
		}

		return null;
	}

	private void updateSurveyViews() {
		viewpanel.removeAll();
		viewpanel.repaint();
		viewpanel.revalidate();
		if (current != null) {
			current.setVisible(false);
		}
		String tabid = survey.getTabId();

		WhatifUtils.p(tabid);

		if (!tabid.isEmpty()) {
			try {
				WorkspaceTab c = getWorkspaceTab(tabid);
				System.out.println("ID: " + c.getId());
				current = c;
				viewpanel.add(c);
				current.setVisible(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		viewpanel.repaint();
		viewpanel.validate();
	}

	private JLabel getMainLabel(String text) {
		return new JLabel("<html><font size=\"+2\">" + text + "  </font></html>", SwingConstants.RIGHT);
	}

	@Override
	protected void disposeOWLView() {
		// replaced active views
		resetTabs();
	}

	/*
	 * TABS: org.coode.owlviz.OWLVizTab protege.survey.ProtegeSurveyTab
	 * org.coode.dlquery.DLQueryTab org.protege.editor.owl.OWLIndividualsTab
	 * org.protege.editor.owl.OWLClassesTab
	 * org.protege.editor.owl.OWLOntologyTab
	 * org.protege.editor.owl.OWLObjectPropertiesTab
	 * inference.inspector.InferenceInspectorTab
	 * 
	 */

	/*
	 * org.protege.editor.owl.OWLDatatypeAnnotations
	 * org.whatif.tools.EnhancedClassHierarchyView
	 * org.protege.editor.owl.SelectedObjectView
	 * org.protege.editor.owl.InferredObjectPropertyTree
	 * org.protege.editor.owl.OWLAxiomAnnotationsView
	 * org.coode.change.ChangeView
	 * org.protege.editor.owl.OWLObjectPropertyDomainsAndRanges
	 * org.protege.editor.owl.OWLObjectPropertyTree
	 * org.protege.editor.owl.OWLOntologyPrefixesView
	 * org.protege.editor.owl.OWLIndividualUsageView
	 * org.protege.editor.owl.OWLMembersList
	 * org.protege.editor.owl.OWLObjectPropertyDescription
	 * org.protege.editor.owl.OWLDataPropertyUsageView
	 * org.protege.editor.owl.OWLIndividualRelationships
	 * org.protege.editor.owl.InferredAxioms org.coode.browser.LookupView
	 * org.protege.editor.owl.OWLAnnotationPropertyTree
	 * org.protege.editor.owl.OWLIndividualTypes
	 * org.coode.existentialtree.ReciprocalsTreeView
	 * org.protege.editor.owl.OWLObjectPropertyUsageView
	 * org.protege.editor.owl.OWLDataPropertyTree org.protege.editor.owl.Rules
	 * org.protege.editor.owl.GeneralClassAxioms
	 * org.whatif.tools.SyntacticModuleExtractorView
	 * org.whatif.tools.ProtegeSurveyView
	 * org.protege.editor.owl.OWLAnnotationPropertyUsageView
	 * org.coode.matrix.ClassPalette org.protege.editor.owl.OWLIndividualsList
	 * org.protege.editor.owl.OWLDatatypeDescription
	 * org.protege.editor.owl.OWLDatatypeList
	 * org.coode.matrix.ClassMembersMatrixView
	 * org.protege.editor.owl.OWLIndividualsByInferredType
	 * org.protege.editor.owl.OWLAssertedSuperClassHierarchy
	 * org.protege.editor.owl.OWLOntologyMetricsView
	 * org.protege.editor.owl.OWLClassAnnotations
	 * org.protege.editor.owl.OWLInferredSuperClassHierarchy
	 * org.protege.editor.owl.RDFXML
	 * org.protege.editor.owl.OWLObjectPropertyCharacteristics
	 * org.coode.matrix.InferredClassMatrixView
	 * org.protege.editor.owl.OWLClassUsageView org.coode.change.AxiomsView
	 * org.protege.editor.owl.NavigationView org.coode.matrix.IndividualsPalette
	 * org.protege.editor.owl.OWLAnnotationPropertyDescription
	 * org.protege.editor.owl.OWLIndividualAnnotations
	 * org.protege.editor.owl.InferredOWLClassHierarchy
	 * org.coode.matrix.DataPropsPalette org.coode.matrix.DataPropertyMatrixView
	 * org.protege.editor.owl.OWLOntologyAnnotationsView
	 * org.coode.existentialtree.ExistentialTreeView
	 * org.protege.editor.owl.ManchesterOWLSyntax
	 * org.coode.matrix.IndividualMatrixView
	 * org.coode.matrix.AnnotationURIPalette
	 * org.protege.editor.owl.OWLOntologyImportsTable
	 * org.protege.editor.owl.OWLXML org.coode.matrix.ObjectPropertyMatrixView
	 * org.coode.existentialtree.ExistentialTreeView2
	 * org.protege.editor.owl.OWLDatatypeUsageView
	 * org.protege.editor.owl.ManchesterSyntaxFrameView
	 * org.coode.existentialtree.TextOutlineView
	 * org.protege.editor.owl.SelectedEntityView
	 * org.protege.editor.owl.OWLClassDescription org.coode.change.DiffView
	 * org.protege.editor.owl.OWLAnnotationPropertyAnnotations
	 * org.protege.editor.owl.OWLFunctionalSyntax org.whatif.tools.HistoryView
	 * org.protege.editor.owl.OWLAssertedClassHierarchy
	 * org.protege.editor.owl.OWLDataPropertyAnnotations
	 * org.coode.browser.OWLDocView
	 * org.protege.editor.owl.OWLDataPropertyDescription
	 * org.whatif.tools.EntailmentInspectorView
	 * org.protege.editor.owl.OWLIndividualsByType
	 * org.whatif.tools.SurveyWidgetsView org.coode.matrix.ClassMatrixView
	 * org.protege.editor.owl.OWLInferredMembersList
	 * org.protege.editor.owl.DLMetricsView
	 * org.protege.editor.owl.OWLDataPropertyDomainsAndRanges
	 * org.protege.editor.owl.OWLObjectPropertyAnnotations
	 * org.protege.editor.owl.OWLDataPropertyCharacteristics
	 * org.protege.editor.owl.OWLAssertedClassHierarchy
	 */

	private void setQuestionMetadata() {
		qm.setEnd(System.currentTimeMillis());
		qm.setClickcount(clickCount);
		qm.setKeystrokecount(keystrokeCount);
		qm.setScrollAmount(scrollAmount);
	}
}

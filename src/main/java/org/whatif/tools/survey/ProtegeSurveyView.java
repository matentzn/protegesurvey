package org.whatif.tools.survey;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
import org.whatif.tools.survey.util.WrapLayout;
import org.whatif.tools.util.MyGlassPane;
import org.whatif.tools.util.WhatifUtils;

public class ProtegeSurveyView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = -4515710047558710080L;

	private static final Logger log = Logger.getLogger(ProtegeSurveyView.class);

	// JPanel surveypanel = null;
	JPanel sidebarpanel = null;
	JPanel viewpanel = null;
	JPanel scenariopanel = null;
	JPanel scenariodescriptionpanel = null;
	JTextPane scenariodescription = new JTextPane();
	JPanel taskpanel = null;
	JPanel taskdescriptionpanel = null;
	JTextPane taskdescription = new JTextPane();
	JPanel questionpanel = null;
	JPanel questiondescriptionpanel = null;
	private JProgressBar progressBar = null;
	int qcount = 0;
	Timer timer;
	Border compound;
	JComponent questionoptions = null;
	CheckboxGroup group = new CheckboxGroup();
	List<Checkbox> cblist = new ArrayList<Checkbox>();

	// HashSet<String> openTabs = new HashSet<String>();

	// private OWLSelectionModelListener listener;

	JButton button_startsurvey = new JButton("Start Survey");
	JTextField namefield = new JTextField();
	JButton button_scenario = new JButton("Ok");
	JButton button_task = new JButton("Ok");
	JButton button_question = new JButton("Next");
	JButton button_expection = new JButton("Next");
	JButton button_rate = new JButton("Next");
	JButton button_skip = new JButton("Skip");
	JButton button_restart_survey = new JButton("Restart");

	Map<String, WorkspaceTab> tabs = new HashMap<String, WorkspaceTab>();
	WorkspaceTab current = null;

	static Toolkit tk = Toolkit.getDefaultToolkit();
	static long eventMask = AWTEvent.MOUSE_WHEEL_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK;

	ProtegeSurvey survey = null;
	QuestionMetadata qm = null;
	int clickCount = 0;
	int clickCount_component = 0;
	int keystrokeCount = 0;
	double scrollAmount = 0;

	@Override
	protected void initialiseOWLView() throws Exception {
		startSurvey();
		log.info("Protege Survey initialized");
	}

	private void initialiseSurvey(File file) {
		WhatifUtils.p(new File("").getAbsolutePath());
		SurveyRenderer factory = new SwingSurveyRenderer();
		survey = new ProtegeSurvey(file, factory);
		progressBar.setMaximum(survey.getTotalQuestionCount());
	}

	private void setupSidebarPanel() {
		// Set up Sidebar panel
		Border loweredbevel = BorderFactory.createLineBorder(Color.BLACK);
		Border empytyborder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		// This creates a nice frame.
		compound = BorderFactory.createCompoundBorder(loweredbevel, empytyborder);
		sidebarpanel = new JPanel();
		sidebarpanel.setLayout(new GridBagLayout());
		sidebarpanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		sidebarpanel.setPreferredSize(new Dimension(400, 600));
		sidebarpanel.setMaximumSize(new Dimension(400, 100000));
		sidebarpanel.setMinimumSize(new Dimension(400, 600));
		
		scenariopanel = new JPanel();
		scenariopanel.setLayout(new BoxLayout(scenariopanel, BoxLayout.PAGE_AXIS));
		scenariopanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel startsurvey = new JPanel(new BorderLayout());
		startsurvey.add(new JLabel("Participant ID:"), BorderLayout.NORTH);
		startsurvey.add(namefield, BorderLayout.CENTER);
		startsurvey.add(button_startsurvey, BorderLayout.SOUTH);
		startsurvey.setMaximumSize(new Dimension(1000, 80));
		startsurvey.setAlignmentX(Component.LEFT_ALIGNMENT);
		scenariopanel.add(startsurvey);

		taskpanel = new JPanel();
		taskpanel.setLayout(new BoxLayout(taskpanel, BoxLayout.PAGE_AXIS));
		taskpanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		questionpanel = new JPanel();
		questionpanel.setLayout(new BoxLayout(questionpanel, BoxLayout.PAGE_AXIS));
		questionpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		questionpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		//

		progressBar = new JProgressBar();
		qcount = 0;
		JPanel progress = new JPanel(new BorderLayout());
		progress.add(progressBar, BorderLayout.CENTER);
		JLabel l = new JLabel("Progress: ");
		l.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 10));
		progress.add(l, BorderLayout.LINE_START);
		progress.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
		progress.setMaximumSize(new Dimension(1000, 30));
		// questionpanel.setPreferredSize(new Dimension(300,0));
		// questionpanel.setBackground(Color.BLUE);

		GridBagConstraints c = new GridBagConstraints();

		c.weightx = 1;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.1;
		c.gridx = 0;
		c.gridy = 0;
		sidebarpanel.add(progress, c);
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 1;
		sidebarpanel.add(scenariopanel, c);
		c.gridx = 0;
		c.gridy = 2;
		sidebarpanel.add(taskpanel, c);
		c.gridx = 0;
		c.gridy = 3;
		sidebarpanel.add(questionpanel, c);
		
		// Register listeners
		prepareRatingGroup();
		registerSidebarButtonActionListener();
	}

	private void registerSidebarButtonActionListener() {
		removeAllListeners(button_startsurvey);
		button_startsurvey.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
				int result = fileChooser.showOpenDialog(ProtegeSurveyView.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					initialiseSurvey(selectedFile);
					boolean started = survey.start(getParticipantName());
					if (started) {
						updateScenario();
					}
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
				// survey.applyTaskChanges(getOWLModelManager().getActiveOntology(),
				// false);
				boolean reasonercondition = getOWLModelManager().getOWLReasonerManager().getReasonerStatus()
						.equals(ReasonerStatus.INITIALIZED);
				WhatifUtils.p("#######" + reasonercondition + " "
						+ getOWLModelManager().getOWLReasonerManager().getReasonerStatus());
				if (survey.validateTask(getO(), reasonercondition)) {
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
				String rating = group.getSelectedCheckbox() == null ? "" : group.getSelectedCheckbox().getName();
				survey.validateRating(rating);
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

		// This is where the tabs will be displayed
		viewpanel = new JPanel(new GridLayout(1, 1));

		// to collect clicks and so on globally
		tk.addAWTEventListener(createMouseAndKeyListener(), eventMask);

		setupSidebarPanel();

		// surveypanel.add(new JPanel(), c);
		removeAll();
		add(sidebarpanel, BorderLayout.WEST);
		add(viewpanel, BorderLayout.CENTER);
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
		Font f = new JLabel().getFont();

		SurveyState state = survey.getState();
		switch (state) {
		case READY:
			break;
		case SCENARIO:
			taskpanel.removeAll();
			questionpanel.removeAll();
			scenariopanel.removeAll();
			viewpanel.removeAll();
			button_scenario.setEnabled(true);
			questionpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
			button_scenario.setEnabled(false);
			textPanelLightGray(scenariodescription);
			button_task.setEnabled(true);
			questionpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			prepareTaskPanel(set, f);
			survey.applyTaskChanges(getO());
			if (!survey.taskNeedsConfirmation()) {
				System.out.println("test");
				button_task.doClick();
			}
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
			stopTimer();
			updateSurveyViews();
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					button_scenario.setEnabled(false);
					button_task.setEnabled(false);
					textPanelLightGray(taskdescription);
					prepareQuestionPanel(set, f);
				}
			});
			
			resetQuestionMetadata();
			timeoutQuestion();
			break;
		case QUESTIONVALIDATE:
			stopTimer();
			questionpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			progressBar.setValue(qcount);
			survey.nextState();
			updateScenario();
			break;
		case RATEQUESTION:
			if (survey.rateQuestion()) {
				prepareQuestionPanelForRating(set, f);
			} else {
				survey.nextState();
				updateScenario();
			}
			break;
		case FINISHED:
			questionpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
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

		progressBar.revalidate();
		progressBar.repaint();

		revalidate();
		repaint();

	}

	private void stopTimer() {
		if (timer != null && timer.isRunning()) {
			timer.stop();
		}
	}

	private void textPanelLightGray(JTextPane jtp) {
		StyledDocument doc = jtp.getStyledDocument();
		SimpleAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setForeground(attrs, Color.LIGHT_GRAY);
		doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
		jtp.setDocument(doc);
	}

	private OWLOntology getO() {
		return getOWLEditorKit().getModelManager().getActiveOntology();
	}

	private void timeoutQuestion() {
		if (survey.currentQuestionIsTimed()) {
			int timeout = survey.getCurrentQuestionTimeout();
			timer = new Timer(timeout * 1000, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (survey.getState().equals(SurveyState.QUESTION))
						button_skip.doClick();
				}
			});
			timer.start();
		}
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
		clickCount_component = 0;
		keystrokeCount = 0;
		scrollAmount = 0;
		qm = new QuestionMetadata();
		qm.setStart(System.currentTimeMillis());
	}

	private void prepareFinishedPanel(SimpleAttributeSet set, Font f) {
		scenariodescriptionpanel = new JPanel(new BorderLayout());
		JTextPane finisheddescription = new JTextPane();
		finisheddescription.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		finisheddescription.setFont(f);
		// Set the attributes before adding text
		finisheddescription.setCharacterAttributes(set, true);
		finisheddescription.setText("Survey Finished! Thank you for you participation.");
		finisheddescription.setEditable(false);
		scenariodescriptionpanel.add(finisheddescription);

		scenariopanel.add(getMainLabel("Thank you!", ""));
		scenariodescriptionpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		scenariopanel.add(scenariodescriptionpanel);
		/// Add confirm button
		JPanel p = new JPanel(new GridLayout(1, 1));
		p.add(button_restart_survey);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		scenariopanel.add(p);
		scenariopanel.add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private void prepareTaskPanel(SimpleAttributeSet set, Font f) {
		taskdescriptionpanel = new JPanel(new BorderLayout());

		if (survey.currentScenarioInvolvesOntology()) {
			int tasks = survey.getRemaingTaskCount();
			taskpanel.add(getMainLabel("Authoring Action", tasks + " remaining in scenario"));
		}

		taskdescription.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		taskdescription.setFont(f);

		taskdescription.setContentType("text/html");
		taskdescription.setCharacterAttributes(set, true);
		taskdescription.setText(survey.getTaskDescription());
		taskdescription.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(taskdescription);
		taskdescriptionpanel.add(scrollPane);
		taskdescriptionpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		taskpanel.add(taskdescriptionpanel);

		/// Add confirm button
		JPanel p = new JPanel(new GridLayout(1, 1));
		p.add(button_task);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		taskpanel.add(p);
		taskpanel.add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private void prepareScenarioPanel(SimpleAttributeSet set, Font f) {
		scenariodescriptionpanel = new JPanel(new BorderLayout());

		// c.weighty = 0.5;

		// c.gridx = 0;
		// c.gridy = 0;
		if (survey.currentScenarioInvolvesOntology()) {
			int scenarios = survey.getRemainingScenarioCount();
			scenariopanel.add(getMainLabel("Scenario", scenarios + " remaining"));
		}

		scenariodescription.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		scenariodescription.setContentType("text/html");
		scenariodescription.setFont(f);
		// Set the attributes before adding text
		scenariodescription.setCharacterAttributes(set, true);
		scenariodescription.setText(survey.getScenarioDescription());
		scenariodescription.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(scenariodescription);
		scenariodescriptionpanel.add(scrollPane);
		scenariodescriptionpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		scenariopanel.add(scenariodescriptionpanel);

		JPanel p = new JPanel(new GridLayout(1, 1));
		p.add(button_scenario);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);

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

		questionpanel.setBorder(compound);
		questiondescriptionpanel = new JPanel(new BorderLayout());
		int questct = survey.getRemainingQuestionCount();
		questionpanel.add(getMainLabel("Question", questct + " questions remaining in task"));

		JTextPane questiondescription = new JTextPane();
		questiondescription.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		questiondescription.setFont(f);
		questiondescription.setContentType("text/html");

		// Set the attributes before adding text
		questiondescription.setCharacterAttributes(set, true);
		questiondescription.setText(survey.getQuestionDescription());
		questiondescription.setEditable(false);
		scrollPane = new JScrollPane(questiondescription);
		questiondescriptionpanel.add(scrollPane);
		questiondescriptionpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		questionpanel.add(questiondescriptionpanel);
		questionoptions = (JComponent) survey.getQuestionOptions();
		questionoptions.setAlignmentX(Component.LEFT_ALIGNMENT);
		questionpanel.add(questionoptions);

		/// Add confirm button
		p = new JPanel(new GridLayout(1, 2));
		p.add(button_question);
		p.add(button_skip);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		questionpanel.add(p);
		questionpanel.add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private void prepareQuestionPanelForRating(SimpleAttributeSet set, Font f) {
		/*
		 * for (Checkbox cb : cblist) { cb.setState(false); }
		 */
		group.setSelectedCheckbox(null);

		JScrollPane scrollPane;
		JPanel p;
		questionpanel.removeAll();
		questiondescriptionpanel = new JPanel(new BorderLayout());
		questionpanel.add(getMainLabel("Rate View", ""));
		questionpanel.setBorder(compound);

		JTextPane questiondescription = new JTextPane();
		questiondescription.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		questiondescription.setFont(f);
		questiondescription.setContentType("text/html");

		// Set the attributes before adding text
		questiondescription.setCharacterAttributes(set, true);
		questiondescription.setText("<font size=\"5\">Please rate how well the view supported you at answering the question.</font>");
		questiondescription.setEditable(false);
		scrollPane = new JScrollPane(questiondescription);
		questiondescriptionpanel.add(scrollPane);
		questionpanel.add(questiondescriptionpanel);
		questiondescriptionpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		questionoptions = new JPanel(new GridLayout(1, 1));
		JPanel cbp = new JPanel();
		cbp.setLayout(new WrapLayout());
		cbp.setSize(new Dimension(350, 1));
		cbp.add(new JLabel("poor"));
		for (Checkbox cb : cblist) {
			cbp.add(cb);
		}
		cbp.add(new JLabel("perfect"));
		questionoptions.setAlignmentX(Component.LEFT_ALIGNMENT);

		questionoptions.add(cbp);
		questionpanel.add(questionoptions);

		/// Add confirm button
		p = new JPanel(new GridLayout(1, 1));
		p.add(button_rate);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		questionpanel.add(p);
		questionpanel.add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private void prepareRatingGroup() {
		group = new CheckboxGroup();
		createCheckBox("0");
		createCheckBox("1");
		createCheckBox("2");
		createCheckBox("3");
		createCheckBox("4");
	}

	private void createCheckBox(String name) {
		Checkbox cb0 = new Checkbox(name, group, false);
		cb0.setName(name);
		cblist.add(cb0);
	}

	private WorkspaceTab getWorkspaceTab(String s) throws Exception {

		if (tabs.containsKey(s)) {
			return tabs.get(s);
		}

		TabbedWorkspace workspace = (TabbedWorkspace) getWorkspace();
		// openTabs.clear();
		/*
		 * for (WorkspaceTabPlugin plugin : workspace.getOrderedPlugins()) {
		 * System.out.println("GR: " + plugin.getId()); }
		 */
		for (WorkspaceTabPlugin plugin : workspace.getOrderedPlugins()) {
			if (s.equals(plugin.getId())) {
				// System.out.println("CL: " + plugin.getClass());
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

	private JLabel getMainLabel(String text, String subtext) {
		String s = subtext.isEmpty() ? "" : "(" + subtext + ")";
		JLabel j = new JLabel("<html><font size=\"+2\">" + text + "  </font><br>" + s + "</html>", SwingConstants.LEFT);
		j.setHorizontalTextPosition(SwingConstants.LEFT);
		j.setHorizontalAlignment(SwingConstants.LEFT);
		j.setAlignmentX(Component.LEFT_ALIGNMENT);
		j.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 0));
		return j;
	}

	@Override
	protected void disposeOWLView() {
		// replaced active views
		resetTabs();
	}

	/*
	 * TABS: org.coode.owlviz.OWLVizTab protege.survey.ProtegeSurveyTab
	 * org.protege.editor.owl.OWLOntologyTab
	 * org.protege.editor.owl.OWLEntitesTab org.protege.editor.owl.OWLClassesTab
	 * org.protege.editor.owl.OWLObjectPropertiesTab
	 * org.protege.editor.owl.OWLDataPropertiesTab
	 * org.protege.editor.owl.OWLAnnotationsPropertiesTab
	 * org.protege.editor.owl.OWLIndividualsTab org.coode.owlviz.OWLVizTab
	 * org.coode.dlquery.DLQueryTab org.protege.ontograf.OntoGrafTab
	 * swrltab-plugin.SWRLTab 
	 * protege.survey.ProtegeSurveyAllEntitiesTab
	 * swrltab-plugin.SQWRLTab protege.survey.ProtegeSurveyTab
	 * inference.inspector.InferenceInspectorTab
	 * org.protege.editor.owl.rdf.SparqlTab
	 * protege.survey.all
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
		qm.setClickcountComponent(clickCount_component);
		qm.setKeystrokecount(keystrokeCount);
		qm.setScrollAmount(scrollAmount);
		qcount++;
	}
}

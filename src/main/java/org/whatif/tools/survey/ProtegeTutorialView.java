package org.whatif.tools.survey;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLOntology;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class ProtegeTutorialView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = -4515710047558710080L;

	private static final Logger log = Logger.getLogger(ProtegeTutorialView.class);

	@Override
	protected void initialiseOWLView() throws Exception {
	    add(getBrowserPanel("https://www.youtube.com/v/b-Cr0EWwaTk?fs=1"),BorderLayout.CENTER);
        add(new JLabel("TEST"),BorderLayout.SOUTH);
		log.info("Protege Tutorial initialized");
	}
	
	public static JPanel getBrowserPanel(String url) {
	    JPanel webBrowserPanel = new JPanel(new BorderLayout());
	    JWebBrowser webBrowser = new JWebBrowser();
	    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
	    webBrowser.setBarsVisible(false);
	    webBrowser.navigate(url);
	    return webBrowserPanel;
	}

	private OWLOntology getO() {
		return getOWLEditorKit().getModelManager().getActiveOntology();
	}

	@Override
	protected void disposeOWLView() {
		// TODO Auto-generated method stub

	}
}

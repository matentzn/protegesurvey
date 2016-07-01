package org.whatif.tools.survey.util;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.whatif.tools.util.WhatifUtils;

public class SwingSurveyRenderer implements SurveyRenderer {
	
	
	
	@Override
	public Object renderQuestion(ProtegeSurveyQuestion q) {
		List<String> options = q.getOptions();
		JPanel p = new JPanel();
				
		//WhatifUtils.p("TEST"+q.getOptionsType());
		if(q.getOptionsType().equals("radio")) {
			CheckboxGroup group = new CheckboxGroup();
			p.setLayout(new WrapLayout());
			p.setSize(new Dimension(350, 1));
			for(String op:options) {
				Checkbox cb = new Checkbox(op, group, false);
				p.add(cb);
			}
		} else if(q.getOptionsType().equals("check")) {
			//p.setLayout(new GridLayout(rows,3));
			p.setLayout(new WrapLayout());
			p.setSize(new Dimension(350, 1));
			for(String op:options) {
				Checkbox cb = new Checkbox(op, false);
				p.add(cb);
			}
		} else if(q.getOptionsType().startsWith("rate_")) {
			List<Checkbox> cblist = prepareRatingGroup();
			p.setLayout(new WrapLayout());
			p.setSize(new Dimension(350, 1));
			String[] spl = q.getOptionsType().split("_");
			p.add(new JLabel(spl[1].replaceAll("-", " ")));
			for (Checkbox cb : cblist) {
				p.add(cb);
			}
			p.add(new JLabel(spl[2].replaceAll("-", " ")));
		}
		if(q.getOptionsType().equals("text")) {
			p.setLayout(new GridLayout(1,1));
			JTextArea textArea = new JTextArea("Your answer here..", 5, 10);
			textArea.addMouseListener(new MouseAdapter(){
				boolean first = true;
	            @Override
	            public void mouseClicked(MouseEvent e){
	            	if(first) {
	            		textArea.setText("");
	            		first=false;
	            	}
	            }
	        });

			p.add(textArea);
		} 
		
		return p;
	}
	
	private List<Checkbox> prepareRatingGroup() {
		CheckboxGroup group = new CheckboxGroup();
		List<Checkbox> cblist = new ArrayList<Checkbox>();
		createCheckBox("0",cblist,group);
		createCheckBox("1",cblist,group);
		createCheckBox("2",cblist,group);
		createCheckBox("3",cblist,group);
		createCheckBox("4",cblist,group);
		return cblist;
	}

	private void createCheckBox(String name,List<Checkbox> cblist,CheckboxGroup group) {
		Checkbox cb0 = new Checkbox(name, group, false);
		cb0.setName(name);
		cblist.add(cb0);
	}

	@Override
	public Set<String> parseResults(Object object) {
		JPanel panel = (JPanel)object;
		Set<String> result = new HashSet<String>();
		for(Component c:panel.getComponents()) {
			if(c instanceof JTextArea) {
				JTextArea ta = (JTextArea)c;
				result.add(ta.getText());
			} else if(c instanceof Checkbox) {
				Checkbox cb = (Checkbox)c;
				if(cb.getState()) {
					result.add(cb.getLabel());
				}
			}
		}
		return result;
	}

}

package org.whatif.tools.survey.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ProtegeSurveySampler {

	public static void main(String[] args)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		File in = new File("C:\\Users\\nico\\Desktop\\inf_exp\\protege_survey_template.xml");
		File outdir = new File("C:\\Dropbox\\UoM\\projects\\whatif\\evaluation\\sampled");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(in);
		doc.getDocumentElement().normalize();

		Map<String, List<String>> groups_treatments = new HashMap<String, List<String>>();
		Map<String, List<String>> groups_tasks = new HashMap<String, List<String>>();
		Map<String, Integer> groups_indexed = new HashMap<String, Integer>();

		Set<String> tasks = new HashSet<String>(); /// randomise checkbox
													/// questions

		NodeList nLisaat = doc.getElementsByTagName("task");
		Integer index_i = 0;
		for (int temp = 0; temp < nLisaat.getLength(); temp++) {

			Node nNode = nLisaat.item(temp);

			System.out.println("\nCurrent Element :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element task = (Element) nNode;
				String treatment = task.getAttribute("treatments");

				String group = task.getAttribute("group");
				String id = task.getAttribute("id");
				tasks.add(id);
				System.out.println("ID: " + id + " G: " + group + " T: " + treatment);
				if (!group.isEmpty() && treatment.contains(",")) {
					if (!groups_indexed.containsKey(group)) {
						groups_indexed.put(group, index_i);
						index_i++;
					}
					groups_treatments.put(group, Arrays.asList(treatment.split(",")));
					if (!groups_tasks.containsKey(group)) {
						List<String> l = new ArrayList<String>();
						l.add("");
						l.add("");
						groups_tasks.put(group, l);
					}
					Integer task_index = Integer.valueOf(id.substring(id.length() - 1)) - 1;
					System.out.println(task_index);
					groups_tasks.get(group).set(task_index, id);
				}
			}

		}

		System.out.println("''''");
		System.out.println(groups_treatments.size());
		System.out.println(groups_tasks.size());

		Set<String> alreadyused = new HashSet<String>();
		alreadyused.add("1111111");
		alreadyused.add("0111101");
		alreadyused.add("1010111");
		alreadyused.add("1111010");
		List<String> result = new ArrayList<String>();
		printBin("", 7, result);
		result.removeAll(alreadyused);
		Collections.shuffle(result);
		int part = 5;
		Set<String> sample = new HashSet<String>(result.subList(0, 15));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		for (String s : sample) {
			System.out.println("#########");
			String[] explode = s.split("");
			System.out.println(Arrays.toString(explode));
			Document copiedDocument = db.newDocument();
			Node originalRoot = doc.getDocumentElement();
			Node copiedRoot = copiedDocument.importNode(originalRoot, true);
			copiedDocument.appendChild(copiedRoot);

			NodeList nList = copiedDocument.getElementsByTagName("task");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element task = (Element) nNode;
					String group = task.getAttribute("group");
					if (!group.isEmpty()) {
						String id = task.getAttribute("id");
						System.out.println("ID: " + id);
						String ind = id.substring(id.length() - 1);
						System.out.println("IND: " + ind);
						String treatment = explode[groups_indexed.get(group)];
						System.out.println("TREAT:" + treatment + ":");
						String tab = "";
						if (ind.equals("1")) {
							if (treatment.equals("1")) {
								tab = "inference.inspector.InferenceInspectorTab";
							} else {
								if (id.contains("fhkb")) {
									tab = "org.protege.editor.owl.OWLIndividualsTab";
								} else {
									tab = "org.protege.editor.owl.OWLClassesTab";
								}
							}
						} else {
							if (treatment.equals("1")) {
								if (id.contains("fhkb")) {
									tab = "org.protege.editor.owl.OWLIndividualsTab";
								} else {
									tab = "org.protege.editor.owl.OWLClassesTab";
								}
							} else {

								tab = "inference.inspector.InferenceInspectorTab";
							}
						}
						System.out.println("TAB: " + tab);
						NodeList qlist = task.getElementsByTagName("question");
						for (int t = 0; t < qlist.getLength(); t++) {

							Node qnode = qlist.item(t);
							
							if (qnode.getNodeType() == Node.ELEMENT_NODE) {

								Element question = (Element) qnode;
								System.out.println("Q: " + question.getAttribute("id"));
								System.out.println(question.getAttribute("tab"));
								if (question.getAttribute("tab").equals("rand")) {
									question.setAttribute("tab", tab);
								}
								else {
									System.out.println("#########");
								}
							}
						}
					}
				}

			}
			// Use a Transformer for output
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			StreamResult streamResult = new StreamResult(new File(outdir, "protege_survey_p" + part + ".xml"));
			part++;
			DOMSource source = new DOMSource(copiedDocument);
			transformer.transform(source, streamResult);
			/// System.out.println(result.size());
			//System.exit(0);
		}

	}

	public static void printBin(String soFar, int iterations, List<String> result) {
		if (iterations == 0) {
			result.add(soFar);
		} else {
			printBin(soFar + "0", iterations - 1, result);
			printBin(soFar + "1", iterations - 1, result);
		}
	}
}

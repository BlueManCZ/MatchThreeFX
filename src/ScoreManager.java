package MatchThree;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class ScoreManager {

    private String fileName;
    private List<Score> scores = new ArrayList<>();
    private Document doc;

    ScoreManager(String fileName) {
        this.fileName = fileName;
        File file = new File(fileName);
        try {
            boolean created = file.createNewFile();
            System.out.println("Scores file was" + (created ? "" : "n't") + " created.");
            update();
        } catch (IOException e1) {
            System.err.println("Error while creating or reading scores file.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void update() throws Exception {
        InputStream input = new FileInputStream(fileName);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        try {
            doc = documentBuilder.parse(input);
            Element rootElement = doc.getDocumentElement();

            input.close();

            for (int i = 0; i < rootElement.getChildNodes().getLength(); i++) {
                Node node = rootElement.getChildNodes().item(i);

                if (node.getNodeName().equals("tr")) {
                    String name = null;
                    int result = 0;
                    for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                        Node line = node.getChildNodes().item(j);
                        if (line.getNodeName().equals("th")) {
                            if (line.getAttributes().item(0).getTextContent().equals("Name")) {
                                name = line.getTextContent();
                            }
                            if (line.getAttributes().item(0).getTextContent().equals("Result")) {
                                result = Integer.valueOf(line.getTextContent());
                            }
                        }
                    }
                    Score score = new Score(name, result);
                    scores.add(score);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Score file is empty or corrupted.");
            doc = documentBuilder.newDocument();
            doc.appendChild(doc.createElement("table"));
        }
    }

    void appendScore(Score score) {
        this.scores.add(score);
        Element tr = doc.createElement("tr");
        Element name = doc.createElement("th");
        name.setAttribute("class", "Name");
        name.appendChild(doc.createTextNode(score.getName()));
        Element result = doc.createElement("th");
        result.setAttribute("class", "Result");
        result.appendChild(doc.createTextNode((String.valueOf(score.getResult()))));
        tr.appendChild(name);
        tr.appendChild(result);
        doc.getDocumentElement().appendChild(tr);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult output = new StreamResult(new FileOutputStream(fileName));
            transformer.transform(source, output);
            output.getOutputStream().close();
        } catch (Exception e) {
            System.err.println("Error while writing into XML");
            e.printStackTrace();
        }
    }

    List<Score> getScores() {
        scores.sort((score1, score2) -> Integer.compare(score2.getResult(), score1.getResult()));
        return scores;
    }
}

package son.nevymenene;

import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class XMLGenerator {

    public static void main(String[] args) throws Exception {
        // Tu predpokladáme, že máte načítaný ResultSet
        DbfConnector conn = new DbfConnector();
        ResultSet rs = conn.getResultSetFromDatabase(); // Naplniť ResultSet podľa databázy

        // Vytvorenie XML dokumentu
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Korene XML dokumentu
        Element rootElement = doc.createElement("Road");
        rootElement.setAttribute("Version", "6.2");
        Element savedBy = doc.createElement("SavedBy");
        savedBy.setAttribute("SwName", "Supercom Android App");
        savedBy.setAttribute("SwVersion", "1.1 ALPHA");

        Element savedDate = doc.createElement("SaveDate");
        savedDate.setTextContent(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));

        doc.appendChild(rootElement);
        rootElement.appendChild(savedBy);
        rootElement.appendChild(savedDate);

        // Struktúra mapovania
        String currentDom = null;
        String currentVchod = null;
        String currentByt = null;

        // Spracovanie každého riadku v ResultSet
        while (rs.next()) {
            String clcis = rs.getString("A.CLCIS6");
            String dom = (rs.getString("A.CLCIS6")).substring(0, 3);
            String vchod ="";// doplniť vchod z databazy
            String byt = (rs.getString("A.CLCIS6")).substring(3, 6);
            String porcm = rs.getString("B.PORCM");
            String miestnost = rs.getString("B.DRUH");

            String[] merace = new String[2];
            merace[0] = rs.getString("B.VCMER1");//prvy merac
            merace[1] = rs.getString("B.VCMER2");// druhy merac

            // Ak sa zmení dom, vytvoríme nový element <dom>
            if (!dom.equals(currentDom)) {
                currentDom = dom;
                Element domElement = doc.createElement("Group");
                domElement.setAttribute("Caption", "ČOBJ: " + dom);
                Element infoElement = doc.createElement("Info");
                Element hintElement = doc.createElement("Hint");
                Element userElement = doc.createElement("User");
                infoElement.appendChild(hintElement);
                infoElement.appendChild(userElement);
                domElement.appendChild(infoElement);
                rootElement.appendChild(domElement);

                // Pre každý nový dom, v ňom budeme kontrolovať vchody a byty
                currentVchod = null;
            }

            // Ak sa zmení vchod, vytvoríme nový element <vchod>
            Element domElement = (Element) rootElement.getLastChild();
            if (!vchod.equals(currentVchod)) {
                currentVchod = vchod;
                Element vchodElement = doc.createElement("Group");
                vchodElement.setAttribute("Caption", "VCHOD: " + vchod);
                Element infoElement = doc.createElement("Info");
                Element hintElement = doc.createElement("Hint");
                Element userElement = doc.createElement("User");
                infoElement.appendChild(hintElement);
                infoElement.appendChild(userElement);
                vchodElement.appendChild(infoElement);
                domElement.appendChild(vchodElement);

                // Pre každý nový vchod, v ňom budeme kontrolovať byty
                currentByt = null;
            }

            // Ak sa zmení byt, vytvoríme nový element <byt>
            Element vchodElement = (Element) domElement.getLastChild();
            if (!byt.equals(currentByt)) {
                currentByt = byt;
                Element bytElement = doc.createElement("Group");
                bytElement.setAttribute("Caption", "Poschodie:  " + "Č.bytu:" + byt);
                Element infoElement = doc.createElement("Info");
                Element hintElement = doc.createElement("Hint");
                Element userElement = doc.createElement("User");
                infoElement.appendChild(hintElement);
                infoElement.appendChild(userElement);
                bytElement.appendChild(infoElement);
                vchodElement.appendChild(bytElement);
            }

            // Pridáme merač do príslušného bytu
            Element bytElement = (Element) vchodElement.getLastChild();
            //Merace v byte mame v poli merace, pretoze v byte mozu byt na jednom radiatore aj dva merace  
            for (String merac : merace) {
                if (merac != null && !merac.isEmpty()) {

                    Element meracElement = doc.createElement("Task");
                    meracElement.setAttribute("Caption", porcm + miestnost);
                    meracElement.setAttribute("Status", "ToDo");
                    meracElement.setAttribute("Agent", "http://www.sontex.com/Son556-read");

                    Element infoElement = doc.createElement("Info");
                    Element hintElement = doc.createElement("Hint");
                    Element userElement = doc.createElement("User");

                    userElement.setTextContent(clcis);
                    infoElement.appendChild(hintElement);
                    infoElement.appendChild(userElement);
                    meracElement.appendChild(infoElement);

                    Element paramElement = doc.createElement("Param");
                    Element radioAddrElement = doc.createElement("RadioAddr");
                    Element dataToReadElement = doc.createElement("DataToRead");

                    radioAddrElement.setTextContent(merac);
                    dataToReadElement.setTextContent("1");
                    paramElement.appendChild(radioAddrElement);
                    paramElement.appendChild(dataToReadElement);
                    meracElement.appendChild(paramElement);

                    Element lastActionElement = doc.createElement("LastActionDate");
                    lastActionElement.setTextContent(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));
                    meracElement.appendChild(lastActionElement);

                    Element dataElement = doc.createElement("Data");
                    meracElement.appendChild(dataElement);

                    bytElement.appendChild(meracElement);

                }

            }

        }

        // Uloženie XML do súboru
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("nevymenenePRVN.xml"));
        transformer.transform(source, result);

        System.out.println("XML súbor bol úspešne vytvorený!");
    }

}

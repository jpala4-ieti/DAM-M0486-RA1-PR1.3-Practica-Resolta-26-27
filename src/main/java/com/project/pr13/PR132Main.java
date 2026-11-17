package com.project.pr13;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.project.pr13.format.AsciiTablePrinter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Classe principal que permet gestionar un fitxer XML de cursos amb opcions per llistar, afegir i eliminar alumnes, 
 * així com mostrar informació dels cursos i mòduls.
 * 
 * Aquesta classe inclou funcionalitats per interactuar amb un fitxer XML, executar operacions de consulta,
 * i realitzar modificacions en el contingut del fitxer.
 */
public class PR132Main {

    private final Path xmlFilePath;
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Constructor de la classe PR132Main.
     * 
     * @param xmlFilePath Ruta al fitxer XML que conté la informació dels cursos.
     */
    public PR132Main(Path xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    /**
     * Mètode principal que inicia l'execució del programa.
     * 
     * @param args Arguments passats a la línia de comandament (no s'utilitzen en aquest programa).
     */
    public static void main(String[] args) {
        String userDir = System.getProperty("user.dir");
        Path xmlFilePath = Paths.get(userDir, "data", "pr13", "cursos.xml");

        PR132Main app = new PR132Main(xmlFilePath);
        app.executar();
    }

    /**
     * Executa el menú principal del programa fins que l'usuari decideixi sortir.
     */
    public void executar() {
        boolean exit = false;
        while (!exit) {
            mostrarMenu();
            System.out.print("Escull una opció: ");
            int opcio = scanner.nextInt();
            scanner.nextLine(); // Netegem el buffer del scanner
            exit = processarOpcio(opcio);
        }
    }

    /**
     * Processa l'opció seleccionada per l'usuari.
     * 
     * @param opcio Opció seleccionada al menú.
     * @return True si l'usuari decideix sortir del programa, false en cas contrari.
     */
    public boolean processarOpcio(int opcio) {
        String cursId;
        String nomAlumne;
        switch (opcio) {
            case 1:
                List<List<String>> cursos = llistarCursos();
                imprimirTaulaCursos(cursos);
                return false;
            case 2:
                System.out.print("Introdueix l'ID del curs per veure els seus mòduls: ");
                cursId = scanner.nextLine();
                List<List<String>> moduls = mostrarModuls(cursId);
                imprimirTaulaModuls(moduls);
                return false;
            case 3:
                System.out.print("Introdueix l'ID del curs per veure la llista d'alumnes: ");
                cursId = scanner.nextLine();
                List<String> alumnes = llistarAlumnes(cursId);
                imprimirLlistaAlumnes(alumnes);
                return false;
            case 4:
                System.out.print("Introdueix l'ID del curs on vols afegir l'alumne: ");
                cursId = scanner.nextLine();
                System.out.print("Introdueix el nom complet de l'alumne a afegir: ");
                nomAlumne = scanner.nextLine();
                afegirAlumne(cursId, nomAlumne);
                return false;
            case 5:
                System.out.print("Introdueix l'ID del curs on vols eliminar l'alumne: ");
                cursId = scanner.nextLine();
                System.out.print("Introdueix el nom complet de l'alumne a eliminar: ");
                nomAlumne = scanner.nextLine();
                eliminarAlumne(cursId, nomAlumne);
                return false;
            case 6:
                System.out.println("Sortint del programa...");
                return true;
            default:
                System.out.println("Opció no reconeguda. Si us plau, prova de nou.");
                return false;
        }
    }

    /**
     * Mostra el menú principal amb les opcions disponibles.
     */
    private void mostrarMenu() {
        System.out.println("\nMENÚ PRINCIPAL");
        System.out.println("1. Llistar IDs de cursos i tutors");
        System.out.println("2. Mostrar IDs i títols dels mòduls d'un curs");
        System.out.println("3. Llistar alumnes d’un curs");
        System.out.println("4. Afegir un alumne a un curs");
        System.out.println("5. Eliminar un alumne d'un curs");
        System.out.println("6. Sortir");
    }

    /**
     * Llegeix el fitxer XML i llista tots els cursos amb el seu tutor i nombre d'alumnes.
     * 
     * @return Llista amb la informació dels cursos (ID, tutor, nombre d'alumnes).
     */
    public List<List<String>> llistarCursos() {
        List<List<String>> cursos = new ArrayList<>();
        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodesCurs = (NodeList) xpath.evaluate(
                    "/cursos/curs", document, XPathConstants.NODESET);

            for (int i = 0; i < nodesCurs.getLength(); i++) {
                // Expressions relatives: s'avaluen a partir del node de cada curs
                Node curs = nodesCurs.item(i);
                String id = xpath.evaluate("@id", curs);
                String tutor = xpath.evaluate("tutor", curs);
                // count() retorna el nombre de nodes sense haver-los de recórrer
                Double totalAlumnes = (Double) xpath.evaluate(
                        "count(alumnes/alumne)", curs, XPathConstants.NUMBER);
                cursos.add(List.of(id, tutor, String.valueOf(totalAlumnes.intValue())));
            }
        } catch (XPathExpressionException | RuntimeException e) {
            // carregarDocumentXML llança RuntimeException si el fitxer no es pot llegir
            System.out.println("Error en llistar els cursos: " + e.getMessage());
        }
        return cursos;
    }

    /**
     * Imprimeix per consola una taula amb la informació dels cursos.
     * 
     * @param cursos Llista amb la informació dels cursos.
     */
    public void imprimirTaulaCursos(List<List<String>> cursos) {
        List<String> capçaleres = List.of("ID", "Tutor", "Total Alumnes");
        AsciiTablePrinter.imprimirTaula(capçaleres, cursos);
    }

    /**
     * Mostra els mòduls d'un curs especificat pel seu ID.
     * 
     * @param idCurs ID del curs del qual es volen veure els mòduls.
     * @return Llista amb la informació dels mòduls (ID, títol).
     */
    public List<List<String>> mostrarModuls(String idCurs) {
        List<List<String>> moduls = new ArrayList<>();
        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = crearXPath(Map.of("idCurs", idCurs));
            NodeList nodesModul = (NodeList) xpath.evaluate(
                    "/cursos/curs[@id=$idCurs]/moduls/modul", document, XPathConstants.NODESET);

            for (int i = 0; i < nodesModul.getLength(); i++) {
                Node modul = nodesModul.item(i);
                moduls.add(List.of(xpath.evaluate("@id", modul), xpath.evaluate("titol", modul)));
            }
        } catch (XPathExpressionException | RuntimeException e) {
            System.out.println("Error en mostrar els mòduls: " + e.getMessage());
        }
        return moduls;
    }

    /**
     * Imprimeix per consola una taula amb la informació dels mòduls.
     * 
     * @param moduls Llista amb la informació dels mòduls.
     */
    public void imprimirTaulaModuls(List<List<String>> moduls) {
        List<String> capçaleres = List.of("ID Mòdul", "Títol");
        AsciiTablePrinter.imprimirTaula(capçaleres, moduls);
    }

    /**
     * Llista els alumnes inscrits en un curs especificat pel seu ID.
     * 
     * @param idCurs ID del curs del qual es volen veure els alumnes.
     * @return Llista amb els noms dels alumnes.
     */
    public List<String> llistarAlumnes(String idCurs) {
        List<String> alumnes = new ArrayList<>();
        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = crearXPath(Map.of("idCurs", idCurs));
            NodeList nodesAlumne = (NodeList) xpath.evaluate(
                    "/cursos/curs[@id=$idCurs]/alumnes/alumne", document, XPathConstants.NODESET);

            for (int i = 0; i < nodesAlumne.getLength(); i++) {
                alumnes.add(nodesAlumne.item(i).getTextContent());
            }
        } catch (XPathExpressionException | RuntimeException e) {
            System.out.println("Error en llistar els alumnes: " + e.getMessage());
        }
        return alumnes;
    }

    /**
     * Imprimeix per consola la llista d'alumnes d'un curs.
     * 
     * @param alumnes Llista d'alumnes a imprimir.
     */
    public void imprimirLlistaAlumnes(List<String> alumnes) {
        System.out.println("Alumnes:");
        alumnes.forEach(alumne -> System.out.println("- " + alumne));
    }

    /**
     * Afegeix un alumne a un curs especificat pel seu ID.
     * 
     * @param idCurs ID del curs on es vol afegir l'alumne.
     * @param nomAlumne Nom de l'alumne a afegir.
     */
    public void afegirAlumne(String idCurs, String nomAlumne) {
        if (nomAlumne == null || nomAlumne.isBlank()) {
            System.out.println("El nom de l'alumne no pot estar buit.");
            return;
        }
        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = crearXPath(Map.of("idCurs", idCurs));

            Node curs = (Node) xpath.evaluate(
                    "/cursos/curs[@id=$idCurs]", document, XPathConstants.NODE);
            if (curs == null) {
                System.out.println("No s'ha trobat cap curs amb l'ID proporcionat.");
                return;
            }

            Node alumnes = (Node) xpath.evaluate("alumnes", curs, XPathConstants.NODE);
            if (alumnes == null) {
                // El curs encara no té llista d'alumnes: es crea davant dels mòduls
                // (si tampoc no hi ha mòduls, insertBefore amb null l'afegeix al final)
                alumnes = document.createElement("alumnes");
                curs.insertBefore(alumnes, (Node) xpath.evaluate(
                        "moduls", curs, XPathConstants.NODE));
            }

            Element nouAlumne = document.createElement("alumne");
            nouAlumne.setTextContent(nomAlumne);
            alumnes.appendChild(nouAlumne);

            guardarDocumentXML(document);
            System.out.println("Alumne afegit correctament al curs.");
        } catch (XPathExpressionException | RuntimeException e) {
            System.out.println("Error en afegir l'alumne: " + e.getMessage());
        }
    }

    /**
     * Elimina un alumne d'un curs especificat pel seu ID.
     * 
     * @param idCurs ID del curs d'on es vol eliminar l'alumne.
     * @param nomAlumne Nom de l'alumne a eliminar.
     */
    public void eliminarAlumne(String idCurs, String nomAlumne) {
        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = crearXPath(Map.of("idCurs", idCurs, "nomAlumne", nomAlumne));

            Node alumne = (Node) xpath.evaluate(
                    "/cursos/curs[@id=$idCurs]/alumnes/alumne[text()=$nomAlumne]",
                    document, XPathConstants.NODE);
            if (alumne == null) {
                System.out.println("L'alumne no està inscrit en el curs o no existeix.");
                return;
            }

            // Un node només el pot eliminar el seu pare
            alumne.getParentNode().removeChild(alumne);

            guardarDocumentXML(document);
            System.out.println("Alumne eliminat correctament del curs.");
        } catch (XPathExpressionException | RuntimeException e) {
            System.out.println("Error en eliminar l'alumne: " + e.getMessage());
        }
    }

    /**
     * Crea un XPath que resol les variables ($nom) de les expressions amb els valors del mapa.
     *
     * Els valors que escriu l'usuari es passen com a variables i no enganxats dins de
     * l'expressió: així un apòstrof (per exemple "D'ANGELO, Marc") no trenca l'expressió
     * ni permet modificar-la.
     *
     * @param variables Nom i valor de cada variable que fan servir les expressions.
     * @return XPath configurat amb les variables.
     */
    private static XPath crearXPath(Map<String, String> variables) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setXPathVariableResolver(nom -> variables.get(nom.getLocalPart()));
        return xpath;
    }

    /**
     * Carrega el document XML des de la ruta especificada.
     * 
     * @param pathToXml Ruta del fitxer XML a carregar.
     * @return Document XML carregat.
     */
    private Document carregarDocumentXML(Path pathToXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(pathToXml.toFile());
        } catch (Exception e) {
            throw new RuntimeException("Error en carregar el document XML.", e);
        }
    }

    /**
     * Guarda el document XML proporcionat en la ruta del fitxer original.
     * 
     * @param document Document XML a guardar.
     */
    private void guardarDocumentXML(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            // Sense això, l'espai en blanc original es manté i el fitxer s'omple de línies buides
            eliminarEspaisEnBlanc(document);

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlFilePath.toFile());
            transformer.transform(source, result);
            System.out.println("El fitxer XML ha estat guardat amb èxit.");
        } catch (TransformerException e) {
            System.out.println("Error en guardar el fitxer XML.");
            e.printStackTrace();
        }
    }

    /**
     * Elimina, de manera recursiva, els nodes de text que només contenen espais, tabuladors
     * o salts de línia (l'indentació del fitxer original). Si no s'eliminen, en tornar a
     * escriure el document amb INDENT = "yes" el fitxer s'omple de línies en blanc.
     * És el mateix mètode trimWhitespace que s'explica a la teoria.
     *
     * @param node Node a partir del qual es fa la neteja (normalment, el Document).
     */
    private static void eliminarEspaisEnBlanc(Node node) {
        NodeList fills = node.getChildNodes();
        // Es recorre de darrere cap endavant perquè getChildNodes() és una llista «viva»
        for (int i = fills.getLength() - 1; i >= 0; i--) {
            Node fill = fills.item(i);
            if (fill.getNodeType() == Node.TEXT_NODE && fill.getTextContent().isBlank()) {
                node.removeChild(fill);
            } else {
                eliminarEspaisEnBlanc(fill);
            }
        }
    }
}

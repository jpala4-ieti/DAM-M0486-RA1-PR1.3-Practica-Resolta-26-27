package com.project.pr13;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * Classe principal que crea un document XML amb informació de llibres i el guarda en un fitxer.
 * 
 * Aquesta classe permet construir un document XML, afegir elements i guardar-lo en un directori
 * especificat per l'usuari.
 */
public class PR131Main {

    private File dataDir;

    /**
     * Constructor de la classe PR131Main.
     * 
     * @param dataDir Directori on es guardaran els fitxers de sortida.
     */
    public PR131Main(File dataDir) {
        this.dataDir = dataDir;
    }

    /**
     * Retorna el directori de dades actual.
     * 
     * @return Directori de dades.
     */
    public File getDataDir() {
        return dataDir;
    }

    /**
     * Actualitza el directori de dades.
     * 
     * @param dataDir Nou directori de dades.
     */
    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    /**
     * Mètode principal que inicia l'execució del programa.
     * 
     * @param args Arguments passats a la línia de comandament (no s'utilitzen en aquest programa).
     */
    public static void main(String[] args) {
        String userDir = System.getProperty("user.dir");
        File dataDir = new File(userDir, "data" + File.separator + "pr13");

        PR131Main app = new PR131Main(dataDir);
        app.processarFitxerXML("biblioteca.xml");
    }

    /**
     * Processa el document XML creant-lo, guardant-lo en un fitxer i comprovant el directori de sortida.
     * 
     * @param filename Nom del fitxer XML a guardar.
     */
    public void processarFitxerXML(String filename) {
        if (comprovarIDirCrearDirectori(dataDir)) {
            Document doc = construirDocument();
            File fitxerSortida = new File(dataDir, filename);
            guardarDocument(doc, fitxerSortida);
        }
    }

    /**
     * Comprova si el directori existeix i, si no és així, el crea.
     * 
     * @param directori Directori a comprovar o crear.
     * @return True si el directori ja existeix o s'ha creat amb èxit, false en cas contrari.
     */
    private boolean comprovarIDirCrearDirectori(File directori) {
        if (!directori.exists()) {
            return directori.mkdirs();
        }
        return true;
    }

    /**
     * Crea un document XML amb l'estructura d'una biblioteca i afegeix un llibre amb els seus detalls.
     * Cal construir-lo amb DocumentBuilder. No és privat perquè els tests el puguin comprovar.
     * 
     * @return Document XML creat o null en cas d'error.
     */
    static Document construirDocument() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // Element arrel
            Element biblioteca = doc.createElement("biblioteca");
            doc.appendChild(biblioteca);

            // Llibre, amb l'identificador com a atribut
            Element llibre = doc.createElement("llibre");
            llibre.setAttribute("id", "001");
            biblioteca.appendChild(llibre);

            // Cada dada del llibre és un element fill amb un node de text
            afegirElementAmbText(doc, llibre, "titol", "El viatge dels venturons");
            afegirElementAmbText(doc, llibre, "autor", "Joan Pla");
            afegirElementAmbText(doc, llibre, "anyPublicacio", "1998");
            afegirElementAmbText(doc, llibre, "editorial", "Edicions Mar");
            afegirElementAmbText(doc, llibre, "genere", "Aventura");
            afegirElementAmbText(doc, llibre, "pagines", "320");
            afegirElementAmbText(doc, llibre, "disponible", "true");

            return doc;
        } catch (ParserConfigurationException e) {
            System.out.println("No s'ha pogut crear el document XML: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea un element amb un node de text i l'afegeix com a fill de l'element pare.
     *
     * @param doc Document al qual pertanyen els elements.
     * @param pare Element que rebrà el fill nou.
     * @param nom Nom de l'element nou.
     * @param text Contingut de text de l'element nou.
     */
    private static void afegirElementAmbText(Document doc, Element pare, String nom, String text) {
        Element element = doc.createElement(nom);
        element.appendChild(doc.createTextNode(text));
        pare.appendChild(element);
    }

    /**
     * Guarda el document XML proporcionat en el fitxer especificat.
     * 
     * @param doc Document XML a guardar.
     * @param fitxerSortida Fitxer de sortida on es guardarà el document.
     */
    private static void guardarDocument(Document doc, File fitxerSortida) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            // El fitxer de l'annex no porta la declaració <?xml ...?> a la primera línia
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.transform(new DOMSource(doc), new StreamResult(fitxerSortida));
            System.out.println("El fitxer XML s'ha guardat a: " + fitxerSortida.getPath());
        } catch (TransformerException e) {
            System.out.println("No s'ha pogut guardar el fitxer XML: " + e.getMessage());
        }
    }
}

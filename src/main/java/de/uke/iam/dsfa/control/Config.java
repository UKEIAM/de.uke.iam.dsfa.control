package de.uke.iam.dsfa.control;

import de.samply.config.util.JAXBUtil;
import de.uke.iam.dsfa.control.config.Dsfa;
import de.uke.iam.dsfa.control.config.ObjectFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;

public enum Config {
    /**
     * The singleton instance
     */
    instance;

    /**
     * Default name of the file from which configuration is read
     */
    private static final String DEFAULT_CONFIG = "dsfa.control.config.xml";

    /**
     * The default project name or prefix for the filefinderutil.
     */
    private static final String DEFAULT_PROJECT_NAME = "dsfa";

    /**
     * The default fallbackfolder for configuration files.
     */
    private static final String DEFAULT_FALLBACK = "/WEB-INF/classes/";

    /**
     * Configuration model
     */
    private final Dsfa config;

    //TODO: make getting config consistent across project
    Config() {
        try {
            JAXBContext configContext = JAXBContext.newInstance(ObjectFactory.class);
            config = JAXBUtil.findUnmarshall(DEFAULT_CONFIG, configContext, Dsfa.class, DEFAULT_PROJECT_NAME, DEFAULT_FALLBACK);

        } catch (FileNotFoundException | JAXBException | SAXException | ParserConfigurationException e) {
            //TODO use a self created Exception class
            throw new RuntimeException("Cannot read config", e);
        }
    }

    public Dsfa getConfig() {
        return config;
    }

    /**
     * Returns the implementation version as defined per maven
     * @return  maven version number
     */
    public String getVersionNumber() {
        return getClass().getPackage().getImplementationVersion();
    }
}

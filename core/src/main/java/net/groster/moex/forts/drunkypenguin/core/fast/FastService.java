package net.groster.moex.forts.drunkypenguin.core.fast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.groster.moex.forts.drunkypenguin.core.config.Configuration;
import net.groster.moex.forts.drunkypenguin.core.config.Updater;
import org.openfast.template.MessageTemplate;
import org.openfast.template.loader.XMLMessageTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class FastService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastService.class);
    private static final int WAIT_FOR_XML_FILE_PERIOD_MS = 1_000;
    private volatile boolean initThreadContinueWorking = true;
    private MessageTemplate[] templates;
    private Configuration configuration;
    private final Thread initThread = new Thread() {
        @Override
        public void run() {
            LOGGER.info("Loading FAST templates.");
            final File templatesXmlFile = new File(Updater.TEMPLATES_XML_FILE_NAME);
            final XMLMessageTemplateLoader xmlMessageTemplateLoader = new XMLMessageTemplateLoader();
            while (isInitThreadContinueWorking()) {
                LOGGER.info("Checking, if '" + Updater.TEMPLATES_XML_FILE_NAME + "' file exists.");
                if (templatesXmlFile.isFile()) {
                    LOGGER.info("Found '" + Updater.TEMPLATES_XML_FILE_NAME + "'. Parsing.");
                    try (FileInputStream templatesXmlFileIS = new FileInputStream(templatesXmlFile)) {
                        templates = xmlMessageTemplateLoader.load(templatesXmlFileIS);
                        if (templates.length != MessageType.values().length) {
                            LOGGER.error("Wrong templates number. {'templates.length'='" + templates.length
                                    + "'} != {'MessageType.values().length'='" + MessageType.values().length + "'}.");
                        }
                        for (final MessageTemplate template : templates) {
                            MessageType.getByName(template.getName()).setId(Integer.parseInt(template.getId()));
                        }
                        LOGGER.info("Loaded FAST templates successfully.");
                        break;
                    } catch (FileNotFoundException fNFE) {
                    } catch (IOException ioE) {
                        break;
                    }
                }

                LOGGER.warn("There is no '" + Updater.TEMPLATES_XML_FILE_NAME + "'. Will wait '"
                        + WAIT_FOR_XML_FILE_PERIOD_MS + "'ms. for it.");
                try {
                    Thread.sleep(WAIT_FOR_XML_FILE_PERIOD_MS);
                } catch (InterruptedException iE) {
                }
            }
            try {
                LOGGER.info("Loading FAST configuration.");
                final File configurationXmlFile = new File(Updater.CONFIGURATION_XML_FILE_NAME);
                final Unmarshaller unmarshaller = JAXBContext.newInstance(Configuration.class).createUnmarshaller();

                while (isInitThreadContinueWorking()) {
                    LOGGER.info("Checking, if '" + Updater.CONFIGURATION_XML_FILE_NAME + "' file exists.");
                    if (configurationXmlFile.isFile()) {
                        LOGGER.info("Found '" + Updater.CONFIGURATION_XML_FILE_NAME + "'. Parsing.");
                        configuration = (Configuration) unmarshaller.unmarshal(configurationXmlFile);
                        LOGGER.info("Loaded FAST configuration successfully.");
                        break;
                    }

                    LOGGER.warn("There is no '" + Updater.CONFIGURATION_XML_FILE_NAME + "'. Will wait '"
                            + WAIT_FOR_XML_FILE_PERIOD_MS + "'ms. for it.");
                    try {
                        Thread.sleep(WAIT_FOR_XML_FILE_PERIOD_MS);
                    } catch (InterruptedException iE) {
                    }
                }
                JAXBContext.newInstance(Configuration.class).createMarshaller().marshal(configuration, new File(
                        "conf.xml"));
            } catch (JAXBException jaxbE) {
                throw new RuntimeException("It's fatal, dunno what to do with this", jaxbE);
            }
        }
    };

    @PostConstruct
    public void postConstruct() {
        initThread.start();
    }

    @PreDestroy
    public void preDestroy() {
        initThreadContinueWorking = false;
        if (initThread != null) {
            initThread.interrupt();
        }
    }

    public boolean isInitThreadContinueWorking() {
        return initThreadContinueWorking;
    }
}
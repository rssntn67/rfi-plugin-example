package it.xeniaprogetti.rfi.plugin.example.events;

import java.util.List;

import org.opennms.integration.api.v1.config.events.EventDefinition;
import org.opennms.integration.api.xml.ClasspathEventDefinitionLoader;

public class EventConfExtension implements org.opennms.integration.api.v1.config.events.EventConfExtension {

    private final ClasspathEventDefinitionLoader classpathEventDefinitionLoader = new ClasspathEventDefinitionLoader(
            EventConfExtension.class,
            "plugin.ext.events.xml",
            "INC-MIB-AL.events.xml",
            "INC-MIB-AL.translator.events.xml"
    );

    @Override
    public List<EventDefinition> getEventDefinitions() {
        return classpathEventDefinitionLoader.getEventDefinitions();
    }
}

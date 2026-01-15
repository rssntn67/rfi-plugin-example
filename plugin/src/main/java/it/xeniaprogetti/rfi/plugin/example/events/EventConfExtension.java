package it.xeniaprogetti.rfi.plugin.example.events;

import java.util.List;

import org.opennms.integration.api.v1.config.events.EventDefinition;
import org.opennms.integration.api.xml.ClasspathEventDefinitionLoader;

public class EventConfExtension implements org.opennms.integration.api.v1.config.events.EventConfExtension {

    private final ClasspathEventDefinitionLoader classpathEventDefinitionLoader = new ClasspathEventDefinitionLoader(
            EventConfExtension.class,
            "plugin.ext.events.xml",
            "INC-MIB-AL.events.xml",
            "INC-MIB-AL.translator.events.xml",
            "MIKOM_OMC_Alarmforwarding-MIB.events.xml",
            "MIKOM_OMC_Alarmforwarding-MIB.translator.events.xml",
            "AEM3-MIB.events.xml",
            "AEM3-MIB.translator.events.xml",
            "PBH-MIB-ALM.events.xml",
            "PBH-MIB-ALM.translator.events.xml",
            "SCAIR-MIB-ALM.events.xml",
            "SCAIR-MIB-ALM.translator.events.xml",
            "SCR-MIB-ALM.events.xml",
            "SCR-MIB-ALM.translator.events.xml",
            "SMARTS-94-MIB.events.xml",
            "SMARTS-94-MIB.translator.events.xml",
            "TEKOTELECOM-OMC-MIB.events.xml",
            "TEKOTELECOM-OMC-MIB.translator.events.xml",
            "NSN-SNMP-NBI-TOPOLOGY-MIB.events.xml"
    );

    @Override
    public List<EventDefinition> getEventDefinitions() {
        return classpathEventDefinitionLoader.getEventDefinitions();
    }
}

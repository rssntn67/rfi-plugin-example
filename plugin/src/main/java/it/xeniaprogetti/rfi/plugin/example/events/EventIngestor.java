package it.xeniaprogetti.rfi.plugin.example.events;

import it.xeniaprogetti.rfi.plugin.example.events.acom.AComEventIngestorComponent;
import it.xeniaprogetti.rfi.plugin.example.events.andrew.AndrewEventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.axell.AxellEventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.pbh.PbhEventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.scair.ScairEventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.scr.ScrEventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.smarts94.Smarts94EventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.teko.TekoEventIngestor;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@Component(name = "allEventIngestor",
//        immediate = true)
public class EventIngestor implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventIngestor.class);
    public static final Set<String> ALL_EXCLUDED_UEIS =
            Stream.of(
                            AndrewEventIngestor.INTERESTING_ANDREW_UEIS,
                            AComEventIngestorComponent.INTERESTING_ACOM_UEIS,
                            AxellEventIngestor.INTERESTING_AXELL_UEIS,
                            PbhEventIngestor.INTERESTING_PBH_UEIS,
                            ScairEventIngestor.INTERESTING_SCAIR_UEIS,
                            ScrEventIngestor.INTERESTING_SCR_UEIS,
                            Smarts94EventIngestor.INTERESTING_SMARTS94_UEIS,
                            TekoEventIngestor.INTERESTING_TEKO_UEIS
                    )
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

    //private EventForwarder eventForwarder;
    //private NodeDao nodeDao;
    private EventSubscriptionService eventSubscriptionService;

    public EventIngestor() {
    }

    /*@Reference
    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }
    @Reference
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }*/
    @Reference
    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

    @Activate
    public void activate(){
        eventSubscriptionService.addEventListener(this);
        log.info("AllEventIngestor registered on UEIS");
    }

    @Deactivate
    public void deactivate(){
        try {
            eventSubscriptionService.removeEventListener(this);
            log.info("AllEventIngestor deregistered");
        } catch (Exception e) {
            log.warn("Error while deregistering listener for AllEvent", e);
        }
    }

    @Override
    public String getName() {
        return "allEventIngestor";
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(InMemoryEvent e) {

        String uei = e.getUei();

        if(ALL_EXCLUDED_UEIS.contains(uei)){
            return;
        }

        log.info("Arrived new event not filtered: {}", e);
    }
}

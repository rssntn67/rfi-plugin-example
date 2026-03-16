package it.xeniaprogetti.rfi.plugin.example.events.smarts94;

import it.xeniaprogetti.rfi.plugin.example.events.pbh.PbhEventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.scair.ScairEventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.scair.ScairEventIngestorTest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventForwarder;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.immutables.ImmutableEventParameter;
import org.opennms.integration.api.v1.model.immutables.ImmutableInMemoryEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

public class Smarts94EventIngestorTest {

    private final static String EVENT_DATE = "1763109163";

    @Test
    public void testTranslate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        Smarts94EventIngestor smarts94EventIngestor = new Smarts94EventIngestor();
        smarts94EventIngestor.setEventSubscriptionService(eventSubscriptionService);
        smarts94EventIngestor.setNodeDao(nodeDao);
        smarts94EventIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/SMARTS-94-MIB/smTrapNotificationInformational", date);
        ImmutableInMemoryEvent eventTranslated = smarts94EventIngestor.translate(event);

        assertEquals("uei.opennms.org/translator/SMARTS-94-MIB/smTrapNotificationInformational", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(Smarts94EventIngestor.TIME_SMARTS94_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventClear(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        Smarts94EventIngestor smarts94EventIngestor = new Smarts94EventIngestor();
        smarts94EventIngestor.setEventSubscriptionService(eventSubscriptionService);
        smarts94EventIngestor.setNodeDao(nodeDao);
        smarts94EventIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/SMARTS-94-MIB/smTrapNotificationClear", date);
        smarts94EventIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/SMARTS-94-MIB/smTrapNotificationClear", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(Smarts94EventIngestor.TIME_SMARTS94_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventAlarmInformational(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        Smarts94EventIngestor smarts94EventIngestor = new Smarts94EventIngestor();
        smarts94EventIngestor.setEventSubscriptionService(eventSubscriptionService);
        smarts94EventIngestor.setNodeDao(nodeDao);
        smarts94EventIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/SMARTS-94-MIB/smTrapNotificationInformational", date);
        smarts94EventIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/SMARTS-94-MIB/smTrapNotificationInformational", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(Smarts94EventIngestor.TIME_SMARTS94_PARAMETER).get(0).getValue());
    }

    @Test
    public void testActivate() {

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        Smarts94EventIngestor smarts94EventIngestor = new Smarts94EventIngestor();
        smarts94EventIngestor.setEventSubscriptionService(eventSubscriptionService);
        smarts94EventIngestor.setNodeDao(nodeDao);
        smarts94EventIngestor.setEventForwarder(eventForwarder);

        smarts94EventIngestor.activate();

        verify(eventSubscriptionService).addEventListener(
                eq(smarts94EventIngestor),
                eq(Smarts94EventIngestor.INTERESTING_SMARTS94_UEIS)
        );
        verifyNoMoreInteractions(eventSubscriptionService);
    }

    @Test
    public void testEventNotHandle(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao = mock(NodeDao.class);
        Smarts94EventIngestorTest.FakeEventSubscriptionService fakeEventSubscriptionService = new Smarts94EventIngestorTest.FakeEventSubscriptionService();

        // uso uno SPY del listener così posso verificare se onEvent è stato chiamato o no
        Smarts94EventIngestor smarts94Ingestor = new Smarts94EventIngestor();
        smarts94Ingestor.setNodeDao(nodeDao);
        smarts94Ingestor.setEventForwarder(eventForwarder);
        smarts94Ingestor.setEventSubscriptionService(fakeEventSubscriptionService);

        Smarts94EventIngestor spyListener = spy(smarts94Ingestor);

        spyListener.activate();

        // evento con UEI NON tra quelli INTERESSING_ACOM_UEIS
        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/translator/SMARTS-MIB-ALARM/mOverallAlarmClearTrap", date);

        // Simulo il dispatcher che prova a "distribuire" l'evento
        fakeEventSubscriptionService.fire(event);

        // Verifico che onEvent NON sia mai stato chiamato
        verify(spyListener, never()).onEvent(any(InMemoryEvent.class));
        verify(eventForwarder, never()).sendAsync(any());
    }



    private static Date getEventDateFromString(){
        long unixSeconds = Long.parseLong(EVENT_DATE);
        Instant trapInstant = Instant.ofEpochSecond(unixSeconds);
        return Date.from(trapInstant);
    }

    private static InMemoryEvent getEvent(String uei, Date date){

        return ImmutableInMemoryEvent.newBuilder()
                .setUei(uei)
                .setTime(date)
                .setNodeId(1)
                .addParameter(ImmutableEventParameter.newBuilder()
                        .setName(Smarts94EventIngestor.TIME_SMARTS94_PARAMETER)
                        .setValue(EVENT_DATE)
                        .build())
                .setSource("SCAIR_Test")
                .build();
    }

    private static class FakeEventSubscriptionService implements EventSubscriptionService {

        private final Map<EventListener, List<String>> listeners = new HashMap<>();

        // metodo per il test
        public void fire(InMemoryEvent event) {
            for (Map.Entry<EventListener, List<String>> entry : listeners.entrySet()) {
                if (entry.getValue().contains(event.getUei())) {
                    entry.getKey().onEvent(event);
                }
            }
        }

        @Override
        public void addEventListener(EventListener listener) {

        }

        @Override
        public void addEventListener(EventListener listener, Collection<String> ueis) {
            listeners.put(listener, ueis.stream().toList());
        }

        @Override
        public void addEventListener(EventListener listener, String uei) {

        }

        @Override
        public void removeEventListener(EventListener listener) {

        }

        @Override
        public void removeEventListener(EventListener listener, Collection<String> ueis) {
            listeners.remove(listener, ueis.stream().toList());
        }

        @Override
        public void removeEventListener(EventListener listener, String uei) {

        }

        @Override
        public boolean hasEventListener(String uei) {
            return false;
        }
    }
}

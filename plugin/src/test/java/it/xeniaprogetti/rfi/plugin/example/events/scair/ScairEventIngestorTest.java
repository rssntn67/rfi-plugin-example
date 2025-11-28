package it.xeniaprogetti.rfi.plugin.example.events.scair;

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
import static org.mockito.Mockito.*;

public class ScairEventIngestorTest {
    private final static String EVENT_DATE = "Fri May 11 15:15:08 CEST 2018";

    @Test
    public void testTranslate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        ScairEventIngestor scairEventIngestor = new ScairEventIngestor();
        scairEventIngestor.setEventSubscriptionService(eventSubscriptionService);
        scairEventIngestor.setNodeDao(nodeDao);
        scairEventIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/SCAIR-MIB-ALARM/scairEventTrapAlarmIndeterminate", date);
        ImmutableInMemoryEvent eventTranslated = scairEventIngestor.translate(event);

        assertEquals("uei.opennms.org/translator/SCAIR-MIB-ALARM/scairEventTrapAlarmIndeterminate", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(ScairEventIngestor.TIME_SCAIR_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventClear(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        ScairEventIngestor scairIngestor = new ScairEventIngestor();
        scairIngestor.setEventSubscriptionService(eventSubscriptionService);
        scairIngestor.setNodeDao(nodeDao);
        scairIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/SCAIR-MIB-ALARM/scairEventTrapClear", date);
        scairIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/SCAIR-MIB-ALARM/scairEventTrapClear", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(ScairEventIngestor.TIME_SCAIR_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventAlarmIndeterminate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        ScairEventIngestor scairEventIngestor = new ScairEventIngestor();
        scairEventIngestor.setEventSubscriptionService(eventSubscriptionService);
        scairEventIngestor.setNodeDao(nodeDao);
        scairEventIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/SCAIR-MIB-ALARM/scairEventTrapAlarmIndeterminate", date);
        scairEventIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/SCAIR-MIB-ALARM/scairEventTrapAlarmIndeterminate", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(ScairEventIngestor.TIME_SCAIR_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventNotificationIndeterminate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        ScairEventIngestor scairEventIngestor = new ScairEventIngestor();
        scairEventIngestor.setEventSubscriptionService(eventSubscriptionService);
        scairEventIngestor.setNodeDao(nodeDao);
        scairEventIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/SCAIR-MIB-ALARM/scairEventTrapNotificationIndeterminate", date);
        scairEventIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/SCAIR-MIB-ALARM/scairEventTrapNotificationIndeterminate", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(ScairEventIngestor.TIME_SCAIR_PARAMETER).get(0).getValue());
    }

    @Test
    public void testActivate() {

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        ScairEventIngestor scairIngestor = new ScairEventIngestor();
        scairIngestor.setEventSubscriptionService(eventSubscriptionService);
        scairIngestor.setNodeDao(nodeDao);
        scairIngestor.setEventForwarder(eventForwarder);

        scairIngestor.activate();

        verify(eventSubscriptionService).addEventListener(
                eq(scairIngestor),
                eq(ScairEventIngestor.INTERESTING_SCAIR_UEIS)
        );
        verifyNoMoreInteractions(eventSubscriptionService);
    }

    @Test
    public void testEventNotHandle(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao = mock(NodeDao.class);
        ScairEventIngestorTest.FakeEventSubscriptionService fakeEventSubscriptionService = new ScairEventIngestorTest.FakeEventSubscriptionService();

        // uso uno SPY del listener così posso verificare se onEvent è stato chiamato o no
        ScairEventIngestor scairIngestor = new ScairEventIngestor();
        scairIngestor.setNodeDao(nodeDao);
        scairIngestor.setEventForwarder(eventForwarder);
        scairIngestor.setEventSubscriptionService(fakeEventSubscriptionService);

        ScairEventIngestor spyListener = spy(scairIngestor);

        spyListener.activate();

        // evento con UEI NON tra quelli INTERESSING_ACOM_UEIS
        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/translator/SCAIR-MIB-ALARM/mOverallAlarmClearTrap", date);

        // Simulo il dispatcher che prova a "distribuire" l'evento
        fakeEventSubscriptionService.fire(event);

        // Verifico che onEvent NON sia mai stato chiamato
        verify(spyListener, never()).onEvent(any(InMemoryEvent.class));
        verify(eventForwarder, never()).sendAsync(any());
    }

    private static Date getEventDateFromString(){
        LocalDateTime ldt = LocalDateTime.parse(EVENT_DATE, ScairEventIngestor.TRAP_TIME_FORMATTER);
        Instant trapInstant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(trapInstant);
    }

    private static InMemoryEvent getEvent(String uei, Date date){

        return ImmutableInMemoryEvent.newBuilder()
                .setUei(uei)
                .setTime(date)
                .setNodeId(1)
                .addParameter(ImmutableEventParameter.newBuilder()
                        .setName(ScairEventIngestor.TIME_SCAIR_PARAMETER)
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

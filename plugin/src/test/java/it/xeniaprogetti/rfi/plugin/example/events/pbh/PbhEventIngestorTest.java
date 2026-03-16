package it.xeniaprogetti.rfi.plugin.example.events.pbh;

import it.xeniaprogetti.rfi.plugin.example.events.andrew.AndrewEventIngestor;
import it.xeniaprogetti.rfi.plugin.example.events.andrew.AndrewEventIngestorTest;
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
import static org.mockito.Mockito.never;

public class PbhEventIngestorTest {
    private final static String EVENT_DATE = "Fri May 11 15:15:08 CEST 2018";

    @Test
    public void testTranslate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        PbhEventIngestor pbhEventIngestor = new PbhEventIngestor();
        pbhEventIngestor.setEventSubscriptionService(eventSubscriptionService);
        pbhEventIngestor.setNodeDao(nodeDao);
        pbhEventIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/PBH-MIB-ALARM/pbhEventTrapAlarmIndeterminate", date);
        ImmutableInMemoryEvent eventTranslated = pbhEventIngestor.translate(event);

        assertEquals("uei.opennms.org/translator/PBH-MIB-ALARM/pbhEventTrapAlarmIndeterminate", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(PbhEventIngestor.TIME_PBH_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventClear(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        PbhEventIngestor pbhIngestor = new PbhEventIngestor();
        pbhIngestor.setEventSubscriptionService(eventSubscriptionService);
        pbhIngestor.setNodeDao(nodeDao);
        pbhIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/PBH-MIB-ALARM/pbhEventTrapClear", date);
        pbhIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/PBH-MIB-ALARM/pbhEventTrapClear", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(PbhEventIngestor.TIME_PBH_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventAlarmIndeterminate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        PbhEventIngestor pbhIngestor = new PbhEventIngestor();
        pbhIngestor.setEventSubscriptionService(eventSubscriptionService);
        pbhIngestor.setNodeDao(nodeDao);
        pbhIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/PBH-MIB-ALARM/pbhEventTrapAlarmIndeterminate", date);
        pbhIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/PBH-MIB-ALARM/pbhEventTrapAlarmIndeterminate", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(PbhEventIngestor.TIME_PBH_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventNotificationIndeterminate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        PbhEventIngestor pbhIngestor = new PbhEventIngestor();
        pbhIngestor.setEventSubscriptionService(eventSubscriptionService);
        pbhIngestor.setNodeDao(nodeDao);
        pbhIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/PBH-MIB-ALARM/pbhEventTrapNotificationIndeterminate", date);
        pbhIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/PBH-MIB-ALARM/pbhEventTrapNotificationIndeterminate", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(PbhEventIngestor.TIME_PBH_PARAMETER).get(0).getValue());
    }

    @Test
    public void testActivate() {

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        PbhEventIngestor pbhIngestor = new PbhEventIngestor();
        pbhIngestor.setEventSubscriptionService(eventSubscriptionService);
        pbhIngestor.setNodeDao(nodeDao);
        pbhIngestor.setEventForwarder(eventForwarder);

        pbhIngestor.activate();

        verify(eventSubscriptionService).addEventListener(
                eq(pbhIngestor),
                eq(PbhEventIngestor.INTERESTING_PBH_UEIS)
        );
        verifyNoMoreInteractions(eventSubscriptionService);
    }

    @Test
    public void testEventNotHandle(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao = mock(NodeDao.class);
        PbhEventIngestorTest.FakeEventSubscriptionService fakeEventSubscriptionService = new PbhEventIngestorTest.FakeEventSubscriptionService();

        // uso uno SPY del listener così posso verificare se onEvent è stato chiamato o no
        PbhEventIngestor pbhEventIngestor = new PbhEventIngestor();
        pbhEventIngestor.setEventForwarder(eventForwarder);
        pbhEventIngestor.setNodeDao(nodeDao);
        pbhEventIngestor.setEventSubscriptionService(fakeEventSubscriptionService);

        PbhEventIngestor spyListener = spy(pbhEventIngestor);

        spyListener.activate();

        // evento con UEI NON tra quelli INTERESSING_ACOM_UEIS
        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/translator/PBH-MIB-ALARM/mOverallAlarmClearTrap", date);

        // Simulo il dispatcher che prova a "distribuire" l'evento
        fakeEventSubscriptionService.fire(event);

        // Verifico che onEvent NON sia mai stato chiamato
        verify(spyListener, never()).onEvent(any(InMemoryEvent.class));
        verify(eventForwarder, never()).sendAsync(any());
    }

    private static Date getEventDateFromString(){
        LocalDateTime ldt = LocalDateTime.parse(EVENT_DATE, PbhEventIngestor.TRAP_TIME_FORMATTER);
        Instant trapInstant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(trapInstant);
    }

    private static InMemoryEvent getEvent(String uei, Date date){

        return ImmutableInMemoryEvent.newBuilder()
                .setUei(uei)
                .setTime(date)
                .setNodeId(1)
                .addParameter(ImmutableEventParameter.newBuilder()
                        .setName(PbhEventIngestor.TIME_PBH_PARAMETER)
                        .setValue(EVENT_DATE)
                        .build())
                .setSource("PbhTest")
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

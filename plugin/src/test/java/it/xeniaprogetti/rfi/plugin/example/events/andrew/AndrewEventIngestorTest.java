package it.xeniaprogetti.rfi.plugin.example.events.andrew;

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

public class AndrewEventIngestorTest {

    private final static String EVENT_DATE = "2025-11-13,11:20:00";

    @Test
    public void testTranslate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AndrewEventIngestor andrewIngestor = new AndrewEventIngestor();
        andrewIngestor.setEventSubscriptionService(eventSubscriptionService);
        andrewIngestor.setNodeDao(nodeDao);
        andrewIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapCritical", date);
        ImmutableInMemoryEvent eventTranslated = andrewIngestor.translate(event);

        assertEquals("uei.opennms.org/translator/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapCritical", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AndrewEventIngestor.TIME_ANDREW_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventIndeterminate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AndrewEventIngestor andrewIngestor = new AndrewEventIngestor();
        andrewIngestor.setEventSubscriptionService(eventSubscriptionService);
        andrewIngestor.setNodeDao(nodeDao);
        andrewIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapIndeterminate", date);
        andrewIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapIndeterminate", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AndrewEventIngestor.TIME_ANDREW_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventCritical(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AndrewEventIngestor andrewIngestor = new AndrewEventIngestor();
        andrewIngestor.setEventSubscriptionService(eventSubscriptionService);
        andrewIngestor.setNodeDao(nodeDao);
        andrewIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapCritical", date);
        andrewIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapCritical", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AndrewEventIngestor.TIME_ANDREW_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventMinor(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AndrewEventIngestor andrewIngestor = new AndrewEventIngestor();
        andrewIngestor.setEventSubscriptionService(eventSubscriptionService);
        andrewIngestor.setNodeDao(nodeDao);
        andrewIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapMinor", date);
        andrewIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapMinor", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AndrewEventIngestor.TIME_ANDREW_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventMajor(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AndrewEventIngestor andrewIngestor = new AndrewEventIngestor();
        andrewIngestor.setEventSubscriptionService(eventSubscriptionService);
        andrewIngestor.setNodeDao(nodeDao);
        andrewIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapMajor", date);
        andrewIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapMajor", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AndrewEventIngestor.TIME_ANDREW_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventWarning(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AndrewEventIngestor andrewIngestor = new AndrewEventIngestor();
        andrewIngestor.setEventSubscriptionService(eventSubscriptionService);
        andrewIngestor.setNodeDao(nodeDao);
        andrewIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapWarning", date);
        andrewIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/MIKOM_OMC_Alarmforwarding-MIB/mAlarmTrapWarning", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AndrewEventIngestor.TIME_ANDREW_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventClear(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AndrewEventIngestor andrewIngestor = new AndrewEventIngestor();
        andrewIngestor.setEventSubscriptionService(eventSubscriptionService);
        andrewIngestor.setNodeDao(nodeDao);
        andrewIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB/mAlarmClearTrap", date);
        andrewIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/MIKOM_OMC_Alarmforwarding-MIB/mAlarmClearTrap", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AndrewEventIngestor.TIME_ANDREW_PARAMETER).get(0).getValue());
    }

    @Test
    public void testActivate() {

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AndrewEventIngestor andrewIngestor = new AndrewEventIngestor();
        andrewIngestor.setEventSubscriptionService(eventSubscriptionService);
        andrewIngestor.setNodeDao(nodeDao);
        andrewIngestor.setEventForwarder(eventForwarder);

        andrewIngestor.activate();

        verify(eventSubscriptionService).addEventListener(
                eq(andrewIngestor),
                eq(AndrewEventIngestor.INTERESTING_ANDREW_UEIS)
        );
        verifyNoMoreInteractions(eventSubscriptionService);
    }

    @Test
    public void testEventNotHandle(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao = mock(NodeDao.class);
        AndrewEventIngestorTest.FakeEventSubscriptionService fakeEventSubscriptionService = new AndrewEventIngestorTest.FakeEventSubscriptionService();

        // uso uno SPY del listener così posso verificare se onEvent è stato chiamato o no
        AndrewEventIngestor andrewEventIngestor = new AndrewEventIngestor();
        andrewEventIngestor.setEventForwarder(eventForwarder);
        andrewEventIngestor.setNodeDao(nodeDao);
        andrewEventIngestor.setEventSubscriptionService(fakeEventSubscriptionService);

        AndrewEventIngestor spyListener = spy(andrewEventIngestor);

        spyListener.activate();

        // evento con UEI NON tra quelli INTERESSING_ACOM_UEIS
        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/MIKOM_OMC_Alarmforwarding-MIB/mOverallAlarmClearTrap", date);

        // Simulo il dispatcher che prova a "distribuire" l'evento
        fakeEventSubscriptionService.fire(event);

        // Verifico che onEvent NON sia mai stato chiamato
        verify(spyListener, never()).onEvent(any(InMemoryEvent.class));
        verify(eventForwarder, never()).sendAsync(any());
    }


    private static Date getEventDateFromString(){
        LocalDateTime ldt = LocalDateTime.parse(EVENT_DATE, AndrewEventIngestor.TRAP_TIME_FORMATTER);
        Instant trapInstant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(trapInstant);
    }

    private static InMemoryEvent getEvent(String uei, Date date){

        return ImmutableInMemoryEvent.newBuilder()
                .setUei(uei)
                .setTime(date)
                .setNodeId(1)
                .addParameter(ImmutableEventParameter.newBuilder()
                        .setName(AndrewEventIngestor.TIME_ANDREW_PARAMETER)
                        .setValue(EVENT_DATE)
                        .build())
                .setSource("AcmTest")
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

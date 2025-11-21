package it.xeniaprogetti.rfi.plugin.example.events.axell;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AxellEventIngestorTest {

    private final static String EVENT_DATE = "2025/11/13 11:20:00";

    @Test
    public void testTranslate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AxellEventIngestor axellIngestor = new AxellEventIngestor();
        axellIngestor.setEventSubscriptionService(eventSubscriptionService);
        axellIngestor.setNodeDao(nodeDao);
        axellIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/AEM3-MIB/aemAlarmNotificationCritical", date);
        ImmutableInMemoryEvent eventTranslated = axellIngestor.translate(event);

        assertEquals("uei.opennms.org/translator/AEM3-MIB/aemAlarmNotificationCritical", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AxellEventIngestor.TIME_AXELL_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventIndeterminate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AxellEventIngestor axellIngestor = new AxellEventIngestor();
        axellIngestor.setEventSubscriptionService(eventSubscriptionService);
        axellIngestor.setNodeDao(nodeDao);
        axellIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/AEM3-MIB/aemAlarmNotificationIndeterminate", date);
        axellIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());
        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/AEM3-MIB/aemAlarmNotificationIndeterminate", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AxellEventIngestor.TIME_AXELL_PARAMETER).get(0).getValue());

    }

    @Test
    public void testOnEventCritical(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AxellEventIngestor axellIngestor = new AxellEventIngestor();
        axellIngestor.setEventSubscriptionService(eventSubscriptionService);
        axellIngestor.setNodeDao(nodeDao);
        axellIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/AEM3-MIB/aemAlarmNotificationCritical", date);
        axellIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());
        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/AEM3-MIB/aemAlarmNotificationCritical", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AxellEventIngestor.TIME_AXELL_PARAMETER).get(0).getValue());

    }

    @Test
    public void testOnEventMajor(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AxellEventIngestor axellIngestor = new AxellEventIngestor();
        axellIngestor.setEventSubscriptionService(eventSubscriptionService);
        axellIngestor.setNodeDao(nodeDao);
        axellIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/AEM3-MIB/aemAlarmNotificationMajor", date);
        axellIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());
        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/AEM3-MIB/aemAlarmNotificationMajor", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AxellEventIngestor.TIME_AXELL_PARAMETER).get(0).getValue());

    }

    @Test
    public void testOnEventMinor(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AxellEventIngestor axellIngestor = new AxellEventIngestor();
        axellIngestor.setEventSubscriptionService(eventSubscriptionService);
        axellIngestor.setNodeDao(nodeDao);
        axellIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/AEM3-MIB/aemAlarmNotificationMinor", date);
        axellIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());
        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/AEM3-MIB/aemAlarmNotificationMinor", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AxellEventIngestor.TIME_AXELL_PARAMETER).get(0).getValue());

    }

    @Test
    public void testOnEventWarning(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AxellEventIngestor axellIngestor = new AxellEventIngestor();
        axellIngestor.setEventSubscriptionService(eventSubscriptionService);
        axellIngestor.setNodeDao(nodeDao);
        axellIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/AEM3-MIB/aemAlarmNotificationWarning", date);
        axellIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());
        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/AEM3-MIB/aemAlarmNotificationWarning", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AxellEventIngestor.TIME_AXELL_PARAMETER).get(0).getValue());

    }

    @Test
    public void testOnEventNotification(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AxellEventIngestor axellIngestor = new AxellEventIngestor();
        axellIngestor.setEventSubscriptionService(eventSubscriptionService);
        axellIngestor.setNodeDao(nodeDao);
        axellIngestor.setEventForwarder(eventForwarder);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/AEM3-MIB/aemAlarmClearanceNotification", date);
        axellIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());
        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/AEM3-MIB/aemAlarmClearanceNotification", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AxellEventIngestor.TIME_AXELL_PARAMETER).get(0).getValue());

    }

    @Test
    public void testActivate(){
        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AxellEventIngestor axellIngestor = new AxellEventIngestor();
        axellIngestor.setEventSubscriptionService(eventSubscriptionService);
        axellIngestor.setNodeDao(nodeDao);
        axellIngestor.setEventForwarder(eventForwarder);

        axellIngestor.activate();

        verify(eventSubscriptionService).addEventListener(
                eq(axellIngestor),
                eq(AxellEventIngestor.INTERESTING_AXELL_UEIS)
        );
        verifyNoMoreInteractions(eventSubscriptionService);
    }

    @Test
    public void testEventNotHandle(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao = mock(NodeDao.class);
        AxellEventIngestorTest.FakeEventSubscriptionService fakeEventSubscriptionService = new AxellEventIngestorTest.FakeEventSubscriptionService();

        // uso uno SPY del listener così posso verificare se onEvent è stato chiamato o no
        AxellEventIngestor axellEventIngestor = new AxellEventIngestor();
        axellEventIngestor.setEventForwarder(eventForwarder);
        axellEventIngestor.setNodeDao(nodeDao);
        axellEventIngestor.setEventSubscriptionService(fakeEventSubscriptionService);

        AxellEventIngestor spyListener = spy(axellEventIngestor);

        spyListener.activate();

        // evento con UEI NON tra quelli INTERESSING_ACOM_UEIS
        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/AEM3-MIB/aemControlRequestNotification", date);

        // Simulo il dispatcher che prova a "distribuire" l'evento
        fakeEventSubscriptionService.fire(event);

        // Verifico che onEvent NON sia mai stato chiamato
        verify(spyListener, never()).onEvent(any(InMemoryEvent.class));
        verify(eventForwarder, never()).sendAsync(any());
    }

    private static Date getEventDateFromString(){
        LocalDateTime ldt = LocalDateTime.parse(EVENT_DATE, AxellEventIngestor.TRAP_TIME_FORMATTER);
        Instant trapInstant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(trapInstant);
    }

    private static InMemoryEvent getEvent(String uei, Date date){

        return ImmutableInMemoryEvent.newBuilder()
                .setUei(uei)
                .setTime(date)
                .setNodeId(1)
                .addParameter(ImmutableEventParameter.newBuilder()
                        .setName(AxellEventIngestor.TIME_AXELL_PARAMETER)
                        .setValue(EVENT_DATE)
                        .build())
                .setSource("AndrewTest")
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

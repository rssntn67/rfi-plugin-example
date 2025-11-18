package it.xeniaprogetti.rfi.plugin.example.events;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.integration.api.v1.dao.AlarmDao;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventForwarder;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.immutables.ImmutableEventParameter;
import org.opennms.integration.api.v1.model.immutables.ImmutableInMemoryEvent;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

import static it.xeniaprogetti.rfi.plugin.example.events.AComEventIngestor.TRAP_TIME_FORMATTER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class AcomEventIngestorTest {

    private static String EVENT_DATE = "20251113112000+0200";

    @Test
    public void testTranslate(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        AlarmDao alarmDao  = mock(AlarmDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AComEventIngestor acomIngestor = new AComEventIngestor(eventForwarder, nodeDao, alarmDao, eventSubscriptionService);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/INC-MIB-AL/tiIncTrapWarning", date);
        ImmutableInMemoryEvent eventTranslated = acomIngestor.translate(event);

        assertEquals("uei.opennms.org/translator/INC-MIB-AL/tiIncTrapWarning", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AComEventIngestor.TIME_ACOM_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventCleared(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        AlarmDao alarmDao  = mock(AlarmDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AComEventIngestor acomIngestor = new AComEventIngestor(eventForwarder, nodeDao, alarmDao, eventSubscriptionService);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/INC-MIB-AL/tiIncTrapCleared", date);
        acomIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/INC-MIB-AL/tiIncTrapCleared", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AComEventIngestor.TIME_ACOM_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventNormal(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        AlarmDao alarmDao  = mock(AlarmDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AComEventIngestor acomIngestor = new AComEventIngestor(eventForwarder, nodeDao, alarmDao, eventSubscriptionService);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/INC-MIB-AL/tiIncTrapNormal", date);
        acomIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/INC-MIB-AL/tiIncTrapNormal", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AComEventIngestor.TIME_ACOM_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventWarning(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        AlarmDao alarmDao  = mock(AlarmDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AComEventIngestor acomIngestor = new AComEventIngestor(eventForwarder, nodeDao, alarmDao, eventSubscriptionService);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/INC-MIB-AL/tiIncTrapWarning", date);
        acomIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/INC-MIB-AL/tiIncTrapWarning", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AComEventIngestor.TIME_ACOM_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventMinor(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        AlarmDao alarmDao  = mock(AlarmDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AComEventIngestor acomIngestor = new AComEventIngestor(eventForwarder, nodeDao, alarmDao, eventSubscriptionService);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/INC-MIB-AL/tiIncTrapMinor", date);
        acomIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/INC-MIB-AL/tiIncTrapMinor", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AComEventIngestor.TIME_ACOM_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventMajor(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        AlarmDao alarmDao  = mock(AlarmDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AComEventIngestor acomIngestor = new AComEventIngestor(eventForwarder, nodeDao, alarmDao, eventSubscriptionService);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/INC-MIB-AL/tiIncTrapMajor", date);
        acomIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/INC-MIB-AL/tiIncTrapMajor", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AComEventIngestor.TIME_ACOM_PARAMETER).get(0).getValue());
    }

    @Test
    public void testOnEventCritical(){

        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao  = mock(NodeDao.class);
        AlarmDao alarmDao  = mock(AlarmDao.class);
        EventSubscriptionService eventSubscriptionService  = mock(EventSubscriptionService.class) ;

        AComEventIngestor acomIngestor = new AComEventIngestor(eventForwarder, nodeDao, alarmDao, eventSubscriptionService);

        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/INC-MIB-AL/tiIncTrapCritical", date);
        acomIngestor.onEvent(event);

        final ArgumentCaptor<InMemoryEvent> capture = ArgumentCaptor.forClass(InMemoryEvent.class);
        verify(eventForwarder).sendAsync(capture.capture());

        InMemoryEvent eventTranslated = capture.getValue();

        assertEquals("uei.opennms.org/translator/INC-MIB-AL/tiIncTrapCritical", eventTranslated.getUei());
        assertEquals("rfi-plugin-example", eventTranslated.getSource());
        assertEquals(getEventDateFromString(), eventTranslated.getTime());
        assertEquals(EVENT_DATE, eventTranslated.getParametersByName(AComEventIngestor.TIME_ACOM_PARAMETER).get(0).getValue());
    }

    @Test
    public void testActivate() {
        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao = mock(NodeDao.class);
        EventSubscriptionService eventSubscriptionService = mock(EventSubscriptionService.class);

        AComEventIngestorComponent acom = new AComEventIngestorComponent();
        acom.setEventForwarder(eventForwarder);
        acom.setNodeDao(nodeDao);
        acom.setEventSubscriptionService(eventSubscriptionService);

        acom.activate();

        verify(eventSubscriptionService).addEventListener(
                eq(acom),
                eq(AComEventIngestorComponent.INTERESTING_ACOM_UEIS)
        );
        verifyNoMoreInteractions(eventSubscriptionService);
    }

    @Test
    public void testEventNotHandle(){
        EventForwarder eventForwarder = mock(EventForwarder.class);
        NodeDao nodeDao = mock(NodeDao.class);
        FakeEventSubscriptionService fakeEventSubscriptionService = new FakeEventSubscriptionService();

        // uso uno SPY del listener così posso verificare se onEvent è stato chiamato o no
        AComEventIngestorComponent acomEventIngestor = new AComEventIngestorComponent();
        acomEventIngestor.setEventForwarder(eventForwarder);
        acomEventIngestor.setNodeDao(nodeDao);
        acomEventIngestor.setEventSubscriptionService(fakeEventSubscriptionService);

        AComEventIngestorComponent spyListener = spy(acomEventIngestor);

        spyListener.activate();

        // evento con UEI NON tra quelli INTERESSING_ACOM_UEIS
        Date date = new Date();
        InMemoryEvent event = getEvent("uei.opennms.org/traps/INC-MIB-AL/tiIncSpontaneousSumRep", date);

        // Simulo il dispatcher che prova a "distribuire" l'evento
        fakeEventSubscriptionService.fire(event);

        // Verifico che onEvent NON sia mai stato chiamato
        verify(spyListener, never()).onEvent(any(InMemoryEvent.class));
        verify(eventForwarder, never()).sendAsync(any());
    }

    private static Date getEventDateFromString(){

        Instant trapInstant = ZonedDateTime.parse(EVENT_DATE, TRAP_TIME_FORMATTER).toInstant();
        return  Date.from(trapInstant);
    }

    private static InMemoryEvent getEvent(String uei, Date date){

        return ImmutableInMemoryEvent.newBuilder()
                .setUei(uei)
                .setTime(date)
                .addParameter(ImmutableEventParameter.newBuilder()
                        .setName(AComEventIngestor.TIME_ACOM_PARAMETER)
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

package it.xeniaprogetti.rfi.plugin.example.events;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opennms.integration.api.v1.dao.AlarmDao;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventForwarder;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.immutables.ImmutableEventParameter;
import org.opennms.integration.api.v1.model.immutables.ImmutableInMemoryEvent;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

import static it.xeniaprogetti.rfi.plugin.example.events.AComEventIngestor.TRAP_TIME_FORMATTER;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class AcmTest {

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
    public void testOnevent(){

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
}

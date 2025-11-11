package it.xeniaprogetti.rfi.plugin.example.events;

import org.opennms.integration.api.v1.dao.AlarmDao;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventForwarder;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.immutables.ImmutableInMemoryEvent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class AComEventIngestor implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(AComEventIngestor.class);

	private static String UEI_ACOM_MATCH = "uei.opennms.org/traps/INC-MIB-AL";
	private static String NODE_LABEL_ACOM_PARAMETER_MATCH = ".1.3.6.1.4.1.231.7.99.4.2.1.1.11";

	private final EventForwarder eventForwarder;
	private final NodeDao nodeDao;
	private final AlarmDao alarmDao;
    private final EventSubscriptionService eventSubscriptionService;

	private boolean syncAct = false;
	private Date startSyncDate;

    public AComEventIngestor(EventForwarder eventForwarder,
                             NodeDao nodeDao,
                             AlarmDao alarmDao,
                             EventSubscriptionService eventSubscriptionService) {
        // costruttore vuoto necessario
        this.eventForwarder = eventForwarder;
        this.nodeDao = nodeDao;
        this.alarmDao = alarmDao;
        this.eventSubscriptionService = eventSubscriptionService;

    }

    public void start(){
        eventSubscriptionService.addEventListener(this);
    }

	@Override
	public String getName() {

		return "acomEventIngestor";
	}

	@Override
	public int getNumThreads() {		
		return 1;
	}

	@Override
	public void onEvent(InMemoryEvent e) {



		if(!e.getUei().startsWith(UEI_ACOM_MATCH)) {
			return;
		}

        LOG.info("arrive new event: {}", e);

        ImmutableInMemoryEvent translate = translate(e);

        LOG.info("send event translated: {} ", translate);
        eventForwarder.sendAsync( translate );
			
	}


	private ImmutableInMemoryEvent translate(InMemoryEvent e) {

        LOG.info("translate event : {}", e);
		String nodeLabel = e.getParametersByName(NODE_LABEL_ACOM_PARAMETER_MATCH).get(0).getValue();
		Node nodeByLabel = nodeDao.getNodeByLabel(nodeLabel);
		
		String uei = e.getUei().replace(UEI_ACOM_MATCH, UEI_ACOM_MATCH + "/translator");
		
		return ImmutableInMemoryEvent.newBuilderFrom(e)
				.setNodeId(nodeByLabel == null ? 1 : nodeByLabel.getId())
				.setUei(uei)
				.build();
	} 
	
	public synchronized void startSyncActive(Date startSyncDate) {	
		syncAct = true;
		this.startSyncDate = startSyncDate;
		
		//clean all alarms
		alarmDao.getAlarms().stream()
				.filter(a -> { return a.getReductionKey().startsWith(UEI_ACOM_MATCH) && a.getLastEventTime().before(startSyncDate); })
				.forEach(a -> alarmDao.clear(a.getId()));
		
		
	}
	
	public synchronized void endSync() {
		syncAct = false;
	}


}

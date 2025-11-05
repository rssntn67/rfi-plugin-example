package it.xeniaprogetti.rfi.plugin.example.events;

import org.opennms.integration.api.v1.dao.AlarmDao;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventForwarder;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.immutables.ImmutableInMemoryEvent;

import java.util.Date;

public class AComEventIngestore implements EventListener, Runnable {

	private static String UEI_ACOM_MATCH = "uei.opennms.org/traps/INC-MIB-AL";
	private static String NODE_LABEL_ACOM_PARAMETER_MATCH = ".1.3.6.1.4.1.231.7.99.4.2.1.1.11";
	
	private int threads;
	private final EventForwarder eventForwarder;
	private final NodeDao nodeDao;
	private final AlarmDao alarmDao;
	
	private boolean syncAct = false;
	private Date startSyncDate;
	
	public AComEventIngestore(int threads, EventForwarder eventForwarder, NodeDao nodeDao, AlarmDao alarmDao) {
	
		this.threads = threads;
		this.eventForwarder = eventForwarder;
		this.nodeDao = nodeDao;
		this.alarmDao = alarmDao;
		
		//ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();		
		//ScheduledFuture<ScheduledExecutorService>
	}
	
	@Override
	public String getName() {

		return "AcomEventIngestor";
	}

	@Override
	public int getNumThreads() {		
		return threads;
	}

	@Override
	public void onEvent(InMemoryEvent e) {
		
		if(!e.getUei().startsWith(UEI_ACOM_MATCH)) {
			return;
		}
	
		
		
		eventForwarder.sendAsync(  translate(e)  );
			
	}
	
	private ImmutableInMemoryEvent translate(InMemoryEvent e) {
		
		String nodeLabel = e.getParametersByName(NODE_LABEL_ACOM_PARAMETER_MATCH).get(0).getValue();
		Node nodeByLabel = nodeDao.getNodeByLabel(nodeLabel);
		
		String uei = e.getUei().replace(UEI_ACOM_MATCH, UEI_ACOM_MATCH + "/translator");
		
		return ImmutableInMemoryEvent.newBuilderFrom(e)
				.setNodeId(nodeByLabel.getId())
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}

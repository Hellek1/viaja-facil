package eu.hellek.gba.server.TaskQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

import eu.hellek.gba.model.PQA;
import eu.hellek.gba.model.PlanQuadrat;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.utils.Utils;

public class SomethingTask implements DeferredTask {

	private static final long serialVersionUID = 1L;
	
	private int startIndex;
	
	public SomethingTask(int startIndex) {
		this.startIndex = startIndex;
	}

	@Override
	public void run() {
		DeferredTaskContext.setDoNotRetry(true);
		String functionName = "SomethingTask";
		try {
			QuotaService qs = QuotaServiceFactory.getQuotaService();
			long starttime = qs.getCpuTimeInMegaCycles();
			Objectify ofy = Dao.getInstance().getObjectify();
			Logger.getLogger(functionName).log(Level.WARNING, functionName + ": Updating 1000 PQs starting with offset " + startIndex);
			Query<PlanQuadrat> q = ofy.query(PlanQuadrat.class).offset(startIndex).limit(1000);
			List<PlanQuadrat> pqs = q.list();
			List<PQA> toUpdate = new ArrayList<PQA>(1000);
			for(PlanQuadrat pq : pqs) {
				if(pq.getMainLineKeys() != null) {
					PQA newPQA = new PQA(pq, Dao.getRootEntityPQA());
					toUpdate.add(newPQA);
				}
			}
			ofy.put(toUpdate);
			long endtime = qs.getCpuTimeInMegaCycles();
			double cpuSeconds = qs.convertMegacyclesToCpuSeconds(endtime - starttime);
			Logger.getLogger(functionName).log(Level.FINEST, functionName + ": " + cpuSeconds + " CPU seconds");
		} catch (Exception e) {
			Logger.getLogger(functionName).log(Level.SEVERE, functionName + ": "  + e);
			e.printStackTrace();
			Utils.eMailError(e, functionName);
		}
	}

}

package eu.hellek.gba.server.TaskQueue;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.PlanQuadrat;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.utils.Utils;

public class CheckPQsTask implements DeferredTask {

	private static final long serialVersionUID = 1L;
	
	private Key<Line> lineKey;
	
	public CheckPQsTask(Key<Line> lineKey) {
		this.lineKey = lineKey;
	}

	@Override
	public void run() {
		DeferredTaskContext.setDoNotRetry(true);
		String functionName = "CheckPQsTask";
		try {
			QuotaService qs = QuotaServiceFactory.getQuotaService();
			long starttime = qs.getCpuTimeInMegaCycles();
			Objectify ofy = Dao.getInstance().getObjectify();
			Key<Line> l = lineKey;
			Query<PlanQuadrat> q2 = ofy.query(PlanQuadrat.class).filter("directLineKeys", l);
			List<PlanQuadrat> rl = q2.list();
			if(!(rl.size() > 0)) {
				System.err.println("Did not get any PQs for Line " + l);
			}
			for(PlanQuadrat pq : rl) {
				if(pq.getMainLineKeys() != null) {
					int mlk_size = pq.getMainLineKeys().size();
					if(mlk_size != pq.getIgnore().size() || mlk_size != pq.getIndicesMLK().size() || pq.getTwoway().size() != mlk_size) {
						System.err.println("Some sizes did not match for PQ " + pq.getGeoCell());
					}
					for(Key<Line> key : pq.getMainLineKeys()) {
						if(!pq.getIndices().get(pq.getDirectLineKeys().indexOf(key)).equals(pq.getIndicesMLK().get(pq.getMainLineKeys().indexOf(key)))) {
							System.err.println("Indices did not match for line " + key + " in PQ " + pq.getGeoCell());
						}
						if(!pq.getIgnore().get(pq.getMainLineKeys().indexOf(key))) {
							if(Dao.getInstance().getSearchPointsForLine(key, pq.getGeoCell(), 0, ofy).size() == 0) {
								System.err.println("GeoCell was not set to ignore. Nevertheless got zero points for line " + key + " in PQ " + pq.getGeoCell());
								System.err.println("Line full name: " + Dao.getInstance().getLineByKey(key, ofy));
							}
						}
					}
				}
			}
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

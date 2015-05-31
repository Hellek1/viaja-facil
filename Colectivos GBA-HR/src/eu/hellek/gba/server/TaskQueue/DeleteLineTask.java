package eu.hellek.gba.server.TaskQueue;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.dao.ManagementDao;
import eu.hellek.gba.server.utils.Utils;

public class DeleteLineTask implements DeferredTask {

	private static final long serialVersionUID = 1L;
	
	private String payload;
	
	public DeleteLineTask(String payload) {
		this.payload = payload;
	}

	@Override
	public void run() {
		DeferredTaskContext.setDoNotRetry(true);
		String functionName = "DeleteLineTask";
		try {
			String line = payload;
			QuotaService qs = QuotaServiceFactory.getQuotaService();
	        long start = qs.getCpuTimeInMegaCycles();
	        Objectify ofy = Dao.getInstance().getObjectify();
	        Line l = Dao.getInstance().getLineByKey(new Key<Line>(Line.class, Long.parseLong(line)), ofy);
	        ManagementDao.getInstance().removeLine(l, ofy);
			long end = qs.getCpuTimeInMegaCycles();
	        double cpuSeconds = qs.convertMegacyclesToCpuSeconds(end - start);
	        Logger.getLogger(functionName).log(Level.INFO, "deleted line with key " + line);
	        Logger.getLogger(functionName).log(Level.FINEST, functionName + ": " + cpuSeconds + " CPU seconds");
		} catch (Exception e) {
			Logger.getLogger(functionName).log(Level.SEVERE, functionName + ": "  + e);
			e.printStackTrace();
			Utils.eMailError(e, functionName);
		}
	}

}

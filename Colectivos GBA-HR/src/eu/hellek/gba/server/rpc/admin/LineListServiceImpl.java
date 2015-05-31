package eu.hellek.gba.server.rpc.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import eu.hellek.gba.client.admin.LineListService;
import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.PQA;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.server.TaskQueue.AddLineTask;
import eu.hellek.gba.server.TaskQueue.AddTrainTask;
import eu.hellek.gba.server.TaskQueue.DeleteLineTask;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.dao.ManagementDao;
import eu.hellek.gba.server.rpc.ListPointsServiceImpl;
import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.SearchResultProxy;

@SuppressWarnings("serial")
public class LineListServiceImpl extends RemoteServiceServlet implements LineListService {

	public String resetTrainNodes() throws IllegalArgumentException {
		try {
			Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			cache.clear();
		} catch (CacheException e) {
			e.printStackTrace();
		}
		Dao.resetTrainNodes();
		Dao.getTrainNodes();
		Dao.getTrainNodeKeyMap();
		Dao.getSubteKeys();
		Dao.getTrainKeys();
		return "done";
	}
	
	public String checkLines() throws IllegalArgumentException {
		Objectify ofy = Dao.getInstance().getObjectify();
		/*Queue queue = QueueFactory.getQueue("highspeed");
		Query<Line> q1 = ofy.query(Line.class).filter("type", 1);
		List<Key<Line>> buses = q1.listKeys();
		for(Key<Line> l : buses) {
			CheckPQsTask dTask = new CheckPQsTask(l);
			queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
		}*/
		PQA temp = Dao.getInstance().getPQA("31bcbc44r", ofy);
		int index = temp.getLineKeys().indexOf(new Key<Line>(Line.class, 60003));
		System.err.println("Ignore for that line at index " + index + ": " + temp.getIgnore(index));
		System.err.println("index for that line: " + temp.getIndices().get(index));
		
		
		System.err.println("Sizes: ");
		System.err.println("keys:\t " + temp.getLineKeys().size());
		System.err.println("indices:\t " + temp.getIndices().size());
		System.err.print("keys: ");
		for(Key<Line> k : temp.getLineKeys()) {
			System.err.print(k + "\t");
		}
		System.err.println();
		System.err.print("indices: ");
		for(int i : temp.getIndices()) {
			System.err.print(i + "\t");
		}
		System.err.println();
		System.err.print("ignore: ");
		for(int i = 0; i < temp.getLineKeys().size(); i++) {
			System.err.print(temp.getIgnore(i) + "\t");
		}
		System.err.println();
		/*try {
			Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			cache.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		return "done";
	}
	
	public List<String> getLines() throws IllegalArgumentException {
		QuotaService qs = QuotaServiceFactory.getQuotaService();
        long start = qs.getCpuTimeInMegaCycles();
        Objectify ofy = Dao.getInstance().getObjectify();
        List<Line> lines = ManagementDao.getInstance().getLines(ofy);
		List<String> strings = new ArrayList<String>();
		if(lines == null) {
//			strings.add("No lines");
//			strings.add("-1");
		} else if (lines.isEmpty()) {
//			strings.add("No lines");
//			strings.add("-1");
		} else {
			Iterator<Line> iterator = lines.iterator();
			while(iterator.hasNext()) {
				Line temp = iterator.next();
//				System.err.println("A: " + temp.getId().toString());
//				System.err.println("B: " + KeyFactory.keyToString(temp.getId()));
				strings.add(temp.toString());
				strings.add(temp.getId().toString());
			}
		}
		long end = qs.getCpuTimeInMegaCycles();
        double cpuSeconds = qs.convertMegacyclesToCpuSeconds(end - start);
        Logger.getLogger("LineListServiceImpl").log(Level.FINEST, "getLines(): " + cpuSeconds + " CPU seconds");
		return strings;	
	}
	
	// Format von String points: lat,lon,Strassenname,ignore als float,float,string ohne beistrich,boolean(0/1)
	// Die ersten 3 Felder: Nummer, Ramal und ob zug/ubahn oder bus (nur bei letzteren werden zwischenpunkte erstellt)
	public String addLine(String points) throws IllegalArgumentException {
		String[] parts = points.split(Pattern.quote(","));
		if(parts[2].equals("bus")) {
			Queue queue = QueueFactory.getQueue("mydefault");
			AddLineTask dTask = new AddLineTask(points);
	        queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
		} else if(parts[2].equals("subte") || parts[2].equals("tren")) {
			Queue queue = QueueFactory.getQueue("addTrain");
			AddTrainTask dTask = new AddTrainTask(points);
	        queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
		}
		return "done";
	}
	
	public String deleteLine(String line) throws IllegalArgumentException {
		Queue queue = QueueFactory.getQueue("deleteLine");
		DeleteLineTask dTask = new DeleteLineTask(line);
		queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
		return "done";
	}

	public String deleteAllTrains() throws IllegalArgumentException {
		System.err.println("deleteAllTrains called");
		/*Queue queue = QueueFactory.getQueue("deleteLine");
		Objectify ofy = Dao.getInstance().getObjectify();
		//List<Key<Line>> keys = ManagementDao.getInstance().getAllTrainKeys(ofy);
		int [] lines = { 4, 10, 28, 46, 53, 59, 65, 85, 99, 100, 112, 114, 148, 181 };
		for(int l : lines) {
			Query<Line> q = ofy.query(Line.class).filter("linenum", Integer.toString(l));
			List<Key<Line>> keys = q.listKeys();
			for(Key<Line> k : keys) {
				DeleteLineTask dTask = new DeleteLineTask("" + k.getId());
				queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
			}
		}*/
		return "done";
	}

	public SearchResultProxy getAllConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte) throws IllegalArgumentException {
		QuotaService qs = QuotaServiceFactory.getQuotaService();
        long start = qs.getCpuTimeInMegaCycles();
        String functionName = "getAllConnections";
        SearchResultProxy direct = new ListPointsServiceImpl().getDirectConnections(lat1, lon1, lat2, lon2, ignoreTrains, ignoreSubte);
        List<ConnectionProxy> conns = direct.getConnections();
        SearchResultProxy indirect = new ListPointsServiceImpl().getIndirectConnections(lat1, lon1, lat2, lon2, ignoreTrains, ignoreSubte, direct.getMlkSet1String(), direct.getMlkSet2String());
        conns.addAll(indirect.getConnections());
        long end = qs.getCpuTimeInMegaCycles();
		double cpuSeconds = qs.convertMegacyclesToCpuSeconds(end - start);
		Logger.getLogger("LineListServiceImpl").log(Level.INFO, functionName + ": " + cpuSeconds + " CPU seconds.");
        return new SearchResultProxy(conns, null, null);
	}
	
	public SearchResultProxy getTrainConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte) throws IllegalArgumentException {
		QuotaService qs = QuotaServiceFactory.getQuotaService();
        long start = qs.getCpuTimeInMegaCycles();
        Dao.getInstance();
        Objectify ofy = ObjectifyService.begin();
        String functionName = "getTrainConnections";
        HashSet<Key<Line>> tabuTrainsSet = new HashSet<Key<Line>>();
		if(ignoreTrains) {
			tabuTrainsSet.addAll(Dao.getTrainKeys());
		}
		if(ignoreSubte) {
			tabuTrainsSet.addAll(Dao.getSubteKeys());
		}
		ConnectionProxy connTren = Dao.getInstance().indirectSearch(new GeoPt(lat1, lon1), new GeoPt(lat2, lon2), tabuTrainsSet, new HashSet<Key<Line>>(), ofy);
		List<ConnectionProxy> conns = new LinkedList<ConnectionProxy>();
		conns.add(connTren);
        long end = qs.getCpuTimeInMegaCycles();
		double cpuSeconds = qs.convertMegacyclesToCpuSeconds(end - start);
		Logger.getLogger("LineListServiceImpl").log(Level.INFO, functionName + ": " + cpuSeconds + " CPU seconds.");
		if(connTren != null) {
			return new SearchResultProxy(conns, null, null);
		} else {
			return null;
		}
	}
	
	public List<Float> getPoints(String line) throws IllegalArgumentException {
		QuotaService qs = QuotaServiceFactory.getQuotaService();
        long start = qs.getCpuTimeInMegaCycles();
        Dao.getInstance();
        Objectify ofy = ObjectifyService.begin();
		Key<Line> k = new Key<Line>(Line.class, Long.parseLong(line));
		Line l = Dao.getInstance().getLineByKey(k, ofy);
		Collection<Point> points = Dao.getInstance().getPointsToDisplayForLine(l, ofy);
		List<Float> coords = new ArrayList<Float>();
		for(Point p : points) {
			coords.add(p.getLatlon().getLatitude());
			coords.add(p.getLatlon().getLongitude());
		}
//		Logger.getLogger("AdminInterface").log(Level.INFO, "In getPoints("+line+")");
//		Logger.getLogger("AdminInterface").log(Level.INFO, "Line: " + l.toString());
//		Logger.getLogger("AdminInterface").log(Level.INFO, "Key: " + k.toString());
		long end = qs.getCpuTimeInMegaCycles();
        double cpuSeconds = qs.convertMegacyclesToCpuSeconds(end - start);
        Logger.getLogger("GetPointsServiceImpl").log(Level.INFO, "getPoints() for line " + l.getLinenum() + " " + l.getRamal() + ": " + cpuSeconds + " CPU seconds");
		return coords;
	}
	
}

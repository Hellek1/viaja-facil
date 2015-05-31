package eu.hellek.gba.server.TaskQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.model.TrainNode;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.dao.ManagementDao;
import eu.hellek.gba.server.utils.Utils;

public class AddTrainTask implements DeferredTask {
	
	private static final long serialVersionUID = 1L;
	
	private String payload;
	
	public AddTrainTask(String payload) {
		this.payload = payload;
	}

	/**
	 * Format von String points: lat,lon,Strassenname,ignore als float,float,string ohne beistrich,boolean(0/1)
	 * Die ersten 3 Felder: Nummer, Ramal und ob zug/ubahn oder bus (nur bei letzteren werden zwischenpunkte erstellt)
	 * danach: x,y,uniqueName,Name (coordinaten einer station, der linienübergreifende name der station, der für die aktuelle linie gültige Name)
	*/ 
	@Override
	public void run() {
		DeferredTaskContext.setDoNotRetry(true);
		String functionName = "AddTrainTask";
		try {
			QuotaService qs = QuotaServiceFactory.getQuotaService();
	        long starttime = qs.getCpuTimeInMegaCycles();
	        String points = payload;
			String[] parts = points.split(Pattern.quote(","));
			Line l = null;
			List<Point> pointsList = new ArrayList<Point>();
			if(parts[2].equals("subte")) {
				if(parts[0].contains("Metrobus")) {
					l = new Line(parts[0], parts[1], 13);
				} else if(parts[0].contains("Premetro")) {
					l = new Line(parts[0], parts[1], 15);
				} else {
					l = new Line(parts[0], parts[1], 11);
				}
			} else if(parts[2].equals("tren")) {
				l = new Line(parts[0], parts[1], 21);
			}
			Objectify ofy = Dao.getInstance().getObjectify();
			if(ManagementDao.getInstance().checkIfLineExists(l, ofy)) {
				throw new Exception("Line already exists: "  + l);
			}
			ManagementDao.getInstance().addLine(l, ofy);
			TrainNode lastNode = null;
//			Key knLast = null;
			int start = 3; // felder 0,1,2 sind diese extradaten, ab feld 3 kommen die punkte
			for(int i = start; i<parts.length; i=i+4) {
				float a = Float.valueOf(parts[i]);
				float b = Float.valueOf(parts[i+1]);
				Point p = new Point(parts[i+2], a, b, new Key<Line>(Line.class, l.getId()));
//				com.beoui.geocell.model.Point _p = new com.beoui.geocell.model.Point(a,b);
				String cell = Utils.computeGeoCell(p);
				TrainNode n = new TrainNode(cell, cell, parts[i+3], parts[i+2], new Key<Line>(Line.class, l.getId()), l.getType(), pointsList.size(), Dao.getRootEntityTrain());
				Key<TrainNode> kN = ManagementDao.getInstance().addTrainNode(n);
				if(lastNode != null) {
					lastNode.setNextNode(kN);
					ManagementDao.getInstance().updateTrainNode(lastNode);
//					n.addConnectedNode(knLast);
//					Dao.getInstance().addOrUpdateTrainNode(n);
				}
				lastNode = n;
				if(i>=start+2) {
					Point lastPoint = pointsList.get(pointsList.size()-1);
					lastPoint.setNextMainPointIndex(pointsList.size());
				}
				p.setIndex(pointsList.size());
				pointsList.add(p);
			}
			ManagementDao.getInstance().safePoints(pointsList, ofy);
			long endtime = qs.getCpuTimeInMegaCycles();
	        double cpuSeconds = qs.convertMegacyclesToCpuSeconds(endtime - starttime);
	        Logger.getLogger(functionName).log(Level.FINEST, functionName + ": " + cpuSeconds + " CPU seconds");
	        Logger.getLogger(functionName).log(Level.INFO, functionName + ": added Train " + l);
		} catch (Exception e) {
			Logger.getLogger(functionName).log(Level.SEVERE, functionName + ": "  + e);
			Logger.getLogger(functionName).log(Level.SEVERE, functionName + ": Payload was: "  + payload);
			e.printStackTrace();
			Utils.eMailError(e, functionName);
		}
	}

}

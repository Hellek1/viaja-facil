package eu.hellek.gba.server.TaskQueue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import eu.hellek.gba.server.utils.Utils;

public class RemoteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		service(req, resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		service(req, resp);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.err.println("RemoteServlet called");
		PrintWriter out = resp.getWriter();
		out.println("Success");
		ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
		try {
			String obj = (String)ois.readObject();
//			System.out.println(obj);
			String secretCode = obj.substring(0, 31);
			// TODO
			if(secretCode.equals("replace me")) {
				String action = obj.substring(31, 32);
				if(action.equals("a")) {
					String data = obj.substring(32);
					String[] parts = data.split(Pattern.quote(","));
					if(parts[2].equals("bus")) {
						Queue queue = QueueFactory.getQueue("mydefault");
						AddLineTask dTask = new AddLineTask(data);
						queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
					} else if(parts[2].equals("subte") || parts[2].equals("tren")) {
						Queue queue = QueueFactory.getQueue("addTrain");
						AddTrainTask dTask = new AddTrainTask(data);
				        queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
					}
				} else if(action.equals("d")) {
					Queue queue = QueueFactory.getQueue("highspeed");
					for(int i = 0; i < 20000; i = i + 1000) {
						SomethingTask dTask = new SomethingTask(i);
						queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
					}
					/*
					Queue queue = QueueFactory.getQueue("deleteLine");
					String[] lines = new String[] { "21", "28" , "36", "4", "42", "51", "57", "8", "61", "62", "79" };
					Objectify ofy = Dao.getInstance().getObjectify();
					for(String line : lines) {
						Query<Line> q = ofy.query(Line.class).filter("linenum", line);
						List<Key<Line>> keys = q.listKeys();
						for(Key<Line> k : keys) {
							DeleteLineTask dTask = new DeleteLineTask("" + k.getId());
							queue.add(TaskOptions.Builder.withDefaults().payload(dTask));
						}
					}*/
				}
			} else {
				Logger.getLogger("RemoteServlet").log(Level.SEVERE, "Somebody tried to access with wrong code: " + req.getRemoteAddr());
				Utils.eMailError(new Exception("Somebody tried to access with wrong code: " + req.getRemoteAddr()), "RemoteServlet");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}

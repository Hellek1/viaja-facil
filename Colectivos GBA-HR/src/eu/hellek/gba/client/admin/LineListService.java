package eu.hellek.gba.client.admin;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import eu.hellek.gba.shared.SearchResultProxy;

@RemoteServiceRelativePath("getLines")
public interface LineListService extends RemoteService {
	String resetTrainNodes() throws IllegalArgumentException;
	String checkLines() throws IllegalArgumentException;
	List<String> getLines() throws IllegalArgumentException;
	String addLine(String points) throws IllegalArgumentException;
	String deleteLine(String line) throws IllegalArgumentException;
	String deleteAllTrains() throws IllegalArgumentException;
	SearchResultProxy getAllConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte) throws IllegalArgumentException;
	SearchResultProxy getTrainConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte) throws IllegalArgumentException;
	List<Float> getPoints(String line) throws IllegalArgumentException;
}

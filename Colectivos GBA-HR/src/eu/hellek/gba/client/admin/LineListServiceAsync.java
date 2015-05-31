package eu.hellek.gba.client.admin;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import eu.hellek.gba.shared.SearchResultProxy;

public interface LineListServiceAsync {
	void resetTrainNodes(AsyncCallback<String> callback) throws IllegalArgumentException;
	void checkLines(AsyncCallback<String> callback) throws IllegalArgumentException;
	void getLines(AsyncCallback<List<String>> callback) throws IllegalArgumentException;
	void addLine(String points, AsyncCallback<String> callback) throws IllegalArgumentException;
	void deleteLine(String line, AsyncCallback<String> callback) throws IllegalArgumentException;
	void deleteAllTrains(AsyncCallback<String> callback) throws IllegalArgumentException;
	void getAllConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte, AsyncCallback<SearchResultProxy> callback) throws IllegalArgumentException;
	void getTrainConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte, AsyncCallback<SearchResultProxy> callback) throws IllegalArgumentException;
	void getPoints(String line, AsyncCallback<List<Float>> callback) throws IllegalArgumentException;
}

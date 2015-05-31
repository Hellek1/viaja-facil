package eu.hellek.gba.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import eu.hellek.gba.shared.SearchResultProxy;

public interface ListPointsServiceAsync {
	void getDirectConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte, AsyncCallback<SearchResultProxy> callback) throws IllegalArgumentException;
	void getIndirectConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte, List<String> mlkSet1String, List<String> mlkSet2String, AsyncCallback<SearchResultProxy> callback) throws IllegalArgumentException;
}

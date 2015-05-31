package eu.hellek.gba.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import eu.hellek.gba.shared.SearchResultProxy;

@RemoteServiceRelativePath("listPoints")
public interface ListPointsService extends RemoteService {
	SearchResultProxy getDirectConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte) throws IllegalArgumentException;
	SearchResultProxy getIndirectConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte, List<String> mlkSet1String, List<String> mlkSet2String) throws IllegalArgumentException;
}

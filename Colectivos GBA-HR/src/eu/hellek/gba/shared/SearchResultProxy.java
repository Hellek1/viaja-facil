package eu.hellek.gba.shared;

import java.io.Serializable;
import java.util.List;

public class SearchResultProxy implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ConnectionProxy> connections;
	private List<String> mlkSet1String;
	private List<String> mlkSet2String;
	private String error;
	
	public static final String noResults = "no results";
	public static final String tooManyReqests = "too many requests";
	public static final String distanceTooLittle = "not enough distance";
	
	public SearchResultProxy() { }
	
	public SearchResultProxy(List<ConnectionProxy> connections, List<String> mlkSet1String, List<String> mlkSet2String) {
		this.connections = connections;
		this.mlkSet1String = mlkSet1String;
		this.mlkSet2String = mlkSet2String;
		this.error = null;
	}
	
	public SearchResultProxy(String error, List<String> mlkSet1String, List<String> mlkSet2String) {
		this.connections = null;
		this.mlkSet1String = mlkSet1String;
		this.mlkSet2String = mlkSet2String;
		this.error = error;
	}

	public List<ConnectionProxy> getConnections() {
		return connections;
	}

	public String getError() {
		return error;
	}

	public List<String> getMlkSet1String() {
		return mlkSet1String;
	}

	public List<String> getMlkSet2String() {
		return mlkSet2String;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setConnections(List<ConnectionProxy> connections) {
		this.connections = connections;
	}

	public void setMlkSet1String(List<String> mlkSet1String) {
		this.mlkSet1String = mlkSet1String;
	}

	public void setMlkSet2String(List<String> mlkSet2String) {
		this.mlkSet2String = mlkSet2String;
	}
	
}

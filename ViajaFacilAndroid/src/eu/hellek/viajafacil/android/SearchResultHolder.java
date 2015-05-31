package eu.hellek.viajafacil.android;

import java.util.ArrayList;
import java.util.List;

import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.SearchResultProxy;

/*
 * stores the search results so that they are not lost while App switches between activities
 */
public class SearchResultHolder {
	
	private List<SearchResultProxy> resultList;
	private static SearchResultHolder instance;
	
	private SearchResultHolder() {
		resultList = new ArrayList<SearchResultProxy>();
	}
	
	public static synchronized SearchResultHolder getInstance() {
		if(instance == null) {
			instance = new SearchResultHolder();
		}
		return instance;
	}

	public synchronized void clear() {
		resultList.clear();
	}
	
	public synchronized void add(SearchResultProxy r) {
		resultList.add(r);
	}
	
	public synchronized List<SearchResultProxy> getResultList() {
		return resultList;
	}
	
	public ConnectionProxy getAtIndex(int index) {
		if(index < resultList.get(0).getConnections().size()) {
			return resultList.get(0).getConnections().get(index);
		} else {
			return resultList.get(1).getConnections().get(index - resultList.get(0).getConnections().size());
		}
	}
	
	public int numResults() {
		int count = 0;
		for(SearchResultProxy srp : resultList) {
			count += srp.getConnections().size();
		}
		return count;
	}

	public void setResultList(List<SearchResultProxy> resultList) {
		this.resultList = resultList;
	}

}

package eu.hellek.gba.server.dao;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapBusCached <A, B> extends LinkedHashMap<A, B> {
	
	private static final long serialVersionUID = 1L;
	
	private final int maxEntries;
	
	public MapBusCached(int maxEntries) {
		super(maxEntries + 1, 1.0f, true);
		this.maxEntries = maxEntries;
	}
	
	@Override
    protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
        return super.size() > maxEntries;
    }
	
}

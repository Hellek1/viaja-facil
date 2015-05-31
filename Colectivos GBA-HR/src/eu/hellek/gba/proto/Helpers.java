package eu.hellek.gba.proto;

import java.util.ArrayList;
import java.util.List;

import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.LineProxy;
import eu.hellek.gba.shared.SearchResultProxy;

/*
 * has to be kept identical on serverside and client side
 */
public class Helpers {

	public static SearchResultProxy copyFromProto(eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy proto) {
		SearchResultProxy res = new SearchResultProxy();
		res.setError(proto.getError());
		res.setMlkSet1String(proto.getMlkSet1StringList());
		res.setMlkSet2String(proto.getMlkSet2StringList());
		List<ConnectionProxy> connections = new ArrayList<ConnectionProxy>();
		for(eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy cProto : proto.getConnectionsList()) {
			ConnectionProxy cProxy = new ConnectionProxy();
			List<LineProxy> lines = new ArrayList<LineProxy>();
			for(eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy.LineProxy lProto : cProto.getLinesList()) {
				LineProxy lProxy = new LineProxy();
				lProxy.setAllPoints(lProto.getAllPointsList());
				lProxy.setAlternativeLines(lProto.getAlternativeLinesList());
				lProxy.setDestStreet(lProto.getDestStreet());
				lProxy.setStartStreet(lProto.getStartStreet());
				lProxy.setDistance(lProto.getDistance());
				lProxy.setKey(lProto.getKey());
				lProxy.setLinenum(lProto.getLinenum());
				lProxy.setRamal(lProto.getRamal());
				lProxy.setRelevantPoints(lProto.getRelevantPointsList());
				lProxy.setStations(lProto.getStationsList());
				lProxy.setType(lProto.getType());
				lines.add(lProxy);
			}
			cProxy.setLines(lines);
			connections.add(cProxy);
		}
		res.setConnections(connections);
		return res;
	}
	
	public static eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy copyToProto(SearchResultProxy proxy) {
		eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.Builder srpBuilder = eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.newBuilder();
		if(proxy.getError() != null) {
			srpBuilder.setError(proxy.getError());
		}
		if(proxy.getMlkSet1String() != null) {
			srpBuilder.addAllMlkSet1String(proxy.getMlkSet1String());
		}
		if(proxy.getMlkSet2String() != null) {
			srpBuilder.addAllMlkSet2String(proxy.getMlkSet2String());
		}
		if(proxy.getConnections() != null) {
			List<eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy> connectionProtos = new ArrayList<eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy>();
			for(ConnectionProxy cProxy : proxy.getConnections()) {
				eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy.Builder cBuilder = eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy.newBuilder();
				if(cProxy.getLines() != null) {
					List<eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy.LineProxy> lineProtos = new ArrayList<eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy.LineProxy>();
					for(LineProxy lProxy : cProxy.getLines()) {
						eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy.LineProxy.Builder lBuilder = eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy.LineProxy.newBuilder();
						lBuilder.addAllAllPoints(lProxy.getAllPoints());
						lBuilder.addAllAlternativeLines(lProxy.getAlternativeLines());
						if(lProxy.getDestStreet() != null) {
							lBuilder.setDestStreet(lProxy.getDestStreet());
						}
						if(lProxy.getStartStreet() != null) {
							lBuilder.setStartStreet(lProxy.getStartStreet());
						}
						lBuilder.setDistance(lProxy.getDistance());
						if(lProxy.getKey() != null) {
							lBuilder.setKey(lProxy.getKey());
						}
						if(lProxy.getLinenum() != null) {
							lBuilder.setLinenum(lProxy.getLinenum());
						}
						if(lProxy.getRamal() != null) {
							lBuilder.setRamal(lProxy.getRamal());
						}
						lBuilder.addAllRelevantPoints(lProxy.getRelevantPoints());
						if(lProxy.getStations() != null) {
							lBuilder.addAllStations(lProxy.getStations());
						}
						lBuilder.setType(lProxy.getType());
						eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy.LineProxy lProto = lBuilder.build();
						lineProtos.add(lProto);
					}
					cBuilder.addAllLines(lineProtos);
				}
				eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy.ConnectionProxy cProto = cBuilder.build();
				connectionProtos.add(cProto);
			}
			srpBuilder.addAllConnections(connectionProtos);
		}
		eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy res = srpBuilder.build();
		return res;
	}
	
}

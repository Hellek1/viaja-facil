package eu.hellek.gba.server;

import java.util.Comparator;

import eu.hellek.gba.model.Line;

public class LineComparator implements Comparator<Line> {

	@Override
	public int compare(Line l1, Line l2) {
		if(l1.getLinenum().equals(l2.getLinenum())) {
			return l1.getRamal().compareTo(l2.getRamal());
		} else {
			return l1.getLinenum().compareTo(l2.getLinenum());
		}
	}

}

package eu.hellek.gba.tools;

import com.beoui.geocell.model.BoundingBox;

import eu.hellek.gba.server.utils.MyGeocellUtils;

public class ListOfGeoCellsToListOfCoords {

	public static void main(String[] args) {
		// SELECT * FROM PQA WHERE lineKeys = KEY('Line', 2005) AND geoCell = '31bcbc44r'
		// SELECT * FROM Point WHERE ANCESTOR IS KEY('Line', 60003) AND defaultGeoCell = '31bcbc44r'
		String [] s = new String[] { "31bcb6e3l", "31bcb6e9l", "31bcb6ebl", "31bcbc41l", "31bcbc43l", "31bcbc49l", "31bcbc4bl", "31bcb6e3r", "31bcb6e9r", "31bcb6ebr", "31bcbc41r", "31bcbc43r", "31bcbc49r", "31bcbc4br", "31bcb6e6l", "31bcb6ecl", "31bcb6eel", "31bcbc44l", "31bcbc46l", "31bcbc4cl", "31bcbc4el", "31bcb6e6r", "31bcb6ecr", "31bcb6eer", "31bcbc44r", "31bcbc46r", "31bcbc4cr", "31bcbc4er", "31bcb6e7l", "31bcb6edl", "31bcb6efl", "31bcbc45l", "31bcbc47l", "31bcbc4dl", "31bcbc4fl", "31bcb6e7r", "31bcb6edr", "31bcb6efr", "31bcbc45r", "31bcbc47r", "31bcbc4dr", "31bcbc4fr", "31bcb6f2l", "31bcb6f8l", "31bcb6fal", "31bcbc50l", "31bcbc52l", "31bcbc58l", "31bcbc5al" };

		for(String ss : s) {
			BoundingBox bb_res = MyGeocellUtils.computeBox(ss);
//			System.out.print(ss + ": ");
			System.out.print(bb_res.getNorth() + "," + bb_res.getEast() + ",");
			System.out.print(bb_res.getSouth() + "," + bb_res.getWest() + ",");
//			System.out.println();
		}
	}
}
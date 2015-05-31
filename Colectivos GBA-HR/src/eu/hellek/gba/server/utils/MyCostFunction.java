package eu.hellek.gba.server.utils;

import com.beoui.geocell.model.CostFunction;


public class MyCostFunction implements CostFunction {

    public double defaultCostFunction(int numCells, int resolution) {
            if(resolution < Utils.geoCellResolution) {
            	return 100;
            } else if (resolution == Utils.geoCellResolution) {
            	return 50;
            } else {
            	return 150;
            }
    }
    
}
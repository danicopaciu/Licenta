package com.springapp.mvc.model.prediction;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.timeseries.WekaForecaster;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class EnergyConsumptionRegression {

    public static double getPredictedEnergy(List<Double> energyHistory) {
        ArrayList<Attribute> atts = new ArrayList<Attribute>(2);
        atts.add(new Attribute("step"));
        atts.add(new Attribute("energy"));

        Instances data = new Instances("EnergyPrediction", atts, 0);
        data.setClassIndex(data.numAttributes() - 1);

        int i = 0;
        double lastEnergy = 0;

        for (Double energy : energyHistory) {
            if (energy != 0) {
                double[] instanceValue = new double[data.numAttributes()];
                instanceValue[0] = i;
                instanceValue[1] = energy;
                DenseInstance denseInstance1 = new DenseInstance(1.0, instanceValue);

                data.add(denseInstance1);
                i++;
                lastEnergy = energy;
            }
        }

        if (i == 0) {
            return 0;
        }
        //library cannot make predictions if only 1 value
        if (i == 1) {
            return lastEnergy;
        }

        return forecast(data);
    }

    // HOW TO: http://wiki.pentaho.com/display/DATAMINING/Time+Series+Analysis+and+Forecasting+with+Weka
    private static double forecast(Instances dataset) {
        try {

            // new forecaster
            WekaForecaster forecaster = new WekaForecaster();

            // set the targets we want to forecast. This method calls
            // setFieldsToLag() on the lag maker object for us
            forecaster.setFieldsToForecast("energy");

            // default underlying classifier is SMOreg (SVM) - we'll use
            // gaussian processes for regression instead
            forecaster.setBaseForecaster(new GaussianProcesses());

            // build the model
            forecaster.buildForecaster(dataset);

            // prime the forecaster with enough recent historical data
            // to cover up to the maximum lag.
            forecaster.primeForecaster(dataset);

            List<List<NumericPrediction>> forecast = forecaster.forecast(1);

            // output the predictions. Outer list is over the steps; inner list is over
            // the targets
//            for (int i = 0; i < 12; i++) {
//                List<NumericPrediction> predsAtStep = forecast.get(i);
//                for (int j = 0; j < 2; j++) {
//                    NumericPrediction predForTarget = predsAtStep.get(j);
//                    System.out.print("" + predForTarget.predicted() + " ");
//                }
//                System.out.println();
//            }

            NumericPrediction prediction = forecast.get(0).get(0);

            return prediction.predicted();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

//    private void linearRegression(){
    //        LinearRegression model = new LinearRegression();
//        try {
//            model.buildClassifier(data); //the last instance with missing class is not used
////            System.out.println(model);
//
//
//            double[] missingInstanceValue = new double[data.numAttributes()];
//            missingInstanceValue[0] = i;
//            missingInstanceValue[1] = Utils.missingValue();
//            DenseInstance missingInstance = new DenseInstance(1.0, missingInstanceValue);
////            data.add(missingInstance);
//
//            double predictedEnergy = model.classifyInstance(missingInstance);
////            System.out.println(predictedEnergy);
//
//            return predictedEnergy;
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}





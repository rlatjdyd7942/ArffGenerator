/**
 * Created by SeongYong on 2015-06-07.
 */

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Instance;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ArffGenerator {

    private FastVector atts;
    private FastVector attNominal;
    private Instances data;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-M-d-h-m-s-SSS");
    private SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:00");
    private String lastApp = "";

    public ArffGenerator() {
        //init();
    }

    private void init() {
        atts = new FastVector();
        atts.addElement(new Attribute("app name", (FastVector)null));
        atts.addElement(new Attribute("date time", "HH:mm:ss"));
        data = new Instances("MyRelation", atts, 0);
    }

    private void createAttributeFromFileList(ArrayList<String> fileNames) {
        atts = new FastVector();

        attNominal = new FastVector();
        ArrayList<String> appNames = new ArrayList();
        int count = 0;
        for (String fileName : fileNames) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(fileName));
                String s;
                String[] parsed;

                while ((s = in.readLine()) != null) {
                    try {
                        parsed = s.split("\\|");
                        if (parsed[0].equals("1")) {
                            if (!appNames.contains(parsed[1])) {
                                appNames.add(parsed[1]);
                                ++count;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        attNominal.addElement("");
        for (String appName : appNames)
            attNominal.addElement(appName);
        atts.addElement(new Attribute("app name", attNominal));
        atts.addElement(new Attribute("last app", attNominal));
        atts.addElement(new Attribute("date time"));
        data = new Instances("MyRelation", atts, 0);
    }

    private Instance getInstance(String[] parsed) {
        double[] vals = new double[data.numAttributes()];

        vals[0] = attNominal.indexOf(parsed[1]);
        vals[1] = attNominal.indexOf(lastApp);
        try {
            Date date = inputFormat.parse(parsed[11]);
            //vals[1] = data.attribute(1).parseDate(outputFormat.format(date));
            vals[2] = date.getHours();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Instance(1.0, vals);
    }

    private void parseLog(String fileName) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String s;
            String[] parsed;

            while ((s = in.readLine()) != null) {
                try {
                    parsed = s.split("\\|");
                    if (parsed[0].equals("1")) {
                        if (!lastApp.equals(parsed[1]) && !parsed[1].equals("com.sec.android.app.launcher") && !parsed[1].equals("com.android.systemui") && !parsed[1].equals("nbt.ideainbox.chimisearch")) {
                            data.add(getInstance(parsed));
                            lastApp = parsed[1];
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generate(ArrayList<String> fileList, String resultFileName) throws Exception {
        for (String fileName : fileList)
            parseLog(fileName);
        BufferedWriter out = new BufferedWriter(new FileWriter(resultFileName));
        out.write(data.toString());
        out.close();
        //System.out.println(data);
    }

    private static String convert(String str) {
        String result = null;
        try {
            ByteArrayOutputStream requestOutputStream = new ByteArrayOutputStream();
            requestOutputStream.write(str.getBytes("UTF-8"));
            result = requestOutputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        ArffGenerator gen = new ArffGenerator();
        ArrayList<String>  fileNames = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d");
        Date startDate = dateFormat.parse("2015-5-6");
        Date endDate = dateFormat.parse("2015-6-5");
        for (; startDate.compareTo(endDate) != 0; startDate.setTime(startDate.getTime() + 60 * 60 * 1000) ) {
            fileNames.add("data\\" + dateFormat.format(startDate) + ".dat");
        }
        gen.createAttributeFromFileList(fileNames);
        gen.generate(fileNames, "training_data.arff");
        fileNames.clear();
/*
        FastVector atts;
        FastVector attsRel;
        FastVector attVals;
        FastVector attValsRel;
        Instances data;
        Instances dataRel;
        double[] vals;
        double[] valsRel;
        int i;

        // 1. set up attributes
        atts = new FastVector();
        // - numeric
        atts.addElement(new Attribute("att1"));
        // - nominal
        attVals = new FastVector();
        for (i = 0; i < 5; i++)
            attVals.addElement("val" + (i+1));
        atts.addElement(new Attribute("att2", attVals));
        // - string
        atts.addElement(new Attribute("att3", (FastVector) null));
        // - date
        atts.addElement(new Attribute("att4", "yyyy-MM-dd"));
        // - relational
        attsRel = new FastVector();
        // -- numeric
        attsRel.addElement(new Attribute("att5.1"));
        // -- nominal
        attValsRel = new FastVector();
        for (i = 0; i < 5; i++)
            attValsRel.addElement("val5." + (i+1));
        attsRel.addElement(new Attribute("att5.2", attValsRel));
        dataRel = new Instances("att5", attsRel, 0);
        atts.addElement(new Attribute("att5", dataRel, 0));

        // 2. create Instances object
        data = new Instances("MyRelation", atts, 0);

        // 3. fill with data
        // first instance
        vals = new double[data.numAttributes()];
        // - numeric
        vals[0] = Math.PI;
        // - nominal
        vals[1] = attVals.indexOf("val3");
        // - string
        vals[2] = data.attribute(2).addStringValue("This is a string!");
        // - date
        vals[3] = data.attribute(3).parseDate("2001-11-09");
        // - relational
        dataRel = new Instances(data.attribute(4).relation(), 0);
        // -- first instance
        valsRel = new double[2];
        valsRel[0] = Math.PI + 1;
        valsRel[1] = attValsRel.indexOf("val5.3");
        dataRel.add(new Instance(1.0, valsRel));
        // -- second instance
        valsRel = new double[2];
        valsRel[0] = Math.PI + 2;
        valsRel[1] = attValsRel.indexOf("val5.2");
        dataRel.add(new Instance(1.0, valsRel));
        vals[4] = data.attribute(4).addRelation(dataRel);
        // add
        data.add(new Instance(1.0, vals));

        // second instance
        vals = new double[data.numAttributes()];  // important: needs NEW array!
        // - numeric
        vals[0] = Math.E;
        // - nominal
        vals[1] = attVals.indexOf("val1");
        // - string
        vals[2] = data.attribute(2).addStringValue("And another one!");
        // - date
        vals[3] = data.attribute(3).parseDate("2000-12-01");
        // - relational
        dataRel = new Instances(data.attribute(4).relation(), 0);
        // -- first instance
        valsRel = new double[2];
        valsRel[0] = Math.E + 1;
        valsRel[1] = attValsRel.indexOf("val5.4");
        dataRel.add(new Instance(1.0, valsRel));
        // -- second instance
        valsRel = new double[2];
        valsRel[0] = Math.E + 2;
        valsRel[1] = attValsRel.indexOf("val5.1");
        dataRel.add(new Instance(1.0, valsRel));
        vals[4] = data.attribute(4).addRelation(dataRel);
        // add
        data.add(new Instance(1.0, vals));

        // 4. output data
        System.out.println("haha");
        System.out.println(data.toString());
        BufferedWriter out = new BufferedWriter(new FileWriter(fileNames[0]));
        out.write(data.toString());
        out.close();
*/
    }
}

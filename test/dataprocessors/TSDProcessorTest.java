/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataprocessors;

import java.util.HashMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import javafx.geometry.Point2D;
import static org.junit.Assert.*;

/**
 *
 * @author James
 */
public class TSDProcessorTest {

    public TSDProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of processString method, of class TSDProcessor. Should fail and pass
     * an Exception due to the wrong formatting.
     */
    @Test(expected = Exception.class)
    public void testProcessStringFail() throws Exception {
        String tsdString = "";
        TSDProcessor instance = new TSDProcessor();
        instance.processString(tsdString);
    }
    
     /**
     * Test of processString method, of class TSDProcessor. Should fail and pass
     * an Exception due to the wrong formatting.
     */
    @Test(expected = Exception.class)
    public void testProcessStringFailTwo() throws Exception {
        String tsdString = "dawd21d12";
        TSDProcessor instance = new TSDProcessor();
        instance.processString(tsdString);
    }


    /**
     * Should not fail because the given String is formatted correctly in tsd.
     * The hashmaps are equal since the keys and Points/Labels match up with
     * what is expected.
     */
    @Test
    public void testProcessStringSuccess() throws Exception {
        String tsdString = "@Instance1\tlabel2\t1,1\n";
        TSDProcessor instance = new TSDProcessor();
        instance.processString(tsdString);
        HashMap<String, String> labels = new HashMap();
        labels.put("@Instance1", "label2");
        HashMap<String, Point2D> location = new HashMap();
        Point2D point = new Point2D(Double.parseDouble("1"), Double.parseDouble("1"));
        location.put("@Instance1", point);
        assertEquals(instance.getDataLabels(), labels);
        assertEquals(instance.getDataPoints(), location);

    }

    /**
     * Should not fail because the given String is formatted correctly in tsd.
     * The hashmaps are equal since the keys and Points/Labels match up with
     * what is expected.
     */
    @Test
    public void testProcessStringSuccessTwo() throws Exception {
        String tsdString = "@Instance1\tlabel2\t1,1\n@Instance2\tlabel3\t2,2";
        TSDProcessor instance = new TSDProcessor();
        instance.processString(tsdString);
        HashMap<String, String> labels = new HashMap();
        labels.put("@Instance1", "label2");
        labels.put("@Instance2", "label3");
        HashMap<String, Point2D> location = new HashMap();
        Point2D point = new Point2D(Double.parseDouble("1"), Double.parseDouble("1"));
        Point2D point2 = new Point2D(Double.parseDouble("2"), Double.parseDouble("2"));
        location.put("@Instance1", point);
        location.put("@Instance2", point2);
        assertEquals(instance.getDataLabels(), labels);
        assertEquals(instance.getDataPoints(), location);

    }

}

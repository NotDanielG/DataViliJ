package dataprocessors;

import org.junit.Test;

import static org.junit.Assert.*;

public class TSDProcessorTest {

    @Test
    public void correctProcessString() throws Exception{
        String testString = "@A" + "\t" + "1" + "\t" + "2,2";
        TSDProcessor processor = new TSDProcessor();
        processor.processString(testString);
    }

    @Test(expected = TSDProcessor.InvalidDataNameException.class)
    public void incorrectProcessString() throws Exception{
        String testString = "dw@A" + "\t" + "1" + "\t" + "2,2";
        TSDProcessor processor = new TSDProcessor();

        processor.processString(testString);
    }

}
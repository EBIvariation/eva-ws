package uk.ac.ebi.eva.server;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Tom Smith
 */
public class UtilsTest {
    @Test
    public void createExclusionFieldString() throws Exception {
        List<String> exclude = new ArrayList<>();
        exclude.add("files");
        exclude.add("st");
        String expected = "{ 'files' : 0, 'st' : 0 }";

        assertEquals(expected, Utils.createExclusionFieldString(exclude));
    }

}
package io.github.warhead501.omniscience.api.data;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class DataKeyTest {

    @Test
    public void testGetter_Setter() {
        String key = "test";
        DataKey testKey = DataKey.of(key);
        assertEquals(key, testKey.toString());
    }
}

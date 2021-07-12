package com.knoban.atlas.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToolsTest {

    private static final String A = "AB C DEF";
    private static final String B = "A B C D E F ";

    @Test
    public void formatLore_LengthA1() {
        List<String> lore = Tools.formatLore(A, 1);
        assertEquals(3, lore.size());
    }

    @Test
    public void formatLore_ValueA1() {
        List<String> lore = Tools.formatLore(A, 1);
        assertEquals("AB", lore.get(0));
        assertEquals("C", lore.get(1));
        assertEquals("DEF", lore.get(2));
    }

    @Test
    public void formatLore_LengthA3() {
        List<String> lore = Tools.formatLore(A, 3);
        assertEquals(3, lore.size());
    }

    @Test
    public void formatLore_ValueA3() {
        List<String> lore = Tools.formatLore(A, 3);
        assertEquals("AB", lore.get(0));
        assertEquals("C", lore.get(1));
        assertEquals("DEF", lore.get(2));
    }

    @Test
    public void formatLore_LengthA4() {
        List<String> lore = Tools.formatLore(A, 4);
        assertEquals(2, lore.size());
    }

    @Test
    public void formatLore_ValueA4() {
        List<String> lore = Tools.formatLore(A, 4);
        assertEquals("AB C", lore.get(0));
        assertEquals("DEF", lore.get(1));
    }

    @Test
    public void formatLore_LengthA8() {
        List<String> lore = Tools.formatLore(A, 8);
        assertEquals(1, lore.size());
    }

    @Test
    public void formatLore_ValueA8() {
        List<String> lore = Tools.formatLore(A, 8);
        assertEquals("AB C DEF", lore.get(0));
    }

    @Test
    public void formatLore_LengthB1() {
        List<String> lore = Tools.formatLore(B, 1);
        assertEquals(6, lore.size());
    }

    @Test
    public void formatLore_ValueB1() {
        List<String> lore = Tools.formatLore(B, 1);
        assertEquals("A", lore.get(0));
        assertEquals("B", lore.get(1));
        assertEquals("C", lore.get(2));
        assertEquals("D", lore.get(3));
        assertEquals("E", lore.get(4));
        assertEquals("F", lore.get(5));
    }

    @Test
    public void formatLore_LengthB3() {
        List<String> lore = Tools.formatLore(B, 3);
        assertEquals(3, lore.size());
    }

    @Test
    public void formatLore_ValueB3() {
        List<String> lore = Tools.formatLore(B, 3);
        assertEquals("A B", lore.get(0));
        assertEquals("C D", lore.get(1));
        assertEquals("E F", lore.get(2));
    }

    @Test
    public void formatLore_LengthB5() {
        List<String> lore = Tools.formatLore(B, 5);
        assertEquals(2, lore.size());
    }

    @Test
    public void formatLore_ValueB5() {
        List<String> lore = Tools.formatLore(B, 5);
        assertEquals("A B C", lore.get(0));
        assertEquals("D E F", lore.get(1));
    }

    @Test
    public void formatLore_LengthB8() {
        List<String> lore = Tools.formatLore(B, 8);
        assertEquals(2, lore.size());
    }

    @Test
    public void formatLore_ValueB8() {
        List<String> lore = Tools.formatLore(B, 8);
        assertEquals("A B C D", lore.get(0));
        assertEquals("E F", lore.get(1));
    }
}

package com.knoban.atlas.commands;

import com.knoban.atlas.commands.Formation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormationTest {

    public static final Formation
            CASE_A = new Formation.FormationBuilder()
            .player()
            .list("test1", "test2", "test3")
            .number(0, 10, 0.5)
            .string("foo", "bar").build(),
            CASE_B = new Formation.FormationBuilder()
                    .string()
                    .number(-1, -1, -1)
                    .list()
                    .player().build(),
            CASE_C = new Formation.FormationBuilder().build(),
            CASE_K = new Formation.FormationBuilder()
                    .list("level")
                    .player()
                    .list("set", "add")
                    .number(-500, 500, 250).build();

    @Test
    public void specialCase_CaseK() {
        CASE_K.lastMatch(new String[]{"level", "kNoAPP", "set", "1000"});
    }

    @Test
    public void getArgType_CaseA() {
        assertEquals(Formation.INVALID, CASE_A.getArgType(-1));
        assertEquals(Formation.PLAYER, CASE_A.getArgType(0));
        assertEquals(Formation.LIST, CASE_A.getArgType(1));
        assertEquals(Formation.NUMBER, CASE_A.getArgType(2));
        assertEquals(Formation.STRING, CASE_A.getArgType(3));
        assertEquals(Formation.INVALID, CASE_A.getArgType(4));
    }

    @Test
    public void getArgType_CaseB() {
        assertEquals(Formation.INVALID, CASE_B.getArgType(-1));
        assertEquals(Formation.STRING, CASE_B.getArgType(0));
        assertEquals(Formation.NUMBER, CASE_B.getArgType(1));
        assertEquals(Formation.LIST, CASE_B.getArgType(2));
        assertEquals(Formation.PLAYER, CASE_B.getArgType(3));
        assertEquals(Formation.INVALID, CASE_B.getArgType(4));
    }

    @Test
    public void getArgType_CaseC() {
        assertEquals(Formation.INVALID, CASE_C.getArgType(-1));
        assertEquals(Formation.INVALID, CASE_C.getArgType(0));
        assertEquals(Formation.INVALID, CASE_C.getArgType(1));
    }
}
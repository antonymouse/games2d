package com.goldberg.games2d.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelTest {
    @Test
    public void testRead(){
        Level level = new Level();
        try {
            level.read("data/level1.txt");
        }catch (Throwable t){
            fail(t);
        }
    }

}
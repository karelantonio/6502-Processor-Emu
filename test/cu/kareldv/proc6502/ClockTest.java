/*
 * Copyright 2022 Karel Gonzalez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cu.kareldv.proc6502;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Karel
 */
public class ClockTest {
    
    public ClockTest() {
    }

    @Test
    public void testTicks() {
        System.out.println("sleepTime");
        
        long sleep = 10;
        
        Clock myClock = new Clock(sleep);
        
        long start = System.currentTimeMillis();
        myClock.consumeTicks(5);
        long end = System.currentTimeMillis();
        
        assertTrue(end-start > 50);
        System.out.println("OK, time="+(end-start));
        
    }

    @Test
    public void testKillSleep() {
        System.out.println("killSleep");
        
        long sleep = 100;
        
        Clock myClock = new Clock(sleep);
        
        Thread t = new Thread( () -> {
            long start = System.currentTimeMillis();
            myClock.consumeTicks(10);
            long end = System.currentTimeMillis();
            assertTrue(end-start<=200);
            System.out.println("OK, time="+(end-start));
        });
        
        t.start();
        
        myClock.consumeTick();
        myClock.killSleep();
        
        try {
            t.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
}

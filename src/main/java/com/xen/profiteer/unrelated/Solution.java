package com.xen.profiteer.unrelated;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Solution {

    public static void main(String[] args) {
        Solution solution = new Solution();
        System.out.println(solution.solution(new int[]{1, 3, 3}, 7, 1, 1));
    }

    public int solution(int[] A, int X, int Y, int Z) {

        int time = 0; // Thought about making time a long because # of cars * max capacity is sort of large,
        // but then I noticed X+Y+Z fits in an int, and that's really our upper limit.
        Pumps pumps = new Pumps(X, Y, Z);

        // For every car, we'll get the time it waited at the front of the line
        // until the next available pump got released.
        // The solution is the time it took for the last car to get to an available pump.
        for (int req : A) {
            int timeWaited = pumps.getPump(req, time);

            if (timeWaited == -1) { // Car got stuck, idiot driver won't clear queue. Cars behind him are honking.
                return -1;
            }

            time += timeWaited;
        }

        return time;

    }

    // In a proper environment this will be a Lombok @Data class with getters and setters
    private class Pump {
        public int fuel;
        public int startedPumpingTime = 0;
        public int pumpingAmount = 0;

        public Pump(int fuel) {
            this.fuel = fuel;
        }

        public int timeUntilFree(int currentTime) {
            int result = startedPumpingTime + pumpingAmount - currentTime;
            return result > 0 ? result : 0;
        }
    }

    private class Pumps {
        private List<Pump> pumps;

        public Pumps(int... capacities) {
            pumps = Arrays.stream(capacities).boxed().map(Pump::new).collect(Collectors.toList());
        }

        // This method will return the time this car had to wait until a pump was released
        // It will also pick the useful pump (with enough fuel) that will release the fastest,
        // and update the pump with the new information (time, capacity etc.)
        public int getPump(int req, int currentTime) {
            Pump nextPump = null;

            for (Pump p : pumps) {
                // Haven't found a pump yet, or we can find a pump that releases earlier
                if (p.fuel >= req &&
                        (nextPump == null || p.timeUntilFree(currentTime) < nextPump.timeUntilFree(currentTime))) {
                    nextPump = p;
                    if (nextPump.timeUntilFree(currentTime) == 0) {
                        break;
                    }
                }
            }
            // No pumps will ever have capacity (aka get out of the line, jackass)
            if (nextPump == null) {
                return -1;
            }

            int waitTime = nextPump.timeUntilFree(currentTime);
            // The fix was here. The currentTime I sent over was not updated yet.
            // There's a better way of doing this more cleanly, but this one only requires
            // one line to be changed
            assignPump(nextPump, req, currentTime + waitTime);

            return waitTime;
        }

        public void assignPump(Pump pump, int req, int currentTime) {
            pump.fuel -= req;
            pump.startedPumpingTime = currentTime;
            pump.pumpingAmount = req;
        }
    }
}

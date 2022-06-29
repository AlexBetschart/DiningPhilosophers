import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.*;
import java.util.Random;
import java.lang.Math;

/**
 * @Author: Alex Betschart
 * A5Q2
 */
public class PhilosopherDFSF {
    static final int PHILOSOPHER_NUM = 5;
    static final int TIME_TO_HUNGRY = 50000;
    static final int TIME_TO_STARVATION = 100000;


    static Philosopher[] philosophers = new Philosopher[PHILOSOPHER_NUM];
    static Chopstick[] chopsticks = new Chopstick[PHILOSOPHER_NUM];
    static int idCounter =0;

    public static void main(String[] args) {
        for (int i = 0; i < PHILOSOPHER_NUM; i++) {
            chopsticks[i] = new Chopstick();
            philosophers[i] = new Philosopher(i, chopsticks[i], chopsticks[(i + 1) % PHILOSOPHER_NUM]);
        }
        for (int i = 0; i < PHILOSOPHER_NUM; i++) {
            philosophers[i] = new Philosopher(i, chopsticks[i], chopsticks[(i + 1) % PHILOSOPHER_NUM]);
            philosophers[i].start();
        }
    }

    static class Chopstick {
        public Semaphore chopstickMutex = new Semaphore(1);

        /**
         * acquires mutex
         */
        void grab() {
            try {
                chopstickMutex.acquire();
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        /**
         * releases mutex
         */
        void release() {
            chopstickMutex.release();
        }

        /**
         * @return true if semaphore is open
         */
        boolean isFree() {
            return chopstickMutex.availablePermits() > 0;
        }
    }

    static class Philosopher extends Thread {
        public int number;
        public Chopstick leftChopstick;
        public Chopstick rightChopstick;
        public int timeSinceEaten;

        Philosopher(int num, Chopstick left, Chopstick right) {
            number = num;
            leftChopstick = left;
            rightChopstick = right;
            timeSinceEaten = 0;
        }

        /**
         * Try to pick up left and right chopstick, if either is already picked up, put any held chopsticks down
         * and think for a random time between 1-10 seconds
         *
         */
        public void run() {

            //Let philosophers i and i+2 eat, others wait.
            //Doesn't work exactly right because of context switching, but each philosopher eats roughly the same amount
            // eg after 16 minute of running each philosopher ate 40 times +- 2
            while (true) {
                if (number == idCounter % 5 || number == (idCounter+2) % 5){
                    grabBothChopsticks();
                    eat();
                    releaseBothChopsticks();
                    think();
                }
                else{
                    System.out.println("Not P" + (number+1) + "'s turn to consume.");
                    think();
                }
                if (number == 0){
                    idCounter++;
                }
            }
        }

        /**
         * Think for random time. Increment timeSinceEaten by thinking time.
         */
        void think() {
            try {
                int sleepTime = ThreadLocalRandom.current().nextInt(1000, 10000);
                System.out.println("P" + (number + 1) + " thinks for " + sleepTime + "ms");
                Thread.sleep(sleepTime);
                timeSinceEaten+=sleepTime;
                if (timeSinceEaten > TIME_TO_HUNGRY){
                    System.out.println("P" + (number+1) + " is hungry");
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        /**
         * Eat for (1-10) seconds
         * Reset timeSinceEaten counter to zero
         */
        void eat() {
            try {
                int sleepTime = ThreadLocalRandom.current().nextInt(1000, 10000);
                System.out.println("P" + (number + 1) + " eats for " + sleepTime + "ms");
                Thread.sleep(sleepTime);
                timeSinceEaten=0;
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        //If both chops sticks are available, grab them.
        Semaphore grabMutex = new Semaphore(1);
        void grabBothChopsticks(){
            try {
                grabMutex.acquire();
                if (leftChopstick.isFree() && rightChopstick.isFree()) {
                    leftChopstick.grab();
                    System.out.println("P" + (number + 1) + " grabs left chopstick");
                    rightChopstick.grab();
                    System.out.println("P" + (number + 1) + " grabs right chopstick");
                    System.out.println("P" + (number + 1) + " acquired both chopsticks");
                }
                grabMutex.release();
            } catch (Exception e){
                e.printStackTrace(System.out);
            }
        }

        /**
         * Release both chopsticks after eating
         */
        void releaseBothChopsticks(){
            leftChopstick.release();
            System.out.println("P" + (number + 1) + " releases left chopstick");
            rightChopstick.release();
            System.out.println("P" + (number + 1) + " releases right chopstick");
        }
    }
}

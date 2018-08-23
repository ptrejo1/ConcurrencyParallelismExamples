/* 
	Example of semaphores
	More threads than locks 
	Simulation ski rental
*/

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.Random;

final class SkiPair {
	private String name;
	private boolean inUse;

	public SkiPair(String name, boolean inUse) {
		setName(name);
		setinUse(inUse);
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String isInUse() { return inUse; }
	public void setInUse(boolean inUse) { this.inUse = inUse; }
}

final class SkisForRent {
	public static final int MaxSkiPairs = 150;
	public static final int MinInStock = 5;
	private final Semaphore semaphore = new Semaphore(MaxSkiPairs - MinInStock);
	private SkiPair[] inventory;

	SkisForRent() {
		inventory = new SkiPair[MaxSkiPairs];
		for (int i = 0; i < MaxSkiPairs; i++) inventory[i] = new SkiPair("skiPair-" + i, false);
    }

	public SkiPair rentSkiPair() throws Exception {
		semaphore.acquire();
		return getSkiPair();
	}

	public void returnSkiPair(SkiPair sp) {
		if (isReturnable(sp)) semaphore.release();	// no longer used by thread
    }

    protected synchronized boolean getSkiPair (SkiPair sp) {
    	for (SkiPair sp : inventory) {
    		if (!sp.isInUse()) {
    		sp.setInUse(true);
    		return sp;
    		}
    	}
    	return null;
    }

    protected synchronized boolean isReturnable (SkiPair sp) {
    	if (sp.isInUse()) {
    		sp.setInUse(false);
    		return true;
    	}
    	return false;
    }
}

public class Semaphores {
	public static void main(String[] args) {
		Random rand = new Random();
		final SkisForRent sfr = new SkisForRent();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				String name = Thread.currentThread().getName();
				try {
					while (true) {
						SkiPair sp = sfr.rentSkiPair();
						System.out.printf("%s renting %s%n", name, sp.getName());
						Thread.sleep(rand.nextInt(2000));
						System.out.printf("%s returning %s%n", name, sp.getName());
			    		sfr.returnSkiPair(sp);
					}
				} catch (Exception e) { System.err.println(e); }
			}
		};

		ExecutorService[] executors = new ExecutorService[SkisForRent.MaxSkiPairs + 1];
		for (ExecutorService executor : executors) {
			executor = Executors.newSingleThreadExecutor();		// pools of single threads
			executor.execute(runnable);
		}
	}
}
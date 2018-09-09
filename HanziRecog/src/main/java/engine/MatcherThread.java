/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-swingui.uicommon.org
 *
 *  Refactorized by I-Tang HIU
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package engine;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Refactorized by I-Tang HIU August 2018
 *
 * MatcherThread is Thread for running character match comparisons via
 * its given StrokesMatcher.  It reports its results to the given
 * IResultsHandler.  This used to be tied together with the StrokesMatcher
 * implementation, but tying the Thread functionality to the comparison
 * computation was suboptimal.
 * <p>
 * This is written with the expectation that only one comparsion
 * need by done at any given time, and that it's more efficient
 * to use the same Thread instance rather than allocating a new
 * Thread all the time.  So you instantiate one of these and start it up.
 * After which you instruct it to run comparisons by invoking the
 * setStrokeMatcher method.  When it's done, it will go to sleep
 * waiting for the next comparison.  Since it's always alive,
 * you probably want to run this Thread as a daemon.
 */
public class MatcherThread extends Thread {

  // All computation is performed in the given StrokesMatcher
  private StrokesMatcher strokesMatcher;
  private Object matcherLock = new Object();
  private Set<Consumer<Character[]>> resultsHandlers = new LinkedHashSet();// handlers to report the results to
  private boolean running = true;

  /**
   * Init the Thread that does comparisons.
   * The thread sits idle waiting until it needs to do a comparison.
   */
  public MatcherThread() {

    setDaemon(true); // no sense in holding up app shutdown, so make it a daemon Thread.
    setPriority(Thread.NORM_PRIORITY);// NORM_PRIORITY so it doesn't compete with event dispatch
    start();// Start it up.  It will immediately go to sleep waiting for a comparison.
  }

  /**
   * @param resultsHandler a new IResultsHandler to report results to
   */
  public void addResultsHandler(Consumer<Character[]> resultsHandler) {

    synchronized (resultsHandlers) {
      resultsHandlers.add(resultsHandler);
    }
  }

  /**
   * @param resultsHandler a IResultsHandler that we should not report results to any longer
   * @return true if the handler was currently registered, false otherwise
   */
  public boolean removeResultsHandler(Consumer<Character[]> resultsHandler) {

    synchronized (resultsHandlers) {
      return resultsHandlers.remove(resultsHandler);
    }
  }

  /**
   * If you need to kill the Thread for real once and for all invoke this.
   * (Note don't invoke this to stop a running comparison, just set a new
   * StrokesMatcher instead).  Instead of using this method, it is recommended
   * to just make this a Daemon Thread on instantiation, just leave this Thread
   * running for the lifetime of the app.  The Thread will remain asleep when
   * idle.
   */
  public void kill() {

    running = false;
    synchronized (this.matcherLock) {
      this.matcherLock.notify();
    }
  }

  public void run() {

    while (running) {
      // Just process forever until the Thread is done.
      StrokesMatcher matcher = null;
      synchronized (this.matcherLock) {
        try {
          if (strokesMatcher == null)
            matcherLock.wait();
        }
        catch (InterruptedException ie) {
          // just loop again, but we don't expect this
        }

        if (strokesMatcher != null) {
          matcher = strokesMatcher;
          // Now that we're running it, we only need the local reference.
          // We null out the matcher so that the next time through the loop
          // a null matcher can signal it's ok for the Thread to sleep.
          strokesMatcher = null;
        }
      }
      Character[] results = null;
      if (matcher == null)
        return;
      results = matcher.doMatching();
      // null results mean computation was prematurely aborted (replaced by another MatchRunner.
      // We don't update in this case, just finish.
      if (results != null)
        synchronized (resultsHandlers) {
          for (Consumer<Character[]> handler : resultsHandlers)
            handler.accept(results);
        }
    }
  }

  /**
   * Sets the StrokeMatcher defining the parameters of the comparison
   * that the MatcherThread should run.  Invoking this method kicks
   * off a comparison in the Thread instance.
   * <p>
   * If there is currently a comparison running in the Thread when
   * this is invoked, that comparison will be stopped and its results
   * discarded, and the Thread will begin processing the new StrokesMatcher
   * instance instead.
   *
   * @param strokesMatcher
   */
  public void setStrokesMatcher(StrokesMatcher strokesMatcher) {

    synchronized (matcherLock) {
      if (this.strokesMatcher != null)
        this.strokesMatcher.stop();
      this.strokesMatcher = strokesMatcher;
      matcherLock.notify();
    }
  }


}

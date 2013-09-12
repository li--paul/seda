package seda.sandStorm.core;

import seda.sandStorm.api.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Collection;
import java.util.ArrayList;

/**
 * This class maps the BlockingQueue interface onto
 * QueueIF so that they can work together.
 *
 * @author Jean Morissette
 */
public class QueueAdapter implements QueueIF {

  BlockingQueue queue;

  public QueueAdapter(BlockingQueue queue) {
    this.queue = queue;
  }

  public void enqueue(QueueElementIF element) throws SinkException {
    try {
      queue.add(element);
    } catch(Exception e) { throw new SinkFullException(); }
  }

  public boolean enqueue_lossy(QueueElementIF element) {
    return queue.offer(element);
  }

  // TODO jm7: This method isn't atomic and thus not conform to the specification.
  public void enqueue_many(QueueElementIF[] elements) throws SinkException {
    for (int i = 0; i < elements.length; i++) {
      queue.offer(elements[i]);
    }
  }

  public Object enqueue_prepare(QueueElementIF[] elements) throws SinkException {
    throw new UnsupportedOperationException();
  }

  public void enqueue_commit(Object enqueue_key) {
    throw new UnsupportedOperationException();
  }

  public void enqueue_abort(Object enqueue_key) {
    throw new UnsupportedOperationException();
  }

  public void setEnqueuePredicate(EnqueuePredicateIF pred) {
    System.out.println("ERROR: QueueAdapter.setEnqueuePredicate not supported");
  }

  public EnqueuePredicateIF getEnqueuePredicate() {
    System.out.println("ERROR: QueueAdapter.getEnqueuePredicate not supported");
    return null;
  }


  //--- SourceIF


  public QueueElementIF dequeue() {
    return (QueueElementIF) queue.poll();
  }

  public QueueElementIF[] dequeue_all() {
    Collection<QueueElementIF> c = new ArrayList<QueueElementIF>();
    queue.drainTo(c);
    return (QueueElementIF[]) c.toArray();
  }

  public int dequeue_all(Collection c) {
    return queue.drainTo(c);
  }

  public QueueElementIF[] dequeue(int num) {
    Collection c = new ArrayList();
    dequeue(c, num);
    return (QueueElementIF[]) c.toArray(new QueueElementIF[c.size()]);
  }

  public int dequeue(Collection c, int num) {
    return queue.drainTo(c, num);
  }

  public QueueElementIF blocking_dequeue(int timeout_millis) {
    try {
      if (timeout_millis == 0) // non-blocking
        return (QueueElementIF) queue.poll();
      else if (timeout_millis < 0) // block forever
        return (QueueElementIF) queue.take();
      else
        return (QueueElementIF) queue.poll(timeout_millis, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  public QueueElementIF[] blocking_dequeue_all(int timeout_millis) {
    Collection c = new ArrayList();
    blocking_dequeue_all(c, timeout_millis);
    return (QueueElementIF[]) c.toArray(new QueueElementIF[c.size()]);
  }

  public int blocking_dequeue_all(Collection c, int timeout_millis) {
    return blocking_dequeue(c, timeout_millis, Integer.MAX_VALUE);
  }

  public QueueElementIF[] blocking_dequeue(int timeout_millis, int num) {
    Collection c = new ArrayList();
    blocking_dequeue(c, timeout_millis, num);
    return (QueueElementIF[]) c.toArray(new QueueElementIF[c.size()]);
  }

  public int blocking_dequeue(Collection c, int timeout_millis, int num) {
    int count = 0;
    try {
      if (timeout_millis == 0) // don't wait
        count = queue.drainTo(c, num);
      else if (timeout_millis < 0) { // wait forever
        c.add(queue.take());
        count = queue.drainTo(c, num-1) + 1;
      }
      else {
        Object o = queue.poll(timeout_millis, TimeUnit.MILLISECONDS);
        if (o != null) {
          c.add(o);
          count = queue.drainTo(c, num-1) + 1;
        } else {
          count = queue.drainTo(c, num);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return count;
  }

  public int size() {
    return queue.size();
  }

  public int profileSize() {
    return queue.size();
  }

}

package Structures;

import java.util.ArrayDeque;

// represents a queue
public class MQueue<T> implements ICollection<T> {
  private ArrayDeque<T> contents;

  public MQueue() {
    this.contents = new ArrayDeque<T>();
  }

  // checks if the queue is empty
  public boolean isEmpty() {
    return contents.isEmpty();
  }

  // adds an item to the queue
  public void add(T item) {
    contents.add(item);
  }

  // returns the first item in the queue and removes it
  public T remove() {
    return contents.remove();
  }
}

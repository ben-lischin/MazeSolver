package Structures;

import java.util.Stack;

// represents a stack
public class MStack<T> implements ICollection<T> {
  private Stack<T> contents;

  public MStack() {
    this.contents = new Stack<T>();
  }

  // checks if the stack is empty
  public boolean isEmpty() {
    return contents.isEmpty();
  }

  // adds an item to the stack
  public void add(T item) {
    contents.push(item);
  }

  // returns the first item in the stack and removes it
  public T remove() {
    return contents.pop();
  }
}
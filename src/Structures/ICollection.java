package Structures;

// interface for queue and stack, used to represent a collection of data
public interface ICollection<T> {
  // checks if the collection is empty
  boolean isEmpty();

  // adds an item to the end of a collection
  void add(T item);

  // returns the first item in the collection and removes it
  T remove();
}

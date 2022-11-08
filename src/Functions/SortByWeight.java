package Functions;

import java.util.Comparator;

import Structures.Edge;

// comparator used for sorting edges by weight
public class SortByWeight implements Comparator<Edge> {
  // compares two edges according to their weight
  public int compare(Edge edge1, Edge edge2) {
    return edge1.weight - edge2.weight;
  }
}

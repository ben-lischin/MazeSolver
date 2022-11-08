package Functions;

import java.util.ArrayList;
import java.util.HashMap;

import Structures.Edge;
import Structures.Vertex;
import javalib.worldimages.Posn;

// used to represent all information for the union find algorithm
public class UnionFind {
  private HashMap<Posn, Posn> representatives = new HashMap<Posn, Posn>();
  private ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
  private ArrayList<Edge> worklist = new ArrayList<Edge>();

  public UnionFind() {
  }

  // checks if there is more than one tree with the hashmap
  // helper for minSpanTree
  private boolean moreThanOneTree() {
    int x = 0;
    for (Posn p : this.representatives.keySet()) {
      if (this.representatives.get(p).equals(p)) {
        x++;
        if (x > 1) {
          return true;
        }
      }
    }
    return false;
  }

  // generates the minimum spanning tree
  public ArrayList<Edge> minSpanTree(ArrayList<Vertex> board) {
    // build representatives hashmap and worklist list
    for (Vertex v : board) {
      this.union(this.representatives, v.p, v.p);
      for (Edge e : v.outEdges) {
        this.worklist.add(e);
      }
      this.worklist.sort(new SortByWeight());
    }
    // run until there is one tree left. that is the minimum spanning tree
    while (this.moreThanOneTree()) {
      Edge v = this.worklist.get(0);
      Posn x = v.from.p;
      Posn y = v.to.p;

      while (!x.equals(this.representatives.get(x))) {
        x = this.representatives.get(x);
      }

      while (!y.equals(this.representatives.get(y))) {
        y = this.representatives.get(y);
      }

      if (x.equals(y)) {
        this.worklist.remove(0);
      } else {
        this.edgesInTree.add(v);
        this.union(this.representatives, x, y);
      }
    }
    return this.edgesInTree;
  }

  // makes an entry in the hashmap, or overrides an existing entry with that key
  private void union(HashMap<Posn, Posn> r, Posn p1, Posn p2) {
    r.put(p1, p2);
  }

  //getter for edgesInTree
  public ArrayList<Edge> getEdgesInTree() {
    return this.edgesInTree;
  }
}
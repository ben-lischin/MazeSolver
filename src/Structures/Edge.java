package Structures;

// represents an Edge
public class Edge {
  public Vertex from;
  public Vertex to;
  public int weight;
  
  public Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  @Override
  // checks equality of two edges
  // direction of an edge is not important
  // (two edges are considered equal even if their to and from fields are swapped)
  public boolean equals(Object o) {
    if (o instanceof Edge) {
      Edge e = (Edge) o;
      return (this.from.p.equals(e.from.p) && this.to.p.equals(e.to.p))
              || (this.to.p.equals(e.from.p) && this.from.p.equals(e.to.p));
    } else {
      return false;
    }
  }

  @Override
  // generates hashcode for an edge
  public int hashCode() {
    return this.from.p.x * 1000000 - this.from.p.x * 13 + this.to.p.x * 90000 + this.to.p.y * 2;
  }
}

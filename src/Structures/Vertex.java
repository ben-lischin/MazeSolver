package Structures;

import java.awt.*;
import java.util.ArrayList;

import javalib.worldimages.Posn;

// represents a Vertex
public class Vertex {
  public Posn p;
  public Color color;
  public ArrayList<Edge> outEdges = new ArrayList<Edge>();
  public ArrayList<Edge> outEdgesInTree = new ArrayList<Edge>();
  public Boolean visited = false;

  public Vertex(Posn p, Color color) {
    this.p = p;
    this.color = color;
  }

  // adds a vertex to this vertex's outEdges list
  public void addEdge(Vertex v, int r) {
    Edge e = new Edge(this, v, r);
    this.outEdges.add(e);
  }

  @Override
  // checks equality of two vertices
  public boolean equals(Object o) {
    if (o instanceof Vertex) {
      Vertex v = (Vertex) o;
      return this.p.equals(v.p) && this.color.equals(v.color);

    } else {
      return false;
    }
  }

  @Override
  // generates hashcode for a vertex
  public int hashCode() {
    return this.p.x * 100000000 + this.p.y * 3;
  }
}

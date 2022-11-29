package Maze;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import Functions.UnionFind;
import Structures.Edge;
import Structures.ICollection;
import Structures.MQueue;
import Structures.MStack;
import Structures.Vertex;
import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.LineImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;

//Represents the maze
// handles constructing and solving it
public class Maze extends World {
  public int r;
  public int c;
  public static int CELL_SIZE = 11;
  private UnionFind uf;
  private WorldScene scene;
  private ArrayList<Vertex> board;
  private ICollection<Vertex> worklist;
  private Boolean searching;
  private Boolean continueSearch;
  private Boolean solved;
  private HashMap<Vertex, Vertex> toFrom;
  private ArrayList<Vertex> shortPath;
  private String searchType;


  public Maze(int r, int c) {
    this.r = r;
    this.c = c;

    this.initialize();
  }

  // initializes the maze: sets fields, creates maze
  private void initialize() {
    this.uf = new UnionFind();
    this.board = new ArrayList<Vertex>();
    this.worklist = new MQueue<Vertex>();
    this.searching = false;
    this.continueSearch = false;
    this.solved = false;
    this.toFrom = new HashMap<Vertex, Vertex>();
    this.shortPath = new ArrayList<Vertex>();
    this.searchType = "";

    this.scene = new WorldScene(Maze.CELL_SIZE * this.c, Maze.CELL_SIZE * this.r);

    this.createBoard();
    this.uf.minSpanTree(this.board);
    this.filterOutEdges();
    this.drawCells();
    this.drawWalls();
  }

  // generates all the vertices for the maze stored in a list of vertices (board)
  // also adds edge connections to each vertex's outEdge list
  private void createBoard() {
    for (int i = 0; i < this.r; i++) {
      for (int j = 0; j < this.c; j++) {
        // if top left, green
        if (i == 0 && j == 0) {
          this.board.add(new Vertex(new Posn(j, i), Color.GREEN));
        }
        // if bottom right, magenta
        else if (i == this.r - 1 && j == this.c - 1) {
          this.board.add(new Vertex(new Posn(j, i), Color.MAGENTA));
        }
        // else, gray
        else {
          this.board.add(new Vertex(new Posn(j, i), Color.LIGHT_GRAY));
        }
      }
    }

    // add edge connections to outedges
    for (int i = 0; i < this.board.size(); i++) {
      Vertex v = this.board.get(i);

      // add edge with left vertex of v
      if (v.p.x != 0) {
        v.addEdge(this.board.get(i - 1), new Random().nextInt(100));
      }

      // add edge with right node of v
      if (v.p.x != this.c - 1) {
        v.addEdge(this.board.get(i + 1), new Random().nextInt(100));
      }

      // add edge with below node of v
      if (v.p.y != this.r - 1) {
        v.addEdge(this.board.get(i + this.c), new Random().nextInt(100));
      }

      // add edge with top node of v
      if (v.p.y != 0) {
        v.addEdge(this.board.get(i - this.c), new Random().nextInt(100));
      }
    }
  }

  // filters the list of out edges from next to only
  // keep those that are in the minimum spanning tree
  private void filterOutEdges() {
    for (Vertex v : this.board) {
      for (Edge e : v.outEdges) {
        if (this.uf.getEdgesInTree().contains(e)) {
          v.outEdgesInTree.add(e);
        }
      }
    }
  }

  // draws the cells
  private void drawCells() {
    RectangleImage cell;
    for (Vertex v : this.board) {
      cell = new RectangleImage(Maze.CELL_SIZE, Maze.CELL_SIZE, OutlineMode.SOLID, v.color);
      scene.placeImageXY(cell, v.p.x * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2) + 1,
              v.p.y * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2) + 1);
    }
  }

  // draws the walls where there is not an edge in the minimum spanning tree
  private void drawWalls() {
    LineImage hLine; // horizontal line
    LineImage vLine; // vertical line

    // go row by row, check bottom connections. if no connection, draw a
    // horizontal line on the bottom
    for (int i = 0; i < this.r - 1; i++) {
      for (int j = 0; j < this.c; j++) {
        if (!this.uf.getEdgesInTree().contains(new Edge(new Vertex(new Posn(j, i), Color.WHITE),
                new Vertex(new Posn(j, i + 1), Color.WHITE), 0))) {
          hLine = new LineImage(new Posn(Maze.CELL_SIZE, 0), Color.BLACK);
          scene.placeImageXY(hLine, (Maze.CELL_SIZE * j + (Maze.CELL_SIZE / 2) + 1),
                  (Maze.CELL_SIZE * (i + 1)));
          // place hline below
        }
      }
    }

    // go column by column, check right connection, if no connection, draw a
    // vertical line on te right
    for (int i = 0; i < this.r; i++) {
      for (int j = 0; j < this.c - 1; j++) {
        if (!this.uf.getEdgesInTree().contains(new Edge(new Vertex(new Posn(j, i), Color.WHITE),
                new Vertex(new Posn(j + 1, i), Color.WHITE), 0))) {
          vLine = new LineImage(new Posn(0, Maze.CELL_SIZE), Color.BLACK);
          scene.placeImageXY(vLine, (Maze.CELL_SIZE * (j + 1)),
                  (Maze.CELL_SIZE * i + (Maze.CELL_SIZE / 2) + 1));
          // place vline right
        }
      }
    }
  }

  @Override
  // draws the scene
  public WorldScene makeScene() {
    return scene;
  }

  @Override
  // reacts to a user pressing the "b", "d", " ", or "r" keys,
  // setting up the draw methods to show a bfs or dfs,
  // to pause/resume the search, or to reset the maze, respectively
  public void onKeyEvent(String key) {
    if (key.equals("b") && searching == false) {
      this.searchType = key;
      this.continueSearch = true;
      this.searching = true;
      this.bfs();
    } else if (key.equals("d") && searching == false) {
      this.searchType = key;
      this.continueSearch = true;
      this.searching = true;
      this.dfs();
    } else if (key.equals(" ") && this.solved == false) {
      this.continueSearch = !this.continueSearch;
    } else if (key.equals("r")) {
      initialize();
    }
  }

  // searches the maze according to a breadth first search
  // uses a queue input
  private void bfs() {
    Vertex from = this.board.get(0);
    this.worklist = new MQueue<Vertex>();
    // initialize the worklist with the starting (from) vertex
    this.worklist.add(from);
  }

  // searches the maze according to a depth first search
  // uses a stack input
  private void dfs() {
    Vertex from = this.board.get(0);
    this.worklist = new MStack<Vertex>();
    // initialize the worklist with the starting (from) vertex
    this.worklist.add(from);
  }

  // helper for bfs and dfs methods
  // mutates the visited list of vertices according to
  // the desired search method
  private void searchHelper(ICollection<Vertex> worklist) {
    Vertex from = this.board.get(0);
    Vertex to = this.board.get(this.board.size() - 1);
    // run while worklist is NOT empty
    if (!worklist.isEmpty()) {
      Vertex next = worklist.remove();
      if (next.equals(to)) { // reached the end
        // reset worklist
        this.worklist = new MQueue<Vertex>();
        // make final cell colored
        scene.placeImageXY(new RectangleImage(Maze.CELL_SIZE - 1, Maze.CELL_SIZE - 1,
                        OutlineMode.SOLID, new Color(Math.max(225, 255-(int)(255 * ((double)next.p.x / (double)this.c))),140, Math.max(0, 255-(int)(225 * ((double)next.p.y / (double)this.r))))),
                Maze.CELL_SIZE * next.p.x + (Maze.CELL_SIZE / 2) + 1,
                Maze.CELL_SIZE * next.p.y + (Maze.CELL_SIZE / 2) + 1);
        // stop the search
        this.continueSearch = false;

        // make the path to solve the maze and draw it
        this.makeShortPath(to, from);

      } else if (next.visited == true) {
        // do nothing, we have already been here
      } else {
        // place dynamically colored cells along the working path
        scene.placeImageXY(new RectangleImage(Maze.CELL_SIZE - 1, Maze.CELL_SIZE - 1,
                        OutlineMode.SOLID, new Color(255-(int)(50 * ((double)next.p.x / (double)this.c)),140, 255-(int)(225 * ((double)next.p.y / (double)this.r)))),
                Maze.CELL_SIZE * next.p.x + (Maze.CELL_SIZE / 2) + 1,
                Maze.CELL_SIZE * next.p.y + (Maze.CELL_SIZE / 2) + 1);

        // add all the neighbors of next to the worklist so they will be processed
        // construct the hashMap
        for (Edge e : next.outEdgesInTree) {
          // only add if they have not been visited
          if (e.to.visited == false) {
            worklist.add(e.to);
            this.toFrom.put(e.to, next);
          }
        }
        // make next visited, since we just dealt with it (visited it)
        next.visited = true;
      }
    }
  }

  // draws the shortest path to complete the maze
  private void animateShortPath() {
    RectangleImage cell = new RectangleImage(Maze.CELL_SIZE - 3, Maze.CELL_SIZE - 3,
            OutlineMode.SOLID, Color.WHITE);
    // draw first
    Vertex v = this.shortPath.get(0);
    scene.placeImageXY(cell, v.p.x * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2) + 1,
            v.p.y * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2) + 1);
    // remove first from the shortPath list
    this.shortPath.remove(0);

  }

  // makes the shortest path to complete the maze
  private void makeShortPath(Vertex last, Vertex start) {
    if (!last.equals(start)) {
      this.shortPath.add(last);
      makeShortPath(this.toFrom.get(last), start);
    } else {
      this.shortPath.add(start);
    }
  }

  @Override
  // runs on tick, checks if it should still run the search
  public void onTick() {
    if (this.continueSearch) {
      this.searchHelper(this.worklist);
    }
    if (!this.shortPath.isEmpty()) {
      this.solved = true;
      this.animateShortPath();
    }
  }
}



// examples
class ExamplesMaze {

  ICollection<String> queue1 = new MQueue<String>();
  ICollection<String> stack1 = new MStack<String>();

  Maze maze1 = new Maze(60, 100);

  Maze mazeSeed = new Maze(60, 100);

  Vertex v1 = new Vertex(new Posn(4, 4), Color.WHITE);
  Vertex v1Right = new Vertex(new Posn(5, 4), Color.WHITE);
  Vertex v1Left = new Vertex(new Posn(3, 4), Color.WHITE);
  Vertex v1Top = new Vertex(new Posn(4, 3), Color.WHITE);
  Vertex v1Bottom = new Vertex(new Posn(4, 5), Color.WHITE);

  Edge edge1 = new Edge(this.v1, this.v1Bottom, 5);
  Edge edge2 = new Edge(this.v1, this.v1Top, 6);
  Edge edge3 = new Edge(this.v1, this.v1Left, 69);
  Edge edge4 = new Edge(this.v1, this.v1Right, 420);

  Maze mazeSeed2 = new Maze(4, 4);
  LineImage vwall = new LineImage(new Posn(0, Maze.CELL_SIZE), Color.BLACK);
  LineImage hwall = new LineImage(new Posn(Maze.CELL_SIZE, 0), Color.BLACK);
  RectangleImage cellStart = new RectangleImage(Maze.CELL_SIZE,
      Maze.CELL_SIZE, OutlineMode.SOLID, Color.GREEN);
  RectangleImage cellOther = new RectangleImage(Maze.CELL_SIZE,
      Maze.CELL_SIZE, OutlineMode.SOLID, Color.LIGHT_GRAY);
  RectangleImage cellEnd = new RectangleImage(Maze.CELL_SIZE,
      Maze.CELL_SIZE, OutlineMode.SOLID, Color.MAGENTA);

  Maze maze2x2Seed = new Maze(2, 2);
  // cellStart, cellOther, cellEnd
  // vwall (x1)
  Vertex tl = new Vertex(new Posn(0, 0), Color.GREEN);
  Vertex tr = new Vertex(new Posn(0, 1), Color.LIGHT_GRAY);
  Vertex bl = new Vertex(new Posn(1, 0), Color.LIGHT_GRAY);
  Vertex br = new Vertex(new Posn(1, 1), Color.MAGENTA);
  Edge e01 = new Edge(tl, tr, 5);
  Edge e02 = new Edge(tl, bl, 5);
  Edge e13 = new Edge(tr, br, 5);
  Edge e23 = new Edge(bl, br, 5);
  ArrayList<Edge> el0 = new ArrayList<Edge>();
  ArrayList<Edge> el1 = new ArrayList<Edge>();
  ArrayList<Edge> el2 = new ArrayList<Edge>();
  ArrayList<Edge> el3 = new ArrayList<Edge>();

  void initialData() {
    queue1 = new MQueue<String>();
    stack1 = new MStack<String>();
    maze1 = new Maze(60, 100);
    mazeSeed = new Maze(60, 100);
    v1 = new Vertex(new Posn(4, 4), Color.WHITE);
    v1Right = new Vertex(new Posn(5, 4), Color.WHITE);
    v1Left = new Vertex(new Posn(3, 4), Color.WHITE);
    v1Top = new Vertex(new Posn(4, 3), Color.WHITE);
    v1Bottom = new Vertex(new Posn(4, 5), Color.WHITE);

    edge1 = new Edge(this.v1, this.v1Bottom, 5);
    edge2 = new Edge(this.v1, this.v1Top, 6);
    edge3 = new Edge(this.v1, this.v1Left, 69);
    edge4 = new Edge(this.v1, this.v1Right, 420);

    mazeSeed2 = new Maze(4, 4);
    vwall = new LineImage(new Posn(0, Maze.CELL_SIZE), Color.BLACK);
    hwall = new LineImage(new Posn(Maze.CELL_SIZE, 0), Color.BLACK);
    cellStart = new RectangleImage(Maze.CELL_SIZE,
        Maze.CELL_SIZE, OutlineMode.SOLID, Color.GREEN);
    cellOther = new RectangleImage(Maze.CELL_SIZE,
        Maze.CELL_SIZE, OutlineMode.SOLID, Color.LIGHT_GRAY);
    cellEnd = new RectangleImage(Maze.CELL_SIZE,
        Maze.CELL_SIZE, OutlineMode.SOLID, Color.MAGENTA);

    maze2x2Seed = new Maze(2, 2);
    tl = new Vertex(new Posn(0, 0), Color.GREEN);
    tr = new Vertex(new Posn(0, 1), Color.LIGHT_GRAY);
    bl = new Vertex(new Posn(1, 0), Color.LIGHT_GRAY);
    br = new Vertex(new Posn(1, 1), Color.MAGENTA);
    e01 = new Edge(tl, tr, 5);
    e02 = new Edge(tl, bl, 5);
    e13 = new Edge(tr, br, 5);
    e23 = new Edge(bl, br, 5);
    el0 = new ArrayList<Edge>();
    el1 = new ArrayList<Edge>();
    el2 = new ArrayList<Edge>();
    el3 = new ArrayList<Edge>();

  }

  void testBigBang(Tester t) {
    initialData();
    this.maze1.bigBang(Maze.CELL_SIZE * this.maze1.c,
        Maze.CELL_SIZE * this.maze1.r, 1.0 / 100.0);
    // this.maze2x2Seed.bigBang(Maze.CELL_SIZE * this.maze1.c, Maze.CELL_SIZE *
    // this.maze1.r, 1.0 / 100.0);
  }

  void testIsEmpty(Tester t) {
    initialData();
    t.checkExpect(this.queue1.isEmpty(), true);
    t.checkExpect(this.stack1.isEmpty(), true);
    this.queue1.add("a");
    this.queue1.add("b");
    this.stack1.add("c");
    t.checkExpect(this.queue1.isEmpty(), false);
    t.checkExpect(this.stack1.isEmpty(), false);
  }

  void testRemove(Tester t) {
    initialData();
    this.queue1.add("a");
    this.queue1.add("b");
    this.stack1.add("c");
    t.checkExpect(queue1.remove(), "a");
    t.checkExpect(queue1.remove(), "b");

  }

  // adds a vertex to this vertex's outEdges list
  void testaAddEdge(Tester t) {
    initialData();
    this.v1.addEdge(v1Bottom, 4);
    this.v1.addEdge(v1Top, 5);
    this.v1.addEdge(v1Left, 27);
    this.v1.addEdge(v1Right, 66);
    t.checkExpect(this.v1.outEdges.size(), 4);

    ArrayList<Edge> dummy = new ArrayList<Edge>();
    dummy.add(new Edge(this.v1, this.v1Bottom, 4));
    dummy.add(new Edge(this.v1, this.v1Top, 5));
    dummy.add(new Edge(this.v1, this.v1Left, 27));
    dummy.add(new Edge(this.v1, this.v1Right, 66));
    t.checkExpect(this.v1.outEdges, dummy);

  }

  // checks equality of two vertices
  void testVertexEquals(Tester t) {
    initialData();
    t.checkExpect(this.v1.equals(v1Bottom), false);
    t.checkExpect(this.v1.equals(v1Top), false);
    t.checkExpect(this.v1.equals(v1Left), false);
    t.checkExpect(this.v1.equals(v1Right), false);
    Vertex v1Copy = new Vertex(this.v1.p, Color.WHITE);
    t.checkExpect(this.v1.equals(v1Copy), true);
    t.checkExpect(v1Copy.equals(this.v1), true);
    t.checkExpect(v1Copy.equals(v1Copy), true);
  }

  // generates hashcode for a vertex
  void testVertexHashCode(Tester t) {
    initialData();
    t.checkExpect(this.v1.hashCode(), 4 * 100000000 + 4 * 3);
    t.checkExpect(this.v1Bottom.hashCode(), 4 * 100000000 + 5 * 3);
    t.checkExpect(this.v1Right.hashCode(), 5 * 100000000 + 4 * 3);
  }

  void testEdgeEquals(Tester t) {
    initialData();
    t.checkExpect(this.edge1.equals(this.edge2), false);
    t.checkExpect(this.edge2.equals(this.edge3), false);
    t.checkExpect(this.edge3.equals(this.edge2), false);
    t.checkExpect(this.edge2.equals(this.edge4), false);

    Edge edge1Copy = new Edge(this.v1, this.v1Bottom, 5);
    t.checkExpect(this.edge1.equals(edge1Copy), true);
    t.checkExpect(this.edge1.equals(this.edge1), true);
    t.checkExpect(edge1Copy.equals(this.edge1), true);
  }

  void testEdgeHashCode(Tester t) {
    initialData();
    t.checkExpect(this.edge1.hashCode(), 4359958);
    t.checkExpect(this.edge2.hashCode(), 4359954);
    t.checkExpect(this.edge3.hashCode(), 4269956);
    t.checkExpect(this.edge4.hashCode(), 4449956);

  }

  void testCreateBoard(Tester t) {
    initialData();
    t.checkExpect(this.mazeSeed.board.size(), 6000);

    Maze mazeSeedCopy = new Maze(60, 100);
    t.checkExpect(this.mazeSeed.board, mazeSeed.board);

    this.mazeSeed.board = new ArrayList<Vertex>();
    mazeSeedCopy.board = new ArrayList<Vertex>();
    this.mazeSeed.createBoard();
    mazeSeedCopy.createBoard();
    t.checkExpect(this.mazeSeed.board, mazeSeedCopy.board);

    // might need to test the adding to outedges and connecting the neighbors
  }

  void testMoreThanOneTree(Tester t) {
    initialData();

    t.checkExpect(this.mazeSeed.uf.moreThanOneTree(), false);

    Maze mazeSeedCopy = new Maze(60, 100);
    for (Vertex v : mazeSeedCopy.board) {
      mazeSeedCopy.uf.union(mazeSeedCopy.uf.representatives, v.p, v.p);
    }
    t.checkExpect(mazeSeedCopy.uf.moreThanOneTree(), true);

  }

  void testMinSpanTree(Tester t) {
    initialData();
    // a successful min span tree should only have one tree
    t.checkExpect(this.mazeSeed.uf.moreThanOneTree(), false);
    // the worklist after a successful min-span tree should be left with non-minimum
    // edges
    t.checkExpect(this.mazeSeed.uf.worklist.size(), 8461);
    // the first thing in the worklist after the tree is created should be heavier
    // than all the edges in tree
    t.checkExpect(this.mazeSeed.uf.worklist.get(0).weight, 64);

    boolean bruh = true;
    for (Edge e : this.mazeSeed.uf.edgesInTree) {
      if (e.weight > this.mazeSeed.uf.worklist.get(0).weight) {
        bruh = false;
      }
    }
    t.checkExpect(bruh, true);

    t.checkExpect(this.mazeSeed.uf.edgesInTree.size(), 5999);

  }

  void testUnion(Tester t) {
    initialData();
    t.checkExpect(this.mazeSeed.uf.representatives.get(mazeSeed.uf.edgesInTree.get(0).from.p),
        new Posn(20, 0));
    this.mazeSeed.uf.union(this.mazeSeed.uf.representatives, mazeSeed.uf.edgesInTree.get(0).from.p,
        new Posn(69, 69));
    t.checkExpect(this.mazeSeed.uf.representatives.get(mazeSeed.uf.edgesInTree.get(0).from.p),
        new Posn(69, 69));

    // potentially add another instance of union another key-value for testing

  }

  void testSortByWeightComparator(Tester t) {
    initialData();
    SortByWeight wazoo = new SortByWeight();
    t.checkExpect(wazoo.compare(edge1, edge2), -1);
    t.checkExpect(wazoo.compare(edge3, edge2), 63);
    t.checkExpect(wazoo.compare(edge4, edge1), 415);
    t.checkExpect(wazoo.compare(edge3, edge1), 64);
    t.checkExpect(wazoo.compare(edge2, edge1), 1);
  }

  void testMakeScene(Tester t) {
    initialData();
    WorldScene scene = new WorldScene(Maze.CELL_SIZE * 4, Maze.CELL_SIZE * 4);
    // place cells
    // row 1
    scene.placeImageXY(this.cellStart,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 2
    scene.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 3
    scene.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 4
    scene.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene.placeImageXY(this.cellEnd,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));

    // place vertical walls (x7)
    // right of....
    // 0,0
    scene.placeImageXY(this.vwall,
        1 * Maze.CELL_SIZE,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 0,3
    scene.placeImageXY(this.vwall,
        3 * Maze.CELL_SIZE,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 1,1
    scene.placeImageXY(this.vwall,
        2 * Maze.CELL_SIZE,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 2,0
    scene.placeImageXY(this.vwall,
        1 * Maze.CELL_SIZE,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 2,2
    scene.placeImageXY(this.vwall,
        3 * Maze.CELL_SIZE,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 3,0
    scene.placeImageXY(this.vwall,
        1 * Maze.CELL_SIZE,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 3,1
    scene.placeImageXY(this.vwall,
        2 * Maze.CELL_SIZE,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));

    // place horizontal walls (x2)
    scene.placeImageXY(this.hwall,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE);
    scene.placeImageXY(this.hwall,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE);

    t.checkExpect(this.mazeSeed2.makeScene(), scene);

  }

  void testDrawCells(Tester t) {
    initialData();
    WorldScene scene2 = new WorldScene(Maze.CELL_SIZE * 4, Maze.CELL_SIZE * 4);
    // place cells
    // row 1
    scene2.placeImageXY(this.cellStart,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 2
    scene2.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 3
    scene2.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 4
    scene2.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellEnd,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));

    this.mazeSeed2.drawCells();
    t.checkExpect(this.mazeSeed2.scene, scene2);

  }

  void testDrawWalls(Tester t) {
    initialData();
    WorldScene scene2 = new WorldScene(Maze.CELL_SIZE * 4, Maze.CELL_SIZE * 4);
    // place cells
    // row 1
    scene2.placeImageXY(this.cellStart,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 2
    scene2.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 3
    scene2.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // row 4
    scene2.placeImageXY(this.cellOther,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellOther,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    scene2.placeImageXY(this.cellEnd,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));

    // place vertical walls (x7)
    // right of....
    // 0,0
    scene2.placeImageXY(this.vwall,
        1 * Maze.CELL_SIZE,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 0,3
    scene2.placeImageXY(this.vwall,
        3 * Maze.CELL_SIZE,
        0 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 1,1
    scene2.placeImageXY(this.vwall,
        2 * Maze.CELL_SIZE,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 2,0
    scene2.placeImageXY(this.vwall,
        1 * Maze.CELL_SIZE,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 2,2
    scene2.placeImageXY(this.vwall,
        3 * Maze.CELL_SIZE,
        2 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 3,0
    scene2.placeImageXY(this.vwall,
        1 * Maze.CELL_SIZE,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));
    // 3,1
    scene2.placeImageXY(this.vwall,
        2 * Maze.CELL_SIZE,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2));

    // place horizontal walls (x2)
    scene2.placeImageXY(this.hwall,
        1 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        2 * Maze.CELL_SIZE);
    scene2.placeImageXY(this.hwall,
        3 * Maze.CELL_SIZE + (Maze.CELL_SIZE / 2),
        3 * Maze.CELL_SIZE);

    this.mazeSeed2.drawWalls();
    t.checkExpect(this.mazeSeed2.scene, scene2);

  }

  void testOnKeyEvent(Tester t) {
    initialData();
    t.checkExpect(this.maze1.searchType, "");
    this.maze1.onKeyEvent("b");
    t.checkExpect(this.maze1.searchType, "b");
    this.maze1.onKeyEvent("d");
    t.checkExpect(this.maze1.searchType, "d");
    this.maze1.onKeyEvent("z");
    t.checkExpect(this.maze1.searchType, "d");

  }

}

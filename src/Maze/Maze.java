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

// Represents the maze
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

package Maze;

//class for running main method
public class MazeRunner {
  public static void main(String[] args) {
    Maze maze;
    //default size: 60x100
    int row = 60;
    int col = 100;

    //interpret the command-line arguments, construct maze with appropriate size
    int length = args.length;
    switch (length) {
      case 0:
        //default size
        break;
      case 2:
        //input size
        row = Integer.parseInt(args[0]);
        col = Integer.parseInt(args[1]);
        break;
      default:
        throw new IllegalArgumentException("\nIllegal command-line arguments. Enter number of rows and columns, or no input for default maze size");
    }

    maze = new Maze(row, col);
    maze.bigBang(Maze.CELL_SIZE * maze.c,
            Maze.CELL_SIZE * maze.r, 1.0 / 100.0);
  }
}
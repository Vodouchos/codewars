/*
codewars.com 2kyu kata - Hard Sudoku Solver

Description:
There are several difficulty of sudoku games, we can estimate the difficulty of a sudoku game based on how many cells are given of the 81 cells of the game.

Easy sudoku generally have over 32 givens
Medium sudoku have around 30–32 givens
Hard sudoku have around 28–30 givens
Very Hard sudoku have less than 28 givens
Note: The minimum of givens required to create a unique (with no multiple solutions) sudoku game is 17.

A hard sudoku game means that at start no cell will have a single candidates and thus require guessing and trial and error. A very hard will have several layers of multiple candidates for any empty cell.

Task:
Write a function that solves sudoku puzzles of any difficulty. The function will take a sudoku grid and it should return a 9x9 array with the proper answer for the puzzle.

Or it should raise an error in cases of: invalid grid (not 9x9, cell with values not in the range 1~9); multiple solutions for the same puzzle or the puzzle is unsolvable

Java users: throw an IllegalArgumentException for unsolvable or invalid puzzles or when a puzzle has mutliple solutions.
*/

import java.util.*;

enum State {
    INITIALIZED,
    IMPOSSIBLE,
    SOLVED,
    MULTIPLE_SOLLUTIONS,
    SPLITTING
}

public class SudokuSolver {
  private int[][] inputGrid;  
  
  public SudokuSolver(int[][] grid) {
    inputGrid = grid;
  }
  
  public int[][] solve() {
    if (inputGrid.length != 9) throw new IllegalArgumentException("wrong row count: " + inputGrid.length);
    for (int[] row : inputGrid) if (row.length != 9) throw new IllegalArgumentException("wrong row length: " + row.length);
    
    Sudoku sudoku = new Sudoku(inputGrid);
    sudoku.solve();
    
    if (sudoku.getState() == State.SOLVED) return sudoku.getIntSollution();
    if (sudoku.getState() == State.MULTIPLE_SOLLUTIONS) throw new IllegalArgumentException("multiple sollutions");
    if (sudoku.getState() == State.IMPOSSIBLE) throw new IllegalArgumentException("sollution not possible");
    //System.out.println(sudoku.getState());
    return null;
  }
}

class Sudoku {
  public SudokuElement[][] grid = new SudokuElement[9][9];
  
  private State state = State.INITIALIZED;
  private LinkedList<Integer> trimList = new LinkedList<Integer>(); // list of solved elements to be trimmed
  
  Sudoku(Sudoku sudoku) {
    for (int i = 0; i < 9; i++)
      for (int j = 0; j < 9; j++)
        grid[i][j] = new SudokuElement(sudoku.grid[i][j]);
  }
  
  Sudoku(int[][] inputGrid){
    for(int i = 0; i<9; i++)
      for(int j = 0; j<9; j++){
        grid[i][j] = new SudokuElement(inputGrid[i][j]);
        if (inputGrid[i][j] != 0)
          trimList.addLast(i * 9 + j);
        }
  }
  
  public State getState(){
    return state;
  }
  
  public void solve(){
    while (!trimList.isEmpty()){
      trimOthers(trimList.getFirst());
      trimList.removeFirst();
      if(state == State.IMPOSSIBLE) return;
    }
    // single candidates solved
    
    if (checkSolved()){ // if all elements filled
      state = State.SOLVED; return;
    }
    state = State.SPLITTING;
    split();
  }
  
  public void trimOthers(int elementId){ //check all different elements in same row, column and square for error and for last remaining option - in second case set element and add it to trimList
    trimOthers(elementId / 9, elementId % 9);
  }
  
  public void trimOthers(int row, int col){
    int trimValue = grid[row][col].getValue();
    for (int i = 1 ; i < 9; i++){
      trimElement((row + i) % 9, col, trimValue); //trim others in column
      trimElement(row, (col + i) % 9, trimValue); //trim others in row
    }
    //trim other 4 elements in square
    trimElement((row - (row/3)*3 + 1) % 3 + (row/3)*3, (col - (col/3)*3 + 1) % 3 + (col/3)*3, trimValue);
    trimElement((row - (row/3)*3 + 1) % 3 + (row/3)*3, (col - (col/3)*3 + 2) % 3 + (col/3)*3, trimValue);
    trimElement((row - (row/3)*3 + 2) % 3 + (row/3)*3, (col - (col/3)*3 + 1) % 3 + (col/3)*3, trimValue);
    trimElement((row - (row/3)*3 + 2) % 3 + (row/3)*3, (col - (col/3)*3 + 2) % 3 + (col/3)*3, trimValue);
  }
  
  private void trimElement(int row, int col, int trimValue){
    if (grid[row][col].getValue() == trimValue) {state = State.IMPOSSIBLE; return;} //duplicit value in group
    if (grid[row][col].getValue() != 0) return;
    grid[row][col].setImpossible(trimValue);
    if (grid[row][col].getPossibilities() == 0) {state = State.IMPOSSIBLE; return;} //element cannot be solved
    if (grid[row][col].setIfOneOption()) trimList.add(row * 9 + col);
  }
  
  private boolean checkSolved(){
    for (SudokuElement[] row : grid)
      for (SudokuElement element : row)
        if (element.getValue() == 0)
          return false;
    return true;
  }
  
  public int[][] getIntSollution(){
    int[][] outputGrid = new int[9][9];
    for(int i = 0; i<9; i++)
      for(int j = 0; j<9; j++)
        outputGrid[i][j] = grid[i][j].getValue();
    return outputGrid;
  }
  
  public void setElementValue(int row, int col, int newValue){
    grid[row][col].setValue(newValue);
    trimList.add(row * 9 + col);
  }
  
  private void split(){
    int leastPossibilitiesID = -1;
    int leastPossibilities = 10;
    SudokuElement element = null;
    
    for (int i = 0; i < 81; i++){
      element = grid[i / 9][i % 9];
      if (element.getValue() != 0) continue;
      if (leastPossibilitiesID == -1 || element.getPossibilities() < leastPossibilities) {
        leastPossibilitiesID = i;
        leastPossibilities = element.getPossibilities();
      }
    }
    // least possibilities found
    
    SudokuElement[][] solution = null;
    Sudoku tempSudoku = null;
    List<Integer> possibleValues = grid[leastPossibilitiesID / 9][leastPossibilitiesID % 9].getListOfPossibilities();
    
    for(Integer value : possibleValues){
      tempSudoku = new Sudoku(this);
      tempSudoku.setElementValue(leastPossibilitiesID / 9, leastPossibilitiesID % 9, value);
      tempSudoku.solve();
      
      switch (tempSudoku.getState()){
        case IMPOSSIBLE : 
          continue;
        case MULTIPLE_SOLLUTIONS : 
          state = State.MULTIPLE_SOLLUTIONS;
          return;
        case SOLVED : 
          if (solution == null){
            solution = tempSudoku.grid;
          } else {
            state = State.MULTIPLE_SOLLUTIONS;
            return;
          }
      }
    }
    
    if (solution == null){
        state = State.IMPOSSIBLE;
        return;
      } else {
        grid = solution;
        state = State.SOLVED;
      }
  }
  
}

class SudokuElement {
  private boolean[] possible = new boolean[9];
  private int possibilities;
  private int value; 
  
  SudokuElement(int inputValue){
    if (inputValue < 0 || inputValue > 9) throw new IllegalArgumentException("Wrong input number: " + inputValue);
    value = inputValue;
    if (value == 0) {
      Arrays.fill(possible, true);
      possibilities = 9;
    } else {
      Arrays.fill(possible, false);
      possible[value - 1] = true;
      possibilities = 0;
    }    
  }
  
  SudokuElement(SudokuElement element){
    possible = Arrays.copyOf(element.possible, 9); 
    value = element.value;
    possibilities = element.possibilities; //current amount of possible values
  }
  
  public int getValue(){
    return value;
  }
  
  public int getPossibilities(){
    return possibilities;
  }
  
  public ArrayList<Integer> getListOfPossibilities(){
    ArrayList<Integer> returnList = new ArrayList<Integer>();
    for (int i = 0; i< 9; i++)
      if (possible[i]) returnList.add(i+1);
    return returnList;
  }
  
  public void setValue(int newValue){ 
    value = newValue;
    possibilities = 0;
  }
  
  
  public void setImpossible(int impossibleValue){
    if (value != 0) return;
    if (possible[impossibleValue - 1]) {
      possible[impossibleValue - 1] = false;
      possibilities--;
      }
  }
  
  public boolean setIfOneOption(){ //returns true on change
    if (value != 0) return false;
    if (possibilities == 1){
      for (int i = 0; i < 9; i++)
        if (possible[i]) {
          value = i + 1;
          possibilities = 0;
          break;
        }
      possibilities = 0;
      return true;
    }
    return false;
  }
  
}

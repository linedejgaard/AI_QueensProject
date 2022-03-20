import java.lang.reflect.Array;
import java.util.ArrayList;

import jdk.jshell.spi.ExecutionControl;
import net.sf.javabdd.*;

public class QueensLogic13 implements IQueensLogic {

    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)

    BDDFactory fact;
    BDD True;
    BDD False;

    public QueensLogic13() {
        fact = JFactory.init(20,20); 
        True = fact.one();
        False = fact.zero();
    }

    
    @Override
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {
        // TODO Auto-generated method stub
        
    }

    public BDD initRules(int n) throws ExecutionControl.NotImplementedException {
        int nVars = n*n; // The number of variables to be used.
		fact.setVarNum(nVars);
        throw new ExecutionControl.NotImplementedException("NOT IMPLEMENTED");
    }

    //help get methods: lists

    public ArrayList<Integer> getExcludedVariablesBy(int variable) {

        ArrayList<Integer> excludedVariables = getVariablesInSameCol(variable); //exclude variables in same col
        excludedVariables.addAll(getVariablesInSameRow(variable)); //exclude variables in same col
        excludedVariables.addAll(getVariablesDiagonal(variable)); //exclude variables in same dia

        return excludedVariables;
    }

    public ArrayList<Integer> getVariablesInSameRow(int variable) {
        ArrayList<Integer> variablesInSameRow = new ArrayList<>();
    
        for (int col=0; col<size; col++) {
            
            int row = getRowFromCol(variable, col);
            int variableInRow = getVariable(col, row);

            if (variableInRow != variable) {
                variablesInSameRow.add(variableInRow);
            }
        }
        return variablesInSameRow;
    }

    public ArrayList<Integer> getVariablesInSameCol(int variable) {
        ArrayList<Integer> variablesInSameCol = new ArrayList<>();
        int col = getCol(variable);

        for (int row = 0; row < size; row++) {
            int variableInCol = getVariable(col, row);

            if (variableInCol != variable) {
                variablesInSameCol.add(variableInCol);
            }
        }
        return variablesInSameCol;
    }

    public ArrayList<Integer> getVariablesDiagonal(int variable) {
        ArrayList<Integer> variablesInSameDia = new ArrayList<>();
        int[] leftUp = {-1,-1};
        int[] leftDown = {-1,1};
        int[] rightUp = {1,-1};
        int[] rightDown = {1,1};

        int[][] directions = {leftUp, leftDown, rightDown, rightUp};

        for (int[] direction : directions) {
            int col =  getCol(variable) + direction[0];
            int row = getRow(variable) + direction[1];

            while (isOnBoard(col,row)) {
                variablesInSameDia.add(row * size + col);

                //go further in the direction:
                col += direction[0];
                row += direction[1];
            }
        }
        return  variablesInSameDia;
    }

    //help get methods: variable = col + row * size
    public int getVariable(int col, int row) {
        return col + row*size;
    }

    public int getRowFromCol(int variable, int col) {
        return (variable-col)/size;
    }

    public int getRow(int variable) {
        //return variable/size; //I don't know if this gives rows. It depends on how divid works on ints, I think...
        return getRowFromCol(variable, getCol(variable));
    }

    public int getCol(int variable) {
        return variable % size;
    }

    // help bool
    public boolean isOnBoard(int col, int row) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }
    
}
